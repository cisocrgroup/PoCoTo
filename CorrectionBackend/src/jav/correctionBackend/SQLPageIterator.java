/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend;

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
                Page page = new Page(res.getInt(1));
                page.setStartIndex(res.getInt(2));
                page.setEndIndex(res.getInt(3));
                String path = getPath(d, res.getInt(2), res.getInt(3));
                if (path == null || path.length() <= 0) { // skip garbled tokens with no image file
                    continue;
                }
                int f = path.lastIndexOf(File.separator) + 1; // allways >=0
                int t = path.lastIndexOf(".");
                if (t < f) {
                    t = path.length();
                }
                String filename = path.substring(f, t);
                page.setImageFilename(filename); // this.getTokenByIndex(rs.getInt(1)).getImageFilename());
                page.setImageCanonical(path);
                pages.add(page);
            }
            return pages.iterator();
        }
    }

    private static String getPath(Document d, int minIdx, int maxIdx) {
        for (int i = minIdx; i <= maxIdx; i++) {
            Token token = d.getTokenByIndex(i);
            if (token != null) {
                String path = token.getImageFilename();
                if (path != null && path.length() > 0) {
                    return path;
                }
            }
        }
        return "";
    }

    public SQLPageIterator(Connection c, Document d, String path) throws SQLException {
        super(getIterator(c, d));
        doc = d;
        baseImgPath = path;
    }
}
