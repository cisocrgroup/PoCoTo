/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import java.io.File;
import java.util.ArrayList;

/**
 * A list of pages. It is called `book` to avoid confusion with `document` in
 * package jav.correctionBackend
 *
 * @author flo
 */
public class Book extends ArrayList<Page> {

    private final File imageDir, ocrDir;

    public Book(File imageDir, File ocrDir) {
        if (imageDir == null) {
            this.imageDir = new File("");
        } else {
            this.imageDir = imageDir;
        }
        if (ocrDir == null) {
            this.ocrDir = new File("");
        } else {
            this.ocrDir = ocrDir;
        }
    }

    public Book() {
        this(null, null);
    }

    public File getOcrDir() {
        return ocrDir;
    }

    public File getImageDir() {
        return imageDir;
    }

}
