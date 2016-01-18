/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

import jav.correctionBackend.Document;
import java.io.File;

/**
 *
 * @author finkf
 */
public class AbbyyXmlExporter extends Exporter {

    public AbbyyXmlExporter(File src, File dest, Document document) {
        super(src, dest, document);
    }

    @Override
    protected DocumentCorrector getCorrector() throws Exception {
        return new DocumentCorrectorImpl(
                getSourceFile(),
                getDestinationFile(),
                new AbbyyXmlPageParser()
        );
    }
}
