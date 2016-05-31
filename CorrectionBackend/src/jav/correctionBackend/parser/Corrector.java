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
public class Corrector {

    public static void correct(Book correct, Book incorrect) {
        final int n = Math.min(
                correct.getNumberOfPages(),
                incorrect.getNumberOfPages()
        );
        for (int i = 0; i < n; ++i) {
            correct(correct.getPageAt(i), incorrect.getPageAt(i));
        }
    }

    public static void correct(Page correct, Page incorrect) {
        final int n = Math.min(correct.size(), incorrect.size());
        for (int i = 0; i < n; ++i) {
            correct(correct.get(i), incorrect.get(i));
        }
    }

    public static void correct(Paragraph correct, Paragraph incorrect) {
        final int n = Math.min(correct.size(), incorrect.size());
        for (int i = 0; i < n; ++i) {
            correct(correct.get(i), incorrect.get(i));
        }
    }

    public static int correct(Line correct, Line incorrect) {
        final WagnerFischer wf = new WagnerFischer(correct, incorrect);
        final int res = wf.calculate();
        //Log.debug(Corrector.class, "correct:   %s", correct);
        //Log.debug(Corrector.class, "incorrect: %s", incorrect);
        if (res > 0) {
            correct(wf, correct, incorrect);
        }
        return res;
    }

    public static void correct(WagnerFischer wf, Line gt, Line pred) {
        final WagnerFischer.Trace trace = wf.getTrace();
        // Log.debug(Corrector.class, "correct:   %s", gt);
        // Log.debug(Corrector.class, "trace:     %s", trace);
        // Log.debug(Corrector.class, "incorrect: %s", pred);

        for (int g = 0, p = 0, i = 0; i < trace.size(); ++i) {
            switch (trace.get(i)) {
                case DELETION:
                    // Log.debug(Corrector.class, "delete_1 %d: '%s'", p, pred);
                    pred.delete(p);
                    // Log.debug(Corrector.class, "delete_2 %d: '%s'", p, pred);
                    break;
                case SUBSTITUTION:
                    // Log.debug(Corrector.class, "subst_1 %d:  '%s'", p, pred);
                    pred.substitute(p, gt.get(g));
                    // Log.debug(Corrector.class, "subst_2 %d:  '%s'", p, pred);
                    ++p;
                    ++g;
                    break;
                case INSERTION:
                    // Log.debug(Corrector.class, "insert_1 %d:  '%s'", p, pred);
                    pred.insert(p, gt.get(g).getChar());
                    // Log.debug(Corrector.class, "insert_2 %d:  '%s'", p, pred);
                    ++p;
                    ++g;
                    break;
                default:
                    ++g;
                    ++p;
                    break;
            }
        }
        pred.finishCorrection();
    }

    private Corrector() {

    }
}
