/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Stack;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
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
    private Line currentLine;

    public TeiBookParser(File file) throws SAXException, IOException, ParserConfigurationException {
        this.file = file;
        document = parseXml(file);
    }

    public Book parse() throws Exception {
        XPathExpression xpath = XPathFactory.newInstance().newXPath().compile("/TEI/text");
        Node text = (Node) xpath.evaluate(document, XPathConstants.NODE);
        book = new TeiBook(document, file);
        Stack<Node> stack = new Stack<>();
        stack.push(text);
        while (!stack.isEmpty()) {
            Node top = stack.pop();
            forEach(top);
            if (top.hasChildNodes()) {
                NodeList children = top.getChildNodes();
                final int n = children.getLength();
                for (int i = n; i > 0; --i) {
                    stack.push(children.item(i - 1));
                }
            }
        }
        return book;
    }

    private void forEach(Node node) throws Exception {
        assert (node != null);
        if ("pb".equals(node.getNodeName())) {
            book.add(new Page(getFacs(node), file));
        } else if ("p".equals(node.getNodeName())) {
            getLastPage().add(new Paragraph());
        } else if ("lb".equals(node.getNodeName())) {
            if (currentLine != null) {
                Paragraph p = getLastParagraph();
                StringBuilder builder = new StringBuilder();
                for (Char c : currentLine) {
                    builder.appendCodePoint(c.getChar());
                }
                p.add(currentLine);
            }
            currentLine = null;
        } else if (node.getNodeType() == Node.TEXT_NODE) {
            if (currentLine == null) {
                currentLine = new Line();
            }
            currentLine.addAll(new TeiToken(node));
        }
    }

    private Page getLastPage() throws Exception {
        assert (book != null);
        if (book.isEmpty()) {
            throw new Exception("Missing `pb` tag in tei file");
        }
        return book.getPageAt(book.getNumberOfPages() - 1);
    }

    private Paragraph getLastParagraph() throws Exception {
        assert (book != null);
        if (getLastPage().isEmpty()) {
            getLastPage().add(new Paragraph());
        }
        return getLastPage().get(getLastPage().size() - 1);
    }

    private File getFacs(Node node) {
        if (node.hasAttributes()) {
            if (node.getAttributes().getNamedItem("facs") != null) {
                return new File(
                        node.getAttributes().getNamedItem("facs").getNodeValue()
                );
            }
        }
        return null;
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
