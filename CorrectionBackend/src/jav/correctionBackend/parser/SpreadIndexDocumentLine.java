/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import jav.correctionBackend.Document;
import jav.correctionBackend.Token;

/**
 *
 * @author flo
 */
public class SpreadIndexDocumentLine extends Line {

    private final Document document;

    public SpreadIndexDocumentLine(Document document) {
        this.document = document;
    }

    public void add(Token token) {
        String str;
        if (token.isCorrected()) {
            str = token.getWCOR();
        } else {
            str = token.getWOCR();
        }
        final int n = str.length();
        for (int offset = 0; offset < n;) {
            final int codepoint = str.codePointAt(offset);
            this.add(new SpreadIndexDocumentChar(document, codepoint));
            offset += Character.charCount(codepoint);
        }
    }
}
