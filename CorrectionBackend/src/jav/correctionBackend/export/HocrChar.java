/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

/**
 *
 * @author finkf
 */
public class HocrChar implements Char {

    private int i;
    private BoundingBox bb;
    private HocrToken token;
    private HocrChar prev, next;

    public HocrChar(HocrToken token, BoundingBox bb, int i) {
        this.token = token;
        this.i = i;
        this.bb = bb;
        prev = next = null;
    }

    public boolean isWhiteSpace() {
        return false;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return bb;
    }

    @Override
    public Char getPrev() {
        return prev;
    }

    public void setPrev(HocrChar prev) {
        this.prev = prev;
    }

    @Override
    public Char getNext() {
        return next;
    }

    public void setNext(HocrChar next) {
        this.next = next;
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void substitute(String c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getChar() {
        return token.charAt(i);
    }

    @Override
    public Char append(String c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Char prepend(String c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
