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
public abstract class AbstractHocrChar implements Char {

    private AbstractHocrChar prev, next;

    public AbstractHocrChar() {
        this(null, null);
    }

    public AbstractHocrChar(AbstractHocrChar prev, AbstractHocrChar next) {
        this.prev = prev;
        this.next = next;
    }

    @Override
    public Char getPrev() {
        return prev;
    }

    public void setPrev(AbstractHocrChar prev) {
        this.prev = prev;
    }

    @Override
    public Char getNext() {
        return next;
    }

    public void setNext(AbstractHocrChar next) {
        this.next = next;
    }
}
