/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

import jav.correctionBackend.util.WagnerFischer;
import jav.logging.log4j.Log;
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
        info(i, wf);

        for (int j = 0; j < wf.getTrace().size(); ++j) {
            switch (wf.getTrace().get(j)) {
                case Deletion:
                    insert(i, j, truth.charAt(j));
                    break;
                case Substitution:
                    substitute(i, j, truth.charAt(j));
                    break;
                case Insertion:
                    delete(i, j);
                    break;
                default:
                    break;
            }
        }

    }

    /**
     * Inform about the edit operations (for debugging purposes).
     *
     * @param i index of the line
     * @param wf the resulting Wagner-Fischer structure
     */
    public void info(int i, WagnerFischer wf) {
        Log.debug(this, "i:     %d", i);
        Log.debug(this, "truth: %s", wf.getTruth());
        Log.debug(this, "trace: %s", wf.getTrace());
        Log.debug(this, "test:  %s", wf.getTest());
        StringBuilder builder = new StringBuilder();
        builder.append('\n');
        int[][] matrix = wf.getMatrix();
        for (i = 0; i < matrix.length; ++i) {
            for (int j = 0; j < matrix[i].length; ++j) {
                builder.append(matrix[i][j]).append(' ');
                if (matrix[i][j] < 10) {
                    builder.append(' ');
                }
            }
            builder.append('\n');
        }
        Log.debug(this, "wf %s", builder.toString());
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
