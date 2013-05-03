package jav.correctionBackend;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

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
public class OCRXMLImporter {

    public OCRXMLImporter() {
    }

    public void simpleUpdateDocument(Document doc, String documentfile) {
        new SimpleImporter().parse(doc, documentfile);
    }

    public void importDocument(Document doc, String documentfile, String imgdir) {
        new OCRCXMLImporter().parse(doc, documentfile, imgdir);
    }

    public void importCandidates(Document doc, String documentfile) {
        new CandidateImporter().parse(doc, documentfile);
    }

    public void importProfile(Document doc, String profilefile) {
        new ProfileImporter().parse(doc, profilefile);
    }
}

class SimpleImporter extends DefaultHandler {

    private Document doc;
    private int tokenID;
    private String susp;
    private String norm;

    public void parse(Document d, String f) {
        if (d != null) {
            this.doc = d;
            XMLReader xr;
            try {
                xr = XMLReaderFactory.createXMLReader();
                xr.setContentHandler(this);
                xr.setErrorHandler(this);
                xr.parse(f);
            } catch (SAXException ex) {
            } catch (IOException e) {
            }
        } else {
            throw new NullPointerException();
        }
    }

    @Override
    public void startDocument() {
    }

    @Override
    public void endDocument() {
    }

    @Override
    public void startElement(String uri, String nname, String qname, Attributes atts) {
        if (nname.equals("token")) {
            tokenID = Integer.parseInt(atts.getValue("token_id"));
            norm = atts.getValue("isNormal");
        } else if (nname.equals("abbyy_suspicious")) {
            susp = atts.getValue("value");
        }
    }

    @Override
    public void endElement(String uri, String nname, String qname) {
        if (nname.equals("token")) {
            doc.setNormal(tokenID, norm);
            doc.setSuspicious(tokenID, susp);
        }
    }
}

class CandidateImporter extends DefaultHandler {

    private String content = "";
    private Document doc;
    private int rank;
    private int tokenID;
    private String susp;
    private java.util.regex.Pattern pattern;
    private Candidate tempcand;

    public void parse(Document d, String f) {
        pattern = java.util.regex.Pattern.compile("(.*):\\{(.*),voteWeight=(.*),levDistance=(.*)");

        if (d != null) {
            this.doc = d;
            this.doc.truncateCandidates();
            XMLReader xr;
            try {
                xr = XMLReaderFactory.createXMLReader();
                xr.setContentHandler(this);
                xr.setErrorHandler(this);
                xr.parse(f);
            } catch (SAXException ex) {
            } catch (IOException e) {
            }
        } else {
            throw new NullPointerException();
        }
    }

    @Override
    public void startDocument() {
    }

    @Override
    public void endDocument() {
    }

    @Override
    public void startElement(String uri, String nname, String qname, Attributes atts) {
        if (nname.equals("token")) {
            rank = 0;
        } else if (nname.equals("abbyy_suspicious")) {
            susp = atts.getValue("value");
        }
        content = "";
    }

    @Override
    public void endElement(String uri, String nname, String qname) {
        if (nname.equals("cand")) {
            Matcher matcher = pattern.matcher(content);
            if (matcher.matches()) {
                rank++;
                tempcand = new Candidate(tokenID, rank, matcher.group(1), matcher.group(2), Double.parseDouble(matcher.group(3)), Integer.parseInt(matcher.group(4)));
                doc.addCandidate(tempcand);
                if (rank == 1) {
                    doc.setTopCandDLev(tokenID, Integer.parseInt(matcher.group(4)));
                    doc.setTopSuggestion(tokenID, matcher.group(1));
                }
            }
        } else if (nname.equals("token")) {
            doc.setNumCandidates(tokenID, rank);
            doc.setSuspicious(tokenID, susp);
        } else if( nname.equals("ext_id")) {
            if( !content.equals("")) {
                tokenID = Integer.parseInt(content);
            }
        }
        content = "";
    }

    @Override
    public void characters(char ch[], int start, int length) {
        content += new String(ch, start, length);
    }
}

class ProfileImporter extends DefaultHandler {

    private Document doc;
    private int patternid;
    private int part;
    private Pattern temppattern;
    private PatternOccurrence tempocc;
    private boolean begin;

    public void parse(Document d, String f) {
        patternid = 0;
        if (d != null) {
            this.doc = d;
            XMLReader xr;
            try {
                xr = XMLReaderFactory.createXMLReader();
                xr.setContentHandler(this);
                xr.setErrorHandler(this);
                xr.parse(f);
            } catch (SAXException ex) {
            } catch (IOException e) {
            }
        } else {
            throw new NullPointerException();
        }
    }

    @Override
    public void startDocument() {
    }

    @Override
    public void endDocument() {
    }

    @Override
    public void startElement(String uri, String nname, String qname, Attributes atts) {
        if( qname.equals("ocr_errors")) {
            begin = true;
        } else if( qname.equals("pattern") && begin) {
            String left = atts.getValue("left");
            String right = atts.getValue("right");
            this.part = 0;
            this.temppattern = new Pattern(this.patternid, left, right, 0, 0);
        } else if (qname.equals("type")) {
            String wocr_lc = atts.getValue("wOCR_lc");
            String wsuggest = atts.getValue("wSuggest");
            int freq = Integer.parseInt(atts.getValue("freq"));
            tempocc = new PatternOccurrence(patternid, part, wocr_lc, wsuggest, freq, 0);
            this.part++;
            temppattern.addOccurence(tempocc, true);
        }
    }

