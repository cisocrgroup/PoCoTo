package jav.correctionBackend;

import java.io.*;
import java.util.regex.Pattern;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
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
public class AbbyyXMLParser extends DefaultHandler implements Parser {

    private int orig_id = 1;
    private int tokenIndex_ = 0;
    private int top_ = 0;
    private int bottom_ = 0;
    private int left_ = 0;
    private int right_ = 0;
    private int right_temp = 0;
    private int left_temp = 0;
    private String temp_ = "";
    private String lastchar_;
    private String thischar_;
    private int pages = 0;
    private int position_ = 0;
    private boolean globalIsSuspicious = false;
    private boolean inVariant_ = false;
    private boolean isSuspicious_ = false;
    private boolean isDict_ = false;
    private Document doc_ = null;
    private Token temptoken_ = null;
    private String tempimage_ = null;
    private XMLReader xr;
    private Pattern myAlnum;

    public AbbyyXMLParser(Document d) {
        this.doc_ = d;
//        this.myAlnum = Pattern.compile("[\\p{Space}\\p{Punct}]");
        this.myAlnum = Pattern.compile("[\\pL\\pM\\p{Nd}\\p{Nl}\\p{Pc}[\\p{InEnclosedAlphanumerics}&&\\p{So}]]+");

        try {
            xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(this);
            xr.setErrorHandler(this);
        } catch (SAXException e1) {
        }
    }

