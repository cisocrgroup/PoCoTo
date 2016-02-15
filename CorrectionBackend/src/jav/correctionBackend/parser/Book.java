/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import java.io.File;

/**
 * A list of pages. It is called `book` to avoid confusion with `document` in
 * package jav.correctionBackend
 *
 * @author flo
 */
public abstract class Book {

    private static final File EMPTY_FILE = new File("");

    private final File imageDir, ocrDir;

    public Book(File imageDir, File ocrDir) {
        if (imageDir == null) {
            this.imageDir = EMPTY_FILE;
        } else {
            this.imageDir = imageDir;
        }
        if (ocrDir == null) {
            this.ocrDir = EMPTY_FILE;
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

    /**
     * Write the book to an output file or directory
     *
     * @param out The path to the output file or directory
     * @throws Exception on any error
     */
    public abstract void write(File out) throws Exception;

    /**
     * Get the total number of pages of this book.
     *
     * @return the number of pages
     */
    public abstract int getNumberOfPages();

    /**
     * Get the page at index i.
     *
     * @param i the index of the page
     * @return the page at index i
     */
    public abstract Page getPageAt(int i);

}
