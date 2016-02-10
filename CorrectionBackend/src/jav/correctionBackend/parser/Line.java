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
public class Line extends ArrayList<Char> implements LineReadeable {

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

    @Override
    public int getNumberOfLines() {
        return this.size();
    }

    @Override
    public String getLineAt(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