    @Override
    public void parse(String filename, String imageFile, String encoding) {
        this.tempimage_ = imageFile;
        try {
            InputSource is = new InputSource(getReader(filename));
//            is.setEncoding(encoding);
            xr.parse(is);
        } catch (IOException ex) {
             throw new RuntimeException(ex);
        } catch (SAXException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private final static int BOM_SIZE = 4;
    private Reader getReader(String path) throws IOException {
        PushbackInputStream is = new PushbackInputStream(
                new BufferedInputStream(new FileInputStream(path)), BOM_SIZE);
        byte[] bom = new byte[BOM_SIZE];
        is.read(bom);
        // utf8
        if ((bom[0] == (byte)0xef) && (bom[1] == (byte)0xbb) && (bom[2] == (byte)0xbf)) {
            is.unread(bom, 3, 1);
        } else if ((bom[0] == (byte)0xfe) && (bom[1] == (byte)0xff)) {
            is.unread(bom, 2, 2);
        } else if ((bom[0] == (byte)0xff) && (bom[1] == (byte)0xfe)) {
            is.unread(bom, 2, 2);
        } else if ((bom[0] == (byte)0x0) && (bom[1] == (byte)0x0) && 
                (bom[2] == (byte)0xfe) && (bom[3] == (byte)0xff)) {
            /* do nothing */
        } else if ((bom[0] == (byte)0xff) && (bom[1] == (byte)0xfe) && 
                (bom[2] == (byte)0x0) && (bom[3] == (byte)0x0)) {  
            /* do nothing */
        } else {
            is.unread(bom, 0, BOM_SIZE);
        }
        return new InputStreamReader(is);
    }

    @Override
    public void startDocument() {
//        System.out.println("Parsing started.");
//        this.starttime_ = System.currentTimeMillis();
    }

    @Override
    public void endDocument() {
//        System.out.println("Parsing ended. " + (System.currentTimeMillis() - this.starttime_));
    }

    @Override
    public void startElement(String uri, String nname, String qName, Attributes atts) {
        if (qName.equals("document")) {
        } else if (qName.equals("page")) {
        } else if (qName.equals("block")) {
        } else if (qName.equals("region")) {
        } else if (qName.equals("rect")) {
        } else if (qName.equals("text")) {
        } else if (qName.equals("par")) {
        } else if (qName.equals("line")) {
            top_ = Integer.parseInt(atts.getValue("t"));
            if( top_ == -1) {
                top_ = 1;
            }
            bottom_ = Integer.parseInt(atts.getValue("b"));
            if( bottom_ == -1) {
                bottom_ = 1;
            }
        } else if (qName.equals("variantText")) {
            inVariant_ = true;
        } else if (qName.equals("formatting")) {
        } else if (qName.equals("charParams")) {

//            tempchar_ = new Character(this.tokenIndex_, position_);
//            tempchar_.setLeft(Integer.parseInt(atts.getValue("l")));
//            tempchar_.setRight(Integer.parseInt(atts.getValue("r")));
//            tempchar_.setIsSuspicious((atts.getValue("suspicious") != null));
            this.isSuspicious_ = (atts.getValue("suspicious") != null);
            this.isDict_ = Boolean.parseBoolean(atts.getValue("wordFromDictionary"));

            
            System.out.println("charparams " + this.isSuspicious_ + " " + this.isDict_);
//            doc_.addCharacter(tempchar_);

            left_temp = Integer.parseInt(atts.getValue("l"));
            right_temp = Integer.parseInt(atts.getValue("r"));
            position_++;
        }
    }

    @Override
    public void endElement(String uri, String nname, String qName) {
        if (qName.equals("document")) {
        } else if (qName.equals("page")) {
            orig_id = 1;
            pages++;
        } else if (qName.equals("block")) {
        } else if (qName.equals("region")) {
        } else if (qName.equals("rect")) {
        } else if (qName.equals("text")) {
        } else if (qName.equals("par")) {

            temptoken_ = new Token("\n");
            temptoken_.setSpecialSeq(SpecialSequenceType.NEWLINE);
            temptoken_.setIndexInDocument(tokenIndex_);
            temptoken_.setIsSuspicious(false);
            temptoken_.setIsCorrected(false);
            temptoken_.setIsNormal(false);
            temptoken_.setNumberOfCandidates(0);
            temptoken_.setPageIndex(pages);
            temptoken_.setTokenImageInfoBox(null);

            doc_.addToken(temptoken_);
            tokenIndex_++;
            temptoken_ = null;
            position_ = 0;
            left_ = 0;
            temp_ = "";

            // at end of line, pushback actual token and add newline token
        } else if (qName.equals("line")) {

            if (!temp_.equals("")) {
                temptoken_ = new Token( temp_ );
                if (temp_.matches("^[\\p{Space}]+$")) {
                    temptoken_.setSpecialSeq(SpecialSequenceType.SPACE);
                }
//                else if (temp_.matches("^[\\p{Punct}]+$")) {
//                    temptoken_.setSpecialSeq(SpecialSequenceType.PUNCTUATION);
//                }
                else if (temp_.matches("^[\n\r\f]+$")) {
                    temptoken_.setSpecialSeq(SpecialSequenceType.NEWLINE);
                } else {
                    temptoken_.setSpecialSeq(SpecialSequenceType.NORMAL);
                }
                temptoken_.setIndexInDocument(tokenIndex_);
                temptoken_.setIsSuspicious(this.globalIsSuspicious);
                temptoken_.setIsCorrected(false);
                temptoken_.setPageIndex(pages);
                temptoken_.setIsNormal(myAlnum.matcher(temp_).matches());
                temptoken_.setNumberOfCandidates(0);

                if (left_ > 0 && !temptoken_.getSpecialSeq().equals(SpecialSequenceType.SPACE)) {
                    TokenImageInfoBox tiib = new TokenImageInfoBox();
                    tiib.setCoordinateBottom(bottom_);
                    tiib.setCoordinateLeft(left_);
                    tiib.setCoordinateRight(right_);
                    tiib.setCoordinateTop(top_);
                    tiib.setImageFileName(this.tempimage_);
                    temptoken_.setTokenImageInfoBox(tiib);
                } else {
                    temptoken_.setTokenImageInfoBox(null);
                }

                temptoken_.setOrigID(orig_id);
                doc_.addToken(temptoken_);
                System.out.println("token add " + temptoken_.getWOCR() + " " + temptoken_.isSuspicious());
                this.globalIsSuspicious = false;
                orig_id++;
                tokenIndex_++;
            }

            temptoken_ = new Token( "\n" );
            temptoken_.setSpecialSeq(SpecialSequenceType.NEWLINE);
            temptoken_.setIndexInDocument(tokenIndex_);
            temptoken_.setIsSuspicious(false);
            temptoken_.setIsCorrected(false);
            temptoken_.setIsNormal(false);
            temptoken_.setNumberOfCandidates(0);
            temptoken_.setPageIndex(pages);
            temptoken_.setTokenImageInfoBox(null);

            doc_.addToken(temptoken_);
            tokenIndex_++;
            temptoken_ = null;
            position_ = 0;
            left_ = 0;
            temp_ = "";

        } else if (qName.equals("variantText")) {
            inVariant_ = false;
        } else if (qName.equals("formatting")) {
        } else if (qName.equals("charParams")) {

            if (!inVariant_) {

                // tokenstring empty (happens at begin of document and after closing </line> and </par> tags)
                if (temp_.equals("")) {
                    temp_ = thischar_;
                } else {

                    // previous char alnum and actual char alnum -> attach thischar_ to tempstring
                    if (myAlnum.matcher(lastchar_).matches() && myAlnum.matcher(thischar_).matches()) {
                        temp_ += thischar_;

                        // previous char non-alnum and actual char alnum -> pushback token, attach thischar_ to tempstring
                    } else if (!myAlnum.matcher(lastchar_).matches() && myAlnum.matcher(thischar_).matches()) {

                        temptoken_ = new Token( temp_ );
                        if (temp_.matches("^[\\p{Space}]+$")) {
                            temptoken_.setSpecialSeq(SpecialSequenceType.SPACE);
                        } else if (temp_.matches("^[\\p{Punct}]+$")) {
                            temptoken_.setSpecialSeq(SpecialSequenceType.PUNCTUATION);
                        } else if (temp_.matches("^[\n\r\f]+$")) {
                            temptoken_.setSpecialSeq(SpecialSequenceType.NEWLINE);
                        } else {
                            temptoken_.setSpecialSeq(SpecialSequenceType.NORMAL);
                        }
                        temptoken_.setIndexInDocument(tokenIndex_);
                        temptoken_.setIsSuspicious(this.globalIsSuspicious);
                        temptoken_.setIsCorrected(false);
                        temptoken_.setPageIndex(pages);
                        temptoken_.setIsNormal(myAlnum.matcher(temp_).matches());
                        temptoken_.setNumberOfCandidates(0);

                        // if document has coordinates
                        if (left_ > 0 && !temptoken_.getSpecialSeq().equals(SpecialSequenceType.SPACE)) {
                            TokenImageInfoBox tiib = new TokenImageInfoBox();
                            tiib.setCoordinateBottom(bottom_);
                            tiib.setCoordinateLeft(left_);
                            tiib.setCoordinateRight(right_);
                            tiib.setCoordinateTop(top_);
                            tiib.setImageFileName(this.tempimage_);
                            temptoken_.setTokenImageInfoBox(tiib);
                        } else {
                            temptoken_.setTokenImageInfoBox(null);
                        }

                        temptoken_.setOrigID(orig_id);
                        doc_.addToken(temptoken_);
                        System.out.println("token add " + temptoken_.getWOCR() + " " + temptoken_.isSuspicious());
                        this.globalIsSuspicious = false;
                        orig_id++;
                        tokenIndex_++;
                        temptoken_ = null;
                        position_ = 0;
                        left_ = 0;
                        temp_ = thischar_;

                        // previous char alnum and actual char non-alnum -> pushback token, attach thischar_ to tempstring
                    } else if (myAlnum.matcher(lastchar_).matches() & !myAlnum.matcher(thischar_).matches()) {

                        temptoken_ = new Token( temp_ );
                        if (temp_.matches("^[\\p{Space}]+$")) {
                            temptoken_.setSpecialSeq(SpecialSequenceType.SPACE);
                        }
//                        else if (temp_.matches("^[\\p{Punct}]+$")) {
//                            temptoken_.setSpecialSeq(SpecialSequenceType.PUNCTUATION);
//                        }
                        else if (temp_.matches("^[\n\r\f]+$")) {
                            temptoken_.setSpecialSeq(SpecialSequenceType.NEWLINE);
                        } else {
                            temptoken_.setSpecialSeq(SpecialSequenceType.NORMAL);
                        }
                        temptoken_.setIndexInDocument(tokenIndex_);
                        temptoken_.setIsSuspicious(this.globalIsSuspicious);
                        temptoken_.setIsCorrected(false);
                        temptoken_.setPageIndex(pages);
                        temptoken_.setIsNormal(myAlnum.matcher(temp_).matches());
                        temptoken_.setNumberOfCandidates(0);

                        // if document has coordinates
                        if (left_ > 0 && !temptoken_.getSpecialSeq().equals(SpecialSequenceType.SPACE)) {
                            TokenImageInfoBox tiib = new TokenImageInfoBox();
                            tiib.setCoordinateBottom(bottom_);
                            tiib.setCoordinateLeft(left_);
                            tiib.setCoordinateRight(right_);
                            tiib.setCoordinateTop(top_);
                            tiib.setImageFileName(this.tempimage_);
                            temptoken_.setTokenImageInfoBox(tiib);
                        } else {
                            temptoken_.setTokenImageInfoBox(null);
                        }

                        temptoken_.setOrigID(orig_id);
                        doc_.addToken(temptoken_);
                        System.out.println("token add " + temptoken_.getWOCR() + " " + temptoken_.isSuspicious());
                        this.globalIsSuspicious = false;
                        tokenIndex_++;
                        orig_id++;
                        temptoken_ = null;
                        position_ = 0;
                        temp_ = thischar_;
                        left_ = 0;

                        // previous char non-alnum and actual char non-alnum -> attach tempchar_ to token
                    } else if (!myAlnum.matcher(lastchar_).matches() & !myAlnum.matcher(thischar_).matches()) {
                        temp_ += thischar_;
                    }
                }
                lastchar_ = thischar_;
            }
            
            if( this.isSuspicious_ && !this.isDict_ ) {
                System.out.println("global");
                this.globalIsSuspicious = true;
            }

            // if left unset set it
            if (left_ == 0) {
                left_ = left_temp;
            }
            // set new right coordinate
            right_ = right_temp;
        }
    }

    /*
     * Assumption: abbyy xml output is charwise
     */
    @Override
    public void characters(char ch[], int start, int length) {
        if (length > 1) {
            System.err.println("Error. Length > 1. " + new String(ch, start, length));
        }
        thischar_ = new String(ch, start, length);
    }
}