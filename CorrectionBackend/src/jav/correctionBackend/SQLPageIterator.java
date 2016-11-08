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
        ArrayList<Page> pages = new ArrayList<>();
        ResultSet res = c.createStatement().executeQuery("SELECT pageIndex, MIN(indexInDocument) as min, MAX(indexInDocument) as max from token WHERE indexInDocument <> -1 GROUP BY pageIndex ORDER BY pageIndex");
        while (res.next()) {
            Page page = new Page(res.getInt(1));
            page.setStartIndex(res.getInt(2));
            page.setEndIndex(res.getInt(3));
            String path = d.getTokenByIndex(res.getInt(2)).getImageFilename();
            String filename = path.substring(path.lastIndexOf(File.separator) + 1, path.lastIndexOf("."));
            page.setImageFilename(filename); // this.getTokenByIndex(rs.getInt(1)).getImageFilename());
            page.setImageCanonical(path);
            pages.add(page);
        }
        return pages.iterator();
    }

    public SQLPageIterator(Connection c, Document d, String path) throws SQLException {
        super(getIterator(c, d));
        doc = d;
        baseImgPath = path;
    }
}
