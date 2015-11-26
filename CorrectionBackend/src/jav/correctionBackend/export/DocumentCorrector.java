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
            doCorrectLine(i, corrections);
        }
    }

    private void doCorrectLine(int i, LineReadeable corrections) {
        final String truth = corrections.getLineAt(i);
        final WagnerFischer wf = new WagnerFischer(truth, getLineAt(i));
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

    // interface
    public abstract void substitute(int i, int j, char c);

    public abstract void insert(int i, int j, char c);

    public abstract void delete(int i, int j);

    public abstract void write() throws IOException;
}
