/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

import jav.correctionBackend.SpreadIndexDocument;

/**
 * Class that build Documents from OCR files.
 *
 * @author finkf
 */
interface DocumentBuilder {

    /**
     * Initialize the builder. This method should be called before any other
     * call to the interface's methods.
     */
    public void init();

    /**
     * Append a Page to the document builder.
     *
     * @param page One page of an OCR document.
     */
    public void append(Page page);

    /**
     * Finish building the document and return it.
     *
     * @return the document
     */
    public SpreadIndexDocument build();
}
