/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import jav.logging.log4j.Log;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author finkf
 */
public class TeiBookParser {

    private final File file;
    private final org.w3c.dom.Document document;
    private TeiBook book;
    private TeiLine currentLine;

    public TeiBookParser(File file) throws SAXException, IOException, ParserConfigurationException {
        this.file = file;
        document = parseXml(file);
    }

    public Book parse() throws XPathExpressionException {
        XPathExpression xpath = XPathFactory.newInstance().newXPath().compile("/TEI/text");
        Node text = (Node) xpath.evaluate(document, XPathConstants.NODE);
        forEach(text);
        book = new TeiBook(document, file);
        return book;
    }

    private void forEach(Node node) {
        callback(node);
        if (node.hasChildNodes()) {
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); ++i) {
                forEach(children.item(i));
            }
        }
        for (Node sibling = node.getNextSibling();
                sibling != null;
                sibling = sibling.getNextSibling()) {
            forEach(sibling);
        }
    }

    private void callback(Node node) {
        Log.debug(this, "callback %s", node.getLocalName());
        if ("pb".equals(node.getLocalName())) {
            File facs = null;
            if (node.hasAttributes()
                    && node.getAttributes().getNamedItem("facs") != null) {
                facs = new File(
                        node.getAttributes().getNamedItem("facs").getNodeValue()
                );
            }
            book.add(new Page(facs, file));
        } else if ("p".equals(node.getLocalName())) {
            book.get(book.size() - 1).add(new Paragraph());
        } else if ("lb".equals(node.getLocalName())) {
            if (currentLine != null) {
                Page page = book.get(book.size() - 1);
                Paragraph p = page.get(page.size() - 1);
                p.add(currentLine);
            }
            currentLine = null;
        } else if (node.getNodeType() == Node.TEXT_NODE) {
            if (currentLine == null) {
                currentLine = new TeiLine(node);
            }
            currentLine.add(node.getNodeValue());
        }
    }

    private static org.w3c.dom.Document parseXml(File file)
            throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setExpandEntityReferences(false);
        javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
        db.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(String pid, String sid) {
                return new InputSource(new StringReader(""));
            }
        });
        return db.parse(file);
    }
}
