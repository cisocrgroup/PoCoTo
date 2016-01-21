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
public abstract class AbstractBaseChar implements Char {

    private final Line line;

    public AbstractBaseChar(Line line) {
        this.line = line;
    }

    public Line getLine() {
        return line;
    }

    public int getIndexInLine() {
        return line.indexOf(this);
    }

    @Override
    public Char getPrev() {
        final int i = getIndexInLine();
        return i > 0 ? line.get(i - 1) : null;
    }

    @Override
    public Char getNext() {
        final int i = getIndexInLine();
        if (i != -1) {
            if (i < (line.size() - 1)) {
                return line.get(i + 1);
            }
        }
        return null;
    }

}
