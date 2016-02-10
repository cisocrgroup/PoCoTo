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
class TeiBook extends Book {

    private final org.w3c.dom.Document document;

    public TeiBook(org.w3c.dom.Document document, File file) {
        super(null, file);
        this.document = document;
    }
}
