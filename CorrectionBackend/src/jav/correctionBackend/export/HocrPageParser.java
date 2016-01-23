/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

import com.sun.media.jai.codec.FileSeekableStream;
import jav.logging.log4j.Log;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.media.jai.JAI;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * This class parses Hocr files. It handles both token oriented files
 * (tesseract) and line oriented files (ocropus).
 *
 * @author flo
 */
public class HocrPageParser implements PageParser {

    private static final XPathExpression XPAGE, XPAR, XLINE, XWORD, XCAPS, XSYS;
    public static Pattern BBRE
            = Pattern.compile("bbox\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)");
    private static final Pattern WCONF = Pattern.compile("x_wconf\\s+(\\d+)");

    static {
        try {
            XPAGE = makeXpath("//div[@class=\"ocr_page\"]");
            XPAR = makeXpath(".//p[@class=\"ocr_par\"]");
            XLINE = makeXpath(".//span[@class=\"ocr_line\"]");
            XWORD = makeXpath(".//span[@class=\"ocrx_word\" or @class=\"ocr_word\"]");
            XCAPS = makeXpath("/html/head/meta[@name=\"ocr-capabilities\"]");
            XSYS = makeXpath("/html/head/meta[@name=\"ocr-system\"]");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private org.w3c.dom.Document xml;
    private HocrMeta meta;
    private int imageHeight;
    private File image, ocr;

    @Override
    public void setImageFile(File image) {
        this.image = image;
    }

    @Override
    public void setOcrFile(File ocr) {
        this.ocr = ocr;
    }

    @Override
    public void write(File output) throws Exception {
        Transformer transformer
                = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource domSource = new DOMSource(xml);
        StreamResult out
                = new StreamResult(new FileOutputStream(output));
        transformer.transform(domSource, out);
    }

    @Override
    public Page parse() throws IOException, Exception {
        parseXml();
        Log.info(this, "ocr-capabilities: %s", meta);
        return parsePage();
    }

    private Page parsePage() throws Exception {
        if (!meta.hasPage) {
            throw new Exception("Hocr document has no page segmentation");
        }
        if (!meta.hasLine) {
            throw new Exception("Hocr document has no line segmentation");
        }
        Page page = new Page(image, ocr);
        Node pagenode = (Node) XPAGE.evaluate(xml, XPathConstants.NODE);
        if (pagenode != null) {
            if (meta.hasPar) {
                appendParagraphs(pagenode, page);
            } else { // no paragraphs in document
                Paragraph p = new Paragraph();
                appendLines(pagenode, p);
                page.add(p);
            }
        }
        return page;
    }

    private void appendParagraphs(Node node, Page page) throws Exception {
        NodeList ps = (NodeList) XPAR.evaluate(node, XPathConstants.NODESET);
        if (ps != null) {
            for (int i = 0; i < ps.getLength(); ++i) {
                Paragraph p = new Paragraph();
                appendLines(ps.item(i), p);
                page.add(p);
            }
        }
    }

    private void appendLines(Node node, Paragraph p) throws Exception {
        NodeList ls = (NodeList) XLINE.evaluate(node, XPathConstants.NODESET);
        if (ls != null) {
            for (int i = 0; i < ls.getLength(); ++i) {
                final Node lineNode = ls.item(i);
                Line line = new Line(doGetBoundingBox(lineNode));
                if (meta.hasWord) {
                    appendTokens(lineNode, line);
                } else { // no words; just lines (ocropus)
                    appendCharsToLine(lineNode, line);
                }
                p.add(line);
            }
        }
    }

    private void appendTokens(Node node, Line line) throws Exception {
        NodeList cs = (NodeList) XWORD.evaluate(node, XPathConstants.NODESET);
        if (cs != null && cs.getLength() > 0) {
            HocrToken prevToken = null;
            for (int i = 0; i < cs.getLength(); ++i) {
                final Node tokenNode = cs.item(i);
                if (tokenNode.getFirstChild() != null
                        && !tokenNode.getFirstChild().getTextContent().isEmpty()) {
                    HocrToken newToken = new HocrToken(line, tokenNode, doGetBoundingBox(node));
                    if (prevToken != null) {
                        line.add(new HocrWhitespaceChar(line, prevToken, newToken));
                    }
                    line.addAll(newToken);
                    prevToken = newToken;
                }
            }
        }
    }

    private void appendCharsToLine(Node node, Line line) throws Exception {
        if (node.getFirstChild() != null
                && node.getFirstChild().getTextContent() != null
                && !node.getFirstChild().getTextContent().isEmpty()) {
            HocrToken newToken = new HocrToken(line, node, doGetBoundingBox(node));
            line.addAll(newToken);
        }
    }

    private void parseXml() throws IOException, Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setExpandEntityReferences(false);
        javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
        db.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(String pid, String sid) {
                return new InputSource(new StringReader(""));
            }
        });
        xml = db.parse(ocr);
        parseHocrMeta();
    }

    // This method is a pile of crap
    private BoundingBox doGetBoundingBox(Node node) throws Exception {
        BoundingBox bb = HocrPageParser.getBoundingBox(node);
        if (meta.isOcropus) { // ocropus lives in its own little world
            if (imageHeight == 0) {
                try {
                    FileSeekableStream fss = new FileSeekableStream(image);
                    ParameterBlock pb = new ParameterBlock();
                    pb.add(fss);
                    if (image.getName().endsWith("tiff")
                            || image.getName().endsWith("tif")) {
                        imageHeight = JAI.create("tiff", pb).getHeight();
                    } else if (image.getName().endsWith("jpeg")
                            || image.getName().endsWith("jpg")) {
                        imageHeight = JAI.create("jpeg", pb).getHeight();
                    } else {
                        imageHeight = -1; // just try once for each file
                    }
                } catch (Exception e) {
                    imageHeight = -1; // just try once for each file
                    throw new Exception(e);
                }
            }
            if (imageHeight > 0) {
                bb = new BoundingBox(
                        bb.getLeft(),
                        imageHeight - bb.getBottom() - 1,
                        bb.getRight(),
                        imageHeight - bb.getTop() - 1
                );
            }
        }
        return bb;
    }

    private static XPathExpression makeXpath(String expr) throws Exception {
        return XPathFactory.newInstance().newXPath().compile(expr);
    }

    private class HocrMeta {

        boolean hasPage, hasPar, hasLine, hasWord, isOcropus;

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            if (hasPage) {
                builder.append("ocr_page ");
            }
            if (hasPar) {
                builder.append("ocr_par ");
            }
            if (hasLine) {
                builder.append("ocr_line ");
            }
            if (hasWord) {
                builder.append("ocr(x)_word");
            }
            if (isOcropus) {
                builder.append(" (ocropus)");
            }
            return builder.toString();
        }
    }

    private void parseHocrMeta() throws Exception {
        final Node caps = (Node) XCAPS.evaluate(xml, XPathConstants.NODE);
        meta = new HocrMeta();
        if (caps != null && caps.getAttributes() != null) {
            final Node content = caps.getAttributes().getNamedItem("content");
            meta.hasPage = hasCapability(content, "ocr_page");
            meta.hasPar = hasCapability(content, "ocr_par");
            meta.hasLine = hasCapability(content, "ocr_line");
            meta.hasWord = hasCapability(content, "ocrx_word")
                    || hasCapability(content, "ocr_word");
        }
        final Node sys = (Node) XSYS.evaluate(xml, XPathConstants.NODE);
        if (sys != null && sys.getAttributes() != null) {
            meta.isOcropus = isOcropus(sys.getAttributes().getNamedItem("content"));
        }
    }

    private static boolean hasCapability(Node content, String name) {
        if (content != null) {
            return content.getNodeValue().contains(name);
        } else {
            return false;
        }
    }

    private static boolean isOcropus(Node content) {
        if (content != null) {
            return content.getNodeValue().contains("ocropus");
        } else {
            return false;
        }
    }

    public static int getConfidence(Node node) {
        try {
            Node title = node.getAttributes().getNamedItem("title");
            Matcher m = WCONF.matcher(title.getNodeValue());
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
        } catch (Exception e) {
            // ignore
        }
        return -1;
    }

    public static BoundingBox getBoundingBox(Node node) {
        try {
            Node titleNode = node.getAttributes().getNamedItem("title");
            Matcher m = BBRE.matcher(titleNode.getNodeValue());
            if (m.find()) {
                return new BoundingBox(
                        Integer.parseInt(m.group(1)),
                        Integer.parseInt(m.group(2)),
                        Integer.parseInt(m.group(3)),
                        Integer.parseInt(m.group(4))
                );
            }
        } catch (Exception e) {
            // ignore;
        }
        return new BoundingBox(-1, -1, -1, -1);
    }
}
