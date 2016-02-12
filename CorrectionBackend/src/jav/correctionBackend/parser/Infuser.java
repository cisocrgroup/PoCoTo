/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import jav.correctionBackend.util.WagnerFischer;
import jav.logging.log4j.Log;
import java.util.ArrayList;
import org.netbeans.api.progress.ProgressHandle;

/**
 * This class infuses a ground truth into an OCR document.
 *
 * @author finkf
 */
public class Infuser {

    private Book gt, ocr;
    private ProgressHandle ph;

    public void setProgressHandle(ProgressHandle ph) {
        this.ph = ph;
    }

    public void setGroundTruth(Book gt) {
        this.gt = gt;
    }

    public void setOCR(Book ocr) {
        this.ocr = ocr;
    }

    public void infuse() throws Exception {
        checkPageSizes();
        for (int i = 0; i < gt.size(); ++i) {
            ArrayList<Line> gtlines = gt.get(i).getAllLines();
            ArrayList<Line> ocrlines = ocr.get(i).getAllLines();

            final int on = ocrlines.size();
            final int gn = gtlines.size();
            for (int o = 0, g = 0; o < on && g < gn;) {
                log(String.format("infusing line %d/%d on page %d/%d", o + 1, on, i, gt.size()), false);
                if (lineLengthsAreTooDifferent(ocrlines.get(o).size(), gtlines.get(g).size())) {
                    log(String.format("Skipping line %d on page %d", o + 1, i + 1), true);
                    ++o;
                    continue;
                }
                final WagnerFischer wf = new WagnerFischer(
                        gtlines.get(g).toString(),
                        ocrlines.get(o).toString()
                );
                final int lev = wf.calculate();
                //Log.debug(this, "levenshtein(%d):\n%s", lev, wf.toString());
                //Log.debug(this, "matrix:\n%s", wf.matrixToString());
                if (levenshteinDistanceIsToLarge(lev, ocrlines.get(o).size())) {
                    log(String.format("Skipping line %d on page %d", o + 1, i + 1), true);
                    ++o;
                    continue;
                }
                Corrector.correct(wf, gtlines.get(g), ocrlines.get(o));
                ++g;
                ++o;
            }
        }
    }

    private boolean lineLengthsAreTooDifferent(int on, int gn) {
        final int delta = Math.abs(on - gn);
        return delta > (gn / 2);
    }

    private boolean levenshteinDistanceIsToLarge(int lev, int on) {
        return (on / 2) < lev;
    }

    private void checkPageSizes() throws Exception {
        if (gt.size() != ocr.size()) {
            throw new Exception(
                    String.format(
                            "Page count differ: ocr: %d, gt: %d",
                            ocr.size(),
                            gt.size()
                    )
            );
        }
    }

    private void log(String msg, boolean isError) {
        if (isError) {
            Log.error(this, msg);
        }
        if (ph != null) {
            ph.progress(msg);
        }
    }

}
