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
    private BoundingBox bb;

    public TeiChar(int codepoint, Line line) {
        super(line);
        this.codepoint = codepoint;
        this.bb = new BoundingBox();
    }

    @Override
    public BoundingBox getBoundingBox() {
        return bb;
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
    public void substitute(Char c) {
        this.codepoint = c.getChar();
        this.bb = c.getBoundingBox();
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
