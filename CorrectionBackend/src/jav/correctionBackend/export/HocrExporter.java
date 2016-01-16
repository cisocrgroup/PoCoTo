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
public class HocrExporter extends Exporter {

    public HocrExporter(File src, File dest, Document document) {
        super(src, dest, document);
    }

    @Override
    protected DocumentCorrector getCorrector() throws Exception {
        throw new RuntimeException("Not implemented yet");
        //return new AbbyyXmlCorrector(getSourceFile(), getDestinationFile());
    }
}
