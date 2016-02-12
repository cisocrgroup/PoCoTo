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
public class DocumentLine extends Line {

    private final Document document;

    public DocumentLine(Document document) {
        this.document = document;
    }

    public void add(Token token) {
        DocumentToken dtoken = new DocumentToken(this, token, document);
        this.addAll(dtoken);
    }

//    @Override
//    public void substitute(int idx, int codepoint) {
//        final DocumentChar c = doGet(idx);
//        c.substitute(codepoint);
//    }
//
//    @Override
//    public void insert(int idx, int codepoint) {
//        if (this.isEmpty()) {
//            throw new RuntimeException("Insert into empty line: not supported");
//        }
//        Token token;
//        if (idx >= this.size()) {
//            token = doGet(this.size() - 1).getToken();
//        } else {
//            token = doGet(idx).getToken();
//        }
//        this.add(idx, new DocumentChar(this, token, codepoint));
//    }
//
//    @Override
//    public void delete(int idx) {
//        final Token token = doGet(idx).getToken();
//        remove(idx);
//        String correction = gatherAll(token.getID());
//        if (correction.isEmpty()) {
//            delete(token);
//        }
//    }
//
//    @Override
//    public void finishCorrection() {
//        if (this.isEmpty()) {
//            return;
//        }
//        tokenize();
//        HashMap<Integer, Token> allTokenIds = new HashMap<>();
//        for (int i = 0; i < this.size(); ++i) {
//            final Token token = doGet(i).getToken();
//            allTokenIds.put(token.getID(), token);
//        }
//        for (int id : allTokenIds.keySet()) {
//            correct(allTokenIds.get(id));
//        }
//    }
//
//    private DocumentChar doGet(int idx) {
//        return (DocumentChar) this.get(idx);
//    }
//
//    private void correct(Token token) {
//        String correction = gatherAll(token.getID());
//        try {
//            if (!token.getWOCR().equals(correction)) {
//                Log.debug(this, "correct Token(%d, `%s`) with `%s`", token.getID(), token.getWOCR(), correction);
//                token.setWCOR(correction);
//                document.correctTokenByString(token.getID(), correction);
//            }
//        } catch (SQLException e) {
//            Log.error(this, e);
//            throw new RuntimeException(e);
//        }
//    }
//
//    private String gatherAll(int id) {
//        StringBuilder builder = new StringBuilder();
//        for (int i = 0; i < this.size(); ++i) {
//            final DocumentChar cc = doGet(i);
//            if (cc.getToken().getID() == id && cc.getChar() != 0) {
//                builder.appendCodePoint(cc.getChar());
//            }
//        }
//        return builder.toString();
//    }
//
//    private void correct(Slice slice) {
//        if (slice.b == slice.e) {
//            return;
//        }
//        final Token token = doGet(slice.b).getToken();
//        for (int i = slice.b + 1; i < slice.e; ++i) {
//            doGet(i).setToken(token);
//        }
//        if (slice.isSpace()) {
//            token.setSpecialSeq(SpecialSequenceType.SPACE);
//            token.setWCOR(" ");
//            token.setIsCorrected(true);
//        } else if (slice.isWord()) {
//            token.setSpecialSeq(SpecialSequenceType.NORMAL);
//            token.setIsCorrected(true);
//            token.setIsNormal(true);
//            token.setWCOR(slice.toString());
//        } else {
//            assert (slice.isNonWord());
//            token.setIsCorrected(true);
//            token.setIsNormal(false);
//            token.setSpecialSeq(SpecialSequenceType.PUNCTUATION);
//        }
//    }
//
//    private void delete(Token token) {
//        Log.debug(this, "deleting Token(%d, `%s`)", token.getID(), token.getWOCR());
//        try {
//            document.deleteToken(token.getID());
//        } catch (SQLException e) {
//            Log.error(this, e);
//            throw new RuntimeException(e);
//        }
//    }
//
//    private void tokenize() {
//        for (Slice s = nextSlice(null); s.b != s.e; s = nextSlice(s)) {
//            Log.debug(this, "slice %d %d", s.b, s.e);
//            Log.debug(this, "Token: `%s`", s.toString());
//            correct(s);
//        }
//    }
//
//    private Slice nextSlice(Slice slice) {
//        if (slice == null) {
//            slice = new Slice();
//        } else {
//            slice.b = slice.e;
//        }
//        if (slice.b < this.size()) {
//            if (isSpace(slice.b)) {
//                return nextSpaceSlice(slice);
//            } else if (isWord(slice.b)) {
//                return nextWordSlice(slice);
//            } else {
//                assert (isNonWord(slice.b));
//                return nextNonWordSlice(slice);
//            }
//        }
//        return slice;
//    }
//
//    private Slice nextSpaceSlice(Slice slice) {
//        while (slice.e < this.size() && isSpace(slice.e)) {
//            slice.e++;
//        }
//        return slice;
//    }
//
//    private Slice nextWordSlice(Slice slice) {
//        while (slice.e < this.size() && isWord(slice.e)) {
//            slice.e++;
//        }
//        return slice;
//    }
//
//    private Slice nextNonWordSlice(Slice slice) {
//        while (slice.e < this.size() && isNonWord(slice.e)) {
//            slice.e++;
//        }
//        return slice;
//    }
//
//    private class Slice {
//
//        public int b, e;
//
//        @Override
//        public String toString() {
//            StringBuilder builder = new StringBuilder();
//            for (int i = this.b; i < this.e; ++i) {
//                builder.appendCodePoint(get(i).getChar());
//            }
//            return builder.toString();
//        }
//
//        boolean isSpace() {
//            for (int i = b; i < e; ++i) {
//                if (!Tokenization.isWhitespaceCharacter(get(i).getChar())) {
//                    return false;
//                }
//            }
//            return true;
//        }
//
//        boolean isWord() {
//            for (int i = b; i < e; ++i) {
//                if (!Tokenization.isWordCharacter(get(i).getChar())) {
//                    return false;
//                }
//            }
//            return true;
//        }
//
//        boolean isNonWord() {
//            for (int i = b; i < e; ++i) {
//                if (!Tokenization.isNonWordCharacter(get(i).getChar())) {
//                    return false;
//                }
//            }
//            return true;
//        }
//    }
//
//    private boolean isSpace(int idx) {
//        return Tokenization.isWhitespaceCharacter(get(idx).getChar());
//    }
//
//    private boolean isWord(int idx) {
//        return Tokenization.isWordCharacter(get(idx).getChar());
//    }
//
//    private boolean isNonWord(int idx) {
//        return Tokenization.isNonWordCharacter(get(idx).getChar());
//    }
}
