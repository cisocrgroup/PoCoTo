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
public interface Char {

    public BoundingBox getBoundingBox();

    public Char getPrev();

    public Char getNext();

    public void delete();

    public void substitute(String c);

    public String getChar();

    public Char append(String c);

    public Char prepend(String c);
}
