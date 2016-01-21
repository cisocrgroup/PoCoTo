/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author finkf
 */
public class WagnerFischer {

    private final int[] truth, test;
    private final int[][] matrix;
    private final Trace trace;

    public enum EditOperation {

        Noop,
        Substitution,
        Deletion,
        Insertion
    };

    public class Trace extends ArrayList<EditOperation> {

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (EditOperation e : this) {
                switch (e) {
                    case Deletion:
                        builder.append('-');
                        break;
                    case Insertion:
                        builder.append('+');
                        break;
                    case Substitution:
                        builder.append('#');
                        break;
                    case Noop: // fall through
                    default:
                        builder.append('|');
                        break;
                }
            }
            return builder.toString();
        }
    };

    public WagnerFischer(String truth, String test) {
        this.truth = toArray(truth);
        this.test = toArray(test);
        matrix = new int[this.test.length + 1][this.truth.length + 1];
        trace = new Trace();
    }

    public int[] getTest() {
        return test;
    }

    public int[] getTruth() {
        return truth;
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
     * @return the levenshteindistance between test and truth
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
        assert ((i - 1) < test.length);
        assert ((j - 1) < truth.length);

        if (test[i - 1] == truth[j - 1]) {
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
        for (int i = test.length, j = truth.length; i > 0 && j > 0;) {
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
                trace.add(EditOperation.Noop);
            } else {
                trace.add(EditOperation.Substitution);
            }
        } else if (minArg.i == i && minArg.j == j - 1) {
            trace.add(EditOperation.Insertion);
        } else {
            trace.add(EditOperation.Deletion);
        }
        return minArg;
    }

    private MinArg getMinArg(int i, int j) {
        assert (i > 0);
        assert (j > 0);
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
    }

    private static int[] toArray(String str) {
        final int n = str.codePointCount(0, str.length());
        int res[] = new int[n];
        for (int i = 0, j = 0; i < n && j < str.length();) {
            res[i] = str.codePointAt(j);
            j += Character.charCount(res[i]);
            ++i;
        }
        return res;
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
        StringBuilder builder = new StringBuilder("\n");
        for (int i = 0, j = 0; i < trace.size() && j < test.length; ++i) {
            if (trace.get(i).equals(EditOperation.Insertion)) {
                builder.append('_');
            } else {
                builder.appendCodePoint(test[j]);
                ++j;
            }
        }
        builder.append('\n');
        builder.append(trace.toString());
        builder.append('\n');
        for (int i = 0, j = 0; i < trace.size() && j < truth.length; ++i) {
            if (trace.get(i).equals(EditOperation.Deletion)) {
                builder.append('_');
            } else {
                builder.appendCodePoint(truth[j]);
                ++j;
            }
        }
        return builder.toString();
    }
}
