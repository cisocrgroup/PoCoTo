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

    private final Node node;

    public TeiLine(Node node) {
        this.node = node;
    }

    public void add(String str) {
        str = str.trim();
        if (str.isEmpty()) {
            return;
        }
        if (!this.isEmpty()) {
            add(new TeiChar(' ', this));
        }
        for (int offset = 0; offset < str.length();) {
            final int codepoint = str.codePointAt(offset);
            add(new TeiChar(codepoint, this));
            offset += Character.charCount(codepoint);
        }
    }
}
