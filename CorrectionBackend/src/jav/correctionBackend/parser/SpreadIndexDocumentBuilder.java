/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import jav.correctionBackend.SpreadIndexDocument;
import jav.correctionBackend.Token;
import jav.correctionBackend.TokenImageInfoBox;
import jav.correctionBackend.util.Tokenization;
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
    public void append(Page page) {
        this.ocrfile = page.getOcrFile();
        this.imagefile = page.getImageFile();
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
                addToDocument(TokenBuilder.newNewlineToken(getEOLBoundingBox(l)));
            }
            insertCurrentToken();
            addToDocument(TokenBuilder.newNewlineToken(getEOLBoundingBox(currentLine)));
        }
    }

    @Override
    public SpreadIndexDocument build() {
        return document;
    }

    public void append(Char c) {
        final int currentCodepoint = c.getChar();

        if (previousCodepoint == 0) {
            if (!isWhitespace(currentCodepoint)) {
                tokenBuilder.appendCodepoint(currentCodepoint);
                tokenBuilder.appendBoundingBox(c.getBoundingBox());
                tokenBuilder.appendSuspicious(c.isSuspicious());
            }
        } else if (isWordCharacter(previousCodepoint)) {
            if (isWordCharacter(currentCodepoint)) { // AA
                tokenBuilder.appendCodepoint(currentCodepoint);
                tokenBuilder.appendBoundingBox(c.getBoundingBox());
                tokenBuilder.appendSuspicious(c.isSuspicious());
            } else if (isWhitespace(currentCodepoint)) { // A_
                insertCurrentToken();
                addToDocument(TokenBuilder.newWhitespaceToken(c.getBoundingBox()));
            } else { // A.
                insertCurrentToken();
                tokenBuilder.appendCodepoint(currentCodepoint);
                tokenBuilder.appendBoundingBox(c.getBoundingBox());
                tokenBuilder.appendSuspicious(c.isSuspicious());
            }
        } else if (isWordCharacter(currentCodepoint)) { // .A
            insertCurrentToken();
            tokenBuilder.appendCodepoint(currentCodepoint);
            tokenBuilder.appendBoundingBox(c.getBoundingBox());
            tokenBuilder.appendSuspicious(c.isSuspicious());
        } else if (isWhitespace(currentCodepoint)) { // ._
            insertCurrentToken();
            addToDocument(TokenBuilder.newWhitespaceToken(c.getBoundingBox()));
        } else { // ..
            tokenBuilder.appendCodepoint(currentCodepoint);
            tokenBuilder.appendBoundingBox(c.getBoundingBox());
            tokenBuilder.appendSuspicious(c.isSuspicious());
        }
        previousCodepoint = currentCodepoint;
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

    private void adjustTokenImageInfoBox(TokenImageInfoBox tiib) {
        if (tiib != null && currentLine != null && currentLine.getBoundingBox() != null) {
            tiib.setImageFileName(imagefile.getName());
            BoundingBox bb = currentLine.getBoundingBox();
            tiib.setCoordinateTop(
                    Math.min(bb.getTop(), tiib.getCoordinateTop())
            );
            tiib.setCoordinateBottom(
                    Math.max(bb.getBottom(), tiib.getCoordinateBottom())
            );
        }
    }

    private static BoundingBox getEOLBoundingBox(Line l) {
        if (l != null && l.getBoundingBox() != null) {
            return new BoundingBox(
                    l.getBoundingBox().getRight(),
                    l.getBoundingBox().getTop(),
                    l.getBoundingBox().getRight(),
                    l.getBoundingBox().getBottom()
            );
        }
        return new BoundingBox();
    }

    private Token adjust(Token token) {
        if (token != null) {
            adjustTokenImageInfoBox(token.getTokenImageInfoBox());
            token.setIndexInDocument(++tokenIndex);
            token.setPageIndex(pageIndex);
        }
        return token;
    }

    private static boolean isWordCharacter(int codepoint) {
        return Tokenization.isWordCharacter(codepoint);
    }

    private static boolean isWhitespace(int codepoint) {
        return Tokenization.isWhitespaceCharacter(codepoint);
    }
}
