/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend;

import java.sql.Connection;
import java.util.ArrayList;

/**
 *
 * @author finkf
 */
public class MockDocument extends Document {
    ArrayList<Token> tokens;
    public MockDocument() {
        super(null);
        tokens = new ArrayList<>();
    }
    public final Token findFirstToken(String what) {
        for (Token token: tokens) {
            if (what.equals(token.getWOCR()))
                return token;
        }
        return null;
    }
    public final Token findLastToken(String what) {
        for (int i = tokens.size(); i > 0; --i) {
            if (what.equals(tokens.get(i - 1).getWOCR()))
                return tokens.get(i - 1);
        }
        return null;
    }
    @Override
    protected final int addToken(Token token) {
        tokens.add(token);
        return tokens.size();
    }
    @Override
    protected final int addToken(Token token, Connection conn) {
        return addToken(token);
    }
    @Override
    public final ArrayList<Integer> mergeRightward(int a, int b) {
        throw new RuntimeException("mergeRightward(int, int): not implemented");
    }
    @Override
    public final ArrayList<Integer> splitToken(int a, String b) {
        throw new RuntimeException("splitToken(int, String): not implemented");
    }
    @Override
    public final ArrayList<Integer> deleteToken(int a, int b) {
        throw new RuntimeException("deleteToken(int, int): not implemented");
    }
    @Override
    public final void loadNumberOfTokensFromDB() {
        throw new RuntimeException("loadNumberOfTokensFromDB(): not implemented");
    }
    @Override
    public final void loadNumberOfPagesFromDB() {
        throw new RuntimeException("loadNumberOfPagesFromDB(): not implemented");
    }
    @Override
    public final int getNumberOfPages() {
        return 0;
    }
    @Override
    public final int getNumberOfTokens() {
        return tokens.size();
    }
}
