/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

/**
 *
 * @author finkf
 */
public interface LineReadeable {

    public abstract int getNumberOfLines();

    public abstract String getLineAt(int i);
}
