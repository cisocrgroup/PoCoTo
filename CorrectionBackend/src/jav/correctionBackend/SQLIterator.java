/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend;

import java.util.Iterator;

/**
 *
 * @author finkf
 */
class SQLIterator<T> implements MyIterator<T> {

    private Iterator<T> it;

    public SQLIterator() {
        this(null); // SQLIterator handles it == null
    }

    public SQLIterator(Iterator<T> it) {
        this.it = it;
    }

    protected void setIterator(Iterator<T> it) {
        this.it = it;
    }

    @Override
    public boolean hasNext() {
        return it != null && it.hasNext();
    }

    @Override
    public T next() {
        assert (it != null);
        return it.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void reset() {
    }

    @Override
    public void cancel() {
    }
}
