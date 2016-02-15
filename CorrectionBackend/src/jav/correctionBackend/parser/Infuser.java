/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

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

    /**
     * Infuse the ground truth book into the ocr book
     *
     * @throws Exception
     */
    public void infuse() throws Exception {
        checkPageSizes();
        final int pn = gt.getNumberOfPages();
        for (int i = 0; i < pn; ++i) {
            log(String.format("infusing page %d/%d", i, pn), false);
            ArrayList<Line> gtlines = gt.getPageAt(i).getAllLines();
            ArrayList<Line> ocrlines = ocr.getPageAt(i).getAllLines();
            final int on = ocrlines.size();
            final int gn = gtlines.size();

            for (Index idx = new Index(0, 0); idx.g < gn && idx.o < on; idx.inc()) {
                WagnerFischer wf = allign(gtlines, ocrlines, idx);
                if (wf == null) {
                    break;
                }
                Corrector.correct(wf, gtlines.get(idx.g), ocrlines.get(idx.o));
            }
        }
    }

    private class Index {

        int g, o;

        public Index(int g, int o) {
            this.g = g;
            this.o = o;
        }

        public void inc() {
            g++;
            o++;
        }
    }

    private WagnerFischer allign(ArrayList<Line> gt, ArrayList<Line> ocr, Index idx) {
        for (int gg = idx.g; gg < gt.size(); ++gg) {
            Line gtline = gt.get(gg);
            for (int oo = idx.o; oo < ocr.size(); ++oo) {
                Line ocrline = ocr.get(oo);
                //log(String.format("testing %d %d\n%s\nand\nd%s", gg, oo, gtline, ocrline), true);
                if (!lineLengthsAreTooDifferent(ocrline.size(), gtline.size())) {
                    WagnerFischer wf = new WagnerFischer(gtline, ocrline);
                    final int lev = wf.calculate();
                    if (!levenshteinDistanceIsToLarge(lev, ocrline.size())) {
                        //log(String.format("alligning %d %d\n%s\nand\n%s", gg, oo, gtline, ocrline), true);
                        idx.g = gg;
                        idx.o = oo;
                        return wf;
                    }
                }
            }
        }
        return null;
    }

    private boolean lineLengthsAreTooDifferent(int on, int gn) {
        final int delta = Math.abs(on - gn);
        return delta > (gn / 2);
    }

    private boolean levenshteinDistanceIsToLarge(int lev, int on) {
        return (on / 2) < lev;
    }

    private void checkPageSizes() throws Exception {
        if (gt.getNumberOfPages() != ocr.getNumberOfPages()) {
            throw new Exception(
                    String.format(
                            "Page count differ: ocr: %d, gt: %d",
                            ocr.getNumberOfPages(),
                            gt.getNumberOfPages()
                    )
            );
        }
    }

    private void log(String msg, boolean isError) {
        if (isError) {
            Log.error(this, msg);
        } else if (ph != null) {
            ph.progress(msg);
        }
    }

}
