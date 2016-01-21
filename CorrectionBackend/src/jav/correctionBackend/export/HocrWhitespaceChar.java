/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

/**
 * This class represents HOCR Whitespace chars (which are not explicitly in the
 * file). Any instance of this class *must* lie between two token.
 *
 * @author finkf
 */
public class HocrWhitespaceChar extends AbstractBaseChar {

    private static final int WS = ' ';

    private final HocrToken prevToken, nextToken;

    public HocrWhitespaceChar(Line line, HocrToken prev, HocrToken next) {
        super(line);
        assert (prev != null);
        assert (next != null);
        this.prevToken = prev;
        this.nextToken = next;
    }

    @Override
    public BoundingBox getBoundingBox() {
        BoundingBox prevBB = prevToken.getLastChar().getBoundingBox();
        BoundingBox nextBB = nextToken.getFirstChar().getBoundingBox();
        final int l = prevBB.getRight();
        final int t = Math.min(prevBB.getTop(), nextBB.getTop());
        final int r = nextBB.getLeft();
        final int b = Math.max(prevBB.getBottom(), nextBB.getBottom());
        return new BoundingBox(l, t, r, b);
    }

    @Override
    public boolean isSuspicious() {
        return false;
    }

    @Override
    public int getChar() {
        return WS;
    }

    @Override
    public void delete() {
        final int i = getIndexInLine();
        if (i != -1) {
            HocrToken.merge(prevToken, nextToken, null);
            getLine().remove(i);
        }
    }

    @Override
    public void substitute(int c) {
        if (!Character.isWhitespace(c)) {
            final int i = getIndexInLine();
            if (i != -1) {
                HocrChar newChar = new HocrChar(getLine(), c);
                HocrToken.merge(prevToken, nextToken, newChar);
                getLine().set(i, newChar);
            }
        }
    }

    /**
     * This call can only happen if WhitespaceChar is the last char on line,
     * which is illegeal, since WhitespaceChar are allways between two token.
     */
    @Override
    public void append(int c) {
        throw new UnsupportedOperationException("Append called on HocrWhitespaceChar");
    }

    @Override
    public void prepend(int c) {
        prevToken.getLastChar().append(c);
    }
}
