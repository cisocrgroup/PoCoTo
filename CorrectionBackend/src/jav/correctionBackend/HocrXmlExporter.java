/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author finkf
 */
class HocrXmlExporter extends BaseXmlExporter {
    private static final Pattern imgRe = 
            Pattern.compile("image\\s+\"([^\"]+)\"");
    private static final Pattern tiibRe = 
            Pattern.compile("bbox\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)");
    private static final String tokenStmnt = 
            "SELECT * FROM token WHERE imageFile like %?%";

    public HocrXmlExporter(File src, File dest, Document document) {
        super(src, dest, document);
    }
    
    @Override
    public void export() throws IOException, Exception {
        org.w3c.dom.Document doc = getDom();
        HashMap<Node, String> pageIndex = getImageFileNames(doc);
        ArrayList<Token> tokens = loadAllTokensFromDocument(pageIndex);
        HashMap<Node, TokenImageInfoBox> nodes = loadAllNodes(pageIndex);
    }
    
    private org.w3c.dom.Document getDom() 
            throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(getSourceFile());
    }
    
    private HashMap<Node, String> getImageFileNames(org.w3c.dom.Document doc) 
            throws XPathExpressionException {
        NodeList pageNodes = (NodeList) compileXpath("/body/div[@class='ocr_page']")
                .evaluate(doc, XPathConstants.NODESET);
        HashMap<Node, String> pageIndex = new HashMap();
        for (int i = 0; i < pageNodes.getLength(); ++i) {
            Node node = pageNodes.item(i);
            String path = getImagePath(getAttributeValue(node, "title"));
            if (path != null)
                pageIndex.put(node, path);
        }
        return pageIndex;
    }
    
    private ArrayList<Token> loadAllTokensFromDocument(
            HashMap<Node, String> pageIndex
    ) throws SQLException {
        ArrayList<Token> tokens = new ArrayList();
        for (Node node: pageIndex.keySet()) {
            PreparedStatement s = getDocument().prepareStatement(tokenStmnt);
            s.setString(1, pageIndex.get(node));
            TokenIterator it = getDocument().selectTokens(s);
            while (it.hasNext()) {
                tokens.add(it.next());
            }
        }
        return tokens;
    }
    
    private HashMap<Node, TokenImageInfoBox> loadAllNodes(
            HashMap<Node, String> pageIndex
    ) throws XPathExpressionException, Exception {
        HashMap<Node, TokenImageInfoBox> nodes = new HashMap();
        for (Node node: pageIndex.keySet()) {
            NodeList tokenNodes = (NodeList) compileXpath("/*/div[@class='ocrx_word]")
                    .evaluate(node, XPathConstants.NODESET);
            for (int i = 0; i < tokenNodes.getLength(); ++i) {
                Node tokenNode = tokenNodes.item(i);
                TokenImageInfoBox tiib = getTokenImageInfoBox(
                        tokenNode,
                        pageIndex.get(node)
                );
                nodes.put(tokenNode, tiib);
            }
        }
        return nodes;
    }
    
    private String getImagePath(String title) {
        Matcher m = imgRe.matcher(title);
        if (m.matches()) {
            return m.group(1);
        }
        return null;
    }
    
    private String getAttributeValue(Node node, String name) {
        if (node.hasAttributes())
            return node.getAttributes().getNamedItem(name).getNodeValue();
        return null;
    }
    
    private XPathExpression compileXpath(String exp) 
            throws XPathExpressionException {
        return XPathFactory.newInstance().newXPath().compile(exp);
    }

    private TokenImageInfoBox getTokenImageInfoBox(Node node, String img) 
            throws Exception {
        String title = getAttributeValue(node, "title");
        if (title == null)
            throw new Exception("token without a title");
        return getTokenImageInfoBox(title, img);
    }
    
    private TokenImageInfoBox getTokenImageInfoBox(String title, String img) {
        return new TokenImageInfoBox();
    }
}
