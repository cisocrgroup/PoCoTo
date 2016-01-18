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
public class HocrWhitespaceChar extends AbstractHocrChar {

    private static final String WS = " ";

    private final HocrToken prev, next;

    public HocrWhitespaceChar(HocrToken prev, HocrToken next) {
        assert (prev != null);
        assert (next != null);
        this.prev = prev;
        this.next = next;
    }

    @Override
    public BoundingBox getBoundingBox() {
        throw new UnsupportedOperationException("Not supported yet.");
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
        return WS;
    }

    @Override
    public Char append(String c) {
        return next.get(0).prepend(c);
    }

    @Override
    public Char prepend(String c) {
        return prev.get(prev.size() - 1).append(c);
    }
}
