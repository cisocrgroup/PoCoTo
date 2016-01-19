/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

import jav.correctionBackend.SpreadIndexDocument;
import jav.correctionBackend.Token;
import jav.correctionBackend.TokenImageInfoBox;
import java.io.File;
import org.h2.jdbcx.JdbcConnectionPool;

/**
 *
 * @author finkf
 */
public class SpreadIndexDocumentBuilder implements DocumentBuilder {

    private final JdbcConnectionPool connection;
    private SpreadIndexDocument document;
    private File imagefile, ocrfile; // ocrfile is for later use
    private Line currentLine;
    private int pageIndex, tokenIndex;
    private TokenBuilder tokenBuilder;
    private int previousCodepoint;

    public SpreadIndexDocumentBuilder(JdbcConnectionPool connection) {
        this.connection = connection;
    }

    @Override
    public void init() {
        document = new SpreadIndexDocument(connection);
        imagefile = ocrfile = null;
        currentLine = null;
        pageIndex = tokenIndex = -1; // indexing starts at 0 (++idx)
        tokenBuilder = new TokenBuilder();
    }

    @Override
    public void append(Page page, File imagefile, File ocrfile) {
        this.ocrfile = ocrfile;
        this.imagefile = imagefile;
        ++pageIndex;
        for (Paragraph p : page) {
            for (Line l : p) {
                tokenBuilder.reset();
                previousCodepoint = 0;
                currentLine = l;
                for (Char c : l) {
                    append(c);
                }
                insertCurrentToken();
                addToDocument(TokenBuilder.newNewlineToken());
            }
            insertCurrentToken();
            addToDocument(TokenBuilder.newNewlineToken());
        }
    }

    @Override
    public SpreadIndexDocument build() {
        return document;
    }

    public void append(Char c) {
        String str = c.getChar();

        for (int i = 0; i < str.length();) {
            final int currentCodepoint = str.codePointAt(i);

            if (previousCodepoint == 0) {
                if (!isWhitespace(currentCodepoint)) {
                    tokenBuilder.appendCodepoint(currentCodepoint);
                    tokenBuilder.appendBoundingBox(c.getBoundingBox());
                    tokenBuilder.appendSuspicious(c.isSuspicious());
                }
            } else if (isAlphanumeric(previousCodepoint)) {
                if (isAlphanumeric(currentCodepoint)) { // AA
                    tokenBuilder.appendCodepoint(currentCodepoint);
                    tokenBuilder.appendBoundingBox(c.getBoundingBox());
                    tokenBuilder.appendSuspicious(c.isSuspicious());
                } else if (isWhitespace(currentCodepoint)) { // A_
                    insertCurrentToken();
                    addToDocument(TokenBuilder.newWhitespaceToken());
                } else { // A.
                    insertCurrentToken();
                    tokenBuilder.appendCodepoint(currentCodepoint);
                    tokenBuilder.appendBoundingBox(c.getBoundingBox());
                    tokenBuilder.appendSuspicious(c.isSuspicious());
                }
            } else if (isAlphanumeric(currentCodepoint)) { // .A
                insertCurrentToken();
                tokenBuilder.appendCodepoint(currentCodepoint);
                tokenBuilder.appendBoundingBox(c.getBoundingBox());
                tokenBuilder.appendSuspicious(c.isSuspicious());
            } else if (isWhitespace(currentCodepoint)) { // ._
                insertCurrentToken();
                addToDocument(TokenBuilder.newWhitespaceToken());
            } else { // ..
                tokenBuilder.appendCodepoint(currentCodepoint);
                tokenBuilder.appendBoundingBox(c.getBoundingBox());
                tokenBuilder.appendSuspicious(c.isSuspicious());
            }
            i += Character.charCount(currentCodepoint);
            previousCodepoint = currentCodepoint;
        }
    }

    private void insertCurrentToken() {
        Token t = tokenBuilder.build();
        if (t != null) {
            addToDocument(t);
        }
        tokenBuilder.reset();
    }

    private void addToDocument(Token t) {
        if (t != null) {
            document.addToken(adjust(t));
        }
    }

    private void adjust(TokenImageInfoBox tiib) {
        if (tiib != null) {
            tiib.setImageFileName(imagefile.getName());
            BoundingBox bb = currentLine.getBoundingBox();
            if (bb != null) {
                tiib.setCoordinateTop(
                        Math.min(bb.getTop(), tiib.getCoordinateTop())
                );
                tiib.setCoordinateBottom(
                        Math.max(bb.getBottom(), tiib.getCoordinateBottom())
                );
            }
        }
    }

    private Token adjust(Token token) {
        if (token != null) {
            token.setIndexInDocument(++tokenIndex);
            token.setPageIndex(pageIndex);
            adjust(token.getTokenImageInfoBox());
        }
        return token;
    }

    private boolean isAlphanumeric(int codepoint) {
        return Character.isAlphabetic(codepoint)
                || Character.isDigit(codepoint);
    }

    private boolean isWhitespace(int codepoint) {
        return Character.isWhitespace(codepoint);
    }

}
