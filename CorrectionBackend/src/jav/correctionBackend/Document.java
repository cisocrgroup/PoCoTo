package jav.correctionBackend;

import jav.gui.dialogs.CustomErrorDialog;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import org.h2.jdbcx.JdbcConnectionPool;
import org.openide.awt.UndoRedo;

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
public abstract class Document {

    String baseImagePath = "";
    JdbcConnectionPool jcp;
    int numTokens = 0;
    int numPages = 0;
    boolean hasImages = false;
    String propertiespath = "";
    int undo_redo_id = 0;
    int undo_redo_part = 0;
    UndoRedo.Manager manager = null;

    /**
     *
     * @param c {
     * @see java.sql.Connection} for the document database
     */
    public Document(JdbcConnectionPool j) {
        this.jcp = j;
    }

    public UndoRedo.Manager getUndoRedoManager() {
        return manager;
    }

    public void setUndoRedoManager(UndoRedo.Manager man) {
        this.manager = man;
    }

    /**
     * returns the baseImagePath
     */
    public String getBaseImagePath() {
        return this.baseImagePath;
    }

    public void setBaseImagePath(String s) {
        this.baseImagePath = s;
    }

    protected abstract int addToken(Token t, Connection conn);

    protected abstract int addToken(Token t);

    /**
     * Adds a correction candidate. Linked to a token via the tokenid. Used when
     * importing profiles
     *
     * @param c the {
     * @see jav.correctionBackend.Candidate} to be added
     */
    protected void addCandidate(Candidate c) {
        Connection conn = null;
        try {
            conn = jcp.getConnection();
            PreparedStatement prep = conn.prepareStatement("INSERT INTO candidate VALUES( ?,?,?,?,?,? )");
            prep.setInt(1, c.getTokenID());
            prep.setInt(2, c.getRank());
            prep.setString(3, c.getSuggestion());
            prep.setString(4, c.getInterpretation());
            prep.setDouble(5, c.getVoteweight());
            prep.setInt(6, c.getDlev());

            prep.addBatch();
            prep.executeBatch();
            prep.close();
            conn.close();
        } catch (SQLException ex) {
        }
    }

    protected void addPattern(Pattern p) {
        Connection conn = null;
        try {
            conn = jcp.getConnection();
            PreparedStatement prep = conn.prepareStatement("INSERT INTO pattern VALUES( null, ?, ?, ?, ? )");
            prep.setString(1, p.getLeft());
            prep.setString(2, p.getRight());
            prep.setInt(3, p.getOccurencesN());
            prep.setInt(4, p.getCorrected());

            prep.addBatch();
            prep.executeBatch();
            prep.close();
            conn.close();
        } catch (SQLException ex) {
        }
    }

    protected void addPatternOccurrence(PatternOccurrence po) {
        Connection conn = null;
        try {
            conn = jcp.getConnection();
            PreparedStatement prep = conn.prepareStatement("INSERT INTO patternoccurrence VALUES( ?, ?, ?, ?, ?, ? )");
            prep.setInt(1, po.getPatternID());
            prep.setInt(2, po.getPart());
            prep.setString(3, po.getWOCR_LC());
            prep.setString(4, po.getWSuggestion());
            prep.setInt(5, po.getOccurencesN());
            prep.setInt(6, po.getCorrected());

            prep.addBatch();
            prep.executeBatch();
            prep.close();
            conn.close();
        } catch (SQLException ex) {
        }
    }

    public void clearPatterns() {
        Connection conn = null;
        try {
            conn = jcp.getConnection();
            Statement s = conn.createStatement();
            s.executeUpdate("TRUNCATE TABLE pattern");
            s.executeUpdate("TRUNCATE TABLE patternoccurrence");
            s.close();
            conn.close();
        } catch (SQLException ex) {
        }
    }

    public void clearCandidates() {
        Connection conn = null;
        try {
            conn = jcp.getConnection();
            Statement s = conn.createStatement();
            s.executeUpdate("TRUNCATE TABLE candidate");
            s.close();
            conn.close();
        } catch (SQLException ex) {
        }
    }

