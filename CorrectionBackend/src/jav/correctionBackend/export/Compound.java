/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

import jav.correctionBackend.TokenImageInfoBox;
import jav.logging.log4j.Log;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author finkf
 */
public class Compound<
        A extends HasTokenImageInfoBox, 
        B extends HasTokenImageInfoBox
> implements Iterable<B> {
    private final A a;
    private final HashSet<B> bs;
    public Compound(A a) {
        assert(a != null);
        this.a = a;
        this.bs = new HashSet<>();
    }
    @Override
    public Iterator<B> iterator() {
        return bs.iterator();
    }
    public boolean overlaps(B b) {
        return overlapsOnLine(b);
    }
    public boolean isMapping() {
        return bs.size() == 1;
    }
    public boolean isEmpty() {
        return bs.isEmpty();
    }
    public int size() {
        return bs.size();
    }
    public boolean contains(B b) {
        return bs.contains(b);
    }
    public boolean add(B b) {
        return bs.add(b);
    }
    public B getSingleElement() {
        assert(isMapping());
        return bs.iterator().next();
    }
    public A getA() {
        return a;
    }
    private boolean overlapsOnLine(B b) {
        TokenImageInfoBox x = a.getTokenImageInfoBox();
        TokenImageInfoBox y = b.getTokenImageInfoBox();
        //return a.toString().equals(b.toString());
        //return x.equals(y);
        if (x.getCoordinateBottom() == y.getCoordinateBottom() &&
                x.getCoordinateTop() == y.getCoordinateTop()) {
            if (x.getCoordinateRight() < y.getCoordinateLeft())
                return false;
            if (x.getCoordinateLeft() > y.getCoordinateRight())
                return false;
            return true;
        }
        return false;
    }
    @Override
    public String toString() {
        if (isEmpty()) {
            return a.toString() + " | {} | {}";
        }
        StringBuilder str = new StringBuilder(a.toString());
        str.append(" |");
        for (B b: bs) 
            str.append(" ").append(b.toString());
        str.append(" | ").append(a.getTokenImageInfoBox().toString()).append(" |");
        for (B b: bs) {
            str.append(" ").append(b.getTokenImageInfoBox().toString());
            str.append(" equal: ").append(b.getTokenImageInfoBox().equals(a.getTokenImageInfoBox()));
        }
        return str.toString();
    }
}
