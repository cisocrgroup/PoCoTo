package jav.correctionBackend;

import jav.logging.log4j.Log;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import org.apache.commons.lang3.StringUtils;
import org.h2.jdbcx.JdbcConnectionPool;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressRunnable;
import org.netbeans.api.progress.ProgressUtils;

/**
 * Copyright (c) 2012, IMPACT working group at the Centrum für Informations- und
 * Sprachverarbeitung, University of Munich. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * This file is part of the ocr-postcorrection tool developed by the IMPACT
 * working group at the Centrum für Informations- und Sprachverarbeitung,
 * University of Munich. For further information and contacts visit
 * http://ocr.cis.uni-muenchen.de/
 *
 * @author thorsten (thorsten.vobl@googlemail.com)
 */
public class SpreadIndexDocument extends Document {

    private int myIndex = 0;
    private static final int NORMINDEXPLUS = 11;
    private static final int PUNCTINDEXPLUS = 3;

    public SpreadIndexDocument(JdbcConnectionPool jc) {
        super(jc);
    }

    @Override
    protected int addToken(Token t, Connection conn) {
        try {
            try (PreparedStatement prep = conn.prepareStatement("INSERT INTO TOKEN VALUES( null,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? )")) {
                prep.setInt(1, myIndex);
                prep.setInt(2, t.getOrigID());
                prep.setString(3, t.getWOCR());
                prep.setString(4, t.getWCOR());
                prep.setBoolean(5, t.isNormal());
                prep.setBoolean(6, t.isCorrected());
                prep.setInt(7, t.getNumberOfCandidates());

                TokenImageInfoBox tiib = t.getTokenImageInfoBox();

                if (tiib != null) {
                    prep.setInt(8, tiib.getCoordinateLeft());
                    prep.setInt(9, tiib.getCoordinateRight());
                    prep.setInt(10, tiib.getCoordinateTop());
                    prep.setInt(11, tiib.getCoordinateBottom());
                    prep.setString(13, tiib.getImageFileName());
                } else {
                    prep.setInt(8, -1);
                    prep.setInt(9, -1);
                    prep.setInt(10, -1);
                    prep.setInt(11, -1);
                    prep.setString(13, "");
                }

                prep.setString(12, t.getSpecialSeq().toString());
                prep.setBoolean(14, t.isSuspicious());
                prep.setInt(15, t.getPageIndex());
                prep.setString(16, t.getTopSuggestion());
                prep.setInt(17, t.getTopCandDLev());

                prep.addBatch();
                prep.executeBatch();
            }

            Statement psIdentity = conn.createStatement();
            ResultSet result = psIdentity.executeQuery("CALL SCOPE_IDENTITY()");
            result.next();
            int identity = result.getInt(1);
            result.close();
            psIdentity.close();
            conn.close();
            if (t.isNormal()) {
                myIndex += NORMINDEXPLUS;
            } else {
                myIndex += PUNCTINDEXPLUS;
            }
            return identity;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    protected int addToken(Token t, int index) {
        try (Connection conn = getConnection();
                PreparedStatement prep = conn.prepareStatement("INSERT INTO TOKEN VALUES( null,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? )")) {
            prep.setInt(1, index);
            prep.setInt(2, t.getOrigID());
            prep.setString(3, t.getWOCR());
            prep.setString(4, t.getWCOR());
            prep.setBoolean(5, t.isNormal());
            prep.setBoolean(6, t.isCorrected());
            prep.setInt(7, t.getNumberOfCandidates());

            TokenImageInfoBox tiib = t.getTokenImageInfoBox();

            if (tiib != null) {
                prep.setInt(8, tiib.getCoordinateLeft());
                prep.setInt(9, tiib.getCoordinateRight());
                prep.setInt(10, tiib.getCoordinateTop());
                prep.setInt(11, tiib.getCoordinateBottom());
                prep.setString(13, tiib.getImageFileName());
            } else {
                prep.setInt(8, -1);
                prep.setInt(9, -1);
                prep.setInt(10, -1);
                prep.setInt(11, -1);
                prep.setString(13, "");
            }

            prep.setString(12, t.getSpecialSeq().toString());
            prep.setBoolean(14, t.isSuspicious());
            prep.setInt(15, t.getPageIndex());
            prep.setString(16, t.getTopSuggestion());
            prep.setInt(17, t.getTopCandDLev());

            prep.addBatch();
            prep.executeBatch();
            try (Statement psIdentity = conn.createStatement();
                    ResultSet result = psIdentity.executeQuery("CALL SCOPE_IDENTITY()")) {
                result.next();
                return result.getInt(1);
            }
        } catch (SQLException ex) {
            Log.error(this, ex);
        }
        return 0;
    }

    @Override
    public int addToken(Token t) {
        try (Connection conn = getConnection()) {
            return this.addToken(t, conn);
        } catch (SQLException ex) {
            Log.error(this, "could not insert Token: %s", ex.getMessage());
            return 0;
        }
    }

    @Override
    protected void loadNumberOfTokensFromDB() {
        try (Connection conn = getConnection();
                Statement s = conn.createStatement();
                ResultSet rs = s.executeQuery("SELECT COUNT(indexInDocument) as numTokens FROM TOKEN WHERE indexInDocument<>-1")) {
            if (rs.next()) {
                this.numTokens = rs.getInt(1);
            }
        } catch (SQLException ex) {
            Log.error(this, "could not load number of tokens from db %s", ex.getMessage());
            this.numTokens = 0;
        }
    }

    @Override
    public ArrayList<Integer> deleteToken(int iDFrom, int iDTo)
            throws SQLException {
        //Log.info(this, "deleteToken(%d, %d)", iDFrom, iDTo);
        try (Connection conn = getConnection();
                PreparedStatement setIndex = conn.prepareStatement("UPDATE token SET indexInDocument=-1 WHERE tokenID=?");
                PreparedStatement undo_redo = conn.prepareStatement("INSERT INTO undoredo VALUES( ?,?,?,?,? )")) {
            ArrayList<Integer> retval = new ArrayList<>();

            Token from = this.getTokenByID(iDFrom);
            Token to = this.getTokenByID(iDTo);
            if (from == null || to == null) {
                return retval;
            }

            int indexFrom = from.getIndexInDocument();
            int indexTo = to.getIndexInDocument();

            if (indexTo < indexFrom) {
                Log.error(this, "cannot delete token indexTo < indexFrom");
                return retval;
//                throw new OCRCException("JAV.DOCUMENT.DELETETOKEN invalid range");
            }

            int thisPageIndex = from.getPageIndex();
            if (thisPageIndex != to.getPageIndex()) {
                Log.error(this, "thisPageIndex != to.getPageIndex()");
                return retval;
//                throw new OCRCException("JAV.DOCUMENT.DELETETOKEN: cannot erase across page borders");
            }

            conn.setAutoCommit(false);

            try {
                //reserve undo_redo_parts for the starting token
                Token temp = from;
                assert (temp != null); // at this point temp cannot be null
                while (temp != null && temp.getIndexInDocument() <= indexTo) {
                    retval.add(temp.getID());

                    setIndex.setInt(1, temp.getID());
                    setIndex.addBatch();

                    undo_redo.setInt(1, undo_redo_id);
                    undo_redo.setInt(2, undo_redo_part);
                    undo_redo.setString(3, "undo");
                    undo_redo.setString(4, MyEditType.DELETE.toString());
                    undo_redo.setString(5, "UPDATE token SET indexInDocument=" + temp.getIndexInDocument() + " WHERE tokenID=" + temp.getID());
                    undo_redo.addBatch();

                    undo_redo.setInt(1, undo_redo_id);
                    undo_redo.setInt(2, undo_redo_part);
                    undo_redo.setString(3, "redo");
                    undo_redo.setString(4, MyEditType.DELETE.toString());
                    undo_redo.setString(5, "UPDATE token SET indexInDocument=-1 WHERE tokenID=" + temp.getID());
                    undo_redo.addBatch();
                    undo_redo_part++;

                    temp = this.getNextTokenByIndex(temp.getIndexInDocument());
                }

                undo_redo_part = 0;
                undo_redo_id++;
                this.numTokens -= retval.size();

                setIndex.executeBatch();
                undo_redo.executeBatch();
                conn.commit();
            } catch (SQLException ex) {
                Log.error(this, ex);
                if (conn != null) {
                    conn.rollback();
                }
            } finally {
                conn.setAutoCommit(true);
            }
            return retval;
        }
    }

    @Override
    public ArrayList<Integer> mergeRightward(int tokenID, int numTok) throws SQLException {
        //System.out.println("Beginning database transaction");
        long now = System.currentTimeMillis();

        Connection conn = null;
        PreparedStatement setIndex = null;
        PreparedStatement undo_redo = null;
        PreparedStatement moveIndex = null;
        ArrayList<Integer> retval = new ArrayList<>();

        try {
            Token atIndex = getTokenByID(tokenID);
            Token newToken = new Token("");
            newToken.setWCOR(atIndex.getWDisplay());
            TokenImageInfoBox b = null;
            Token rightToken = this.getNextTokenByIndex(atIndex.getIndexInDocument());

            int i = 0;

            conn = getConnection();
            conn.setAutoCommit(false);

            setIndex = conn.prepareStatement("UPDATE token SET indexInDocument=? WHERE tokenID=?");
            undo_redo = conn.prepareStatement("INSERT INTO undoredo VALUES( ?,?,?,?,? )");

            // reserve undo_redo_part for unsetting the token indices
            undo_redo_part = 2;

            for (; i < numTok; ++i) {
                if (rightToken.getWDisplay().equals("\n")) {
                    break;
                }

                retval.add(rightToken.getID());

                setIndex.setInt(1, -1);
                setIndex.setInt(2, rightToken.getID());
                setIndex.addBatch();

                undo_redo.setInt(1, undo_redo_id);
                undo_redo.setInt(2, undo_redo_part);
                undo_redo.setString(3, "undo");
                undo_redo.setString(4, MyEditType.MERGE.toString());
                undo_redo.setString(5, "UPDATE token SET indexInDocument=" + rightToken.getIndexInDocument() + " WHERE tokenID=" + rightToken.getID());
                undo_redo.addBatch();

                undo_redo.setInt(1, undo_redo_id);
                undo_redo.setInt(2, undo_redo_part);
                undo_redo.setString(3, "redo");
                undo_redo.setString(4, MyEditType.MERGE.toString());
                undo_redo.setString(5, "UPDATE token SET indexInDocument=-1 WHERE tokenID=" + rightToken.getID());
                undo_redo.addBatch();
                undo_redo_part++;

                if ((atIndex.getTokenImageInfoBox() != null) && (rightToken.getTokenImageInfoBox() != null)) {
                    if (b == null) {
                        b = new TokenImageInfoBox();
                        b.setCoordinateBottom(atIndex.getTokenImageInfoBox().getCoordinateBottom());
                        b.setCoordinateTop(atIndex.getTokenImageInfoBox().getCoordinateTop());
                        b.setCoordinateLeft(atIndex.getTokenImageInfoBox().getCoordinateLeft());
                        b.setImageFileName(atIndex.getImageFilename().substring(atIndex.getImageFilename().lastIndexOf(File.separator) + 1, atIndex.getImageFilename().length()));
                    }
                    b.setCoordinateRight(java.lang.Math.max(atIndex.getTokenImageInfoBox().getCoordinateRight(), rightToken.getTokenImageInfoBox().getCoordinateRight()));
                }

                if (!rightToken.getWDisplay().equals(" ")) {
                    newToken.setWCOR((newToken.getWCOR() + rightToken.getWDisplay()));
                }

                rightToken = this.getNextTokenByIndex(rightToken.getIndexInDocument());
            }

            newToken.setIsCorrected(true);
            newToken.setIsNormal(true);
            newToken.setIsSuspicious(false);
            newToken.setNumberOfCandidates(0);
            newToken.setPageIndex(atIndex.getPageIndex());
            newToken.setSpecialSeq(SpecialSequenceType.NORMAL);
            newToken.setTokenImageInfoBox(b);
            newToken.setIndexInDocument(-1);

            int identity = this.addToken(newToken, atIndex.getIndexInDocument());
            retval.add(0, identity);

            setIndex.setInt(1, atIndex.getIndexInDocument());
            setIndex.setInt(2, identity);
            setIndex.addBatch();
            setIndex.setInt(1, -1);
            setIndex.setInt(2, atIndex.getID());
            setIndex.addBatch();

            undo_redo.setInt(1, undo_redo_id);
            undo_redo.setInt(2, 0);
            undo_redo.setString(3, "undo");
            undo_redo.setString(4, MyEditType.MERGE.toString());
            undo_redo.setString(5, "UPDATE token SET indexInDocument=-1 WHERE tokenID=" + identity);
            undo_redo.addBatch();

            undo_redo.setInt(1, undo_redo_id);
            undo_redo.setInt(2, 0);
            undo_redo.setString(3, "redo");
            undo_redo.setString(4, MyEditType.MERGE.toString());
            undo_redo.setString(5, "UPDATE token SET indexInDocument=-1 WHERE tokenID=" + atIndex.getID());
            undo_redo.addBatch();

            undo_redo.setInt(1, undo_redo_id);
            undo_redo.setInt(2, 1);
            undo_redo.setString(3, "undo");
            undo_redo.setString(4, MyEditType.MERGE.toString());
            undo_redo.setString(5, "UPDATE token SET indexInDocument=" + atIndex.getIndexInDocument() + " WHERE tokenID=" + atIndex.getID());
            undo_redo.addBatch();

            undo_redo.setInt(1, undo_redo_id);
            undo_redo.setInt(2, 1);
            undo_redo.setString(3, "redo");
            undo_redo.setString(4, MyEditType.MERGE.toString());
            undo_redo.setString(5, "UPDATE token SET indexInDocument=" + atIndex.getIndexInDocument() + " WHERE tokenID=" + identity);
            undo_redo.addBatch();

            undo_redo_part = 0;
            undo_redo_id++;

            this.numTokens -= i;

            setIndex.executeBatch();
            undo_redo.executeBatch();
            conn.commit();
            long then = System.currentTimeMillis();
            //System.out.println("Database transaction finished. Time taken: " + (then - now));
            return retval;
        } catch (SQLException ex) {
            Log.error(this, ex);
            ex.printStackTrace();
            if (conn != null) {
                conn.rollback();
            }
            return retval;
        } finally {
            if (setIndex != null) {
                setIndex.close();
            }
            if (moveIndex != null) {
                moveIndex.close();
            }
            if (undo_redo != null) {
                undo_redo.close();
            }
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    @Override
    public ArrayList<Integer> splitToken(int tokenID, String editString) throws SQLException {
        Connection conn = null;
        PreparedStatement setIndex = null;
        PreparedStatement undo_redo = null;
        ArrayList<Integer> retval = new ArrayList<>();

        try {
            editString = editString.replaceAll("\\s{2,}", " ");
            editString = editString.replaceAll("^ ", "");
            editString = editString.replaceAll(" $", "");

            conn = getConnection();
            conn.setAutoCommit(false);
            undo_redo = conn.prepareStatement("INSERT INTO undoredo VALUES( ?,?,?,?,? )");
            setIndex = conn.prepareStatement("UPDATE token SET indexInDocument=? WHERE tokenID=?");

            java.util.regex.Pattern myAlnum = java.util.regex.Pattern.compile("[\\pL\\pM\\p{Nd}\\p{Nl}\\p{Pc}[\\p{InEnclosedAlphanumerics}&&\\p{So}]]+");
            StringTokenizer strTok = new StringTokenizer(editString, " ", true);
            Token temp;
            TokenImageInfoBox b;
            int tokensAdded = 0;
            int imgwidth = 0;
            int left = 0;
            int charwidth = 0;
            int tokensToAdd = strTok.countTokens() - 1;
            int newIndex;

            Token atIndex = this.getTokenByID(tokenID);
            int freeIndexPlaces = this.getNextTokenByIndex(atIndex.getIndexInDocument()).getIndexInDocument() - atIndex.getIndexInDocument();
            int spaces = StringUtils.countMatches(editString, " ");
            int normals = tokensToAdd - spaces;

            int indicesNeeded = (normals * 3) + spaces;

            if (freeIndexPlaces < indicesNeeded) {
                this.spreadIndex(tokenID, indicesNeeded);
                atIndex = this.getTokenByID(tokenID);
            }

            if (atIndex.getTokenImageInfoBox() != null) {
                imgwidth = atIndex.getTokenImageInfoBox().getCoordinateRight() - atIndex.getTokenImageInfoBox().getCoordinateLeft();
                left = atIndex.getTokenImageInfoBox().getCoordinateLeft();
                charwidth = imgwidth / editString.length();
            }

            // reserve undo_redo_part
            undo_redo_part = 1;

            setIndex.setInt(1, -1);
            setIndex.setInt(2, atIndex.getID());
            setIndex.addBatch();

            // undo_redo_for original token
            undo_redo.setInt(1, undo_redo_id);
            undo_redo.setInt(2, 0);
            undo_redo.setString(3, "undo");
            undo_redo.setString(4, MyEditType.SPLIT.toString());
            undo_redo.setString(5, "UPDATE token SET indexInDocument=" + atIndex.getIndexInDocument() + " WHERE tokenID=" + atIndex.getID());
            undo_redo.addBatch();

            undo_redo.setInt(1, undo_redo_id);
            undo_redo.setInt(2, 0);
            undo_redo.setString(3, "redo");
            undo_redo.setString(4, MyEditType.SPLIT.toString());
            undo_redo.setString(5, "UPDATE token SET indexInDocument=-1 WHERE tokenID=" + atIndex.getID());
            undo_redo.addBatch();

            newIndex = atIndex.getIndexInDocument() + 1;
            while (strTok.hasMoreTokens()) {

                String corr = strTok.nextToken();
                if (corr.equals(" ")) {
                    b = null;
                    left += charwidth;
                } else if (imgwidth == 0) {
                    b = null;
                } else {
                    b = new TokenImageInfoBox();
                    b.setImageFileName(atIndex.getImageFilename().substring(atIndex.getImageFilename().lastIndexOf(File.separator) + 1, atIndex.getImageFilename().length()));
                    b.setCoordinateBottom(atIndex.getTokenImageInfoBox().getCoordinateBottom());
                    b.setCoordinateTop(atIndex.getTokenImageInfoBox().getCoordinateTop());
                    b.setCoordinateLeft(left);
                    left += charwidth * corr.length();
                    b.setCoordinateRight(left + 2);
                }

                temp = new Token(atIndex.getWOCR());
                temp.setIndexInDocument(-1);
                temp.setWCOR(corr);
                temp.setIsCorrected(false);
                temp.setIsSuspicious(false);
                temp.setNumberOfCandidates(0);
                temp.setPageIndex(atIndex.getPageIndex());
                if (myAlnum.matcher(corr).matches()) {
                    temp.setIsNormal(true);
                } else {
                    temp.setIsNormal(false);
                }
                if (corr.equals(" ")) {
                    temp.setSpecialSeq(SpecialSequenceType.SPACE);
                } else {
                    temp.setSpecialSeq(SpecialSequenceType.NORMAL);
                }

                temp.setTokenImageInfoBox(b);
                int identity = this.addToken(temp, newIndex);
                retval.add(identity);

                undo_redo.setInt(1, undo_redo_id);
                undo_redo.setInt(2, undo_redo_part);
                undo_redo.setString(3, "undo");
                undo_redo.setString(4, MyEditType.SPLIT.toString());
                undo_redo.setString(5, "UPDATE token SET indexInDocument=-1 WHERE tokenID=" + identity);
                undo_redo.addBatch();

                undo_redo.setInt(1, undo_redo_id);
                undo_redo.setInt(2, undo_redo_part);
                undo_redo.setString(3, "redo");
                undo_redo.setString(4, MyEditType.SPLIT.toString());
                undo_redo.setString(5, "UPDATE token SET indexInDocument=" + newIndex + " WHERE tokenID=" + identity);
                undo_redo.addBatch();

                undo_redo_part++;
                if (corr.equals(" ")) {
                    newIndex += 1;
                } else {
                    newIndex += 3;
                }
                tokensAdded++;
            }

            undo_redo_part = 0;
            undo_redo_id++;
            this.numTokens += tokensAdded;

            setIndex.executeBatch();
            undo_redo.executeBatch();
            conn.commit();
            return retval;
        } catch (SQLException ex) {
            if (conn != null) {
                conn.rollback();
            }
            return retval;
        } finally {
            if (setIndex != null) {
                setIndex.close();
            }
            if (undo_redo != null) {
                undo_redo.close();
            }
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    private boolean spreadIndex(final int tokenID, final int indexToAdd) {
        ProgressRunnable<Boolean> r = new ProgressRunnable<Boolean>() {
            @Override
            public Boolean run(ProgressHandle ph) {

                int myIndex = 0;
                Connection conn = null;
                PreparedStatement setIndex = null;
                PreparedStatement updateUndoRedo = null;

                try {
                    conn = getConnection();
//                    Statement s = conn.createStatement();
//                    ResultSet rs = s.executeQuery("SELECT * FROM undoredo");

                    conn.setAutoCommit(false);
                    setIndex = conn.prepareStatement("UPDATE token SET indexInDocument=? WHERE tokenID=?");
                    updateUndoRedo = conn.prepareStatement("UPDATE undoredo SET sql_command=? WHERE operation_id=? AND part=? AND type=?");

                    Iterator<Token> tokit = tokenIterator();
                    while (tokit.hasNext()) {
                        Token tok = tokit.next();

                        setIndex.setInt(1, myIndex);
                        setIndex.setInt(2, tok.getID());
                        setIndex.addBatch();

                        if (tok.getID() == tokenID) {
                            myIndex += indexToAdd;
                        } else if (tok.isNormal()) {
                            myIndex += NORMINDEXPLUS;
                        } else {
                            myIndex += PUNCTINDEXPLUS;
                        }
                    }

                    setIndex.executeBatch();
                    conn.commit();

                    truncateUndoRedo();

//                    while (rs.next()) {
//                        java.util.regex.Pattern tokidp = java.util.regex.Pattern.compile("UPDATE token SET indexInDocument=[0-9]+ WHERE tokenID=([0-9]+)");
//                        String operation_id = rs.getString(1);
//                        String part = rs.getString(2);
//                        String type = rs.getString(3);
//                        String command = rs.getString(5);
//
//                        System.out.println("OLD: " + operation_id + " " + part + " " + type + " " + command);
//
//                        Matcher m = tokidp.matcher(command);
//                        if (m.matches()) {
//                            int tokenID = Integer.parseInt(m.group(1));
//                            updateUndoRedo.setString(1, "UPDATE token SET indexInDocument=" + getTokenByID(tokenID).getIndexInDocument() + " WHERE tokenID=" + tokenID);
//                            updateUndoRedo.setString(2, operation_id);
//                            updateUndoRedo.setString(3, part);
//                            updateUndoRedo.setString(4, type);
//                            System.out.println("NEW: " + operation_id + " " + part + " " + type + "UPDATE token SET indexInDocument=" + getTokenByID(tokenID).getIndexInDocument() + " WHERE tokenID=" + tokenID);
//                            updateUndoRedo.addBatch();
//                        }
//                    }
//
//                    conn.commit();
                    return true;
                } catch (SQLException ex) {
                    if (conn != null) {
                        try {
                            conn.rollback();
                        } catch (SQLException ex1) {
                            Log.error(this, ex1);
                        }
                    }
                    return false;
                } finally {
                    try {
                        if (setIndex != null) {
                            try {
                                setIndex.close();
                            } catch (SQLException ex) {
                                Log.error(this, ex);
                            }
                        }
                        if (updateUndoRedo != null) {
                            try {
                                updateUndoRedo.close();
                            } catch (SQLException ex) {
                                Log.error(this, ex);
                            }
                        }
                        if (conn != null) {
                            conn.setAutoCommit(true);
                            conn.close();
                        }
                    } catch (SQLException ex) {
                        Log.error(this, ex);
                    }
                }
            }
        };
        return ProgressUtils.showProgressDialogAndRun(r, "recalculating index", true);
    }
}
