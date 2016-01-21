/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

/**
 * This class represents a non whitespace HOCR Character.
 *
 * @author finkf
 */
public class HocrChar extends AbstractHocrChar {

    private static final int CONFIDENCE_THRESHOLD = 10;

    private int letter;
    private BoundingBox bb;
    private HocrToken token;

    public HocrChar(HocrToken token, BoundingBox bb, int letter) {
        this.token = token;
        this.letter = letter;
        this.bb = bb;
    }

    public void setBoundingBox(BoundingBox bb) {
        this.bb = bb;
    }

    public void setHocrToken(HocrToken token) {
        assert (token != null);
        this.token = token;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return bb;
    }

    @Override
    public int getChar() {
        return letter;
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
    public void substitute(int c) {
        letter = c;
        token.update();
    }

    @Override
    public void append(int c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void prepend(int c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
