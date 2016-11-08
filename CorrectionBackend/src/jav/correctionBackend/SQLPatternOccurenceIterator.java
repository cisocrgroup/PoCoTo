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
class SQLPatternOccurrenceIterator extends SQLIterator<PatternOccurrence> {

    private static Iterator<PatternOccurrence> getIterator(Connection c, int patternID) throws SQLException {
        ResultSet res = c.createStatement().executeQuery("SELECT * FROM PATTERNOCCURRENCE WHERE patternID=" + patternID + " ORDER BY freq ASC");

        ArrayList<PatternOccurrence> occs = new ArrayList<>();
        while (res.next()) {
            occs.add(
                    new PatternOccurrence(
                            res.getInt(1),
                            res.getInt(2),
                            res.getString(3),
                            res.getString(4),
                            res.getInt(5),
                            res.getInt(6)
                    )
            );
        }
        return occs.iterator();
    }

    public SQLPatternOccurrenceIterator(Connection c, int patternID) throws SQLException {
        super(getIterator(c, patternID));
    }
}
