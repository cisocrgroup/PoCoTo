package jav.correctionBackend;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author thorsten (thorsten.vobl@googlemail.com)
 */
public class HOCRParser extends DefaultHandler implements Parser {

    private int orig_id = 1;
    private SAXParser sx;
    private int tokenIndex_ = 0;
    private int top_ = 0;
    private int bottom_ = 0;
    private int left_ = 0;
    private int right_ = 0;
    private String temp_ = "";
    private int pages = 0;
    private boolean tokenIsToBeAdded = false;
    private Document doc_ = null;
    private Token temptoken_ = null;
    private String tempimage_ = null;
    private java.util.regex.Pattern myAlnum;

    public HOCRParser(Document d) {
        this.doc_ = d;
        this.myAlnum = java.util.regex.Pattern.compile("[\\pL\\pM\\p{Nd}\\p{Nl}\\p{Pc}[\\p{InEnclosedAlphanumerics}&&\\p{So}]]+");
        try {
            sx = new org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl().newSAXParser();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(HOCRParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void parse(String xmlFile, String imgFile, String encoding) {
        try {
            InputStream inputStream = new FileInputStream(xmlFile);
            Reader reader = new InputStreamReader(inputStream, encoding);
            InputSource is = new InputSource(reader);
//            is.setEncoding(encoding);

            this.tempimage_ = imgFile;
            sx.parse(is, this);
        } catch (SAXException ex) {
        } catch (IOException ex) {
        }
    }

    @Override
    public void endDocument() {
        temptoken_ = null;
        pages++;
    }

    @Override
    public void startElement(String uri, String nname, String qName, Attributes atts) {
        if (nname.equals("span") && atts.getValue("class").equals("ocr_word")) {

            String id = atts.getValue("id");
            orig_id = Integer.parseInt(id.substring(id.lastIndexOf("_") + 1, id.length()));
            String[] vals = atts.getValue("title").split(" ");
            this.left_ = Integer.parseInt(vals[1]);
            this.right_ = Integer.parseInt(vals[3]);
            this.tokenIsToBeAdded = true;

        } else if (nname.equals("div") && atts.getValue("class").equals("ocr_page")) {

        } else if (nname.equals("span") && atts.getValue("class").equals("ocr_line")) {

            // beginning of new line, if not first line add newline token
            if (this.temptoken_ != null) {
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
            }

            String[] vals = atts.getValue("title").split(" ");
            this.top_ = Integer.parseInt(vals[2]);
            if (this.top_ < 0) {
                this.top_ = 0;
            }
            this.bottom_ = Integer.parseInt(vals[4]);
        }
    }

    @Override
    public void endElement(String uri, String nname, String qName) {
        // paragraph end, add newline ??
        if (nname.equals("p")) {
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
        }
    }

    @Override
    public void characters(char ch[], int start, int length) {
        this.temp_ = new String(ch, start, length);

        if (this.tokenIsToBeAdded) {

            if (this.temp_.length() > 60) {
                this.temp_ = this.temp_.substring(0, 60);
            }
            temptoken_ = new Token( this.temp_ );
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
            temptoken_.setIsSuspicious(false);
            temptoken_.setIsCorrected(false);
            temptoken_.setPageIndex(pages);
            temptoken_.setIsNormal(myAlnum.matcher(temp_).matches());
            temptoken_.setNumberOfCandidates(0);

            // if document has coordinates
            if (left_ >= 0) { // && (temptoken_.getSpecialSeq().equals(SpecialSequenceType.NORMAL) || temptoken_.getSpecialSeq().equals(SpecialSequenceType.PUNCTUATION))) {
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
            tokenIndex_++;
            this.tokenIsToBeAdded = false;
        }

        // add a space token
        if (this.temp_.equals(" ")) {
            temptoken_ = new Token( " " );
            temptoken_.setSpecialSeq(SpecialSequenceType.SPACE);
            temptoken_.setIndexInDocument(tokenIndex_);
            temptoken_.setIsSuspicious(false);
            temptoken_.setIsCorrected(false);
            temptoken_.setIsNormal(false);
            temptoken_.setNumberOfCandidates(0);
            temptoken_.setPageIndex(pages);
            temptoken_.setTokenImageInfoBox(null);

            doc_.addToken(temptoken_);
            tokenIndex_++;
        }
    }
}
//def coordsToAbbyCoords(hOCRCoords: ((Int,Int),(Int,Int)), p: Page) = {
//   val ((leftDistance,topDistance),(hOCRRight,hOCRbottom)) = hOCRCoords
//   val ((,),(pageRight, pageBottom)) = p.coordinates
//
//   ((leftDistance,topDistance),(pageRight - hOCRRight, pageBottom - hOCRbottom))
// }
