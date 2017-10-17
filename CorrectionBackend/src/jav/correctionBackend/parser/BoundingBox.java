/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import jav.correctionBackend.TokenImageInfoBox;

/**
 *
 * @author flo
 */
public class BoundingBox {

    private int l, t, r, b;

    public BoundingBox(TokenImageInfoBox tiib) {
        this(
                tiib == null ? -1 : tiib.getCoordinateLeft(),
                tiib == null ? -1 : tiib.getCoordinateTop(),
                tiib == null ? -1 : tiib.getCoordinateRight(),
                tiib == null ? -1 : tiib.getCoordinateBottom()
        );
    }

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

    public void setTop(int t) {
        this.t = t;
    }

    public int getLeft() {
        return l;
    }

    public void setLeft(int l) {
        this.l = l;
    }

    public int getRight() {
        return r;
    }

    public void setRight(int r) {
        this.r = r;
    }

    public int getBottom() {
        return b;
    }

    public void setBottom(int b) {
        this.b = b;
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

    public TokenImageInfoBox toTokenImageInfoBox() {
        return new TokenImageInfoBox(l, t, r, b);
    }

    /**
     * Combines this bounding box with another one. The bounding box growths
     * accordingly.
     *
     * @param other the other bounding box
     * @return this
     */
    public BoundingBox combineWith(BoundingBox other) {
        if (other != null) {
            l = Math.min(l, other.l);
            t = Math.min(t, other.t);
            r = Math.max(r, other.r);
            b = Math.max(b, other.b);
        }
        return this;
    }

    /**
     * Creates an array of vertical split bounding boxes
     *
     * @param n number of bounding boxes
     * @return array that contains the bounding boxes
     */
    public BoundingBox[] getVerticalSplits(int n) {
        BoundingBox res[] = new BoundingBox[n];
        final int width = getWidth() / n;
        final int ratio = getWidth() % n;
        int x0 = l;
        for (int i = 0; i < n; ++i) {
            int w = width;
            if (i < ratio) {
                w += 1;
            }
            res[i] = new BoundingBox(x0, t, x0 + w, b);
            x0 += w;
        }
        return res;
    }

}
