/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import java.util.ArrayList;

/**
 *
 * @author flo
 */
public class Line extends ArrayList<Char> {

    private BoundingBox bb;

    public Line() {
        this(null);
    }

    public Line(BoundingBox bb) {
        this.bb = bb;
    }

    public BoundingBox getBoundingBox() {
        return bb;
    }

    public void setBoundingBox(BoundingBox bb) {
        this.bb = bb;
    }

    public void substitute(int idx, int codepoint) {
        get(idx).substitute(codepoint);
    }

    public void delete(int idx) {
        get(idx).delete();
        remove(idx);
    }

    public void insert(int idx, int codepoint) {
        if (0 < size()) {
            if (idx < size()) {
                get(idx).prepend(codepoint);
            } else {
                get(size() - 1).append(codepoint);
            }
        }
    }

    public void finishCorrection() {
        // do nothing
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Char c : this) {
            builder.appendCodePoint(c.getChar());
        }
        return builder.toString();
    }
}
