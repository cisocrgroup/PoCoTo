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

    @Override
    public void write(File output) throws IOException, Exception {
        Transformer transformer
                = TransformerFactory.newInstance().newTransformer();
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
                Line line = new Line();
                appendChars(ls.item(i), line);
                p.add(line);
            }
        }
    }

    private void appendChars(Node linenode, Line line) throws Exception {
        XPathExpression xchar = makeXpath(".//charParams");
        NodeList cs = (NodeList) xchar.evaluate(linenode, XPathConstants.NODESET);
        if (cs != null) {
            for (int i = 0; i < cs.getLength(); ++i) {
                AbbyyXmlChar newChar = new AbbyyXmlChar(cs.item(i));
                if (!line.isEmpty()) {
                    AbbyyXmlChar prev = (AbbyyXmlChar) line.get(line.size() - 1);
                    prev.setNext(newChar);
                    newChar.setPrev(prev);
                }
                line.add(newChar);
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
