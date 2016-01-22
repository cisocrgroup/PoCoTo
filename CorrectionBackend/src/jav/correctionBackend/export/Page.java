/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author flo
 */
public class Page extends ArrayList<Paragraph> {

    private final File image, ocr;

    public Page(File image, File ocr) {
        this.image = image;
        this.ocr = ocr;
    }

    public File getImageFile() {
        return image;
    }

    public File getOcrFile() {
        return ocr;
    }
}
