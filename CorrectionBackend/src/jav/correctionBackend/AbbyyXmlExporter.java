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
class AbbyyXmlExporter extends BaseXmlExporter {

    public AbbyyXmlExporter(File src, File dest, Document document) {
        super(src, dest, document);
    }
    
    @Override
    public void export() throws IOException {
        Files.copy(getSourceFile().toPath(), getDestinationFile().toPath());
    }
    
}
