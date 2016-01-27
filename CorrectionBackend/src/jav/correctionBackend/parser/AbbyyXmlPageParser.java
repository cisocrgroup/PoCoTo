/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
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
 *
 * @author flo
 */
public class AbbyyXmlPageParser implements PageParser {

    private org.w3c.dom.Document xml;
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
    public void write(File output) throws IOException, Exception {
        Transformer transformer
                = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource domSource = new DOMSource(xml);
        StreamResult out
                = new StreamResult(new FileOutputStream(output));
        transformer.transform(domSource, out);
    }

    @Override
    public Page parse() throws Exception {
        return parsePage();
    }

    private Page parsePage() throws IOException, Exception {
        parseXml();
        Page page = new Page(image, ocr);
        XPathExpression xpage = makeXpath("//page");
        Node pagenode = (Node) xpage.evaluate(xml, XPathConstants.NODE);
        if (pagenode != null) {
            appendParagraphs(pagenode, page);
        }
        return page;
    }

    private void appendParagraphs(Node pagenode, Page page) throws Exception {
        XPathExpression xpar = makeXpath(".//par");
        NodeList ps = (NodeList) xpar.evaluate(pagenode, XPathConstants.NODESET);
        if (ps != null) {
            for (int i = 0; i < ps.getLength(); ++i) {
                Paragraph p = new Paragraph();
                appendLines(ps.item(i), p);
                page.add(p);
            }
        }
    }

    private void appendLines(Node pnode, Paragraph p) throws Exception {
        XPathExpression xline = makeXpath(".//line");
        NodeList ls = (NodeList) xline.evaluate(pnode, XPathConstants.NODESET);
        if (ls != null) {
            for (int i = 0; i < ls.getLength(); ++i) {
                final Node lineNode = ls.item(i);
                Line line = new Line(getBoundingBox(lineNode));
                appendChars(lineNode, line);
                p.add(line);
            }
        }
    }

    private void appendChars(Node linenode, Line line) throws Exception {
        XPathExpression xchar = makeXpath(".//charParams");
        NodeList cs = (NodeList) xchar.evaluate(linenode, XPathConstants.NODESET);
        if (cs != null) {
            for (int i = 0; i < cs.getLength(); ++i) {
                final Node charNode = cs.item(i);
                final BoundingBox bb = getBoundingBox(charNode);
                line.add(new AbbyyXmlChar(line, charNode, bb));
            }
        }
    }

    private void parseXml() throws IOException, Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setExpandEntityReferences(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        db.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(String pid, String sid) {
                return new InputSource(new StringReader(""));
            }
        });
        xml = db.parse(ocr);
    }

    private XPathExpression makeXpath(String expr) throws Exception {
        return XPathFactory.newInstance().newXPath().compile(expr);
    }

    private static BoundingBox getBoundingBox(Node node) {
        if (node != null) {
            Node l = node.getAttributes().getNamedItem("l");
            Node t = node.getAttributes().getNamedItem("t");
            Node r = node.getAttributes().getNamedItem("r");
            Node b = node.getAttributes().getNamedItem("b");
            if (l != null && t != null && r != null && b != null) {
                return new BoundingBox(getInt(l), getInt(t), getInt(r), getInt(b));
            }
        }
        return new BoundingBox(-1, -1, -1, -1);
    }

    private static int getInt(Node node) {
        if (node != null) {
            return Integer.parseInt(node.getNodeValue());
        }
        return -1;
    }
}
