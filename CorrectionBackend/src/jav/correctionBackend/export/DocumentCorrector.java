/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

import jav.correctionBackend.util.WagnerFischer;
import java.io.IOException;

/**
 *
 * @author finkf
 */
public abstract class DocumentCorrector implements LineReadeable {

    public void correctThisDocumentWith(LineReadeable corrections) {
        final int n = Math.min(getNumberOfLines(), corrections.getNumberOfLines());
        for (int i = 0; i < n; ++i) {
            doCorrectLine(i, corrections.getLineAt(i));
        }
    }

    private void doCorrectLine(int i, String truth) {
        final WagnerFischer wf = new WagnerFischer(truth, getLineAt(i));
        wf.calculate();

        // j -> trace index, k -> word index
        for (int j = 0, k = 0; j < wf.getTrace().size();) {
            switch (wf.getTrace().get(j)) {
                case Deletion:
                    delete(i, k);
                    ++j;
                    break;
                case Substitution:
                    substitute(i, k, truth.charAt(k));
                    ++k;
                    ++j;
                    break;
                case Insertion:
                    insert(i, k, truth.charAt(k));
                    ++k;
                    ++j;
                    break;
                default:
                    ++k;
                    ++j;
                    break;
            }
        }

    }

    /**
     * Substitute a character.
     *
     * @param i the index of the line
     * @param j the index of the char
     * @param c the substitution char
     */
    public abstract void substitute(int i, int j, char c);

    /**
     * Insert a new character.
     *
     * @param i the index of the line
     * @param j the index where the new character should be inserted. If j is
     * equal to the size of the line, the character is appended to the end of
     * the line
     * @param c the new character
     */
    public abstract void insert(int i, int j, char c);

    /**
     * Delete a character.
     *
     * @param i the index of the line
     * @param j the index of the character to delete
     */
    public abstract void delete(int i, int j);

    /**
     * Write the corrected file
     *
     * @throws IOException if an writing error occurs
     */
    public abstract void write() throws IOException;
}
