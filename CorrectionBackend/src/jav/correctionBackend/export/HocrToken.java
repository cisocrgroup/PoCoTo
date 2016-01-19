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
 * This class represents an non empty HOCR token. It is an error to create an
 * instance that points to an empty HOCR token.
 *
 * @author finkf
 */
public class HocrToken implements Iterable<HocrChar> {

    static Pattern BBRE
            = Pattern.compile("bbox\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)");
    private static final Pattern WCONF = Pattern.compile("x?_wconfg\\s+(\\d+)");

    private Node node, title;
    private ArrayList<HocrChar> chars;
    private BoundingBox bb;

    public HocrToken(Node node) throws Exception {
        this.node = node;
        parse();
        if (chars.isEmpty()) {
            throw new Exception("Empty HOCR token not ignored");
        }
    }

    public HocrChar getFirstChar() {
        assert (!chars.isEmpty());
        return this.get(0);
    }

    public HocrChar getLastChar() {
        assert (!chars.isEmpty());
        return this.get(this.size() - 1);
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

    public int getConfidence() {
        Matcher m = WCONF.matcher(title.getNodeValue());
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        } else {
            return 0;
        }
    }

    public void update() {
        if (!chars.isEmpty()) {
            BoundingBox newBoundingBox = chars.get(0).getBoundingBox();
            StringBuilder builder = new StringBuilder(chars.get(0).getChar());
            for (int i = 1; i < chars.size(); ++i) {
                newBoundingBox.combineWith(chars.get(i).getBoundingBox());
                builder.append(chars.get(i).getChar());
            }
            bb = newBoundingBox;
            node.getFirstChild().setNodeValue(builder.toString());
            Matcher m = BBRE.matcher(title.getNodeValue());
            String replacement = String.format("bbox %d %d %d %d",
                    bb.getLeft(), bb.getTop(), bb.getRight(), bb.getBottom()
            );
            m.replaceFirst(replacement);
            BoundingBox splits[] = bb.getVerticalSplits(chars.size());
            assert (splits.length == chars.size());
            for (int i = 0; i < splits.length; ++i) {
                chars.get(i).setBoundingBox(splits[i]);
            }
        }
    }

    public void delete(HocrChar c) {
        int i = chars.indexOf(c);
        if (i != -1) {
            chars.remove(i);
            if (!chars.isEmpty()) {
                update();
            } else {
                // remove this token from the nodes;
            }
        }
    }

    private void parse() throws Exception {
        title = getTitleNode(this.node);
        bb = getBoundingBox(this.node);
        String token = node.getFirstChild().getNodeValue();
        chars = new ArrayList<>();

        final int n = token.codePointCount(0, token.length());
        BoundingBox splits[] = bb.getVerticalSplits(n);
        for (int i = 0, j = 0; j < n && i < token.length();) {
            final int cp = token.codePointAt(i);
            String str = new String(Character.toChars(cp));
            HocrChar newChar = new HocrChar(this, splits[j], str);
            if (i > 0) {
                newChar.setPrev(chars.get(i - 1));
                chars.get(i - 1).setNext(newChar);
            }
            chars.add(newChar);
            i += Character.charCount(cp);
            ++j;
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
