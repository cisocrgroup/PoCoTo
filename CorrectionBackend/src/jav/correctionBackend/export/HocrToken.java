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

    private Node node;
    private ArrayList<HocrChar> triples;
    private String token;
    private BoundingBox bb;

    public HocrToken(Node node) throws Exception {
        this.node = node;
        parse();
    }

    public HocrChar get(int i) {
        return triples.get(i);
    }

    @Override
    public Iterator<HocrChar> iterator() {
        return triples.iterator();
    }

    public String charAt(int i) {
        return token.substring(i, i + 1);
    }

    private void parse() throws Exception {
        String title = getTitleNode(this.node).getNodeValue();
        Matcher m = BBRE.matcher(title);
        if (!m.find()) {
            throw new Exception("Invalid ocr(x)_word: missing bbox entry in title");
        }
        bb = new BoundingBox(
                Integer.parseInt(m.group(1)),
                Integer.parseInt(m.group(2)),
                Integer.parseInt(m.group(3)),
                Integer.parseInt(m.group(4))
        );
        token = node.getFirstChild().getNodeValue();
        triples = new ArrayList<>();
        BoundingBox splits[] = bb.getHorizontalSplits(token.length());
        for (int i = 0; i < token.length(); ++i) {
            HocrChar newChar = new HocrChar(this, splits[i], i);
            if (i > 0) {
                newChar.setPrev(triples.get(i - 1));
                triples.get(i - 1).setNext(newChar);
            }
            triples.add(newChar);
        }
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
