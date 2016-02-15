/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import jav.correctionBackend.Document;

/**
 *
 * @author flo
 */
public class DocumentParagraph extends Paragraph {

    private final Document document;

    public DocumentParagraph(Document document) {
        this.document = document;
    }
}
