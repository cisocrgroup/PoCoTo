/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author flo
 */
public class Page extends ArrayList<Paragraph> {

    private final static File EMPTY_FILE = new File("");

    private final File image, ocr;

    public Page(File image, File ocr) {
        if (image == null) {
            this.image = EMPTY_FILE;
        } else {
            this.image = image;
        }

        if (ocr == null) {
            this.ocr = EMPTY_FILE;
        } else {
            this.ocr = ocr;
        }

    }

    public Page() {
        this(null, null);
    }

    public File getImageFile() {
        return image;
    }

    public File getOcrFile() {
        return ocr;
    }

}
