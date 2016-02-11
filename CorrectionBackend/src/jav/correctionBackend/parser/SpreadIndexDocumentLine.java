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
import java.util.HashMap;

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
            this.add(new SpreadIndexDocumentChar(this, token, codepoint));
            offset += Character.charCount(codepoint);
        }
    }

    @Override
    public void substitute(int idx, int codepoint) {
        final SpreadIndexDocumentChar c = doGet(idx);
        c.substitute(codepoint);
    }

    @Override
    public void insert(int idx, int codepoint) {
        if (this.isEmpty()) {
            throw new RuntimeException("Insert into empty line: not supported");
        }
        Token token;
        if (idx >= this.size()) {
            token = doGet(this.size() - 1).getToken();
        } else {
            token = doGet(idx).getToken();
        }
        this.add(idx, new SpreadIndexDocumentChar(this, token, codepoint));
    }

    @Override
    public void delete(int idx) {
        get(idx).substitute(0); // 0 means that this token will be deleted
    }

    @Override
    public void finishCorrection() {
        if (this.isEmpty()) {
            return;
        }

        HashMap<Integer, Token> allTokenIds = new HashMap<>();
        for (int i = 0; i < this.size(); ++i) {
            final Token token = doGet(i).getToken();
            allTokenIds.put(token.getID(), token);
        }
        for (int id : allTokenIds.keySet()) {
            correct(allTokenIds.get(id));
        }
    }

    private SpreadIndexDocumentChar doGet(int idx) {
        return (SpreadIndexDocumentChar) this.get(idx);
    }

    private void correct(Token token) {
        String correction = gatherAll(token.getID());

        try {
            if (correction.isEmpty()) {
                Log.debug(this, "deleting Token(%d, `%s`)", token.getID(), token.getWOCR());
                document.deleteToken(token.getID(), token.getID());
            } else if (!token.getWOCR().equals(correction)) {
                Log.debug(this, "correct Token(%d, `%s`) with `%s`", token.getID(), token.getWOCR(), correction);
                token.setWCOR(correction);
                document.correctTokenByString(token.getID(), correction);
            }
        } catch (SQLException e) {
            Log.error(this, e);
            throw new RuntimeException(e);
        }
    }

    private String gatherAll(int id) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.size(); ++i) {
            final SpreadIndexDocumentChar cc = doGet(i);
            if (cc.getToken().getID() == id && cc.getChar() != 0) {
                builder.appendCodePoint(cc.getChar());
            }
        }
        return builder.toString();
    }
}
