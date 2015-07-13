/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 *
 * @author finkf
 */
class BaseXmlExporter {
    private final File src;
    private final File dest;
    private final Document document;

    public BaseXmlExporter(File src, File dest, Document document) {
        assert(src != null);
        assert(dest != null);
        assert(document != null);
        this.src = src;
        this.dest = dest;
        this.document = document;
    }
    
    public File getSourceFile() {
        return src;
    }
    
    public File getDestinationFile() {
        return dest;
    }
    
    public Document getDocument() {
        return document;
    }

    void export() throws IOException {
        Files.copy(src.toPath(), dest.toPath());
    }
    
}
