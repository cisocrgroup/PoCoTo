/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend;

import jav.logging.log4j.Log;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author finkf
 */
public class BaseSaxOcrDocumentParser extends DefaultHandler implements OcrDocumentParser {
    private Document document_;
    private String imageFile_;
    public BaseSaxOcrDocumentParser(Document document) {
        assert(document != null);
        this.document_ = document;
    }
    public Document getDocument() {
        return this.document_;
    }
    public void setDocument(Document document) {
        assert(document != null);
        this.document_ = document;
    }
    public String getImageFile() {
        return this.imageFile_;
    }
    public void setImageFile(String imageFile) {
        this.imageFile_ = imageFile;
    }
    @Override 
    public final void parse(String xml, String img, String enc) {
        Log.info(this, "parse(%s, %s, %s)", xml, img, enc);
        if (img == null || img.isEmpty())
                Log.error(this, "missing image file");
        imageFile_ = img;
        try {
            XMLReader xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(this);
            xr.setErrorHandler(this);
            InputSource is = new InputSource(getReader(xml, enc));
            xr.parse(is);
        } catch (IOException ex) {
            Log.error(this, "Could not read %s: %s", xml, ex);
             throw new RuntimeException(ex);
        } catch (SAXException ex) {
            Log.error(this, "Invalid Xml file %s: %s", xml, ex);
            throw new RuntimeException(ex);
        }
    }
    private final static int BOM_SIZE = 4;
    private Reader getReader(String path, String enc) throws IOException {
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
        return new InputStreamReader(is, enc);
    }
}
