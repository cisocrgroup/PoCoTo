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
    private final ArrayList<EditOperations> trace;

    public enum EditOperations {

        Noop,
        Substitution,
        Deletion,
        Insertion
    };

    public WagnerFischer(String truth, String test) {
        this.truth = truth;
        this.test = test;
        matrix = new int[test.length() + 1][truth.length() + 1];
        trace = new ArrayList<>();
    }

    public String getTest() {
        return test;
    }

    public String getTruth() {
        return truth;
    }

    public ArrayList<EditOperations> getTrace() {
        return trace;
    }

    public int calculate() {
        for (int i = 0; i < matrix.length; ++i) {
            matrix[i][0] = i;
        }
        for (int i = 0; i < matrix[0].length; ++i) {
            matrix[0][i] = i;
        }
        for (int i = 1; i < matrix[0].length; ++i) {
            for (int j = 1; j < matrix[i].length; ++j) {
                setMatrix(i, j);
            }
        }
        backtrack();
        return matrix[test.length()][truth.length()];
    }

    private void setMatrix(int i, int j) {
        MinArg minArg = getMinArg(i, j);
        matrix[i][j] = matrix[minArg.i][minArg.j];
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
        if (matrix[i][j] == matrix[minArg.i][minArg.j]) {
            trace.add(EditOperations.Noop);
        } else if (minArg.i == i - 1 && minArg.j == j - 1) {
            trace.add(EditOperations.Substitution);
        } else if (minArg.i == i && minArg.j == j - 1) {
            trace.add(EditOperations.Deletion);
        } else {
            trace.add(EditOperations.Insertion);
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
                break;

        }
        throw new IllegalArgumentException("Index out of bounds: " + index);
    }

    private class MinArg {

        public MinArg(int i, int j) {
            this.i = i;
            this.j = j;
        }
        public final int i, j;
    }
}
