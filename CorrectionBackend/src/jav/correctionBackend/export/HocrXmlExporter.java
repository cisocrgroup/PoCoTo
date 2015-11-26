/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

import jav.correctionBackend.Document;
import jav.correctionBackend.MyIterator;
import jav.correctionBackend.Token;
import jav.correctionBackend.TokenImageInfoBox;
import jav.logging.log4j.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
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
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author finkf
 */
public class HocrXmlExporter extends BaseXmlExporter {

    private static final Pattern imgRe
            = Pattern.compile("image\\s+\"(.*)\"");
    private static final Pattern tiibRe
            = Pattern.compile("bbox\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)");
    private static final String tokenStmnt
            = "SELECT * FROM token WHERE imageFile = ?";

    public HocrXmlExporter(File src, File dest, Document document) {
        super(src, dest, document);
    }

    @Override
    public void export() throws IOException, Exception {
        org.w3c.dom.Document doc = getDom();
        HashMap<Node, String> pageIndex = getPageIndex(doc);
        CompoundSet<HocrDomNode, HocrToken> compoundSet = getCompoundSet(pageIndex);
        export(compoundSet, doc);
    }

    private void export(
            CompoundSet<HocrDomNode, HocrToken> compoundSet,
            org.w3c.dom.Document doc
    ) throws FileNotFoundException, IOException {
        Writer writer;
        writer = new OutputStreamWriter(
                new FileOutputStream(getDestinationFile())
        );
        for (Compound<HocrDomNode, HocrToken> c : compoundSet.getAbs()) {
            writer.write(c.toString());
            writer.write("\n");
        }
        for (Compound<HocrToken, HocrDomNode> c : compoundSet.getBas()) {
            writer.write(c.toString());
            writer.write("\n");
        }
        writer.close();
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

    private HashMap<Node, String> getPageIndex(org.w3c.dom.Document doc)
            throws XPathExpressionException, Exception {
        NodeList pageNodes = (NodeList) compileXpath("//div[@class='ocr_page']")
                .evaluate(doc, XPathConstants.NODESET);
        HashMap<Node, String> pageIndex = new HashMap<>();
        for (int i = 0; i < pageNodes.getLength(); ++i) {
            Node node = pageNodes.item(i);
            String path = getImagePath(getAttributeValue(node, "title"));
            if (path != null) {
                pageIndex.put(node, path);
            }
        }
        return pageIndex;
    }

    private CompoundSet<HocrDomNode, HocrToken> getCompoundSet(
            HashMap<Node, String> pageIndex
    ) throws SQLException, Exception {
        ArrayList<HocrToken> tokens = loadAllTokensFromDocument(pageIndex);
        ArrayList<HocrDomNode> nodes = loadAllNodes(pageIndex);
        Log.debug(this, "tokens: %d", tokens.size());
        Log.debug(this, "nodes: %d", nodes.size());
        return new CompoundSet<>(nodes, tokens);
    }

    private ArrayList<HocrToken> loadAllTokensFromDocument(
            HashMap<Node, String> pageIndex
    ) throws SQLException {
        ArrayList<HocrToken> tokens = new ArrayList<>();
        for (Node node : pageIndex.keySet()) {
            PreparedStatement s = getDocument().prepareStatement(tokenStmnt);
            s.setString(1, pageIndex.get(node));
            MyIterator<Token> it = getDocument().selectTokens(s);
            while (it.hasNext()) {
                tokens.add(new HocrToken(it.next()));
            }
        }
        return tokens;
    }

    private ArrayList<HocrDomNode> loadAllNodes(
            HashMap<Node, String> pageIndex
    ) throws XPathExpressionException, Exception {
        ArrayList<HocrDomNode> nodes = new ArrayList<>();
        XPathExpression xpath = compileXpath("//span[@class='ocrx_word']");
        for (Node node : pageIndex.keySet()) {
            NodeList words = (NodeList) xpath.evaluate(
                    node,
                    XPathConstants.NODESET
            );
            for (int w = 0; w < words.getLength(); ++w) {
                Node word = words.item(w);
                TokenImageInfoBox tiib = getTokenImageInfoBox(
                        word,
                        pageIndex.get(node)
                );
                nodes.add(new HocrDomNode(word, tiib));
            }
        }
        return nodes;
    }

    private String getImagePath(String title) throws Exception {
        Matcher m = imgRe.matcher(title);
        if (!m.find()) {
            throw new Exception("page without an image: " + title);
        }

        String path = m.group(1);
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private String getAttributeValue(Node node, String name) {
        if (node.hasAttributes()) {
            Node attr = node.getAttributes().getNamedItem(name);
            if (attr != null) {
                return attr.getNodeValue();
            }
        }
        return null;
    }

    private XPathExpression compileXpath(String exp)
            throws XPathExpressionException {
        return XPathFactory.newInstance().newXPath().compile(exp);
    }

    private TokenImageInfoBox getTokenImageInfoBox(Node word, String img)
            throws Exception {
        String wordTitle = getAttributeValue(word, "title");
        if (wordTitle == null) {
            throw new Exception("ocr(x)_word without a title");
        }
        Node line = word.getParentNode();
        if (line == null) {
            throw new Exception("ocr(x)_word without a parent ocr_line");
        }
        String lineTitle = getAttributeValue(line, "title");
        if (lineTitle == null) {
            throw new Exception("ocr_line without a title");
        }
        TokenImageInfoBox wordTiib = getTokenImageInfoBox(wordTitle, img);
        TokenImageInfoBox lineTiib = getTokenImageInfoBox(lineTitle, img);
        if (word.getTextContent() != null && word.getTextContent().equals("ufum")) {
            Log.debug(this, "word:      %s", word.getTextContent());
            Log.debug(this, "wordTitle: %s", wordTitle);
            Log.debug(this, "wordTiib:  %s", wordTiib);
            Log.debug(this, "lineTitle: %s", lineTitle);
            Log.debug(this, "lineTiib:  %s", lineTiib);
        }
        return new TokenImageInfoBox(
                wordTiib.getCoordinateLeft(),
                lineTiib.getCoordinateTop(),
                wordTiib.getCoordinateRight(),
                lineTiib.getCoordinateBottom(),
                img
        );
    }

    private TokenImageInfoBox getTokenImageInfoBox(String title, String img)
            throws Exception {
        Matcher m = tiibRe.matcher(title);
        if (!m.find()) {
            throw new Exception("invalid bounding box: " + title);
        }

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
