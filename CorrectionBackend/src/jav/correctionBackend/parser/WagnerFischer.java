/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import jav.correctionBackend.util.Tokenization;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author finkf
 */
public class WagnerFischer {

    private final Line gt, ocr;
    private final int[][] matrix;
    private final Trace trace;

    public enum EditOperation {

        NOOP, // No operation
        SUBSTITUTION,
        DELETION,
        INSERTION
    };

    public class Trace extends ArrayList<EditOperation> {

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (EditOperation e : this) {
                switch (e) {
                    case DELETION:
                        builder.append('-');
                        break;
                    case INSERTION:
                        builder.append('+');
                        break;
                    case SUBSTITUTION:
                        builder.append('#');
                        break;
                    case NOOP: // fall through
                    default:
                        builder.append('|');
                        break;
                }
            }
            return builder.toString();
        }
    }

    public WagnerFischer(Line gt, Line ocr) {
        this.gt = gt;
        this.ocr = ocr;
        matrix = new int[this.ocr.size() + 1][this.gt.size() + 1];
        trace = new Trace();
    }

    public Line getOcr() {
        return ocr;
    }

    public Line getGroundTruth() {
        return gt;
    }

    public Trace getTrace() {
        return trace;
    }

    public int[][] getMatrix() {
        return matrix;
    }

    /**
     * Calculate the matrix and the trace.
     *
     * @return the Levenshtein distance between test and truth
     */
    public int calculate() {
        for (int i = 0; i < matrix.length; ++i) {
            matrix[i][0] = i;
        }
        for (int i = 0; i < matrix[0].length; ++i) {
            matrix[0][i] = i;
        }
        for (int i = 1; i < matrix.length; ++i) {
            for (int j = 1; j < matrix[i].length; ++j) {
                matrix[i][j] = getMin(i, j);
            }
        }
        backtrack();
        return matrix[matrix.length - 1][matrix[0].length - 1];
    }

    private int getMin(int i, int j) {
        assert (i > 0);
        assert (j > 0);
        assert ((i - 1) < ocr.size());
        assert ((j - 1) < gt.size());

        if (ocr.get(i - 1).getChar() == gt.get(j - 1).getChar()) {
            return matrix[i - 1][j - 1];
        } else {
            int[] tmp = {
                matrix[i - 1][j - 1] + 1,
                matrix[i - 1][j] + 1,
                matrix[i][j - 1] + 1
            };
            return Collections.min(Arrays.asList(ArrayUtils.toObject(tmp)));
        }
    }

    private void backtrack() {
        for (int i = ocr.size(), j = gt.size(); i > 0 || j > 0;) {
            MinArg minArg = setTrace(i, j);
            i = minArg.i;
            j = minArg.j;
        }
        Collections.reverse(trace);
    }

    private MinArg setTrace(int i, int j) {
        MinArg minArg = getMinArg(i, j);
        if (minArg.i == i - 1 && minArg.j == j - 1) {
            if (matrix[i - 1][j - 1] == matrix[i][j]) {
                trace.add(EditOperation.NOOP);
            } else {
                trace.add(EditOperation.SUBSTITUTION);
            }
        } else if (minArg.i == i && minArg.j == j - 1) {
            trace.add(EditOperation.INSERTION);
        } else {
            trace.add(EditOperation.DELETION);
        }
        return minArg;
    }

    private MinArg getMinArg(int i, int j) {
        if (i > 0 && j > 0) {
            int choices[] = {matrix[i - 1][j - 1], matrix[i - 1][j], matrix[i][j - 1]};
            int min = Collections.min(Arrays.asList(ArrayUtils.toObject(choices)));
            int index = Arrays.asList(ArrayUtils.toObject(choices)).indexOf(min);
            switch (index) {
                case 0:
                    return new MinArg(i - 1, j - 1);
                case 1:
                    return new MinArg(i - 1, j);
                case 2:
                    return new MinArg(i, j - 1);
                default:
                    throw new IllegalArgumentException("Index out of bounds: " + index);
            }
        } else if (i > 0) {
            return new MinArg(i - 1, j);
        } else { // j > 0
            return new MinArg(i, j - 1);
        }
    }

    private class MinArg {

        private final int i, j;

        public MinArg(int i, int j) {
            this.i = i;
            this.j = j;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0, j = 0; i < trace.size(); ++i) {
            if (trace.get(i).equals(EditOperation.INSERTION)) {
                builder.append('_');
            } else {
                builder.appendCodePoint(ocr.get(j).getChar());
                if (Tokenization.isNonSpacingMark(ocr.get(j).getChar())) {
                    builder.append('_');
                }
                ++j;
            }
        }
        builder.append('\n');
        builder.append(trace.toString());
        builder.append('\n');
        for (int i = 0, j = 0; i < trace.size(); ++i) {
            if (trace.get(i).equals(EditOperation.DELETION)) {
                builder.append('_');
            } else {
                builder.appendCodePoint(gt.get(j).getChar());
                if (Tokenization.isNonSpacingMark(gt.get(j).getChar())) {
                    builder.append('_');
                }
                ++j;
            }
        }
        return builder.toString();
    }

    public String matrixToString() {
        StringBuilder builder = new StringBuilder();
        builder.append("   ");
        for (int i = 0; i < matrix[0].length; ++i) {
            if (i > 0) {
                if (Tokenization.isNonSpacingMark(gt.get(i - 1).getChar())) {
                    builder.append('_');
                }
                builder.appendCodePoint(gt.get(i - 1).getChar()).append("  ");
            } else {
                builder.append("   ");
            }
        }
        builder.append('\n');

        for (int i = 0; i < matrix.length; ++i) {
            if (i > 0) {
                if (Tokenization.isNonSpacingMark(ocr.get(i - 1).getChar())) {
                    builder.append('_');
                }
                builder.appendCodePoint(ocr.get(i - 1).getChar()).append("  ");
            } else {
                builder.append("   ");
            }
            for (int j = 0; j < matrix[i].length; ++j) {
                if (matrix[i][j] < 10) {
                    builder.append(matrix[i][j]).append("  ");
                } else {
                    builder.append(matrix[i][j]).append(" ");
                }
            }
            builder.append('\n');
        }
        return builder.toString();
    }
}
