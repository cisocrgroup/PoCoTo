/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Node;

/**
 * This class represents an non empty HOCR token. It is an error to create an
 * instance that points to an empty HOCR token.
 *
 * @author finkf
 */
public class HocrToken extends AbstractToken<HocrChar> {

    static Pattern BBRE
            = Pattern.compile("bbox\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)");
    private static final Pattern WCONF = Pattern.compile("x_wconf\\s+(\\d+)");

    private final Node node, title;
    private BoundingBox bb;

    public HocrToken(Line line, Node node) throws Exception {
        this.node = node;
        this.title = getTitleNode(node);
        parse(line);
        if (isEmpty()) {
            throw new Exception("Empty HOCR token not ignored");
        }
    }

    private HocrToken(Node node, Node title, boolean noparse) {
        this.node = node;
        this.title = title;
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
        for (Char c : this) {
            builder.appendCodePoint(c.getChar());
        }
        return builder.toString();
    }

    private BoundingBox gatherBoundingBox() {
        assert (!this.isEmpty());
        BoundingBox acc = this.get(0).getBoundingBox();
        for (int i = 1; i < this.size(); ++i) {
            acc.combineWith(this.get(i).getBoundingBox());
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

    @Override
    public void update() {
        if (!this.isEmpty()) {
            bb = gatherBoundingBox();
            String token = gatherToken();
            // node.getFirstChild().setNodeValue(token);
            node.setTextContent(token);
            Matcher m = BBRE.matcher(title.getNodeValue());
            String replacement = String.format("bbox %d %d %d %d",
                    bb.getLeft(), bb.getTop(), bb.getRight(), bb.getBottom()
            );
            m.replaceFirst(replacement);
            BoundingBox splits[] = bb.getVerticalSplits(this.size());
            assert (splits.length == this.size());
            for (int i = 0; i < splits.length; ++i) {
                get(i).setToken(this);
                get(i).setBoundingBox(splits[i]);
            }

        }
    }

    @Override
    public void removeFromTree() {
        node.getParentNode().removeChild(node);
    }

    private void parse(Line line) throws Exception {
        bb = getBoundingBox(this.node);
        String token = node.getFirstChild().getTextContent();

        final int n = token.codePointCount(0, token.length());
        BoundingBox splits[] = bb.getVerticalSplits(n);
        for (int i = 0, j = 0; j < n && i < token.length();) {
            final int cp = token.codePointAt(i);
            HocrChar newChar = new HocrChar(line, this, splits[j], cp);
            this.add(newChar);
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
