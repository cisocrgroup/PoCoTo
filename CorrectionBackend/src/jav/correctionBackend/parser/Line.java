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

    public void substitute(int idx, Char c) {
        get(idx).substitute(c);
    }

    public void delete(int idx) {
        get(idx).delete();
        remove(idx);
    }

    public void insert(int idx, int codepoint) {
        if (this.isEmpty()) {
            throw new RuntimeException("Cannot insert into empty line");
        }

        if (idx < size()) {
            Char newChar = get(idx).prepend(codepoint);
            add(idx, newChar);
        } else {
            Char newChar = get(size() - 1).append(codepoint);
            add(newChar);
        }
    }

    /**
     * This method should be called if any corrections have been applied to this
     * line. Specialized Line implementations should override this method do do
     * something usefull.
     */
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
