/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

import jav.correctionBackend.SpecialSequenceType;
import jav.correctionBackend.Token;
import jav.correctionBackend.TokenImageInfoBox;
import java.util.regex.Pattern;

/**
 *
 * @author finkf
 */
public class TokenBuilder {

    public static final Pattern ALPHANUM = Pattern.compile(
            "[\\pL\\pM\\p{Nd}\\p{Nl}\\p{Pc}[\\p{InEnclosedAlphanumerics}&&\\p{So}]]+"
    );

    private final StringBuilder token;
    private boolean suspicious;
    private BoundingBox bb;

    public TokenBuilder() {
        this.token = new StringBuilder();
        this.suspicious = false;
        this.bb = null;
    }

    public String getToken() {
        return token.toString();
    }

    public void reset() {
        token.setLength(0);
        suspicious = false;
        bb = null;
    }

    public void appendCodepoint(int codepoint) {
        token.appendCodePoint(codepoint);
    }

    public void appendSuspicious(boolean s) {
        suspicious |= s;
    }

    public void appendBoundingBox(BoundingBox bb) {
        if (this.bb != null) {
            this.bb.combineWith(bb);
        } else {
            this.bb = bb;
        }
    }

    public Token build() {
        if (token.length() == 0) {
            return null;
        }
        Token t = newToken(token.toString());
        t.setTokenImageInfoBox(newTokenImageInfoBox());
        t.setIsSuspicious(suspicious);
        return t;
    }

    public static Token newNewlineToken() {
        return newSpecialSequenceToken(SpecialSequenceType.NEWLINE);
    }

    public static Token newWhitespaceToken() {
        return newSpecialSequenceToken(SpecialSequenceType.SPACE);
    }

    private static Token newSpecialSequenceToken(SpecialSequenceType type) {
        Token t = null;
        switch (type) {
            case NEWLINE:
                t = newToken("\n");
                t.setSpecialSeq(type);
                t.setTokenImageInfoBox(new TokenImageInfoBox(-1, -1, -1, -1));
                break;
            case SPACE:
                t = newToken(" ");
                t.setSpecialSeq(type);
                t.setTokenImageInfoBox(new TokenImageInfoBox(-1, -1, -1, -1));
                break;
            default:
                assert (false);
                break;
        }
        return t;
    }

    private static Token newToken(String str) {
        Token t = new Token(str);
        t.setIsCorrected(false);
        t.setNumberOfCandidates(0);
        boolean normal = ALPHANUM.matcher(str).matches();
        t.setIsNormal(normal);
        t.setSpecialSeq(normal ? SpecialSequenceType.NORMAL : SpecialSequenceType.PUNCTUATION);
        return t;
    }

    private TokenImageInfoBox newTokenImageInfoBox() {
        TokenImageInfoBox tiib = new TokenImageInfoBox(
                bb.getLeft(),
                bb.getTop(),
                bb.getRight(),
                bb.getBottom()
        );
        return tiib;
    }

}
