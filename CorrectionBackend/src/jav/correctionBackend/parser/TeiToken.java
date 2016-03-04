/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import org.w3c.dom.Node;

/**
 *
 * @author finkf
 */
public class TeiToken extends AbstractToken {

    private final Node node;

    public TeiToken(Node node, Line line) {
        this.node = node;
        parse(line);
    }

    @Override
    public void update() {
        node.setNodeValue(this.toString());
    }

    @Override
    public void removeFromTree() {
        node.getParentNode().removeChild(node);
    }

    private void parse(Line line) {
        final String str = node.getNodeValue().trim();
        final int n = str.length();
        for (int i = 0; i < n;) {
            final int codepoint = str.codePointAt(i);
            add(new TeiChar(line, codepoint, this));
            i += Character.charCount(codepoint);
        }
    }
}
