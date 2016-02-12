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

    public class Statistics {

        public int nlines, nlev;
    }

    public Statistics gatherStatistics() throws Exception {
        final Statistics res = new Statistics();
        iterate(new Callback() {
            @Override
            public void apply(Line gt, Line ocr) {
                WagnerFischer wf = new WagnerFischer(gt.toString(), ocr.toString());
                res.nlines++;
                final int lev = wf.calculate();
                res.nlev += lev;
                //Log.debug(this, "Levenshtein(%d):\n%s", lev, wf.toString());
            }
        });
        return res;
    }

    public void infuse() throws Exception {
        iterate(new Callback() {
            @Override
            public void apply(Line gt, Line ocr) {
                WagnerFischer wf = new WagnerFischer(gt.toString(), ocr.toString());
                if (wf.calculate() > 0) {
                    Corrector.correct(wf, gt, ocr);
                }
            }
        });
    }

    private void iterate(Callback callback) throws Exception {
        checkPageSizes();
        for (int i = 0; i < gt.size(); ++i) {
            ArrayList<Line> gtlines = gt.get(i).getAllLines();
            ArrayList<Line> ocrlines = ocr.get(i).getAllLines();
            final int offset = getOffset(gtlines, ocrlines);
            log(String.format("importing page %d from %d", i + 1, gt.size()), false);
            if (offset == -1) {
                String msg = String.format("OCR page %d is too different from ground truth", i + 1);
                log(msg, true);
                continue;
            }
            for (int j = 0; j < gtlines.size() && j + offset < ocrlines.size(); ++j) {
                callback.apply(gtlines.get(j), ocrlines.get(j + offset));
            }
        }
    }

    private int getOffset(ArrayList<Line> gtlines, ArrayList<Line> ocrlines) {
        for (int offset = 0; offset < gtlines.size() && offset < ocrlines.size(); ++offset) {
            final int gtn = gtlines.get(0).size();
            final int ocrn = ocrlines.get(offset).size();
            final int diff = Math.abs(gtn - ocrn);
            if (diff < (gtn / 2)) {
                return offset;
            }
        }
        return -1;
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
        } else if (ph != null) {
            ph.progress(msg);
        }
    }

    private interface Callback {

        public void apply(Line gt, Line ocr);

    }

}
