/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package jav.correctionBackend.export;

import jav.correctionBackend.Token;
import jav.correctionBackend.TokenImageInfoBox;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Node;

/**
 *
 * @author finkf
 */
public class HocrDomNode implements UpdateableDomNode {
    private static final Pattern BOUNDING_BOX_PATTERN = 
            Pattern.compile("bbox\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)");
    private final Node node;
    private final TokenImageInfoBox tiib;
    public HocrDomNode(org.w3c.dom.Node node, TokenImageInfoBox tiib) {
        this.node = node;
        this.tiib = tiib;
    }
    public HocrDomNode(Node node, String img) throws Exception {
        this.node = node;
        this.tiib = getTokenImageInfoBoxFromNode(img);
    }
    @Override
    public TokenImageInfoBox getTokenImageInfoBox() {
        return tiib;
    }
    @Override
    public void correctToken(Token token) {
        String str = token.getWCOR();
        if (str != null && !"".equals(str)) {
            node.setTextContent(str);
        }
    }
    @Override
    public void mergeWith(Iterable<UpdateableDomNode> ns) {
        StringBuilder str = new StringBuilder(getContent());
        for (UpdateableDomNode n: ns) {
            str.append(n.getContent());
            tiib.mergeWith(n.getTokenImageInfoBox());
        }
        node.setTextContent(str.toString());
        try {
            updateTokenImageInfoBox();
        } catch (Exception e) {}
    }
    @Override
    public String getContent() {
        return node.getTextContent();
    }
    @Override
    public String toString() {
        return node.getTextContent();
    }
    
    private void updateTokenImageInfoBox() throws Exception {
        Node title = getTitleNode(node);
        Matcher m = BOUNDING_BOX_PATTERN.matcher(title.getNodeValue());
        String res = m.replaceFirst("bbox $1 $2 $3 $4");
        title.setNodeValue(res);
    }
    
    private static Node getTitleNode(Node node) throws Exception {
        if (node.hasAttributes()) {
            org.w3c.dom.Node attr = node.getAttributes().getNamedItem("title");
            if (attr != null)
                return attr;
        }
        throw new Exception("Invalid ocr(x)_word: missing title");
    }
    
    private TokenImageInfoBox getTokenImageInfoBoxFromNode(String img) 
            throws Exception {
        TokenImageInfoBox a = parseTokenImageInfoBox(
                getTitleNode(node).getNodeValue(),
                img
        );
        Node parent = node.getParentNode();
        if (parent == null)
            throw new Exception("Invalid ocr(x)_word: missing parent line");
        TokenImageInfoBox b = parseTokenImageInfoBox(        
                getTitleNode(parent).getNodeValue(),
                img
        );
        return new TokenImageInfoBox(
                a.getCoordinateLeft(),
                b.getCoordinateTop(),
                a.getCoordinateRight(),
                b.getCoordinateBottom()
        );
        
    }
    private static TokenImageInfoBox parseTokenImageInfoBox(
            String title, 
            String img
    ) throws Exception {
        Matcher m = BOUNDING_BOX_PATTERN.matcher(title);
        if (!m.find())
            throw new Exception("Invalid ocr(x)_word: missing bounding box");
        return new TokenImageInfoBox(
                Integer.parseInt(m.group(1)),
                Integer.parseInt(m.group(2)),
                Integer.parseInt(m.group(3)),
                Integer.parseInt(m.group(4)),
                img
        );
    }
}
