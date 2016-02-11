/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import jav.correctionBackend.Token;

/**
 *
 * @author flo
 */
public class SpreadIndexDocumentChar extends AbstractBaseChar {

    private int codepoint;
    private final Token token;

    public SpreadIndexDocumentChar(Line line, Token token, int codepoint) {
        super(line);
        this.codepoint = codepoint;
        this.token = token;
    }

    public Token getToken() {
        return token;
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void delete() {
        assert (false);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void substitute(int c) {
        codepoint = codepoint;
    }

    @Override
    public void prepend(int c) {
        assert (false);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void append(int c) {
        assert (false);
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
