/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend;

import jav.logging.log4j.Log;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author finkf
 */
class SQLPageIterator extends SQLIterator<Page> {

    private Document doc;
    private String baseImgPath;

    private static Iterator<Page> getIterator(Connection c, Document d) throws SQLException {
        try (ResultSet res = c.createStatement().executeQuery("SELECT pageIndex, MIN(indexInDocument) as min, MAX(indexInDocument) as max from token WHERE indexInDocument <> -1 GROUP BY pageIndex ORDER BY pageIndex")) {
            ArrayList<Page> pages = new ArrayList<>();
            while (res.next()) {
                Page page = Page.fromTokenIndexRange(d, res.getInt(1), res.getInt(2), res.getInt(3));
                if (!page.hasImage()) {
                    Log.info(SQLPageIterator.class, "skipping page index %d: missing image path", page.getIndex());
                    continue;
                }
                String path = page.getImageCanonical();
                int f = path.lastIndexOf(File.separator) + 1; // allways >=0
                int t = path.lastIndexOf(".");
                if (t < f) {
                    t = path.length();
                }
                String filename = path.substring(f, t);
                page.setImageFilename(filename);
                page.setImageCanonical(path);
                pages.add(page);
            }
            return pages.iterator();
        }
    }

    public SQLPageIterator(Connection c, Document d, String path) throws SQLException {
        super(getIterator(c, d));
        doc = d;
        baseImgPath = path;
    }
}
