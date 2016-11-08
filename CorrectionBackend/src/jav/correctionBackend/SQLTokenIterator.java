/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author finkf
 */
class SQLTokenIterator extends SQLIterator<Token> {

    private String baseImagePath;
    private ArrayList<Token> tokens;

    private static ArrayList<Token> getIterator(ResultSet rs) throws SQLException {
        ArrayList<Token> tokens = new ArrayList<>();
        while (rs.next()) {
            Token token = new Token(rs.getString(4));
            token.setId(rs.getInt(1));
            token.setIndexInDocument(rs.getInt(2));
            token.setOrigID(rs.getInt(3));
            token.setWCOR(rs.getString(5));
            token.setIsSuspicious(rs.getBoolean(15));
            token.setIsCorrected(rs.getBoolean(7));
            token.setIsNormal(rs.getBoolean(6));
            token.setNumberOfCandidates(rs.getInt(8));
            token.setPageIndex(rs.getInt(16));
            token.setSpecialSeq(SpecialSequenceType.valueOf(rs.getString(13)));
            token.setTopSuggestion(rs.getString(17));
            token.setTopCandDLev(rs.getInt(18));

            if (rs.getString(14).equals("")) {
                token.setTokenImageInfoBox(null);
            } else {
                TokenImageInfoBox tiib = new TokenImageInfoBox();
                //tiib.setImageFileName(this.baseImagePath + File.separator + rs.getString(14));
                tiib.setImageFileName(rs.getString(14));
                tiib.setCoordinateBottom(rs.getInt(12));
                tiib.setCoordinateTop(rs.getInt(11));
                tiib.setCoordinateLeft(rs.getInt(9));
                tiib.setCoordinateRight(rs.getInt(10));
                token.setTokenImageInfoBox(tiib);
            }
            tokens.add(token);
        }
        rs.close();
        return tokens;
    }

    private static ArrayList<Token> getIterator(Connection c) throws SQLException {
        Statement s = c.createStatement();
        ResultSet res = s.executeQuery("SELECT * FROM TOKEN ORDER BY indexInDocument ASC");
        ArrayList<Token> tokens = getIterator(res);
        return tokens;
    }

    private static ArrayList<Token> getIterator(Connection c, String i) throws SQLException {
        Statement s = c.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        ResultSet res = s.executeQuery("SELECT * FROM TOKEN WHERE indexInDocument >= 0 ORDER BY indexInDocument ASC");
        ArrayList<Token> tokens = getIterator(res);
        return tokens;
    }

    private static ArrayList<Token> getIterator(Connection c, Page p, String i) throws SQLException {
        if (p == null) {
            return null;
        }

        Statement s = c.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        ResultSet res = s.executeQuery("SELECT * FROM TOKEN WHERE indexInDocument >=" + p.getStartIndex() + " AND indexInDocument <=" + p.getEndIndex() + " ORDER BY indexInDocument ASC");

        if (p.getStartIndex() == p.getEndIndex()) {
            return null;
        } else {
            ArrayList<Token> tokens = getIterator(res);
            return tokens;
        }
    }

    private static ArrayList<Token> getIterator(Connection c, PreparedStatement p) throws SQLException {
        ResultSet res = p.executeQuery();
        ArrayList<Token> tokens = getIterator(res);
        return tokens;
    }

    public SQLTokenIterator(Connection c) throws SQLException {
        this(getIterator(c), "");
    }

    public SQLTokenIterator(Connection c, String i) throws SQLException {
        this(getIterator(c, i), i);
    }

    public SQLTokenIterator(Connection c, Page p, String i) throws SQLException {
        this(getIterator(c, p, i), i);
    }

    public SQLTokenIterator(Connection c, PreparedStatement p) throws SQLException {
        this(getIterator(c, p), "");
    }

    private SQLTokenIterator(ArrayList<Token> ts, String i) {
        tokens = ts;
        baseImagePath = i;
        if (tokens != null) {
            setIterator(tokens.iterator());
        }
    }

    @Override
    public void reset() {
        setIterator(tokens.iterator());
    }
}
