/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import jav.correctionBackend.Document;

/**
 *
 * @author flo
 */
public class SpreadIndexDocumentChar implements Char {

    private final int codepoint;
    private final Document document;

    public SpreadIndexDocumentChar(Document document, int codepoint) {
        this.codepoint = codepoint;
        this.document = document;
    }

    @Override
    public BoundingBox getBoundingBox() {
        throw new UnsupportedOperationException("Not supported yet.");
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
    public Char getPrev() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Char getNext() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void substitute(int c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void prepend(int c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void append(int c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
