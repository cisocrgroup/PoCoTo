/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

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
public class HocrPageParser implements PageParser {

    private org.w3c.dom.Document xml;

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
    public Page parse(File input) throws IOException, Exception {
        return parsePage(input);
    }

    private Page parsePage(File input) throws IOException, Exception {
        parseXml(input);
        Page page = new Page();
        // <div class='ocr_page' id='page_1' title='image "0018.tif"; bbox 0 0 1156 1782; ppageno 0'>
        XPathExpression xpage = makeXpath("//div[@class=\"ocr_page\"]");
        Node pagenode = (Node) xpage.evaluate(xml, XPathConstants.NODE);
        if (pagenode != null) {
            appendParagraphs(pagenode, page);
        }
        return page;
    }

    private void appendParagraphs(Node pagenode, Page page) throws Exception {
        // <p class='ocr_par' dir='ltr' id='par_1_1' title="bbox 574 59 612 83">
        XPathExpression xpar = makeXpath(".//p[@class=\"ocr_par\"]");
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
        // <span class='ocr_line' id='line_1_1' title="bbox 574 59 612 83; baseline -0.053 0">
        XPathExpression xline = makeXpath(".//span[@class=\"ocr_line\"]");
        NodeList ls = (NodeList) xline.evaluate(pnode, XPathConstants.NODESET);
        if (ls != null) {
            for (int i = 0; i < ls.getLength(); ++i) {
                final Node lineNode = ls.item(i);
                Line line = new Line(HocrToken.getBoundingBox(lineNode));
                appendTokens(lineNode, line);
                p.add(line);
            }
        }
    }

    private void appendTokens(Node linenode, Line line) throws Exception {
        // <span class='ocr(x)_word' id='word_1_2' title='bbox 211 141 322 176;
        //      x_wconf 72' lang='deu-frak' dir='ltr'>zuuns
        //  </span>
        XPathExpression xchar = makeXpath(".//span[@class=\"ocrx_word\" or @class=\"ocr_word\"]");
        NodeList cs = (NodeList) xchar.evaluate(linenode, XPathConstants.NODESET);
        if (cs != null) {
            HocrToken prevToken = null;
            for (int i = 0; i < cs.getLength(); ++i) {
                final Node tokenNode = cs.item(i);
                if (tokenNode.hasChildNodes()
                        && !tokenNode.getFirstChild().getNodeValue().isEmpty()) {
                    HocrToken newToken = new HocrToken(tokenNode);
                    if (prevToken != null) {
                        line.add(new HocrWhitespaceChar(prevToken, newToken));
                    }
                    for (HocrChar c : newToken) {
                        line.add(c);
                    }
                    prevToken = newToken;
                }
            }
        }
    }

    private void parseXml(File input) throws IOException, Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setExpandEntityReferences(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        db.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(String pid, String sid) {
                return new InputSource(new StringReader(""));
            }
        });
        xml = db.parse(input);
    }

    private XPathExpression makeXpath(String expr) throws Exception {
        return XPathFactory.newInstance().newXPath().compile(expr);
    }
}
