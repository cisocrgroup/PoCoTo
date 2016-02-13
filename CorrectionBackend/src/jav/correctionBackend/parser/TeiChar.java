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
    private final TeiToken teiToken;
    private BoundingBox bb;

    public TeiChar(Line line, int codepoint, TeiToken teiToken) {
        super(line);
        this.teiToken = teiToken;
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
    public void substitute(Char c) {
        this.codepoint = c.getChar();
        this.bb = c.getBoundingBox();
        teiToken.update();
    }

    @Override
    public void delete() {
        teiToken.delete(this);
    }

    @Override
    public TeiChar prepend(int c) {
        TeiChar newChar = new TeiChar(getLine(), c, teiToken);
        teiToken.prepend(this, newChar);
        return newChar;
    }

    @Override
    public TeiChar append(int c) {
        TeiChar newChar = new TeiChar(getLine(), c, teiToken);
        teiToken.append(this, newChar);
        return newChar;
    }
}
