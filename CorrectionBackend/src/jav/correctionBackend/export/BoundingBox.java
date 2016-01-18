/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

/**
 *
 * @author flo
 */
public class BoundingBox {

    private final int l, t, r, b;

    public BoundingBox(int l, int t, int r, int b) {
        this.l = l;
        this.t = t;
        this.r = r;
        this.b = b;
    }

    public BoundingBox() {
        this(-1, -1, -1, -1);
    }

    public int getTop() {
        return t;
    }

    public int getLeft() {
        return l;
    }

    public int getRight() {
        return r;
    }

    public int getBottom() {
        return b;
    }

    public int getWidth() {
        return Math.abs(r - l);
    }

    public int getHeight() {
        return Math.abs(b - t);
    }

    @Override
    public String toString() {
        return String.format("bbox %d %d %d %d", l, t, r, b);
    }
}
