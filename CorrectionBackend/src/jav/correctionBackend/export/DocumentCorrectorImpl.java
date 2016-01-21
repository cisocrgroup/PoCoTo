/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

import jav.logging.log4j.Log;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author finkf
 */
public class DocumentCorrectorImpl extends DocumentCorrector {

    private final ArrayList<Line> lines;
    private final PageParser pageParser;
    private final File input;

    public DocumentCorrectorImpl(File input, PageParser pageParser)
            throws IOException, Exception {
        this.pageParser = pageParser;
        lines = new ArrayList<>();
        this.input = input;
        parseLines();
    }

    @Override
    public int getNumberOfLines() {
        return lines.size();
    }

    @Override
    public String getLineAt(int i) {
        StringBuilder builder = new StringBuilder();
        for (int j = 0; j < lines.get(i).size(); ++j) {
            builder.appendCodePoint(lines.get(i).get(j).getChar());
        }
        return builder.toString();
    }

    @Override
    public void substitute(int i, int j, int c) {
        Log.debug(this, "substitute(%d, %d, %s)", i, j, new String(Character.toChars(c)));
        lines.get(i).substitute(j, c);
    }

    @Override
    public void delete(int i, int j) {
        Log.debug(this, "delete(%d, %d)", i, j);
        lines.get(i).delete(j);
    }

    @Override
    public void insert(int i, int j, int c) {
        Log.debug(this, "insert(%d, %d, %s)", i, j, new String(Character.toChars(c)));
        lines.get(i).insert(j, c);
    }

    @Override
    public void write(File output) throws IOException {
        try {
            pageParser.write(output);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private void parseLines() throws IOException, Exception {
        Page page = pageParser.parse(input);
        for (Paragraph p : page) {
            for (Line l : p) {
                lines.add(l);
            }
            lines.add(new Line()); // append an empty new line after paragraphs
        }
    }
}
