/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

/**
 * This class represents a non whitespace HOCR Character.
 *
 * @author finkf
 */
public class HocrChar extends AbstractBaseChar {

    private int letter;
    private BoundingBox bb;
    private AbstractToken token;

    public HocrChar(Line line, AbstractToken token, BoundingBox bb, int letter) {
        super(line);
        this.token = token;
        this.letter = letter;
        this.bb = bb;
    }

    public HocrChar(Line line, int letter) {
        this(line, null, null, letter);
    }

    public void setBoundingBox(BoundingBox bb) {
        this.bb = bb;
    }

    public void setToken(AbstractToken token) {
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
        return false;
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
        token.append(this, new HocrChar(getLine(), token, bb, c));
    }

    @Override
    public void prepend(int c) {
        token.prepend(this, new HocrChar(getLine(), token, bb, c));
    }

}
