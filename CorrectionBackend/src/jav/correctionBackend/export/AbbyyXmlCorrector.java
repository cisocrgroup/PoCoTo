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
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
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
public class AbbyyXmlCorrector extends DocumentCorrector {

    private final String path, outputpath;
    private final org.w3c.dom.Document xmlDoc;
    private final ArrayList<ArrayList<AbbyyXmlChar>> lines;

    public AbbyyXmlCorrector(String input, String output)
            throws IOException, SAXException, XPathExpressionException,
            ParserConfigurationException {
        this.path = input;
        this.outputpath = output;
        xmlDoc = parseXml();
        lines = parseLines();

    }

    @Override
    public int getNumberOfLines() {
        return lines.size();
    }

    @Override
    public String getLineAt(int i) {
        StringBuilder builder = new StringBuilder();
        for (int j = 0; j < lines.get(i).size(); ++j) {
            builder.append(lines.get(i).get(j).getChar());
        }
        return builder.toString();
    }

    @Override
    public void substitute(int i, int j, char c) {
        lines.get(i).get(j).substitute(c);
    }

    @Override
    public void delete(int i, int j) {
        lines.get(i).get(j).delete();
        lines.get(i).remove(j);
    }

    @Override
    public void insert(int i, int j, char c) {
        if (j < lines.get(i).size()) {
            doInsert(i, j, c);
        } else {
            doAppend(i, c);
        }
    }

    private void doInsert(int i, int j, char c) {
        Node charNode = lines.get(i).get(j).getNode();
        Node clone = charNode.cloneNode(true);
        charNode.insertBefore(clone, charNode);
        lines.get(i).add(j, new AbbyyXmlChar(c, clone));
    }

    private void doAppend(int i, char c) {
        final int n = lines.get(i).size();
        if (n > 0) {
            Node charNode = lines.get(i).get(n - 1).getNode();
            Node clone = charNode.cloneNode(true);
            charNode.insertBefore(clone, null);
            lines.get(i).add(new AbbyyXmlChar(c, clone));
        }
    }

    @Override
    public void write() throws IOException {
        try {
            Transformer transformer
                    = TransformerFactory.newInstance().newTransformer();
            DOMSource domSource = new DOMSource(xmlDoc);
            StreamResult out
                    = new StreamResult(new FileOutputStream(new File(outputpath)));
            transformer.transform(domSource, out);
        } catch (TransformerException e) {
            throw new IOException(e);
        }
    }

    private org.w3c.dom.Document parseXml()
            throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setExpandEntityReferences(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        db.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(String pid, String sid) {
                return new InputSource(new StringReader(""));
            }
        });
        return db.parse(new File(path));
    }

    private ArrayList<ArrayList<AbbyyXmlChar>> parseLines()
            throws XPathExpressionException {
        ArrayList<ArrayList<AbbyyXmlChar>> res = new ArrayList<>();
        XPathExpression xpages = makeXpath("//page");
        XPathExpression xlines = makeXpath("//line");
        NodeList pageNodes = (NodeList) xpages.evaluate(xmlDoc, XPathConstants.NODESET);
        for (int i = 0; i < pageNodes.getLength(); ++i) {
            NodeList nodeLines = (NodeList) xlines.evaluate(pageNodes.item(i), XPathConstants.NODESET);
            for (int j = 0; j < nodeLines.getLength(); ++j) {
                res.add(parseLine(nodeLines.item(j)));
            }
        }
        return res;
    }

    private ArrayList<AbbyyXmlChar> parseLine(Node node)
            throws XPathExpressionException {
        XPathExpression xchar = makeXpath("//charParams");
        NodeList chars = (NodeList) xchar.evaluate(node, XPathConstants.NODESET);
        ArrayList<AbbyyXmlChar> res = new ArrayList<>();
        for (int i = 0; i < chars.getLength(); ++i) {
            res.add(new AbbyyXmlChar(chars.item(i)));
        }
        return res;
    }

    private XPathExpression makeXpath(String expr) throws XPathExpressionException {
        return XPathFactory.newInstance().newXPath().compile(expr);
    }
}
