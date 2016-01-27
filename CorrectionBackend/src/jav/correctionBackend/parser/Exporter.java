/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import jav.correctionBackend.Document;
import jav.correctionBackend.Page;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author finkf
 */
public class Exporter {

    private final File dest, src;
    private final PageParser pageParser;

    public Exporter(File src, File dest, PageParser pageParser) {
        assert (dest != null);
        assert (src != null);
        assert (pageParser != null);
        this.src = src;
        this.dest = dest;
        this.pageParser = pageParser;
    }

    public final File getDestinationFile() {
        return dest;
    }

    public final File getSourceFile() {
        return src;
    }

    public void export(Document document) throws IOException, Exception {
        Page page = document.getPage(src);
        PageLineReader lineReader = new PageLineReader(page, document);
        DocumentCorrector corrector = new DocumentCorrectorImpl(src, pageParser);
        corrector.correctThisDocumentWith(lineReader);
        corrector.write(dest);
    }

}
