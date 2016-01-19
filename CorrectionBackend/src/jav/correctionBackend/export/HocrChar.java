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
public class HocrChar extends AbstractHocrChar {

    private static final int CONFIDENCE_THRESHOLD = 10;

    private String str;
    private BoundingBox bb;
    private final HocrToken token;

    public HocrChar(HocrToken token, BoundingBox bb, String str) {
        this.token = token;
        this.str = str;
        this.bb = bb;
    }

    public void setBoundingBox(BoundingBox bb) {
        this.bb = bb;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return bb;
    }

    @Override
    public String getChar() {
        return str;
    }

    @Override
    public boolean isSuspicious() {
        return token.getConfidence() <= CONFIDENCE_THRESHOLD;
    }

    @Override
    public void delete() {
        token.delete(this);
    }

    @Override
    public Char substitute(String c) {
        str = c;
        token.update();
        return this;
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
