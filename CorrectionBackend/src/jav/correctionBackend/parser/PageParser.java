/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import java.io.File;

/**
 *
 * @author flo
 */
public interface PageParser {

    /**
     * Sets the image file of the OCR Page.
     *
     * @param image the image file.
     */
    public void setImageFile(File image);

    /**
     * Sets the OCR file that will be parsed
     *
     * @param ocr the OCR file
     */
    public void setOcrFile(File ocr);

    /**
     * Parses an OCR File and returns its page representation.
     *
     * @return the page representation of the OCR file
     * @throws Exception if any error occurs.
     */
    public Page parse() throws Exception;

    /**
     * Write the (modified) representation of the Parser to a file. You should
     * call @parse before you call this method. It is an error to call this
     * method before a call to @parse.
     *
     * @param output the output file
     * @throws Exception if any error occurs.
     */
    public void write(File output) throws Exception;
}
