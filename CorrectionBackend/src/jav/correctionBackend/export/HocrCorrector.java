/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author finkf
 */
public class HocrCorrector extends DocumentCorrector {

    private final ArrayList<HocrLine> lines;

    public HocrCorrector() {
        lines = new ArrayList<>();
    }

    @Override
    public int getNumberOfLines() {
        return lines.size();
    }

    @Override
    public String getLineAt(int i) {
        return lines.get(i).toString();
    }

    @Override
    public void substitute(int i, int j, char c) {
        lines.get(i).substitute(j, c);
    }

    @Override
    public void delete(int i, int j) {
        lines.get(i).delete(j);
    }

    @Override
    public void insert(int i, int j, char c) {
        lines.get(i).insert(j, c);
    }

    @Override
    public void write() throws IOException {

    }
}