    @Override
    public void endElement(String uri, String nname, String qname) {
        if (qname.equals("pattern") && begin) {
            doc.addPattern(temppattern);
            Iterator<PatternOccurrence> iter = temppattern.getOccurences().iterator();
            while( iter.hasNext() ) {
                doc.addPatternOccurrence( iter.next() );
            }
            this.patternid++;
        }
    }
}

class OCRCXMLImporter extends DefaultHandler {

    private String imgdir;
    private String content = "";
    private Document doc;
    private SpecialSequenceType spec;
    private int rank;
    private int pages = 0;
    private String imgFilename;
    private int tokenIndex = -1;
    private java.util.regex.Pattern candpattern;
    private java.util.regex.Pattern fileNamePattern;
    private Candidate tempcand;
    private Token temptoken;
    private boolean isNormal;

    public void parse(Document d, String docfile, String imgdir) {
        candpattern = java.util.regex.Pattern.compile("(.*):\\{(.*),voteWeight=(.*),levDistance=(.*)");
        fileNamePattern = java.util.regex.Pattern.compile(".*\\/(.*\\..*)");

        if (d != null) {
            this.doc = d;
            this.imgdir = imgdir;
            XMLReader xr;
            try {
                xr = XMLReaderFactory.createXMLReader();
                xr.setContentHandler(this);
                xr.setErrorHandler(this);
                xr.parse(docfile);
            } catch (SAXException ex) {
            } catch (IOException e) {
            }
        } else {
            throw new NullPointerException();
        }
    }

    @Override
    public void startDocument() {
    }

    @Override
    public void endDocument() {
    }

    @Override
    public void startElement(String uri, String nname, String qname, Attributes atts) {
        if (nname.equals("page")) {

            if (this.imgdir != null) {
                Matcher m = fileNamePattern.matcher(atts.getValue("imageFile"));
                if (m.matches()) {
                    File f = new File(imgdir + File.separator + m.group(1));
                    if (f.exists()) {
                        try {
                            imgFilename = f.getCanonicalPath();
                        } catch (IOException ex) {
                            imgFilename = "";
                        }
                    } else {
                        imgFilename = "";
                    }
                } else {
                    imgFilename = "";
                }
            } else {
                imgFilename = "";
            }

        } else if (nname.equals("token")) {

            rank = 0;
            tokenIndex = Integer.parseInt(atts.getValue("token_id"));
            isNormal = Boolean.parseBoolean(atts.getValue("isNormal"));
            String seq;
            if ((seq = atts.getValue("special_seq")) != null) {
                if (seq.equals("newline")) {
                    spec = SpecialSequenceType.NEWLINE;
                } else if (seq.equals("space")) {
                    spec = SpecialSequenceType.SPACE;
                }
            } else {
                spec = SpecialSequenceType.NORMAL;
            }
        } else if (nname.equals("coord")) {
            TokenImageInfoBox b = new TokenImageInfoBox();
            b.setImageFileName(imgFilename);
            b.setCoordinateLeft(Integer.parseInt(atts.getValue("l")));
            b.setCoordinateRight(Integer.parseInt(atts.getValue("r")));
            b.setCoordinateTop(Integer.parseInt(atts.getValue("t")));
            b.setCoordinateBottom(Integer.parseInt(atts.getValue("b")));
            temptoken.setTokenImageInfoBox(b);
        } else if (nname.equals("abbyy_suspicious")) {
            temptoken.setIsSuspicious(Boolean.parseBoolean(atts.getValue("value")));
        }
        content = "";
    }

    @Override
    public void endElement(String uri, String nname, String qname) {
        // add wocr wcorr 
        if (nname.equals("cand")) {
            rank++;
            Matcher matcher = candpattern.matcher(content);
            if (matcher.matches()) {
                tempcand = new Candidate(tokenIndex, rank, matcher.group(1), matcher.group(2), Double.parseDouble(matcher.group(3)), Integer.parseInt(matcher.group(4)));
                doc.addCandidate(tempcand);
                if (rank == 1) {
                    temptoken.setTopCandDLev(Integer.parseInt(matcher.group(4)));
                    temptoken.setTopSuggestion(matcher.group(1));
                }
            }
        } else if (nname.equals("token")) {
            temptoken.setNumberOfCandidates(rank);
            doc.addToken(temptoken);
        } else if (nname.equals("wOCR")) {
            temptoken = new Token( content );
            temptoken.setIndexInDocument(tokenIndex);
            temptoken.setIsNormal(isNormal);
            temptoken.setIsCorrected(false);
            temptoken.setSpecialSeq(spec);
            temptoken.setPageIndex(pages);
        } else if (nname.equals("wCorr")) {
            temptoken.setWCOR(content);
        } else if (nname.equals("page")) {
            pages++;
        }
        content = "";
    }

    @Override
    public void characters(char ch[], int start, int length) {
        content += new String(ch, start, length);
    }
}