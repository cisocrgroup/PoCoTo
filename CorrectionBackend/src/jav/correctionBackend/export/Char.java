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

    /**
     * Returns the BoundingBox of this Char.
     *
     * @return the BoundingBox of the Char
     */
    public BoundingBox getBoundingBox();

    /**
     * Get the characters of this Char.
     *
     * @return The characters of this Char.
     */
    public String getChar();

    /**
     * Returs whether this Character is suspicious (Abbyy).
     *
     * @return true if this Character is suspicious.
     */
    public String isSuspicious();

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
     * Remove this character from the line.
     */
    public void delete();

    /**
     * Substitute this Character with another.
     *
     * @param c the new Character.
     */
    public void substitute(String c);

    /**
     * Create a new Char instance and append it after this.
     *
     * @param c the data of the new Char.
     * @return the new Char that was appended.
     */
    public Char append(String c);

    /**
     * Create a new Character and put it before this Char.
     *
     * @param c the data of the new Char.
     * @return the new Char that was prepended.
     */
    public Char prepend(String c);
}
