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

    private final String truth, test;
    private final int[][] matrix;
    private final Trace trace;

    public enum EditOperations {

        Noop,
        Substitution,
        Deletion,
        Insertion
    };

    public class Trace extends ArrayList<EditOperations> {

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (EditOperations e : this) {
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
        this.truth = truth;
        this.test = test;
        matrix = new int[test.length() + 1][truth.length() + 1];
        trace = new Trace();
    }

    public String getTest() {
        return test;
    }

    public String getTruth() {
        return truth;
    }

    public Trace getTrace() {
        return trace;
    }

    public int[][] getMatrix() {
        return matrix;
    }

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
        return matrix[test.length()][truth.length()];
    }

    private int getMin(int i, int j) {
        assert (i > 0);
        assert (j > 0);
        assert ((i - 1) < test.length());
        assert ((j - 1) < truth.length());

        if (test.charAt(i - 1) == truth.charAt(j - 1)) {
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
        for (int i = test.length(), j = truth.length(); i > 0 && j > 0;) {
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
                trace.add(EditOperations.Noop);
            } else {
                trace.add(EditOperations.Substitution);
            }
        } else if (minArg.i == i && minArg.j == j - 1) {
            trace.add(EditOperations.Insertion);
        } else {
            trace.add(EditOperations.Deletion);
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

    private class MinArg {

        private final int i, j;

        public MinArg(int i, int j) {
            this.i = i;
            this.j = j;
        }
    }
}
