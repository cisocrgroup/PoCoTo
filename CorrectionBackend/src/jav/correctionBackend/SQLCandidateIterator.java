/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author finkf
 */
class SQLCandidateIterator extends SQLIterator<Candidate> {

    private static Iterator<Candidate> getIterator(Connection c, int id) throws SQLException {
        try (ResultSet res = c.createStatement().executeQuery("SELECT * FROM candidate WHERE tokenID=" + id + " ORDER BY rank ASC")) {
            ArrayList<Candidate> candidates = new ArrayList<>();
            while (res.next()) {
                candidates.add(
                        new Candidate(
                                res.getInt(1),
                                res.getInt(2),
                                res.getString(3),
                                res.getString(4),
                                res.getDouble(5),
                                res.getInt(6)
                        )
                );
            }
            return candidates.iterator();
        }
    }

    public SQLCandidateIterator(Connection c, int tokenID) throws SQLException {
        super(getIterator(c, tokenID));
    }
}
