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
public class TeiChar extends AbstractBaseChar {

    private int codepoint;

    public TeiChar(int codepoint, Line line) {
        super(line);
        this.codepoint = codepoint;
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
    public void delete() {
        getLine().remove(getIndexInLine());
    }

    @Override
    public void substitute(int c) {
        this.codepoint = c;
    }

    @Override
    public TeiChar prepend(int c) {
        final int i = getIndexInLine();
        if (i > 0) {
            getLine().add(i, new TeiChar(codepoint, getLine()));
        }
        return null;
    }

    @Override
    public TeiChar append(int c) {
        final int i = getIndexInLine();
        TeiChar newTeiChar = new TeiChar(codepoint, getLine());
        if (i == (getLine().size() - 1)) {
            getLine().add(newTeiChar);
        } else if (i >= 0) {
            getLine().add(i, newTeiChar);
        }
        return null;
    }

}
