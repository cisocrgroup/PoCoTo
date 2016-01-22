/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

import java.io.File;
import java.io.IOException;

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
     * Parses an OCR File into its Page representation
     *
     * @param input the input file to read
     * @return the page representation of the OCR file
     * @throws IOException if an Input/Output error occurred
     * @throws Exception if some formatting error occurred
     */
    public Page parse(File input) throws IOException, Exception;

    /**
     * Write the (modified) representation of the Parser to a file. You should
     * call @parse before you call this method. It is an error to call this
     * method before a call to @parse.
     *
     * @param output the output file
     * @throws IOException if an Input/Output error occurred
     * @throws Exception if some formatting error occurred
     */
    public void write(File output) throws IOException, Exception;
}
