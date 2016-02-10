/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

/**
 * This interface represents a correctable line.
 *
 * @author finkf
 */
public interface LineCorrectable extends LineReadeable {

    /**
     * Substitute a character.
     *
     * @param i the index of the line
     * @param j the index of the char
     * @param c the substitution char (unicode code point)
     */
    public abstract void substitute(int i, int j, int c);

    /**
     * Insert a new character.
     *
     * @param i the index of the line
     * @param j the index where the new character should be inserted. If j is
     * equal to the size of the line, the character is appended to the end of
     * the line
     * @param c the new character (unicode code point)
     */
    public abstract void insert(int i, int j, int c);

    /**
     * Delete a character.
     *
     * @param i the index of the line
     * @param j the index of the character to delete
     */
    public abstract void delete(int i, int j);

    /**
     * Finish the correction
     *
     * @throws Exception if the correction could not be applied
     */
    public void finishCorrection() throws Exception;
}
