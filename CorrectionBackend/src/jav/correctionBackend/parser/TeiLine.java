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
public class TeiLine extends Line {

    public TeiLine(Node node) {
        assert (node != null);
        parse(node);
    }

    private void parse(Node node) {
        final String line = node.getTextContent();
        final int length = line.length();
        for (int offset = 0; offset < length;) {
            final int codepoint = line.codePointAt(offset);
            offset += Character.charCount(codepoint);
            this.add(new TeiChar(codepoint, this));
        }
    }
}
