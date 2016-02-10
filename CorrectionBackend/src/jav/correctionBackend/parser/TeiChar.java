/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

/**
 *
 * @author finkf
 */
public class TeiChar implements Char {

    private int codepoint;
    private final TeiLine line;

    public TeiChar(int codepoint, TeiLine line) {
        this.codepoint = codepoint;
        this.line = line;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return new BoundingBox();
    }

    @Override
    public int getChar() {
        return codepoint;
    }

    @Override
    public boolean isSuspicious() {
        return false;
    }

    @Override
    public Char getPrev() {
        int i = line.indexOf(this);
        return i > 0 ? line.get(i - 1) : null;
    }

    @Override
    public Char getNext() {
        int i = line.indexOf(this);
        return i < (line.size() - 1) && i > 0 ? line.get(i + 1) : null;
    }

    @Override
    public void delete() {
        line.remove(this);
    }

    @Override
    public void substitute(int c) {
        this.codepoint = c;
    }

    @Override
    public void prepend(int c) {
        int i = line.indexOf(this);
        if (i > 0) {
            line.add(i, new TeiChar(codepoint, line));
        }
    }

    @Override
    public void append(int c) {
        int i = line.indexOf(this);
        TeiChar newTeiChar = new TeiChar(codepoint, line);
        if (i == (line.size() - 1)) {
            line.add(newTeiChar);
        } else if (i >= 0) {
            line.add(i, newTeiChar);
        }
    }

}
