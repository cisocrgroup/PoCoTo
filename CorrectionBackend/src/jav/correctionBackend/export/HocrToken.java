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
    private static final Pattern WCONF = Pattern.compile("x_wconf\\s+(\\d+)");

    private final Node node, title;
    private ArrayList<HocrChar> chars;
    private BoundingBox bb;

    public HocrToken(Node node) throws Exception {
        this.node = node;
        this.title = getTitleNode(node);
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

    public BoundingBox getBoundingBox() {
        return bb;
    }

    private String gatherToken() {
        StringBuilder builder = new StringBuilder();
        for (HocrChar c : chars) {
            builder.append(c.getChar());
        }
        return builder.toString();
    }

    private BoundingBox gatherBoundingBox() {
        assert (!this.chars.isEmpty());
        BoundingBox acc = chars.get(0).getBoundingBox();
        for (int i = 1; i < chars.size(); ++i) {
            acc.combineWith(chars.get(i).getBoundingBox());
        }
        return acc;
    }

    @Override
    public String toString() {
        return String.format("HocrToken `%s` conf %d %s",
                gatherToken(),
                getConfidence(),
                getBoundingBox().toString()
        );
    }

    public void update() {
        if (!chars.isEmpty()) {
            bb = gatherBoundingBox();
            String token = gatherToken();
            // node.getFirstChild().setNodeValue(token);
            node.setTextContent(token);
            Matcher m = BBRE.matcher(title.getNodeValue());
            String replacement = String.format("bbox %d %d %d %d",
                    bb.getLeft(), bb.getTop(), bb.getRight(), bb.getBottom()
            );
            m.replaceFirst(replacement);
            BoundingBox splits[] = bb.getVerticalSplits(chars.size());
            assert (splits.length == chars.size());
            for (int i = 0; i < splits.length; ++i) {
                HocrChar prevChar = i > 0 ? this.chars.get(i - 1) : null;
                HocrChar nextChar = i < (chars.size() - 1) ? this.chars.get(i + 1) : null;
                HocrChar currentChar = chars.get(i);
                currentChar.setHocrToken(this);
                currentChar.setBoundingBox(splits[i]);
                if (prevChar != null) {
                    currentChar.setPrev(prevChar);
                }
                if (nextChar != null) {
                    currentChar.setNext(nextChar);
                }
            }
        }
    }

    public void delete(HocrChar c) {
        assert (!this.chars.isEmpty());
        int i = chars.indexOf(c);
        if (i != -1) {
            chars.remove(i);
            if (!chars.isEmpty()) {
                update();
            } else {
                node.getParentNode().removeChild(node);
            }
        }
    }

    public void mergeRightWith(HocrToken right) {
        assert (!this.chars.isEmpty());
        assert (!right.chars.isEmpty());
        this.chars.addAll(right.chars);
        update();
    }

    private void parse() throws Exception {
        bb = getBoundingBox(this.node);
        String token = node.getFirstChild().getTextContent();
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
