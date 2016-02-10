/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import jav.correctionBackend.util.WagnerFischer;
import jav.logging.log4j.Log;

/**
 *
 * @author finkf
 */
public class Corrector {

    private final LineReadeable corrections;
    private final LineCorrectable toCorrect;

    public Corrector(LineReadeable corrections, LineCorrectable toCorrect) {
        this.corrections = corrections;
        this.toCorrect = toCorrect;
    }

    public void correct() throws Exception {
        final int n = Math.min(
                corrections.getNumberOfLines(),
                toCorrect.getNumberOfLines()
        );
        for (int i = 0; i < n; ++i) {
            applyWagnerFischer(i);
        }
    }

    private void applyWagnerFischer(int i) throws Exception {
        final WagnerFischer wf = new WagnerFischer(
                corrections.getLineAt(i),
                toCorrect.getLineAt(i)
        );
        if (wf.calculate() <= 0) { // skip if Levenshteindistance <= 0
            return;
        }
        Log.info(this, "Levenshtein:\n%s", wf.toString());
        for (int j = 0, k = 0; j < wf.getTrace().size();) {
            switch (wf.getTrace().get(j)) {
                case Deletion:
                    toCorrect.delete(i, k);
                    ++j;
                    break;
                case Substitution:
                    toCorrect.substitute(i, k, wf.getTruth()[k]);
                    ++k;
                    ++j;
                    break;
                case Insertion:
                    toCorrect.insert(i, k, wf.getTruth()[k]);
                    ++k;
                    ++j;
                    break;
                default:
                    ++k;
                    ++j;
                    break;
            }
        }
        toCorrect.finishCorrection();
    }
}
