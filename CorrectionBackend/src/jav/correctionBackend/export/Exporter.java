/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

import jav.correctionBackend.Document;
import jav.correctionBackend.Page;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author finkf
 */
public abstract class Exporter {

    private final File src;
    private final File dest;
    private final Document document;

    public Exporter(File src, File dest, Document document) {
        assert (src != null);
        assert (dest != null);
        assert (document != null);
        this.src = src;
        this.dest = dest;
        this.document = document;
    }

    public final File getSourceFile() {
        return src;
    }

    public final File getDestinationFile() {
        return dest;
    }

    public final Document getDocument() {
        return document;
    }

    public void export() throws IOException, Exception {
        Page page = document.getPage(src);
        DocumentCorrector corrector = getCorrector();
        PageLineReader lineReader = new PageLineReader(page, document);
        corrector.correctThisDocumentWith(lineReader);
        corrector.write();
    }

    protected abstract DocumentCorrector getCorrector() throws Exception;

}
