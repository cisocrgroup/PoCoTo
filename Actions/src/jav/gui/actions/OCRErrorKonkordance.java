package jav.gui.actions;

import jav.correctionBackend.OCRXMLImporter;
import jav.gui.filter.LevDistance_1_Filter;
import jav.gui.main.MainController;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import org.openide.util.Exceptions;

/**
 *Copyright (c) 2012, IMPACT working group at the Centrum für Informations- und Sprachverarbeitung, University of Munich.
 *All rights reserved.

 *Redistribution and use in source and binary forms, with or without
 *modification, are permitted provided that the following conditions are met:

 *Redistributions of source code must retain the above copyright
 *notice, this list of conditions and the following disclaimer.
 *Redistributions in binary form must reproduce the above copyright
 *notice, this list of conditions and the following disclaimer in the
 *documentation and/or other materials provided with the distribution.

 *THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 *IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 *TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 *PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This file is part of the ocr-postcorrection tool developed
 * by the IMPACT working group at the Centrum für Informations- und Sprachverarbeitung, University of Munich.
 * For further information and contacts visit http://ocr.cis.uni-muenchen.de/
 * 
 * @author thorsten (thorsten.vobl@googlemail.com)
 */
public final class OCRErrorKonkordance implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        if( MainController.findInstance().getDocument() != null) {
            try {
                File tempFile = File.createTempFile("document", ".ocrcxml");
                tempFile.deleteOnExit();

                MainController.findInstance().getDocument().exportAsDocXML(tempFile.getCanonicalPath(), false);

                MainController.findInstance().getCorrectionSystem().clearCandidates();
//                new OCRXMLImporter().importCandidates(MainController.findInstance().getDocument(), "C:\\Users\\Scampi_Joe\\AppData\\Local\\Temp\\document38524588653394750.ocrcxml");
    //            LevDistance_1_Filter f = new LevDistance_1_Filter("OCRErrors");
    //            MainController.findInstance().applyFilter(f);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
