/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

/**
 * This class represents HOCR Whitespace chars (which are not explicitly in the
 * file). Any instance of this class *must* lie between two token.
 *
 * @author finkf
 */
public class HocrWhitespaceChar extends AbstractBaseChar {

    private static final int WS = ' ';

    private final AbstractToken prevToken, nextToken;

    public HocrWhitespaceChar(Line line, AbstractToken prev, AbstractToken next) {
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
            prevToken.mergeRightward(null, nextToken);
            getLine().remove(i);
        }
    }

    @Override
    public void substitute(Char c) {
        if (!Character.isWhitespace(c.getChar())) {
            final int i = getIndexInLine();
            if (i != -1) {
                HocrChar newChar = new HocrChar(getLine(), c.getChar());
                prevToken.mergeRightward(newChar, nextToken);
                getLine().set(i, newChar);
            }
        }
    }

    /**
     * This call can only happen if WhitespaceChar is the last char on line,
     * which is illegal, since WhitespaceChar must always lie between two token.
     */
    @Override
    public Char append(int c) {
        throw new UnsupportedOperationException("Append called on HocrWhitespaceChar");
    }

    @Override
    public Char prepend(int c) {
        return prevToken.getLastChar().append(c);
    }
}
