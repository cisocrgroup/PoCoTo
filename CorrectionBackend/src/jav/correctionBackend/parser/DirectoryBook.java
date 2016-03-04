/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import jav.correctionBackend.FileType;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author flo
 */
public class DirectoryBook extends Book {

    private final FileType fileType;
    private final OcrToImageFileMapping mappings;
    private final ArrayList<Page> pages;

    public DirectoryBook(File imagedir, File ocrdir, FileType fileType) throws IOException {
        super(imagedir, ocrdir);
        this.fileType = fileType;
        mappings = new OcrToImageFileMapping(imagedir, ocrdir, fileType.getFilenameFilter());
        pages = new ArrayList<>(mappings.size());
    }

    @Override
    public int getNumberOfPages() {
        return mappings.size();
    }

    @Override
    public Page getPageAt(int i) {
        if (pages.get(i) == null) {
            pages.set(i, parsePage(i));
        }
        return pages.get(i);
    }

    private Page parsePage(int i) {
        PageParser pageParser = fileType.getPageParser();
        pageParser.setImageFile(mappings.get(i).imagefile);
        pageParser.setOcrFile(mappings.get(i).ocrfile);
        try {
            return pageParser.parse();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(File out) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
