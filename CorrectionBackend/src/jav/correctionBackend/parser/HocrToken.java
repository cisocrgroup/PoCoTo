/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import java.util.regex.Matcher;
import org.w3c.dom.Node;

/**
 * This class represents an non empty HOCR token. It is an error to create an
 * instance that points to an empty HOCR token.
 *
 * @author finkf
 */
public class HocrToken extends AbstractToken<HocrChar> {

    private final Node node;
    private BoundingBox bb;

    public HocrToken(Line line, Node node, BoundingBox bb) throws Exception {
        this.node = node;
        this.bb = bb;
        parse(line);
        if (isEmpty()) {
            throw new Exception("Empty HOCR token not ignored");
        }
    }

    private HocrToken(Node node, Node title, BoundingBox bb, boolean noparse) {
        this.node = node;
        this.bb = bb;
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
        return String.format("HocrToken `%s` %s",
                gatherToken(),
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
            Matcher m = HocrPageParser.BBRE.matcher(node.getAttributes().getNamedItem("title").getNodeValue());
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
        final String token = node.getFirstChild().getTextContent();
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
}
