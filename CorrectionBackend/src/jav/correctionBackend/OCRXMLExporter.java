package jav.correctionBackend;

import java.io.*;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringEscapeUtils;

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
public class OCRXMLExporter {

    public OCRXMLExporter() {
    }

    public void export(Document doc, String filename, boolean exportCandidates) {
        try {
            int index = -1;
            Writer w = new OutputStreamWriter(new FileOutputStream(filename), "UTF8");
            BufferedWriter out = new BufferedWriter(w);

            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            out.write("<document>\n");

            Iterator<Page> pages = doc.pageIterator();

            while (pages.hasNext()) {
                Page p = pages.next();
                out.write("<page imageFile=\"" + p.getImageCanonical() + "\" sourceFile=\"\">\n");
                Iterator<Token> tokens = doc.tokenIterator(p);
                while (tokens.hasNext()) {
                    Token t = tokens.next();
                    index++;

                    SpecialSequenceType sst = t.getSpecialSeq();
                    if (sst.equals(SpecialSequenceType.SPACE)) {
                        out.write("<token token_id=\"" + index + "\" special_seq=\"space\" isNormal=\"false\">\n");
                    } else if (sst.equals(SpecialSequenceType.NEWLINE)) {
                        out.write("<token token_id=\"" + index + "\" special_seq=\"newline\" isNormal=\"false\">\n");
                    } else if (sst.equals(SpecialSequenceType.NORMAL)) {
                        out.write("<token token_id=\"" + index + "\" isNormal=\"" + t.isNormal() + "\">\n");
                    } else {
                        out.write("<token token_id=\"" + index + "\" isNormal=\"" + t.isNormal() + "\">\n");                        
                    }
                    
//                    if( t.getID() != t.getIndexInDocument() ) {
                        out.write("<ext_id>"+t.getID()+"</ext_id>\n");
//                    }

                    out.write("<wOCR>" + StringEscapeUtils.escapeXml(t.getWOCR()) + "</wOCR>\n");
                    out.write("<wOCR_lc>" + StringEscapeUtils.escapeXml(t.getWOCR_lc()) + "</wOCR_lc>\n");
                    out.write("<wCorr>" + StringEscapeUtils.escapeXml(t.getWCOR()) + "</wCorr>\n");

                    TokenImageInfoBox b = t.getTokenImageInfoBox();
                    if( b != null) {
                        out.write("<coord l=\"" + b.getCoordinateLeft() + "\" t=\"" + b.getCoordinateTop() + "\" r=\"" + b.getCoordinateRight() + "\" b=\"" + b.getCoordinateBottom() + "\"/>\n");
                    }

                    if( sst.equals(SpecialSequenceType.NORMAL) || sst.equals(SpecialSequenceType.HYPHENATED)) {
                        out.write("<abbyy_suspicious value=\"" + t.isSuspicious() + "\"/>\n");
                    }

                    if ( exportCandidates && t.getNumberOfCandidates() > 0 ) {
                        Iterator<Candidate> cands = doc.candidateIterator(t.getID());
                        while (cands.hasNext()) {
                            Candidate cand = cands.next();
                            out.write("<cand>" + StringEscapeUtils.escapeXml(cand.getSuggestion()) + StringEscapeUtils.escapeXml(cand.getInterpretation()) + ",voteWeight=" + cand.getVoteweight() + ",levDistance=" + cand.getDlev() + "</cand>\n");
                        }
                    }
                    
                    out.write("</token>\n");
                }
                out.write("</page>\n");
            }
            out.write("</document>\n");
            out.flush();
            out.close();

        } catch (IOException ex) {
            Logger.getLogger(OCRXMLExporter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
