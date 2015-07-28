/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

import java.util.HashSet;

/**
 *
 * @author finkf
 */
public class CompoundSet<
        A extends HasTokenImageInfoBox,
        B extends HasTokenImageInfoBox
> {
    private final HashSet<Compound<A, B> > abs;
    private final HashSet<Compound<B, A> > bas;
    
    public CompoundSet(Iterable<A> as, Iterable<B> bs) {
        abs = new HashSet<>();
        bas = new HashSet<>();
        addAllA(as, bs);
        addAllB(as, bs);
    }
    
    private void addAllA(Iterable<A> as, Iterable<B> bs) {
        for (A a: as) {
            Compound<A, B> c = new Compound<>(a);
            abs.add(c);
            for (B b: bs) {
                if (c.overlaps(b))
                    c.add(b);
            }
        }
    }
    
    private void addAllB(Iterable<A> as, Iterable<B>bs) {
        for (B b: bs) {
            Compound<B, A> c = new Compound<>(b);
            bas.add(c);
            for (A a: as) {
                if (c.overlaps(a))
                    c.add(a);
            }
        }
    }
    
    public Iterable<Compound<A, B>> getAbs() {
        return abs;
    }
    
    public Iterable<Compound<B, A>> getBas() {
        return bas;
    }

    public Compound<A, B> getA(A a) {
        for (Compound<A, B> c: abs) {
            if (c.getA() == a)
                return c;
        }
        return null;
    }
    
    public Compound<B, A> getB(B b) {
        for (Compound<B, A> c: bas) {
            if (c.getA() == b)
                return c;
        }
        return null;
    }
}
