/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend;

import jav.logging.log4j.Log;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author finkf
 */
class HocrXmlExporter extends BaseXmlExporter {
    private static final Pattern imgRe = 
            Pattern.compile("image\\s+\"(.*)\"");
    private static final Pattern tiibRe = 
            Pattern.compile("bbox\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)");
    private static final String tokenStmnt = 
            "SELECT * FROM token WHERE imageFile = ?";

    public HocrXmlExporter(File src, File dest, Document document) {
        super(src, dest, document);
    }
    
    @Override
    public void export() throws IOException, Exception {
        org.w3c.dom.Document doc = getDom();
        HashMap<Node, String> pageIndex = getImageFileNames(doc);
        ArrayList<Token> tokens = loadAllTokensFromDocument(pageIndex);
        ArrayList<CompoundSet.Node> nodes = loadAllNodes(pageIndex);
        CompoundSet set = new CompoundSet();
        for (CompoundSet.Node node: nodes) {
            set.add(node, tokens);
        }
        for (CompoundSet.Compound c: set) {
            Attr attr = doc.createAttribute("compound");
            if (c.isOneToOne()) {
                CompoundSet.Node tmp = c.getNodes().next();
                attr.setNodeValue("OneToOne");
                tmp.getNode().getAttributes().setNamedItem(attr);
            } else if (c.isMerge()) {
                attr.setNodeValue("Merge");
                Iterator<CompoundSet.Node> it = c.getNodes();
                while (it.hasNext()) {
                    CompoundSet.Node node = it.next();
                    node.getNode().getAttributes().setNamedItem(attr);
                }
            } else if (c.isSplit()) {
                attr.setNodeValue("Split");
                c.getNodes().next().getNode().getAttributes().setNamedItem(attr);
            } else {
                String x = new StringBuilder("weird stuff (")
                        .append(c.getNumberOfTokens())
                        .append(' ')
                        .append(c.getNumberOfNodes())
                        .append(')')
                        .toString();
                Log.debug(this, "tokenssize: %d nodessize: %d", c.getNumberOfTokens(), c.getNumberOfNodes());
                attr.setNodeValue(x);
                Iterator<CompoundSet.Node> it = c.getNodes();
                while (it.hasNext()) {
                    Node daAttr = attr.cloneNode(true);
                    CompoundSet.Node node = it.next();
                    Log.debug(this, "Weird: %s", node.getTokenImageInfoBox().toString());
                    Log.debug(this, "Weird area %d", node.getTokenImageInfoBox().getArea());
                    Iterator<Token> jt = c.getTokens();
                    while (jt.hasNext()) {
                        Token t = jt.next();
                        Log.debug(this, "Token: %s", t.getTokenImageInfoBox().toString());
                        Log.debug(this, "Token area %d", t.getTokenImageInfoBox().getArea());
                        TokenImageInfoBox overlap = node.getTokenImageInfoBox().calculateOverlappingBox(t.getTokenImageInfoBox());
                        Log.debug(this, "overlap: %s", overlap.toString());
                        Log.debug(this, "overlap area %d", overlap.getArea());
                    }
                    node.getNode().getAttributes().setNamedItem(daAttr);                    
                }
            }
        }
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(getDestinationFile());
        t.transform(source, result);
    }
    
    private org.w3c.dom.Document getDom() 
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
        return db.parse(getSourceFile());
    }
    
    private HashMap<Node, String> getImageFileNames(org.w3c.dom.Document doc) 
            throws XPathExpressionException, Exception {
        NodeList pageNodes = (NodeList) compileXpath("//div[@class='ocr_page']")
                .evaluate(doc, XPathConstants.NODESET);
        HashMap<Node, String> pageIndex = new HashMap();
        for (int i = 0; i < pageNodes.getLength(); ++i) {
            Node node = pageNodes.item(i);
            String path = getImagePath(getAttributeValue(node, "title"));
            if (path != null) {
                pageIndex.put(node, path);
            }
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
    
    private ArrayList<CompoundSet.Node> loadAllNodes(
            HashMap<Node, String> pageIndex
    ) throws XPathExpressionException, Exception {
        ArrayList<CompoundSet.Node> nodes = new ArrayList();
        for (Node node: pageIndex.keySet()) {
            NodeList tokenNodes = (NodeList) compileXpath("//span[@class='ocrx_word']")
                    .evaluate(node, XPathConstants.NODESET);
            for (int i = 0; i < tokenNodes.getLength(); ++i) {
                Node tokenNode = tokenNodes.item(i);
                TokenImageInfoBox tiib = getTokenImageInfoBox(
                        tokenNode,
                        pageIndex.get(node)
                );
                nodes.add(new CompoundSet.Node(tokenNode, tiib));
            }
        }
        return nodes;
    }
    
    private String getImagePath(String title) throws Exception {
        Matcher m = imgRe.matcher(title);
        if (!m.find()) 
            throw new Exception("page without an image: " + title);
        
        String path = m.group(1);
        return path.substring(path.lastIndexOf('/') + 1);
    }
    
    private String getAttributeValue(Node node, String name) {
        if (node.hasAttributes()) {
            Node attr = node.getAttributes().getNamedItem(name);
            if (attr != null)
                return attr.getNodeValue();
        }
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
    
    private TokenImageInfoBox getTokenImageInfoBox(String title, String img) 
            throws Exception {
        Matcher m = tiibRe.matcher(title);
        if (!m.find())
            throw new Exception("invalid bounding box: " + title);
        
        TokenImageInfoBox tiib = new TokenImageInfoBox(
                Integer.parseInt(m.group(1)),
                Integer.parseInt(m.group(2)),
                Integer.parseInt(m.group(3)),
                Integer.parseInt(m.group(4))
        );
        tiib.setImageFileName(img);
        return tiib;
    }
}
