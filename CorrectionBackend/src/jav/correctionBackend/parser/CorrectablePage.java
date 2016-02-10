/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author flo
 */
public class CorrectablePage extends ArrayList<Line> implements LineCorrectable {

    private final File dest;
    private final PageParser pageParser;

    public CorrectablePage(File dest, PageParser pageParser) throws Exception {
        this.dest = dest;
        this.pageParser = pageParser;
        for (Paragraph p : pageParser.parse()) {
            for (Line line : p) {
                this.add(line);
            }
        }
    }

    @Override
    public int getNumberOfLines() {
        return this.size();
    }

    @Override
    public String getLineAt(int i) {
        StringBuilder builder = new StringBuilder();
        for (Char c : this.get(i)) {
            builder.appendCodePoint(c.getChar());
        }
        return builder.toString();
    }

    @Override
    public void substitute(int i, int j, int c) {
        this.get(i).substitute(j, c);
    }

    @Override
    public void delete(int i, int j) {
        this.get(i).delete(j);
    }

    @Override
    public void insert(int i, int j, int c) {
        this.get(i).insert(j, c);
    }

    @Override
    public void finishCorrection() throws Exception {
        pageParser.write(dest);
    }

}
