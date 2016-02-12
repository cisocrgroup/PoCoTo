/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import jav.correctionBackend.Document;
import jav.correctionBackend.Token;
import jav.logging.log4j.Log;
import java.sql.SQLException;

/**
 *
 * @author flo
 */
public class DocumentToken extends AbstractToken<DocumentChar> {

    private final Token token;
    private final Document document;

    public DocumentToken(Line line, Token token, Document document) {
        this.token = token;
        this.document = document;
        parse(line);
    }

    public Token getToken() {
        return token;
    }

    public void appendBoundingBox(BoundingBox bb) {
        BoundingBox tmp = new BoundingBox(token.getTokenImageInfoBox());
        token.getTokenImageInfoBox().setCoordinates(tmp.combineWith(bb));
    }

    @Override
    public void update() {
        String corr = toString();
        try {
            //Log.debug(this, "correct token(%d, '%s') with '%s'", token.getID(), token.getWOCR(), corr);
            token.setIsCorrected(true);
            token.setWCOR(corr);
            document.correctTokenByString(token.getID(), corr);
        } catch (SQLException e) {
            Log.error(this, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeFromTree() {
        //Log.debug(this, "removing token(%d, '%s')", token.getID(), token.getWOCR());
        try {
            document.deleteToken(token.getID());
        } catch (SQLException e) {
            Log.error(this, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Char c : this) {
            builder.appendCodePoint(c.getChar());
        }
        return builder.toString();
    }

    private void parse(Line line) {
        String str;
        if (token.isCorrected()) {
            str = token.getWCOR();
        } else {
            str = token.getWOCR();
        }
        final int n = str.length();
        final int nn = str.codePointCount(0, str.length());
        final BoundingBox[] splits = new BoundingBox(token.getTokenImageInfoBox()).getVerticalSplits(nn);
        for (int offset = 0, i = 0; offset < n; ++i) {
            final int codepoint = str.codePointAt(offset);
            this.add(new DocumentChar(line, this, codepoint, splits[i]));
            offset += Character.charCount(codepoint);
        }
    }

}