    public boolean correctTokenByString(int tokenID, String corr) throws SQLException {

        Connection conn = null;
        PreparedStatement wcor = null;
        PreparedStatement setcor = null;
        PreparedStatement undo_redo = null;

        try {
            Token t = this.getTokenByID(tokenID);
            conn = jcp.getConnection();

            conn.setAutoCommit(false);

            wcor = conn.prepareStatement("UPDATE token SET wCorr=? WHERE tokenID=?");
            wcor.setString(1, corr);
            wcor.setInt(2, tokenID);
            wcor.executeUpdate();

            setcor = conn.prepareStatement("UPDATE token SET isCorrected=? WHERE tokenID=?");
            setcor.setBoolean(1, true);
            setcor.setInt(2, tokenID);
            setcor.executeUpdate();

            undo_redo = conn.prepareStatement("INSERT INTO undoredo VALUES( ?,?,?,?,? )");
            if (!t.isCorrected()) {
                undo_redo.setInt(1, undo_redo_id);
                undo_redo.setInt(2, undo_redo_part);
                undo_redo.setString(3, "undo");
                undo_redo.setString(4, MyEditType.CORRECTED.toString());
                undo_redo.setString(5, "UPDATE token SET isCorrected=false WHERE tokenID=" + tokenID);
                undo_redo.addBatch();

                undo_redo.setInt(1, undo_redo_id);
                undo_redo.setInt(2, undo_redo_part);
                undo_redo.setString(3, "redo");
                undo_redo.setString(4, MyEditType.CORRECTED.toString());
                undo_redo.setString(5, "UPDATE token SET isCorrected=true WHERE tokenID=" + tokenID);
                undo_redo.addBatch();
                undo_redo_part++;
            }

            undo_redo.setInt(1, undo_redo_id);
            undo_redo.setInt(2, undo_redo_part);
            undo_redo.setString(3, "undo");
            undo_redo.setString(4, MyEditType.CORRECTED.toString());
            undo_redo.setString(5, "UPDATE token SET wCorr='" + t.getWDisplay() + "' WHERE tokenID=" + tokenID);
            undo_redo.addBatch();

            undo_redo.setInt(1, undo_redo_id);
            undo_redo.setInt(2, undo_redo_part);
            undo_redo.setString(3, "redo");
            undo_redo.setString(4, MyEditType.CORRECTED.toString());
            undo_redo.setString(5, "UPDATE token SET wCorr='" + corr + "' WHERE tokenID=" + tokenID);
            undo_redo.addBatch();

            undo_redo.executeBatch();

            undo_redo_id++;
            undo_redo_part = 0;

            conn.commit();
            return true;
        } catch (SQLException ex) {
            if (conn != null) {
                conn.rollback();
            }
            return false;
        } finally {
            if (wcor != null) {
                wcor.close();
            }
            if (setcor != null) {
                setcor.close();
            }
            if (undo_redo != null) {
                undo_redo.close();
            }
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    public boolean correctTokensByString(HashMap<Integer, String> art) throws SQLException {
        Connection conn = null;
        PreparedStatement wcor = null;
        PreparedStatement setcor = null;
        PreparedStatement undo_redo = null;

        try {
            Iterator<Integer> iter = art.keySet().iterator();
            conn = jcp.getConnection();

            conn.setAutoCommit(false);
            undo_redo = conn.prepareStatement("INSERT INTO undoredo VALUES( ?,?,?,?,? )");
            wcor = conn.prepareStatement("UPDATE token SET wCorr=? WHERE tokenID=?");
            setcor = conn.prepareStatement("UPDATE token SET isCorrected=? WHERE tokenID=?");

            while (iter.hasNext()) {
                int tokenID = iter.next();
                Token t = this.getTokenByID(tokenID);
                String corr = art.get(tokenID);
                wcor.setString(1, corr);
                wcor.setInt(2, tokenID);
                wcor.addBatch();

                setcor.setBoolean(1, true);
                setcor.setInt(2, tokenID);
                setcor.addBatch();

                if (!t.isCorrected()) {
                    undo_redo.setInt(1, undo_redo_id);
                    undo_redo.setInt(2, undo_redo_part);
                    undo_redo.setString(3, "undo");
                    undo_redo.setString(4, MyEditType.MULTICORRECTED.toString());
                    undo_redo.setString(5, "UPDATE token SET isCorrected=false WHERE tokenID=" + tokenID);
                    undo_redo.addBatch();

                    undo_redo.setInt(1, undo_redo_id);
                    undo_redo.setInt(2, undo_redo_part);
                    undo_redo.setString(3, "redo");
                    undo_redo.setString(4, MyEditType.MULTICORRECTED.toString());
                    undo_redo.setString(5, "UPDATE token SET isCorrected=true WHERE tokenID=" + tokenID);
                    undo_redo.addBatch();
                    undo_redo_part++;
                }

                undo_redo.setInt(1, undo_redo_id);
                undo_redo.setInt(2, undo_redo_part);
                undo_redo.setString(3, "undo");
                undo_redo.setString(4, MyEditType.MULTICORRECTED.toString());
                undo_redo.setString(5, "UPDATE token SET wCorr='" + t.getWDisplay() + "' WHERE tokenID=" + tokenID);
                undo_redo.addBatch();

                undo_redo.setInt(1, undo_redo_id);
                undo_redo.setInt(2, undo_redo_part);
                undo_redo.setString(3, "redo");
                undo_redo.setString(4, MyEditType.MULTICORRECTED.toString());
                undo_redo.setString(5, "UPDATE token SET wCorr='" + corr + "' WHERE tokenID=" + tokenID);
                undo_redo.addBatch();

                undo_redo_part++;
            }

            undo_redo.executeBatch();
            wcor.executeBatch();
            setcor.executeBatch();
            undo_redo_id++;
            undo_redo_part = 0;
            conn.commit();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            if (conn != null) {
                conn.rollback();
            }
            return false;
        } finally {
            if (wcor != null) {
                wcor.close();
            }
            if (setcor != null) {
                setcor.close();
            }
            if (undo_redo != null) {
                undo_redo.close();
            }
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    public int getUndoRedoIndex() {
        return this.undo_redo_id;
    }

    public void cleanupDatabase() {
        try {
            Connection conn = jcp.getConnection();
            Statement s = conn.createStatement();
            s.executeUpdate("DELETE FROM TOKEN WHERE indexInDocument=-1");
            s.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void undoAll() {
        try {
            Connection conn = jcp.getConnection();
            Statement s = conn.createStatement();
            Statement t = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM undoredo WHERE type='undo'");
            while (rs.next()) {
                t.execute(rs.getString(5));
            }
            t.close();
            s.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void removeEdit(int editid) {
        try {
            Connection conn = jcp.getConnection();
            Statement s = conn.createStatement();
            s.execute("DELETE FROM undoredo WHERE operation_id=" + editid);
            s.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void truncateUndoRedo() {
        try {
            Connection conn = jcp.getConnection();
            Statement s = conn.createStatement();
            s.execute("TRUNCATE TABLE undoredo");
            s.close();
            conn.close();
            manager.discardAllEdits();
            undo_redo_id = 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void updatePattern(Pattern p) {
        try {
            Connection conn = jcp.getConnection();
            PreparedStatement prep = conn.prepareStatement("UPDATE pattern SET corrected=? WHERE patternID=?");
            prep.setInt(1, p.getCorrected());
            prep.setInt(2, p.getPatternID());

            prep.addBatch();
            prep.executeBatch();
            prep.close();
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(DefaultDocument.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updatePatternOccurrence(PatternOccurrence p) {
        try {
            Connection conn = jcp.getConnection();
            PreparedStatement prep = conn.prepareStatement("UPDATE patternocccurrence SET corrected=? WHERE patternID=? AND part=?");
            prep.setInt(1, p.getCorrected());
            prep.setInt(2, p.getPatternID());
            prep.setInt(3, p.getPart());

            prep.addBatch();
            prep.executeBatch();
            prep.close();
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(DefaultDocument.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public UndoRedoInformation undo(int index) {
        long time = System.currentTimeMillis();
        System.out.println("starting undo " + index);
        try {
            Connection conn = jcp.getConnection();
            Statement s = conn.createStatement();
            Statement t = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM undoredo WHERE operation_id=" + index + " AND type='undo' ORDER BY part");

            if (rs.next()) {
                if (rs.getString(4).equals(MyEditType.SETCORRECTED.toString())) {

                    SetCorrectedUndoRedoInformation retval = null;

                    java.util.regex.Pattern indexp = java.util.regex.Pattern.compile("UPDATE token SET isCorrected=(.*?) WHERE tokenID=([0-9]+)");
                    Matcher m = indexp.matcher(rs.getString(5));
                    if (m.matches()) {
                        retval = new SetCorrectedUndoRedoInformation(Integer.parseInt(m.group(2)), Boolean.parseBoolean(m.group(1)));
                    }

                    t.execute(rs.getString(5));
                    System.out.println("undo finished. Time taken =" + (System.currentTimeMillis() - time));
                    s.close();
                    t.close();
                    conn.close();
                    return retval;

                } else if (rs.getString(4).equals(MyEditType.CORRECTED.toString())) {

                    CorrectedUndoRedoInformation retval = null;
                    java.util.regex.Pattern corrstring = java.util.regex.Pattern.compile("UPDATE token SET wCorr='(.*?)' WHERE tokenID=([0-9]+)");
                    java.util.regex.Pattern indexp = java.util.regex.Pattern.compile("UPDATE token SET isCorrected=(.*?) WHERE tokenID=([0-9]+)");
                    t.execute(rs.getString(5));

                    Matcher m = indexp.matcher(rs.getString(5));
                    if (m.matches()) {
                        rs.next();
                        t.execute(rs.getString(5));
                        Matcher n = corrstring.matcher(rs.getString(5));
                        if (n.matches()) {
                            retval = new CorrectedUndoRedoInformation(Integer.parseInt(n.group(2)), Boolean.parseBoolean(m.group(1)), n.group(1));
                        }
                    } else {
                        Matcher n = corrstring.matcher(rs.getString(5));
                        if (n.matches()) {
                            retval = new CorrectedUndoRedoInformation(Integer.parseInt(n.group(2)), true, n.group(1));
                        }
                    }
                    System.out.println("undo finished. Time taken =" + (System.currentTimeMillis() - time));
                    s.close();
                    t.close();
                    conn.close();
                    return retval;

                } else if (rs.getString(4).equals(MyEditType.MULTISETCORRECTED.toString())) {

                    MultiSetCorrectedUndoRedoInformation retval = null;
                    SetCorrectedUndoRedoInformation temp;
                    java.util.regex.Pattern indexp = java.util.regex.Pattern.compile("UPDATE token SET isCorrected=(.*?) WHERE tokenID=([0-9]+)");
                    Matcher m = indexp.matcher(rs.getString(5));
                    if (m.matches()) {
                        temp = new SetCorrectedUndoRedoInformation(Integer.parseInt(m.group(2)), Boolean.parseBoolean(m.group(1)));
                        retval = new MultiSetCorrectedUndoRedoInformation(temp);
                    }
                    t.execute(rs.getString(5));

                    while (rs.next()) {
                        m = indexp.matcher(rs.getString(5));
                        if (m.matches()) {
                            temp = new SetCorrectedUndoRedoInformation(Integer.parseInt(m.group(2)), Boolean.parseBoolean(m.group(1)));
                            retval.addSetCorrectedUndoRedoInformation(temp);
                        }
                        t.execute(rs.getString(5));
                    }
                    System.out.println("undo finished. Time taken =" + (System.currentTimeMillis() - time));
                    s.close();
                    t.close();
                    conn.close();
                    return retval;

                } else if (rs.getString(4).equals(MyEditType.MERGE.toString())) {

                    MergeUndoRedoInformation retval = null;
                    ArrayList<Integer> affectedTokens = new ArrayList<>();
                    int tokenID = -1;
                    int poi = -1;
                    java.util.regex.Pattern tokp = java.util.regex.Pattern.compile("UPDATE token SET indexInDocument=[0-9]+ WHERE tokenID=([0-9]+)");
                    java.util.regex.Pattern poip = java.util.regex.Pattern.compile("UPDATE token SET indexInDocument=-1 WHERE tokenID=([0-9]+)");

                    Matcher m;

                    m = poip.matcher(rs.getString(5));
                    if (m.matches()) {
                        poi = Integer.parseInt(m.group(1));
                    }
                    t.execute(rs.getString(5));

                    while (rs.next()) {
                        t.execute(rs.getString(5));
                        m = tokp.matcher(rs.getString(5));
                        if (m.matches()) {
                            tokenID = Integer.parseInt(m.group(1));
                            affectedTokens.add(tokenID);
                        }
                    }

                    this.numTokens += affectedTokens.size() - 1;
                    retval = new MergeUndoRedoInformation(poi, affectedTokens);

                    System.out.println("undo finished. Time taken =" + (System.currentTimeMillis() - time));
                    s.close();
                    t.close();
                    conn.close();
                    return retval;

                } else if (rs.getString(4).equals(MyEditType.MULTICORRECTED.toString())) {

                    MultiCorrectedUndoRedoInformation retval = null;
                    CorrectedUndoRedoInformation temp;
                    java.util.regex.Pattern corrstring = java.util.regex.Pattern.compile("UPDATE token SET wCorr='(.*?)' WHERE tokenID=([0-9]+)");
                    java.util.regex.Pattern indexp = java.util.regex.Pattern.compile("UPDATE token SET isCorrected=(.*?) WHERE tokenID=([0-9]+)");
                    t.execute(rs.getString(5));

                    Matcher m = indexp.matcher(rs.getString(5));
                    if (m.matches()) {
                        rs.next();
                        t.execute(rs.getString(5));
                        Matcher n = corrstring.matcher(rs.getString(5));
                        if (n.matches()) {
                            temp = new CorrectedUndoRedoInformation(Integer.parseInt(n.group(2)), Boolean.parseBoolean(m.group(1)), n.group(1));
                            retval = new MultiCorrectedUndoRedoInformation(temp);
                        }
                    } else {
                        Matcher n = corrstring.matcher(rs.getString(5));
                        if (n.matches()) {
                            temp = new CorrectedUndoRedoInformation(Integer.parseInt(n.group(2)), true, n.group(1));
                            retval = new MultiCorrectedUndoRedoInformation(temp);
                        }
                    }

                    while (rs.next()) {
                        m = indexp.matcher(rs.getString(5));
                        t.execute(rs.getString(5));
                        if (m.matches()) {
                            rs.next();
                            t.execute(rs.getString(5));
                            Matcher n = corrstring.matcher(rs.getString(5));
                            if (n.matches()) {
                                temp = new CorrectedUndoRedoInformation(Integer.parseInt(n.group(2)), Boolean.parseBoolean(m.group(1)), n.group(1));
                                retval.addCorrectedUndoRedoInformation(temp);
                            }
                        } else {
                            Matcher n = corrstring.matcher(rs.getString(5));
                            if (n.matches()) {
                                temp = new CorrectedUndoRedoInformation(Integer.parseInt(n.group(2)), true, n.group(1));
                                retval.addCorrectedUndoRedoInformation(temp);
                            }
                        }
                    }
                    System.out.println("undo finished. Time taken =" + (System.currentTimeMillis() - time));
                    s.close();
                    t.close();
                    conn.close();
                    return retval;

                } else if (rs.getString(4).equals(MyEditType.SPLIT.toString())) {

                    SplitUndoRedoInformation retval = null;
                    ArrayList<Integer> affectedTokens = new ArrayList<>();
                    int tokenID = -1;
                    int poi = -1;
                    java.util.regex.Pattern tokp = java.util.regex.Pattern.compile("UPDATE token SET indexInDocument=[0-9\\-]+ WHERE tokenID=([0-9]+)");

                    t.execute(rs.getString(5));

                    Matcher m = tokp.matcher(rs.getString(5));
                    if (m.matches()) {
                        tokenID = Integer.parseInt(m.group(1));
                        affectedTokens.add(tokenID);
                    }

                    while (rs.next()) {
                        t.execute(rs.getString(5));
                        m = tokp.matcher(rs.getString(5));
                        if (m.matches()) {
                            if (poi == -1) {
                                poi = Integer.parseInt(m.group(1));
                            } else {
                                tokenID = Integer.parseInt(m.group(1));
                                affectedTokens.add(tokenID);
                            }
                        }
                    }

                    this.numTokens -= affectedTokens.size() - 1;
                    retval = new SplitUndoRedoInformation(poi, affectedTokens);

                    System.out.println("undo finished. Time taken =" + (System.currentTimeMillis() - time));
                    s.close();
                    t.close();
                    conn.close();
                    return retval;

                } else if (rs.getString(4).equals(MyEditType.DELETE.toString())) {

                    DeleteUndoRedoInformation retval = null;
                    ArrayList<Integer> affectedTokens = new ArrayList<>();
                    int tokenID = -1;
                    java.util.regex.Pattern tokp = java.util.regex.Pattern.compile("UPDATE token SET indexInDocument=[0-9]+ WHERE tokenID=([0-9]+)");
                    t.execute(rs.getString(5));
                    Matcher m = tokp.matcher(rs.getString(5));
                    if (m.matches()) {
                        tokenID = Integer.parseInt(m.group(1));
                        affectedTokens.add(tokenID);
                    }

                    while (rs.next()) {
                        t.execute(rs.getString(5));

                        m = tokp.matcher(rs.getString(5));
                        if (m.matches()) {
                            tokenID = Integer.parseInt(m.group(1));
                            affectedTokens.add(tokenID);
                        }
                    }

                    this.numTokens += affectedTokens.size();
                    Token tok = this.getTokenByID(affectedTokens.get(0));
                    Token prev = this.getPreviousToken(affectedTokens.get(0));
                    if (tok == null || tok.getPageIndex() != prev.getPageIndex()) {
                        retval = new DeleteUndoRedoInformation(-1, affectedTokens);
                    } else {
                        retval = new DeleteUndoRedoInformation(prev.getID(), affectedTokens);
                    }

                    System.out.println("undo finished. Time taken =" + (System.currentTimeMillis() - time));
                    s.close();
                    t.close();
                    conn.close();
                    return retval;

                } else {
                    // TODO unknown edittype exception
                    conn.close();
                    return null;
                }
            } else {
                // TODO empty resultset exception
                conn.close();
                return null;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public UndoRedoInformation redo(int index) {
        long time = System.currentTimeMillis();
        System.out.println("starting redo " + index);
        try {
            Connection conn = jcp.getConnection();
            Statement s = conn.createStatement();
            Statement t = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM undoredo WHERE operation_id=" + index + " AND type='redo' ORDER BY part");

            if (rs.next()) {
                if (rs.getString(4).equals(MyEditType.SETCORRECTED.toString())) {

                    SetCorrectedUndoRedoInformation retval = null;

                    java.util.regex.Pattern indexp = java.util.regex.Pattern.compile("UPDATE token SET isCorrected=(.*?) WHERE tokenID=([0-9]+)");
                    Matcher m = indexp.matcher(rs.getString(5));
                    if (m.matches()) {
                        retval = new SetCorrectedUndoRedoInformation(Integer.parseInt(m.group(2)), Boolean.parseBoolean(m.group(1)));
                    }

                    t.execute(rs.getString(5));
                    System.out.println("redo finished. Time taken =" + (System.currentTimeMillis() - time));
                    s.close();
                    t.close();
                    conn.close();
                    return retval;

                } else if (rs.getString(4).equals(MyEditType.CORRECTED.toString())) {

                    CorrectedUndoRedoInformation retval = null;
                    java.util.regex.Pattern corrstring = java.util.regex.Pattern.compile("UPDATE token SET wCorr='(.*?)' WHERE tokenID=([0-9]+)");
                    java.util.regex.Pattern indexp = java.util.regex.Pattern.compile("UPDATE token SET isCorrected=(.*?) WHERE tokenID=([0-9]+)");
                    t.execute(rs.getString(5));

                    Matcher m = indexp.matcher(rs.getString(5));
                    if (m.matches()) {
                        rs.next();
                        t.execute(rs.getString(5));
                        Matcher n = corrstring.matcher(rs.getString(5));
                        if (n.matches()) {
                            retval = new CorrectedUndoRedoInformation(Integer.parseInt(n.group(2)), Boolean.parseBoolean(m.group(1)), n.group(1));
                        }
                    } else {
                        Matcher n = corrstring.matcher(rs.getString(5));
                        if (n.matches()) {
                            retval = new CorrectedUndoRedoInformation(Integer.parseInt(n.group(2)), true, n.group(1));
                        }
                    }
                    System.out.println("redo finished. Time taken =" + (System.currentTimeMillis() - time));
                    s.close();
                    t.close();
                    conn.close();
                    return retval;

                } else if (rs.getString(4).equals(MyEditType.MULTISETCORRECTED.toString())) {

                    MultiSetCorrectedUndoRedoInformation retval = null;
                    SetCorrectedUndoRedoInformation temp;
                    java.util.regex.Pattern indexp = java.util.regex.Pattern.compile("UPDATE token SET isCorrected=(.*?) WHERE tokenID=([0-9]+)");
                    Matcher m = indexp.matcher(rs.getString(5));
                    if (m.matches()) {
                        temp = new SetCorrectedUndoRedoInformation(Integer.parseInt(m.group(2)), Boolean.parseBoolean(m.group(1)));
                        retval = new MultiSetCorrectedUndoRedoInformation(temp);
                    }
                    t.execute(rs.getString(5));

                    while (rs.next()) {
                        m = indexp.matcher(rs.getString(5));
                        if (m.matches()) {
                            temp = new SetCorrectedUndoRedoInformation(Integer.parseInt(m.group(2)), Boolean.parseBoolean(m.group(1)));
                            retval.addSetCorrectedUndoRedoInformation(temp);
                        }
                        t.execute(rs.getString(5));

                    }
                    System.out.println("redo finished. Time taken =" + (System.currentTimeMillis() - time));
                    s.close();
                    t.close();
                    conn.close();
                    return retval;

                } else if (rs.getString(4).equals(MyEditType.MERGE.toString())) {

                    MergeUndoRedoInformation retval = null;
                    ArrayList<Integer> affectedTokens = new ArrayList<>();
                    int tokenID = -1;
                    int poi = -1;
                    java.util.regex.Pattern poip = java.util.regex.Pattern.compile("UPDATE token SET indexInDocument=-1 WHERE tokenID=([0-9]+)");
                    java.util.regex.Pattern tokp = java.util.regex.Pattern.compile("UPDATE token SET indexInDocument=[0-9\\-]+ WHERE tokenID=([0-9]+)");

                    Matcher m;

                    m = poip.matcher(rs.getString(5));
                    if (m.matches()) {
                        poi = Integer.parseInt(m.group(1));
                    }
                    t.execute(rs.getString(5));

                    while (rs.next()) {
                        t.execute(rs.getString(5));
                        m = tokp.matcher(rs.getString(5));
                        if (m.matches()) {
                            tokenID = Integer.parseInt(m.group(1));
                            affectedTokens.add(tokenID);
                        }
                    }

                    this.numTokens -= affectedTokens.size() - 1;
                    retval = new MergeUndoRedoInformation(poi, affectedTokens);

                    System.out.println("redo finished. Time taken =" + (System.currentTimeMillis() - time));
                    s.close();
                    t.close();
                    conn.close();
                    return retval;

                } else if (rs.getString(4).equals(MyEditType.MULTICORRECTED.toString())) {

                    MultiCorrectedUndoRedoInformation retval = null;
                    CorrectedUndoRedoInformation temp;
                    java.util.regex.Pattern corrstring = java.util.regex.Pattern.compile("UPDATE token SET wCorr='(.*?)' WHERE tokenID=([0-9]+)");
                    java.util.regex.Pattern indexp = java.util.regex.Pattern.compile("UPDATE token SET isCorrected=(.*?) WHERE tokenID=([0-9]+)");
                    t.execute(rs.getString(5));

                    Matcher m = indexp.matcher(rs.getString(5));
                    if (m.matches()) {
                        rs.next();
                        t.execute(rs.getString(5));
                        Matcher n = corrstring.matcher(rs.getString(5));
                        if (n.matches()) {
                            temp = new CorrectedUndoRedoInformation(Integer.parseInt(n.group(2)), Boolean.parseBoolean(m.group(1)), n.group(1));
                            retval = new MultiCorrectedUndoRedoInformation(temp);
                        }
                    } else {
                        Matcher n = corrstring.matcher(rs.getString(5));
                        if (n.matches()) {
                            temp = new CorrectedUndoRedoInformation(Integer.parseInt(n.group(2)), true, n.group(1));
                            retval = new MultiCorrectedUndoRedoInformation(temp);
                        }
                    }

                    while (rs.next()) {
                        m = indexp.matcher(rs.getString(5));
                        if (m.matches()) {
                            rs.next();
                            t.execute(rs.getString(5));
                            Matcher n = corrstring.matcher(rs.getString(5));
                            if (n.matches()) {
                                temp = new CorrectedUndoRedoInformation(Integer.parseInt(n.group(2)), Boolean.parseBoolean(m.group(1)), n.group(1));
                                retval.addCorrectedUndoRedoInformation(temp);
                            }
                        } else {
                            Matcher n = corrstring.matcher(rs.getString(5));
                            if (n.matches()) {
                                temp = new CorrectedUndoRedoInformation(Integer.parseInt(n.group(2)), true, n.group(1));
                                retval.addCorrectedUndoRedoInformation(temp);
                            }
                        }
                    }
                    System.out.println("redo finished. Time taken =" + (System.currentTimeMillis() - time));
                    s.close();
                    t.close();
                    conn.close();
                    return retval;

                } else if (rs.getString(4).equals(MyEditType.SPLIT.toString())) {

                    SplitUndoRedoInformation retval = null;
                    int poi = -1;
                    ArrayList<Integer> affectedTokens = new ArrayList<>();
                    java.util.regex.Pattern tokp = java.util.regex.Pattern.compile("UPDATE token SET indexInDocument=[0-9\\-]+ WHERE tokenID=([0-9]+)");
                    t.execute(rs.getString(5));

                    Matcher m = tokp.matcher(rs.getString(5));
                    if (m.matches()) {
                        poi = Integer.parseInt(m.group(1));
                    }

                    while (rs.next()) {
                        t.execute(rs.getString(5));
                        m = tokp.matcher(rs.getString(5));
                        if (m.matches()) {
                            affectedTokens.add(Integer.parseInt(m.group(1)));
                        }
                    }

                    this.numTokens += affectedTokens.size() - 1;
                    retval = new SplitUndoRedoInformation(poi, affectedTokens);

                    System.out.println("redo finished. Time taken =" + (System.currentTimeMillis() - time));
                    s.close();
                    t.close();
                    conn.close();
                    return retval;

                } else if (rs.getString(4).equals(MyEditType.DELETE.toString())) {

                    DeleteUndoRedoInformation retval = null;
                    int poi = -1;
                    ArrayList<Integer> affectedTokens = new ArrayList<>();
                    java.util.regex.Pattern tokp = java.util.regex.Pattern.compile("UPDATE token SET indexInDocument=-1 WHERE tokenID=(.*)");
                    t.execute(rs.getString(5));
                    Matcher m = tokp.matcher(rs.getString(5));
                    if (m.matches()) {
                        poi = Integer.parseInt(m.group(1));
                        affectedTokens.add(poi);
                    }

                    while (rs.next()) {
                        m = tokp.matcher(rs.getString(5));
                        if (m.matches()) {
                            affectedTokens.add(Integer.parseInt(m.group(1)));
                        }
                        t.execute(rs.getString(5));
                    }

                    this.numTokens -= affectedTokens.size();
                    retval = new DeleteUndoRedoInformation(poi, affectedTokens);

                    System.out.println("redo finished. Time taken =" + (System.currentTimeMillis() - time));
                    s.close();
                    t.close();
                    conn.close();
                    return retval;

                } else {
                    // TODO unknown edittype exception
                    conn.close();
                    return null;
                }
            } else {
                // TODO empty resultset exception
                conn.close();
                return null;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public Token getTokenByID(int tokenID) {
        Token retval = null;
        try {
            Connection conn = jcp.getConnection();
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM token WHERE tokenid=" + tokenID);

            while (rs.next()) {
                retval = new Token(rs.getString(4));
                retval.setId(rs.getInt(1));
                retval.setIndexInDocument(rs.getInt(2));
                retval.setOrigID(rs.getInt(3));
                retval.setWCOR(rs.getString(5));
                retval.setIsSuspicious(rs.getBoolean(15));
                retval.setIsCorrected(rs.getBoolean(7));
                retval.setIsNormal(rs.getBoolean(6));
                retval.setNumberOfCandidates(rs.getInt(8));
                retval.setPageIndex(rs.getInt(16));
                retval.setSpecialSeq(SpecialSequenceType.valueOf(rs.getString(13)));
                retval.setTopSuggestion(rs.getString(17));
                retval.setTopCandDLev(rs.getInt(18));

                if (rs.getString(14).equals("")) {
                    retval.setTokenImageInfoBox(null);
                } else {
                    TokenImageInfoBox tiib = new TokenImageInfoBox();
                    tiib.setImageFileName(this.baseImagePath + File.separator + rs.getString(14));
                    tiib.setCoordinateBottom(rs.getInt(12));
                    tiib.setCoordinateTop(rs.getInt(11));
                    tiib.setCoordinateLeft(rs.getInt(9));
                    tiib.setCoordinateRight(rs.getInt(10));
                    retval.setTokenImageInfoBox(tiib);
                }
            }
            s.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return retval;
    }

    public Token getNextToken(int tokenID) {
        Token thisT = this.getTokenByID(tokenID);
        Token retval = null;
        try {
            Connection conn = jcp.getConnection();
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM token WHERE indexInDocument>" + thisT.getIndexInDocument() + " ORDER BY indexInDocument LIMIT 1");
            if (rs.next()) {
                retval = new Token(rs.getString(4));
                retval.setId(rs.getInt(1));
                retval.setIndexInDocument(rs.getInt(2));
                retval.setOrigID(rs.getInt(3));
                retval.setWCOR(rs.getString(5));
                retval.setIsSuspicious(rs.getBoolean(15));
                retval.setIsCorrected(rs.getBoolean(7));
                retval.setIsNormal(rs.getBoolean(6));
                retval.setNumberOfCandidates(rs.getInt(8));
                retval.setPageIndex(rs.getInt(16));
                retval.setSpecialSeq(SpecialSequenceType.valueOf(rs.getString(13)));
                retval.setTopSuggestion(rs.getString(17));
                retval.setTopCandDLev(rs.getInt(18));

                if (rs.getString(14).equals("")) {
                    retval.setTokenImageInfoBox(null);
                } else {
                    TokenImageInfoBox tiib = new TokenImageInfoBox();
                    tiib.setImageFileName(this.baseImagePath + File.separator + rs.getString(14));
                    tiib.setCoordinateBottom(rs.getInt(12));
                    tiib.setCoordinateTop(rs.getInt(11));
                    tiib.setCoordinateLeft(rs.getInt(9));
                    tiib.setCoordinateRight(rs.getInt(10));
                    retval.setTokenImageInfoBox(tiib);
                }
            }
            s.close();
            conn.close();
        } catch (SQLException ex) {
        }
        return retval;
    }

    public Token getNextTokenByIndex(int indexInDocument) {
        Token retval = null;
        try {
            Connection conn = jcp.getConnection();
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM token WHERE indexInDocument>" + indexInDocument + " ORDER BY indexInDocument LIMIT 1");
            if (rs.next()) {
                retval = new Token(rs.getString(4));
                retval.setId(rs.getInt(1));
                retval.setIndexInDocument(rs.getInt(2));
                retval.setOrigID(rs.getInt(3));
                retval.setWCOR(rs.getString(5));
                retval.setIsSuspicious(rs.getBoolean(15));
                retval.setIsCorrected(rs.getBoolean(7));
                retval.setIsNormal(rs.getBoolean(6));
                retval.setNumberOfCandidates(rs.getInt(8));
                retval.setPageIndex(rs.getInt(16));
                retval.setSpecialSeq(SpecialSequenceType.valueOf(rs.getString(13)));
                retval.setTopSuggestion(rs.getString(17));
                retval.setTopCandDLev(rs.getInt(18));

                if (rs.getString(14).equals("")) {
                    retval.setTokenImageInfoBox(null);
                } else {
                    TokenImageInfoBox tiib = new TokenImageInfoBox();
                    tiib.setImageFileName(this.baseImagePath + File.separator + rs.getString(14));
                    tiib.setCoordinateBottom(rs.getInt(12));
                    tiib.setCoordinateTop(rs.getInt(11));
                    tiib.setCoordinateLeft(rs.getInt(9));
                    tiib.setCoordinateRight(rs.getInt(10));
                    retval.setTokenImageInfoBox(tiib);
                }
            }
            s.close();
            conn.close();
        } catch (SQLException ex) {
        }
        return retval;
    }

    public Token getNextNormalToken(int tokenID) {
        Token thisT = this.getTokenByID(tokenID);
        Token retval = null;
        try {
            Connection conn = jcp.getConnection();
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM token WHERE indexInDocument>" + thisT.getIndexInDocument() + " AND isNormal=true ORDER BY indexInDocument LIMIT 1");
            if (rs.next()) {
                retval = new Token(rs.getString(4));
                retval.setId(rs.getInt(1));
                retval.setIndexInDocument(rs.getInt(2));
                retval.setOrigID(rs.getInt(3));
                retval.setWCOR(rs.getString(5));
                retval.setIsSuspicious(rs.getBoolean(15));
                retval.setIsCorrected(rs.getBoolean(7));
                retval.setIsNormal(rs.getBoolean(6));
                retval.setNumberOfCandidates(rs.getInt(8));
                retval.setPageIndex(rs.getInt(16));
                retval.setSpecialSeq(SpecialSequenceType.valueOf(rs.getString(13)));
                retval.setTopSuggestion(rs.getString(17));
                retval.setTopCandDLev(rs.getInt(18));

                if (rs.getString(14).equals("")) {
                    retval.setTokenImageInfoBox(null);
                } else {
                    TokenImageInfoBox tiib = new TokenImageInfoBox();
                    tiib.setImageFileName(this.baseImagePath + File.separator + rs.getString(14));
                    tiib.setCoordinateBottom(rs.getInt(12));
                    tiib.setCoordinateTop(rs.getInt(11));
                    tiib.setCoordinateLeft(rs.getInt(9));
                    tiib.setCoordinateRight(rs.getInt(10));
                    retval.setTokenImageInfoBox(tiib);
                }
            }
            s.close();
            conn.close();
        } catch (SQLException ex) {
        }
        return retval;
    }

    public Token getNextNormalTokenByIndex(int indexInDocument) {
        Token retval = null;
        try {
            Connection conn = jcp.getConnection();
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM token WHERE indexInDocument>" + indexInDocument + " AND isNormal=true ORDER BY indexInDocument LIMIT 1");
            if (rs.next()) {
                retval = new Token(rs.getString(4));
                retval.setId(rs.getInt(1));
                retval.setIndexInDocument(rs.getInt(2));
                retval.setOrigID(rs.getInt(3));
                retval.setWCOR(rs.getString(5));
                retval.setIsSuspicious(rs.getBoolean(15));
                retval.setIsCorrected(rs.getBoolean(7));
                retval.setIsNormal(rs.getBoolean(6));
                retval.setNumberOfCandidates(rs.getInt(8));
                retval.setPageIndex(rs.getInt(16));
                retval.setSpecialSeq(SpecialSequenceType.valueOf(rs.getString(13)));
                retval.setTopSuggestion(rs.getString(17));
                retval.setTopCandDLev(rs.getInt(18));

                if (rs.getString(14).equals("")) {
                    retval.setTokenImageInfoBox(null);
                } else {
                    TokenImageInfoBox tiib = new TokenImageInfoBox();
                    tiib.setImageFileName(this.baseImagePath + File.separator + rs.getString(14));
                    tiib.setCoordinateBottom(rs.getInt(12));
                    tiib.setCoordinateTop(rs.getInt(11));
                    tiib.setCoordinateLeft(rs.getInt(9));
                    tiib.setCoordinateRight(rs.getInt(10));
                    retval.setTokenImageInfoBox(tiib);
                }
            }
            s.close();
            conn.close();
        } catch (SQLException ex) {
        }
        return retval;
    }

    public Token getPreviousNormalToken(int tokenID) {
        Token thisT = this.getTokenByID(tokenID);
        Token retval = null;
        try {
            Connection conn = jcp.getConnection();
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM token WHERE indexInDocument<" + thisT.getIndexInDocument() + " AND isNormal=true ORDER BY indexInDocument DESC LIMIT 1");
            if (rs.next()) {
                retval = new Token(rs.getString(4));
                retval.setId(rs.getInt(1));
                retval.setIndexInDocument(rs.getInt(2));
                retval.setOrigID(rs.getInt(3));
                retval.setWCOR(rs.getString(5));
                retval.setIsSuspicious(rs.getBoolean(15));
                retval.setIsCorrected(rs.getBoolean(7));
                retval.setIsNormal(rs.getBoolean(6));
                retval.setNumberOfCandidates(rs.getInt(8));
                retval.setPageIndex(rs.getInt(16));
                retval.setSpecialSeq(SpecialSequenceType.valueOf(rs.getString(13)));
                retval.setTopSuggestion(rs.getString(17));
                retval.setTopCandDLev(rs.getInt(18));

                if (rs.getString(14).equals("")) {
                    retval.setTokenImageInfoBox(null);
                } else {
                    TokenImageInfoBox tiib = new TokenImageInfoBox();
                    tiib.setImageFileName(this.baseImagePath + File.separator + rs.getString(14));
                    tiib.setCoordinateBottom(rs.getInt(12));
                    tiib.setCoordinateTop(rs.getInt(11));
                    tiib.setCoordinateLeft(rs.getInt(9));
                    tiib.setCoordinateRight(rs.getInt(10));
                    retval.setTokenImageInfoBox(tiib);
                }
            }
            s.close();
            conn.close();
        } catch (SQLException ex) {
        }
        return retval;
    }

    public Token getPreviousNormalTokenByIndex(int indexInDocument) {
        Token retval = null;
        try {
            Connection conn = jcp.getConnection();
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM token WHERE indexInDocument<" + indexInDocument + " AND isNormal=true ORDER BY indexInDocument DESC LIMIT 1");
            if (rs.next()) {
                retval = new Token(rs.getString(4));
                retval.setId(rs.getInt(1));
                retval.setIndexInDocument(rs.getInt(2));
                retval.setOrigID(rs.getInt(3));
                retval.setWCOR(rs.getString(5));
                retval.setIsSuspicious(rs.getBoolean(15));
                retval.setIsCorrected(rs.getBoolean(7));
                retval.setIsNormal(rs.getBoolean(6));
                retval.setNumberOfCandidates(rs.getInt(8));
                retval.setPageIndex(rs.getInt(16));
                retval.setSpecialSeq(SpecialSequenceType.valueOf(rs.getString(13)));
                retval.setTopSuggestion(rs.getString(17));
                retval.setTopCandDLev(rs.getInt(18));

                if (rs.getString(14).equals("")) {
                    retval.setTokenImageInfoBox(null);
                } else {
                    TokenImageInfoBox tiib = new TokenImageInfoBox();
                    tiib.setImageFileName(this.baseImagePath + File.separator + rs.getString(14));
                    tiib.setCoordinateBottom(rs.getInt(12));
                    tiib.setCoordinateTop(rs.getInt(11));
                    tiib.setCoordinateLeft(rs.getInt(9));
                    tiib.setCoordinateRight(rs.getInt(10));
                    retval.setTokenImageInfoBox(tiib);
                }
            }
            s.close();
            conn.close();
        } catch (SQLException ex) {
        }
        return retval;
    }

    public Token getPreviousToken(int tokenID) {
        Token thisT = this.getTokenByID(tokenID);
        Token retval = null;
        try {
            Connection conn = jcp.getConnection();
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM token WHERE indexInDocument<" + thisT.getIndexInDocument() + " ORDER BY indexInDocument DESC LIMIT 1");
            if (rs.next()) {
                retval = new Token(rs.getString(4));
                retval.setId(rs.getInt(1));
                retval.setIndexInDocument(rs.getInt(2));
                retval.setOrigID(rs.getInt(3));
                retval.setWCOR(rs.getString(5));
                retval.setIsSuspicious(rs.getBoolean(15));
                retval.setIsCorrected(rs.getBoolean(7));
                retval.setIsNormal(rs.getBoolean(6));
                retval.setNumberOfCandidates(rs.getInt(8));
                retval.setPageIndex(rs.getInt(16));
                retval.setSpecialSeq(SpecialSequenceType.valueOf(rs.getString(13)));
                retval.setTopSuggestion(rs.getString(17));
                retval.setTopCandDLev(rs.getInt(18));

                if (rs.getString(14).equals("")) {
                    retval.setTokenImageInfoBox(null);
                } else {
                    TokenImageInfoBox tiib = new TokenImageInfoBox();
                    tiib.setImageFileName(this.baseImagePath + File.separator + rs.getString(14));
                    tiib.setCoordinateBottom(rs.getInt(12));
                    tiib.setCoordinateTop(rs.getInt(11));
                    tiib.setCoordinateLeft(rs.getInt(9));
                    tiib.setCoordinateRight(rs.getInt(10));
                    retval.setTokenImageInfoBox(tiib);
                }
            }
            s.close();
            conn.close();
        } catch (SQLException ex) {
        }
        return retval;
    }

    public Token getPreviousTokenByIndex(int indexInDocument) {
        Token retval = null;
        try {
            Connection conn = jcp.getConnection();
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM token WHERE indexInDocument<" + indexInDocument + " ORDER BY indexInDocument DESC LIMIT 1");
            if (rs.next()) {
                retval = new Token(rs.getString(4));
                retval.setId(rs.getInt(1));
                retval.setIndexInDocument(rs.getInt(2));
                retval.setOrigID(rs.getInt(3));
                retval.setWCOR(rs.getString(5));
                retval.setIsSuspicious(rs.getBoolean(15));
                retval.setIsCorrected(rs.getBoolean(7));
                retval.setIsNormal(rs.getBoolean(6));
                retval.setNumberOfCandidates(rs.getInt(8));
                retval.setPageIndex(rs.getInt(16));
                retval.setSpecialSeq(SpecialSequenceType.valueOf(rs.getString(13)));
                retval.setTopSuggestion(rs.getString(17));
                retval.setTopCandDLev(rs.getInt(18));

                if (rs.getString(14).equals("")) {
                    retval.setTokenImageInfoBox(null);
                } else {
                    TokenImageInfoBox tiib = new TokenImageInfoBox();
                    tiib.setImageFileName(this.baseImagePath + File.separator + rs.getString(14));
                    tiib.setCoordinateBottom(rs.getInt(12));
                    tiib.setCoordinateTop(rs.getInt(11));
                    tiib.setCoordinateLeft(rs.getInt(9));
                    tiib.setCoordinateRight(rs.getInt(10));
                    retval.setTokenImageInfoBox(tiib);
                }
            }
            s.close();
            conn.close();
        } catch (SQLException ex) {
        }
        return retval;
    }

    public Token getTokenByIndex(int indexInDocument) {
        Token retval = null;
        try {
            Connection conn = jcp.getConnection();
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM token WHERE indexInDocument=" + indexInDocument);
            while (rs.next()) {
                retval = new Token(rs.getString(4));
                retval.setId(rs.getInt(1));
                retval.setIndexInDocument(rs.getInt(2));
                retval.setOrigID(rs.getInt(3));
                retval.setWCOR(rs.getString(5));
                retval.setIsSuspicious(rs.getBoolean(15));
                retval.setIsCorrected(rs.getBoolean(7));
                retval.setIsNormal(rs.getBoolean(6));
                retval.setNumberOfCandidates(rs.getInt(8));
                retval.setPageIndex(rs.getInt(16));
                retval.setSpecialSeq(SpecialSequenceType.valueOf(rs.getString(13)));
                retval.setTopSuggestion(rs.getString(17));
                retval.setTopCandDLev(rs.getInt(18));

                if (rs.getString(14).equals("")) {
                    retval.setTokenImageInfoBox(null);
                } else {
                    TokenImageInfoBox tiib = new TokenImageInfoBox();
                    tiib.setImageFileName(this.baseImagePath + File.separator + rs.getString(14));
                    tiib.setCoordinateBottom(rs.getInt(12));
                    tiib.setCoordinateTop(rs.getInt(11));
                    tiib.setCoordinateLeft(rs.getInt(9));
                    tiib.setCoordinateRight(rs.getInt(10));
                    retval.setTokenImageInfoBox(tiib);
                }
            }
            s.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return retval;
    }

    public Page getPage(int index) {
        Page retval = null;
        try {
            Connection conn = jcp.getConnection();
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT MIN(indexInDocument) as min, MAX(indexInDocument) as max from token WHERE pageIndex=" + index + "AND indexInDocument <> -1");
            if (rs.next()) {
                retval = new Page(index);
                retval.setStartIndex(rs.getInt(1));
                retval.setEndIndex(rs.getInt(2));
                String path = this.getTokenByIndex(rs.getInt(1)).getImageFilename();
                String filename = path.substring(path.lastIndexOf(File.separator) + 1, path.lastIndexOf("."));
                retval.setImageFilename(filename); // this.getTokenByIndex(rs.getInt(1)).getImageFilename());
                retval.setImageCanonical(path);
            }
            s.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return retval;
    }

//    public Page getPage(int index) {
//        Page retval = null;
//        try {
//            Connection conn = jcp.getConnection();
//            Statement s = conn.createStatement();
//            ResultSet rs = s.executeQuery("SELECT * FROM PAGE WHERE INDEX=" + index);
//            while (rs.next()) {
//                retval = new Page(rs.getInt(1));
//                retval.setStartIndex(rs.getInt(2));
//                retval.setEndIndex(rs.getInt(3));
//                retval.setImageFilename(this.baseImagePath + File.separator + rs.getString(4));
//            }
//            s.close();
//            conn.close();
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
//        return retval;
//    }
    public int getNumberOfTokens() {
        if (this.numTokens == 0) {
            this.loadNumberOfTokensFromDB();
        }
        return this.numTokens;
    }

    protected abstract void loadNumberOfTokensFromDB();

    public int getNumberOfPages() {
        if (this.numPages == 0) {
            this.loadNumberOfPagesFromDB();
        }
        return this.numPages;
    }

    protected void loadNumberOfPagesFromDB() {
        try {
            Connection conn = jcp.getConnection();
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT MAX(pageIndex) AS numpages FROM token");
            while (rs.next()) {
                this.numPages = rs.getInt(1) + 1;
            }
            rs.close();
            s.close();
            conn.close();
        } catch (SQLException ex) {
        }
    }

    public MyIterator<Pattern> patternIterator() {
        try {
            return new PatternIterator(jcp.getConnection());
        } catch (SQLException ex) {
            return null;
        }
    }

    public MyIterator<PatternOccurrence> patternOccurrenceIterator(int patternID) {
        try {
            return new PatternOccurrenceIterator(jcp.getConnection(), patternID);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public MyIterator<Page> pageIterator() {
        try {
            return new PageIterator(jcp.getConnection(), this, this.baseImagePath);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public MyIterator<Token> allTokenIterator() {
        try {
            return new TokenIterator(jcp.getConnection());
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * @return TokenIterator positioned at the first token of the document
     */
    public MyIterator<Token> tokenIterator() {
        try {
            return new TokenIterator(jcp.getConnection(), baseImagePath);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * @param page
     * @return TokenIterator for all token of the page, positioned at the first
     */
    public MyIterator<Token> tokenIterator(Page page) {
        try {
            return new TokenIterator(jcp.getConnection(), page, baseImagePath);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public MyIterator<Candidate> candidateIterator(int tokenID) {
        try {
            return new CandidateIterator(jcp.getConnection(), tokenID);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public ArrayList<Integer> deleteToken(int tokenID) throws SQLException {
        Token thisT = this.getTokenByID(tokenID);
        Page page = this.getPage(thisT.getPageIndex());
        int index = thisT.getIndexInDocument();
        Token next = this.getNextTokenByIndex(index);

        if (index == page.getStartIndex()) {
            if (next.getWDisplay().equals(" ")) {
                return this.deleteToken(tokenID, next.getID());
            } else {
                return this.deleteToken(tokenID, tokenID);
            }
        } else if (index == page.getEndIndex() - 1) {
            return this.deleteToken(tokenID, tokenID);
        } else {
            Token prev = this.getPreviousTokenByIndex(index);
            if (prev.getWDisplay().equals(" ") && next.getWDisplay().equals(" ")) {
                return this.deleteToken(tokenID, next.getID());
            } else {
                return this.deleteToken(tokenID, tokenID);
            }
        }
    }

    public abstract ArrayList<Integer> deleteToken(int iDFrom, int iDTo) throws SQLException;

    public abstract ArrayList<Integer> splitToken(int iD, String editString) throws SQLException;

    public void exportAsDocXML(String filename, boolean exportCandidates) {
        new OCRXMLExporter().export(this, filename, exportCandidates);
    }

    public void exportAsPageSeparatedPlaintext(String filename) {
        File f = new File(filename);
        if (!(f.isDirectory() || f.canWrite())) {
            new CustomErrorDialog().showDialog(java.util.ResourceBundle.getBundle("jav/correctionBackend/Bundle").getString("ErrorCantWrite"));
        } else {
            MyIterator<Page> page_iter = this.pageIterator();
            while (page_iter.hasNext()) {
                BufferedWriter writer = null;
                try {
                    Page seite = page_iter.next();
                    writer = new BufferedWriter(new FileWriter(f.getCanonicalPath() + File.separator + seite.getImageFilename() + ".txt"));
                    MyIterator<Token> token_it = this.tokenIterator(seite);
                    while (token_it.hasNext()) {
                        Token t = token_it.next();
                        writer.write(t.getWDisplay());
                    }
                } catch (IOException ex) {
                    new CustomErrorDialog().showDialog(java.util.ResourceBundle.getBundle("jav/correctionBackend/Bundle").getString("IOError"));
                } finally {
                    try {
                        writer.flush();
                        writer.close();
                    } catch (Exception e) {
                        new CustomErrorDialog().showDialog(java.util.ResourceBundle.getBundle("jav/correctionBackend/Bundle").getString("IOError"));
                    }
                }
            }
        }
    }

    public void exportAsPlainText(String filename) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(filename));
            MyIterator<Page> page_iter = this.pageIterator();
            while (page_iter.hasNext()) {
                Page seite = page_iter.next();

                MyIterator<Token> token_it = this.tokenIterator(seite);
                while (token_it.hasNext()) {
                    Token t = token_it.next();
                    writer.write(t.getWDisplay());
                }
                writer.write("\n\n####################################################################################################################\n\n");
            }
        } catch (IOException ex) {
            new CustomErrorDialog().showDialog(java.util.ResourceBundle.getBundle("jav/correctionBackend/Bundle").getString("IOError"));
        } finally {
            try {
                writer.flush();
                writer.close();
            } catch (Exception e) {
                new CustomErrorDialog().showDialog(java.util.ResourceBundle.getBundle("jav/correctionBackend/Bundle").getString("IOError"));
            }
        }
    }

    public ArrayList<Integer> mergeRightward(int iD) throws SQLException {

        Token next = this.getNextToken(iD);
        Token end = null;
        if (next == null) {
            return null;
        }

        boolean skipSpace = false;
        // decide if immediate neighbour should be skipped, 
        // e.g. if it contains just whitespace
        if (next.getWDisplay().equals(" ")) {
            end = this.getNextToken(next.getID());
            if (end == null) {
                try {
                    // delete whitespace at end of document (token after whitespace == null)
                    this.deleteToken(next.getID());
                } catch (SQLException ex) {
                }
                return null;
            }
            skipSpace = true;
        }

        return this.mergeRightward(iD, (skipSpace ? 2 : 1));
    }

    public abstract ArrayList<Integer> mergeRightward(int iD, int numToMerge) throws SQLException;

    public void setSuspicious(int tokenID, String val) {
        try {
            Connection conn = this.jcp.getConnection();
            PreparedStatement prep = conn.prepareStatement("UPDATE token SET isSuspicious=? WHERE tokenID=?");
            prep.setString(1, val);
            prep.setInt(2, tokenID);
            prep.addBatch();
            prep.executeBatch();
            prep.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void setNormal(int tokenID, String val) {
        try {
            Connection conn = this.jcp.getConnection();
            PreparedStatement prep = conn.prepareStatement("UPDATE token SET isNormal=? WHERE tokenID=?");
            prep.setString(1, val);
            prep.setInt(2, tokenID);
            prep.addBatch();
            prep.executeBatch();
            prep.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void setTopSuggestion(int tokenID, String val) {
        try {
            Connection conn = this.jcp.getConnection();
            PreparedStatement prep = conn.prepareStatement("UPDATE token SET topSuggestion=? WHERE tokenID=?");
            prep.setString(1, val);
            prep.setInt(2, tokenID);
            prep.addBatch();
            prep.executeBatch();
            prep.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void setTopCandDLev(int tokenID, int val) {
        try {
            Connection conn = this.jcp.getConnection();
            PreparedStatement prep = conn.prepareStatement("UPDATE token SET topCandDLev=? WHERE tokenID=?");
            prep.setInt(1, val);
            prep.setInt(2, tokenID);
            prep.addBatch();
            prep.executeBatch();
            prep.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void setNumCandidates(int tokenID, int num) {
        try {
            Connection conn = this.jcp.getConnection();
            PreparedStatement prep = conn.prepareStatement("UPDATE token SET numCands=? WHERE tokenID=?");
            prep.setInt(1, num);
            prep.setInt(2, tokenID);
            prep.addBatch();
            prep.executeBatch();
            prep.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

//    public void addToUndoRedo(int id, int part, String type, String edit_type, String sql) {
//        try {
//            Connection conn = this.jcp.getConnection();
//            PreparedStatement prep = conn.prepareStatement(" INSERT INTO undoredo VALUES( ?,?,?,?,? )");
//            prep.setInt(1, id);
//            prep.setInt(2, part);
//            prep.setString(3, type);
//            prep.setString(4, edit_type);
//            prep.setString(5, sql);
//
//            prep.addBatch();
//            prep.executeBatch();
//            prep.close();
//            conn.close();
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
//    }
    public boolean setCorrected(int tokenID, boolean b) throws SQLException {

        Connection conn = null;
        PreparedStatement setcor = null;
        PreparedStatement undo_redo = null;

        try {
            conn = jcp.getConnection();
            conn.setAutoCommit(false);

            setcor = conn.prepareStatement("UPDATE token SET isCorrected=? WHERE tokenID=?");
            setcor.setBoolean(1, b);
            setcor.setInt(2, tokenID);
            setcor.executeUpdate();

            undo_redo = conn.prepareStatement("INSERT INTO undoredo VALUES( ?,?,?,?,? )");
            if (b == true) {
                undo_redo.setInt(1, undo_redo_id);
                undo_redo.setInt(2, undo_redo_part);
                undo_redo.setString(3, "undo");
                undo_redo.setString(4, MyEditType.CORRECTED.toString());
                undo_redo.setString(5, "UPDATE token SET isCorrected=false WHERE tokenID=" + tokenID);
                undo_redo.addBatch();

                undo_redo.setInt(1, undo_redo_id);
                undo_redo.setInt(2, undo_redo_part);
                undo_redo.setString(3, "redo");
                undo_redo.setString(4, MyEditType.CORRECTED.toString());
                undo_redo.setString(5, "UPDATE token SET isCorrected=true WHERE tokenID=" + tokenID);
                undo_redo.addBatch();
            } else {
                undo_redo.setInt(1, undo_redo_id);
                undo_redo.setInt(2, undo_redo_part);
                undo_redo.setString(3, "undo");
                undo_redo.setString(4, MyEditType.CORRECTED.toString());
                undo_redo.setString(5, "UPDATE token SET isCorrected=true WHERE tokenID=" + tokenID);
                undo_redo.addBatch();

                undo_redo.setInt(1, undo_redo_id);
                undo_redo.setInt(2, undo_redo_part);
                undo_redo.setString(3, "redo");
                undo_redo.setString(4, MyEditType.CORRECTED.toString());
                undo_redo.setString(5, "UPDATE token SET isCorrected=false WHERE tokenID=" + tokenID);
                undo_redo.addBatch();
            }
            undo_redo.executeBatch();

            undo_redo_id++;
            undo_redo_part = 0;

            conn.commit();
            return true;
        } catch (SQLException ex) {
            if (conn != null) {
                conn.rollback();
            }
            return false;
        } finally {
            if (setcor != null) {
                setcor.close();
            }
            if (undo_redo != null) {
                undo_redo.close();
            }
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    public boolean setCorrected(ArrayList<Integer> tokenIDs, boolean b) throws SQLException {
        Connection conn = null;
        PreparedStatement setcor = null;
        PreparedStatement undo_redo = null;

        try {
            conn = jcp.getConnection();
            conn.setAutoCommit(false);

            undo_redo = conn.prepareStatement("INSERT INTO undoredo VALUES( ?,?,?,?,? )");
            setcor = conn.prepareStatement("UPDATE token SET isCorrected=? WHERE tokenID=?");

            Iterator<Integer> id_it = tokenIDs.iterator();
            while (id_it.hasNext()) {
                int tokenID = id_it.next();

                setcor.setBoolean(1, b);
                setcor.setInt(2, tokenID);
                setcor.addBatch();

                if (b == true) {
                    undo_redo.setInt(1, undo_redo_id);
                    undo_redo.setInt(2, undo_redo_part);
                    undo_redo.setString(3, "undo");
                    undo_redo.setString(4, MyEditType.CORRECTED.toString());
                    undo_redo.setString(5, "UPDATE token SET isCorrected=false WHERE tokenID=" + tokenID);
                    undo_redo.addBatch();

                    undo_redo.setInt(1, undo_redo_id);
                    undo_redo.setInt(2, undo_redo_part);
                    undo_redo.setString(3, "redo");
                    undo_redo.setString(4, MyEditType.CORRECTED.toString());
                    undo_redo.setString(5, "UPDATE token SET isCorrected=true WHERE tokenID=" + tokenID);
                    undo_redo.addBatch();
                } else {
                    undo_redo.setInt(1, undo_redo_id);
                    undo_redo.setInt(2, undo_redo_part);
                    undo_redo.setString(3, "undo");
                    undo_redo.setString(4, MyEditType.CORRECTED.toString());
                    undo_redo.setString(5, "UPDATE token SET isCorrected=true WHERE tokenID=" + tokenID);
                    undo_redo.addBatch();

                    undo_redo.setInt(1, undo_redo_id);
                    undo_redo.setInt(2, undo_redo_part);
                    undo_redo.setString(3, "redo");
                    undo_redo.setString(4, MyEditType.CORRECTED.toString());
                    undo_redo.setString(5, "UPDATE token SET isCorrected=false WHERE tokenID=" + tokenID);
                    undo_redo.addBatch();
                }
                undo_redo_part++;
            }

            setcor.executeBatch();
            undo_redo.executeBatch();

            undo_redo_id++;
            undo_redo_part = 0;

            conn.commit();
            return true;
        } catch (SQLException ex) {
            if (conn != null) {
                conn.rollback();
            }
            return false;
        } finally {
            if (setcor != null) {
                setcor.close();
            }
            if (undo_redo != null) {
                undo_redo.close();
            }
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    public HashMap<String, OCRErrorInfo> computeErrorFreqList() {
        HashMap<String, OCRErrorInfo> freqList = new HashMap<>();
        Iterator<Token> it = this.tokenIterator();
        while (it.hasNext()) {
            Token tok = it.next();
            if (!tok.isCorrected() && tok.isSuspicious() && (tok.getWOCR().length() > 3) && tok.isNormal()) {
                String tokString = tok.getWOCR();
                if (!freqList.containsKey(tokString)) {
                    freqList.put(tokString, new OCRErrorInfo(1));
                } else {
                    freqList.get(tokString).addOccurence();
                }
            }
        }
        return freqList;
    }

    public boolean checkImageFiles() {
        boolean retval = true;
        try {
            Connection conn = this.jcp.getConnection();
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT DISTINCT imageFile FROM token");
            while (rs.next()) {
                File f = new File(this.baseImagePath + File.separator + rs.getString(1));
                if (!f.exists()) {
                    return false;
                }
            }
            rs.close();
            s.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return retval;
    }

    public void setHasImages(boolean b) {
        this.hasImages = b;
    }

    public boolean getHasImages() {
        return this.hasImages;
    }

    public void setProjectFilename(String s) {
        this.propertiespath = s;
    }

    public String getProjectFilename() {
        return this.propertiespath;

    }
}

class TokenIterator implements MyIterator<Token> {

    private String baseImagePath = "";
    private Connection conn;
    private Statement s;
    private ResultSet rs = null;

    protected TokenIterator(Connection c) {
        try {
            conn = c;
            s = conn.createStatement();
            rs = s.executeQuery("SELECT * FROM TOKEN ORDER BY indexInDocument ASC");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    protected TokenIterator(Connection c, String i) {
        try {
            baseImagePath = i;
            conn = c;
            s = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = s.executeQuery("SELECT * FROM TOKEN WHERE indexInDocument >= 0 ORDER BY indexInDocument ASC");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    protected TokenIterator(Connection c, Page p, String i) {
        if (p.getStartIndex() == p.getEndIndex()) {
            rs = null;
        } else {
            try {
                baseImagePath = i;
                conn = c;
                s = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                rs = s.executeQuery("SELECT * FROM TOKEN WHERE indexInDocument >=" + p.getStartIndex() + " AND indexInDocument <=" + p.getEndIndex() + " ORDER BY indexInDocument ASC");
            } catch (SQLException ex) {
                Logger.getLogger(TokenIterator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public boolean hasNext() {
        if (rs == null) {
            return false;
        } else {
            try {
                if (rs.next()) {
                    return true;
                } else {
                    rs.close();
                    s.close();
                    conn.close();
                    return false;
                }
            } catch (SQLException ex) {
                return false;
            }
        }
    }

    @Override
    public Token next() {
        Token retval = null;
        try {
            retval = new Token(rs.getString(4));
            retval.setId(rs.getInt(1));
            retval.setIndexInDocument(rs.getInt(2));
            retval.setOrigID(rs.getInt(3));
            retval.setWCOR(rs.getString(5));
            retval.setIsSuspicious(rs.getBoolean(15));
            retval.setIsCorrected(rs.getBoolean(7));
            retval.setIsNormal(rs.getBoolean(6));
            retval.setNumberOfCandidates(rs.getInt(8));
            retval.setPageIndex(rs.getInt(16));
            retval.setSpecialSeq(SpecialSequenceType.valueOf(rs.getString(13)));
            retval.setTopSuggestion(rs.getString(17));
            retval.setTopCandDLev(rs.getInt(18));

            if (rs.getString(14).equals("")) {
                retval.setTokenImageInfoBox(null);
            } else {
                TokenImageInfoBox tiib = new TokenImageInfoBox();
                tiib.setImageFileName(this.baseImagePath + File.separator + rs.getString(14));
                tiib.setCoordinateBottom(rs.getInt(12));
                tiib.setCoordinateTop(rs.getInt(11));
                tiib.setCoordinateLeft(rs.getInt(9));
                tiib.setCoordinateRight(rs.getInt(10));
                retval.setTokenImageInfoBox(tiib);
            }
        } catch (SQLException ex) {
            retval = null;
        }
        return retval;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void reset() {
        try {
            rs.beforeFirst();
        } catch (SQLException ex) {
            ex.printStackTrace();
            Logger.getLogger(CandidateIterator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void cancel() {
        try {
            rs.close();
            s.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}

class CandidateIterator implements MyIterator<Candidate> {

    private Statement s;
    private Connection conn;
    private ResultSet rs = null;

    protected CandidateIterator(Connection c, int tokenID) {
        try {
            conn = c;
            s = conn.createStatement();
            rs = s.executeQuery("SELECT * FROM candidate WHERE tokenID=" + tokenID + " ORDER BY rank ASC");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean hasNext() {
        if (rs == null) {
            return false;
        } else {
            try {
                if (rs.next()) {
                    return true;
                } else {
                    rs.close();
                    s.close();
                    conn.close();
                    return false;
                }
            } catch (SQLException ex) {
                return false;
            }
        }
    }

    @Override
    public Candidate next() {
        Candidate retval = null;
        try {
            retval = new Candidate(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getDouble(5), rs.getInt(6));
        } catch (SQLException ex) {
            retval = null;
        }
        return retval;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void reset() {
//        try {
//            rs.beforeFirst();
//        } catch (SQLException ex) {
//            Logger.getLogger(CandidateIterator.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    public void cancel() {
    }
}

class PageIterator implements MyIterator<Page> {

    private Connection conn;
    private Document doc;
    private String baseImgPath;
    private ResultSet rs = null;
    private Statement s;

    protected PageIterator(Connection c, Document d, String path) {
        try {
            doc = d;
            conn = c;
            baseImgPath = path;
            s = conn.createStatement();
            rs = s.executeQuery("SELECT pageIndex, MIN(indexInDocument) as min, MAX(indexInDocument) as max from token WHERE indexInDocument <> -1 GROUP BY pageIndex");
        } catch (SQLException ex) {
            Logger.getLogger(TokenIterator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean hasNext() {
        if (rs == null) {
            return false;
        } else {
            try {
                if (rs.next()) {
                    return true;
                } else {
                    rs.close();
                    s.close();
                    conn.close();
                    return false;
                }
            } catch (SQLException ex) {
                return false;
            }
        }
    }

    @Override
    public Page next() {
        Page retval = null;
        try {
            retval = new Page(rs.getInt(1));
            retval.setStartIndex(rs.getInt(2));
            retval.setEndIndex(rs.getInt(3));
            String path = doc.getTokenByIndex(rs.getInt(2)).getImageFilename();
            String filename = path.substring(path.lastIndexOf(File.separator) + 1, path.lastIndexOf("."));
            retval.setImageFilename(filename); // this.getTokenByIndex(rs.getInt(1)).getImageFilename());
            retval.setImageCanonical(path);
        } catch (SQLException ex) {
        }
        return retval;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void reset() {
//        try {
//            rs.beforeFirst();
//        } catch (SQLException ex) {
//            Logger.getLogger(CandidateIterator.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    public void cancel() {
    }
}

class PatternIterator implements MyIterator<Pattern> {

    private ResultSet rs = null;
    private Statement s;
    private Connection conn;

    protected PatternIterator(Connection c) {
        try {
            conn = c;
            s = conn.createStatement();
            rs = s.executeQuery("SELECT * FROM PATTERN ORDER BY freq DESC");
        } catch (SQLException ex) {
            Logger.getLogger(TokenIterator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean hasNext() {
        if (rs == null) {
            return false;
        } else {
            try {
                if (rs.next()) {
                    return true;
                } else {
                    rs.close();
                    s.close();
                    conn.close();
                    return false;
                }
            } catch (SQLException ex) {
                return false;
            }
        }
    }

    @Override
    public Pattern next() {
        Pattern retval = null;
        try {
            retval = new Pattern(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getInt(5));
        } catch (SQLException ex) {
            retval = null;
        }
        return retval;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void reset() {
//        try {
//            rs.beforeFirst();
//        } catch (SQLException ex) {
//            Logger.getLogger(CandidateIterator.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    @Override
    public void cancel() {
    }
}

class PatternOccurrenceIterator implements MyIterator<PatternOccurrence> {

    private ResultSet rs = null;
    private Statement s;
    private Connection conn;

    protected PatternOccurrenceIterator(Connection c, int patternID) {
        try {
            conn = c;
            s = conn.createStatement();
            rs = s.executeQuery("SELECT * FROM PATTERNOCCURRENCE WHERE patternID=" + patternID + " ORDER BY freq ASC");
        } catch (SQLException ex) {
            Logger.getLogger(TokenIterator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean hasNext() {
        if (rs == null) {
            return false;
        } else {
            try {
                if (rs.next()) {
                    return true;
                } else {
                    rs.close();
                    s.close();
                    conn.close();
                    return false;
                }
            } catch (SQLException ex) {
                return false;
            }
        }
    }

    @Override
    public PatternOccurrence next() {
        PatternOccurrence retval = null;
        try {
            retval = new PatternOccurrence(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getInt(5), rs.getInt(6));
        } catch (SQLException ex) {
        }
        return retval;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void reset() {
//        try {
//            rs.beforeFirst();
//        } catch (SQLException ex) {
//            Logger.getLogger(CandidateIterator.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    @Override
    public void cancel() {
    }
}
