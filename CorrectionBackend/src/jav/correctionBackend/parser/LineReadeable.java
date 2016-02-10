/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

/**
 * This interface represents a class that can be read line by line.
 *
 * @author flo
 */
public interface LineReadeable {

    /**
     * Get the numbers of lines
     *
     * @return the number of lines
     */
    public abstract int getNumberOfLines();

    /**
     * get the string representation of a line.
     *
     * @param i the index of the line
     * @return the String representation of the line
     */
    public abstract String getLineAt(int i);
}
