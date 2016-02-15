/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

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
     * Substitute the code point of this Character with another character.
     *
     * @param c the new character
     */
    public void substitute(Char c);

    /**
     * Prepend a new Character before this character.
     *
     * @param c the code point for the new Char.
     * @return the new Char to append.
     */
    public Char prepend(int c);

    /**
     * Append a new character after this one.
     *
     * @param c the code point for the new Char.
     * @return the new Char to append
     */
    public Char append(int c);
}
