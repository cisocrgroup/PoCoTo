
package jav.correctionBackend;

import java.io.*;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

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
 * @author anna
 */
public class SimpleXmlExporter {
        
    public SimpleXmlExporter() {
        
    }
    
    public void export(Document doc, String outfile, boolean exportCandidates) {
        try {
                Writer w = new OutputStreamWriter(new FileOutputStream(outfile), "UTF8");
                BufferedWriter out = new BufferedWriter(w);                
                export(doc, out, exportCandidates);
        } catch (IOException ex) {
                Logger.getLogger(OcrXmlExporter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void export(Document doc, BufferedWriter out, boolean exportCandidates) throws IOException {
        out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        out.write("<document>\n");
        Iterator<Page> pageIt = doc.pageIterator();
        while (pageIt.hasNext()) {
            Page page = pageIt.next();
            Iterator<Token> tokenIt = doc.tokenIterator(page);
            
            while (tokenIt.hasNext()) {
                Token token = tokenIt.next();
                SpecialSequenceType sst = token.getSpecialSeq();
                if (!sst.equals(SpecialSequenceType.SPACE) && !sst.equals(SpecialSequenceType.NEWLINE)) {
                    out.write(" <token id=\"" + token.getID() + "\" wOCR=\"" + token.getWCOR() + "\"/>\n");
                }
            }
        }
        out.write("</document>\n");
    }
}
