/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

import jav.correctionBackend.SpreadIndexDocument;

/**
 *
 * @author finkf
 */
public class SpreadIndexDokumentBuilder implements DocumentBuilder {

    @Override
    public void append(Page page) {
        for (Paragraph paragraph : page) {
            append(paragraph);
        }
    }

    @Override
    public void append(Paragraph paragraph) {
        for (Line line : paragraph) {
            append(line);
        }
    }

    @Override
    public void append(Line line) {
        for (Char c : line) {
            append(c);
        }
    }

    @Override
    public void append(Char c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SpreadIndexDocument build() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
