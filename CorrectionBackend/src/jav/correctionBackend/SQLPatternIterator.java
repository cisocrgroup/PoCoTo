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
class SQLPatternIterator extends SQLIterator<Pattern> {

    private static Iterator<Pattern> getIterator(Connection c) throws SQLException {
        try (ResultSet res = c.createStatement().executeQuery("SELECT * FROM PATTERN ORDER BY freq DESC")) {
            ArrayList<Pattern> patterns = new ArrayList<>();
            while (res.next()) {
                patterns.add(
                        new Pattern(
                                res.getInt(1),
                                res.getString(2),
                                res.getString(3),
                                res.getInt(4),
                                res.getInt(5)
                        )
                );
            }
            return patterns.iterator();
        }
    }

    public SQLPatternIterator(Connection c) throws SQLException {
        super(getIterator(c));
    }
}
