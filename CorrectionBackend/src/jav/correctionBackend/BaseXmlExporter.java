/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend;

import java.io.File;
import java.io.IOException;
import org.xml.sax.SAXException;

/**
 *
 * @author finkf
 */
abstract class BaseXmlExporter {
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
    
    public final File getSourceFile() {
        return src;
    }
    
    public final File getDestinationFile() {
        return dest;
    }
    
    public final Document getDocument() {
        return document;
    }

    public abstract void export() throws IOException, Exception;
    
}
