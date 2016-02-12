/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import jav.correctionBackend.SpecialSequenceType;
import jav.correctionBackend.util.Tokenization;

/**
 * This class is a Character that is linked to a Token of the correction
 * backend. Most operations of the Character class are not implemented. It is an
 * error to call them. The SpreadIndexDocumentLine class should implement those
 * methods.
 *
 * @author flo
 */
public class DocumentChar extends AbstractBaseChar {

    private int codepoint;
    private DocumentToken token;
    private final BoundingBox bb;

    public DocumentChar(Line line, DocumentToken token, int codepoint, BoundingBox bb) {
        super(line);
        this.codepoint = codepoint;
        this.token = token;
        this.bb = bb;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return bb;
    }

    @Override
    public int getChar() {
        return codepoint;
    }

    @Override
    public boolean isSuspicious() {
        return token.getToken().isSuspicious();
    }

    @Override
    public void delete() {
        token.delete(this);
    }

    @Override
    public void substitute(int codepoint) {
        if (token.getToken().getSpecialSeq() == SpecialSequenceType.SPACE) {
            if (!Tokenization.isWhitespaceCharacter(codepoint)) {
                final DocumentChar prev = (DocumentChar) getPrev();
                if (prev != null) {
                    prev.token.append(prev, this);
                    token.delete(this);
                    this.token = prev.token;
                }
            }
        } else {
            this.codepoint = codepoint;
            token.update();
        }
    }

    @Override
    public DocumentChar prepend(int c) {
        DocumentChar newChar = new DocumentChar(getLine(), token, codepoint, bb);
        token.prepend(this, newChar);
        return newChar;
    }

    @Override
    public DocumentChar append(int c) {
        DocumentChar newChar = new DocumentChar(getLine(), token, codepoint, bb);
        token.append(this, newChar);
        return newChar;
    }

}
