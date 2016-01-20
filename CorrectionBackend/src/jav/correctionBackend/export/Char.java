/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

/**
 * The char interface defines one code point in the OCR document.
 *
 * @author flo
 */
public interface Char {

    /**
     * Returns the BoundingBox of this Char.
     *
     * @return the BoundingBox of the Char
     */
    public BoundingBox getBoundingBox();

    /**
     * Get the code point of this Char.
     *
     * @return The unicode code point of this char
     */
    public int getChar();

    /**
     * Returns whether this Character is suspicious (Abbyy).
     *
     * @return true if this Character is suspicious.
     */
    public boolean isSuspicious();

    /**
     * Returns the previous Char or null if it is the first Char on the line.
     *
     * @return the previous Char or null.
     */
    public Char getPrev();

    /**
     * Return the next Char or null if it is the last Char on the line.
     *
     * @return the next Char or null.
     */
    public Char getNext();

    /**
     * Remove this character from its line.
     */
    public void delete();

    /**
     * Substitute the code point of this Character with another code point.
     *
     * @param c the new code point
     * @return a new Character or a reference to itself
     */
    public Char substitute(int c);

    /**
     * Create a new Char instance and append it after this.
     *
     * @param c the code point for the new Char.
     * @return the new Char that was appended.
     */
    public Char append(int c);

    /**
     * Create a new Character and put it before this Char.
     *
     * @param c the code point for the new Char.
     * @return the new Char that was prepended.
     */
    public Char prepend(int c);
}
