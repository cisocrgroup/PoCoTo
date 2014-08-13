package jav.correctionBackend;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    private static boolean isWord(String name, Attributes attrs) {
        return "span".equals(name) && ("ocr_word".equals(attrs.getValue("class"))
                || "ocrx_word".equals(attrs.getValue("class")));
    }

    private static boolean isLine(String name, Attributes attrs) {
        return "span".equals(name)
                && "ocr_line".equals(attrs.getValue("class"));
    }

    private static boolean isPage(String name, Attributes attrs) {
        return "div".equals(name)
                && "ocr_page".equals(attrs.getValue("class"));
    }

    private static int[] parseBbox(String str) {
        final Pattern p = Pattern.compile("bbox\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)");
        int[] res = {0, 0, 0, 0};
        if (str != null) {
            Matcher m = p.matcher(str);
            if (m.find()) {
                for (int i = 0; i < 4; ++i) {
                    res[i] = Integer.parseInt(m.group(i + 1));
                }
            }
        }
        return res;
    }
    
    private static String parseImageFileName(String str) {
        final Pattern p1 = Pattern.compile("image\\s+\"(.*)\"");
        final Pattern p2 = Pattern.compile("file\\s+(.*)");
        if (str != null) {
            Matcher m = p1.matcher(str);
            if (m.find()) {
                return m.group(1);
            }
            m = p2.matcher(str);
            if (m.find()) {
                return m.group(1);
            }
        }
        return "";
    }
    
    private static int parseId(String str) {
        final Pattern p = Pattern.compile("_(\\d+)$");
        int res = 0;
        if (str != null) {
            Matcher m = p.matcher(str);
            if (m.find()) {
                res = Integer.parseInt(m.group(1));
            }
        }
        return res;
    }

    @Override
    public void startElement(String uri, String nname, String qName, Attributes atts) {
        if (isWord(nname, atts)) {
            orig_id = parseId(atts.getValue("id"));
            int[] bbox = parseBbox(atts.getValue("title"));
            this.left_ = bbox[0];
            this.right_ = bbox[2];
            this.tokenIsToBeAdded = true;

        } else if (isPage(nname, atts)) {
            this.tempimage_ = parseImageFileName(atts.getValue("title"));
        } else if (isLine(nname, atts)) {

            // beginning of new line, if not first line add newline token
            if (this.temptoken_ != null) {
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
            }
            int[] bbox = parseBbox(atts.getValue("title"));
            this.top_ = bbox[1];
            if (this.top_ < 0) {
                this.top_ = 0;
            }
            this.bottom_ = bbox[3];
        }
    }

    @Override
    public void endElement(String uri, String nname, String qName) {
        // paragraph end, add newline ??
        if (nname.equals("p")) {
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
        }
    }

    @Override
    public void characters(char ch[], int start, int length) {
        this.temp_ = new String(ch, start, length);

        if (this.tokenIsToBeAdded) {

            if (this.temp_.length() > 60) {
                this.temp_ = this.temp_.substring(0, 60);
            }
            temptoken_ = new Token(this.temp_);
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
