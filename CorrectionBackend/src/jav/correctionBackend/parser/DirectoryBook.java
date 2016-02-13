/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import jav.correctionBackend.FileType;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author flo
 */
public class DirectoryBook extends Book {

    private final FileType fileType;
    private final OcrToImageFileMapping mapping;

    public DirectoryBook(File imagedir, File ocrdir, FileType fileType) throws IOException {
        super(imagedir, ocrdir);
        this.fileType = fileType;
        mapping = new OcrToImageFileMapping(imagedir, ocrdir, fileType.getFilenameFilter());
    }

    @Override
    public int getNumberOfPages() {
        return mapping.length();
    }

    @Override
    public Page getPageAt(int i) {
        return null;
    }
}
