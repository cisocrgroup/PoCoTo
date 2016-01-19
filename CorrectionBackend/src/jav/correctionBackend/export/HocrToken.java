/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Node;

/**
 *
 * @author finkf
 */
public class HocrToken implements Iterable<HocrChar> {

    static Pattern BBRE
            = Pattern.compile("bbox\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)");
    private static final Pattern WCONF = Pattern.compile("x?_wconfg\\s+(\\d+)");

    private Node node, title;
    private ArrayList<HocrChar> chars;
    private String token;
    private BoundingBox bb;

    public HocrToken(Node node) throws Exception {
        this.node = node;
        parse();
    }

    public HocrChar get(int i) {
        return chars.get(i);
    }

    public int size() {
        return chars.size();
    }

    @Override
    public Iterator<HocrChar> iterator() {
        return chars.iterator();
    }

    public String charAt(int i) {
        return token.substring(i, i + 1);
    }

    public int getConfidence() {
        Matcher m = WCONF.matcher(title.getNodeValue());
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        } else {
            return 0;
        }
    }

    public void update() {
        BoundingBox newBoundingBox = chars.get(0).getBoundingBox();
        for (int i = 1; i < size(); ++i) {
            newBoundingBox.combineWith(chars.get(i).getBoundingBox());
        }
        Matcher m = BBRE.matcher(title.getNodeValue());
        String res = m.replaceFirst("bbox $1 $2 $3 $4");
        title.setNodeValue(res);
        node.getFirstChild().setNodeValue(token);
    }

    private void parse() throws Exception {
        title = getTitleNode(this.node);
        bb = getBoundingBox(this.node);
        token = node.getFirstChild().getNodeValue();
        chars = new ArrayList<>();
        BoundingBox splits[] = bb.getHorizontalSplits(token.length());
        for (int i = 0; i < token.length(); ++i) {
            HocrChar newChar = new HocrChar(this, splits[i], i);
            if (i > 0) {
                newChar.setPrev(chars.get(i - 1));
                chars.get(i - 1).setNext(newChar);
            }
            chars.add(newChar);
        }
    }

    public static BoundingBox getBoundingBox(Node node) {
        try {
            Node titleNode = getTitleNode(node);
            Matcher m = BBRE.matcher(titleNode.getNodeValue());
            if (m.find()) {
                return new BoundingBox(
                        Integer.parseInt(m.group(1)),
                        Integer.parseInt(m.group(2)),
                        Integer.parseInt(m.group(3)),
                        Integer.parseInt(m.group(4))
                );
            }
        } catch (Exception e) {
            // ignore;
        }
        return new BoundingBox(-1, -1, -1, -1);
    }

    private static Node getTitleNode(Node node) throws Exception {
        if (node.hasAttributes()) {
            org.w3c.dom.Node attr = node.getAttributes().getNamedItem("title");
            if (attr != null) {
                return attr;
            }
        }
        throw new Exception("Invalid ocr(x)_word: missing title");
    }
}
