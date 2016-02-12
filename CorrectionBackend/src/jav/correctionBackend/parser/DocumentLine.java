/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import jav.correctionBackend.Document;
import jav.correctionBackend.Token;
import jav.correctionBackend.util.Tokenization;
import jav.logging.log4j.Log;
import java.sql.SQLException;
import java.util.HashSet;

/**
 *
 * @author flo
 */
public class DocumentLine extends Line {

    private final Document document;

    public DocumentLine(Document document) {
        this.document = document;
    }

    public void add(Token token) {
        DocumentToken dtoken = new DocumentToken(this, token, document);
        this.addAll(dtoken);
    }

    @Override
    public void finishCorrection() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.size();) {
            final int e = findEndOf(i);
            builder.setLength(0);
            for (int j = i; j < e; ++j) {
                builder.appendCodePoint(get(j).getChar());
            }
            //Log.debug(this, "merging token '%s'", builder.toString());
            merge(i, e);
            i = e;
        }
    }

    private void merge(int b, int e) {
        if (isSpaceAt(b) || b >= e) { // dont touch those!
            return;
        }

        final int mainId = getIdAt(b);
        HashSet<Integer> ids = new HashSet<>();
        ids.add(mainId);
        int n = 0;
        for (++b; b < e; ++b) {
            final int tmpId = getIdAt(b);
            if (!ids.contains(tmpId)) {
                ids.add(tmpId);
                ++n;
            }
        }
        try {
            if (n > 0) {
                document.mergeRightward(mainId, n);
            }
        } catch (SQLException ex) {
            Log.error(this, ex);
            throw new RuntimeException(ex);
        }
    }

    private int getIdAt(int i) {
        return getTokenAt(i).getID();
    }

    private Token getTokenAt(int i) {
        return getDocumentCharAt(i).getDocumentToken().getToken();
    }

    private int findEndOf(int i) {
        if (isSpaceAt(i)) {
            return findNonSpace(i + 1);
        } else if (isWordAt(i)) {
            return findNonWord(i + 1);
        } else {
            return findNonNonWord(i + 1);
        }
    }

    private int findNonSpace(int i) {
        while (i < this.size() && isSpaceAt(i)) {
            ++i;
        }
        return i;
    }

    private int findNonWord(int i) {
        while (i < this.size() && isWordAt(i)) {
            ++i;
        }
        return i;
    }

    private int findNonNonWord(int i) {
        while (i < this.size() && isNonWordAt(i)) {
            ++i;
        }
        return i;
    }

    private boolean isSpaceAt(int idx) {
        return Tokenization.isWhitespaceCharacter(get(idx).getChar());
    }

    private boolean isWordAt(int idx) {
        return Tokenization.isWordCharacter(get(idx).getChar());
    }

    private boolean isNonWordAt(int idx) {
        return Tokenization.isNonWordCharacter(get(idx).getChar());
    }

    private DocumentChar getDocumentCharAt(int i) {
        return (DocumentChar) get(i);
    }
}
