/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import jav.correctionBackend.Document;
import jav.correctionBackend.MyIterator;

/**
 *
 * @author flo
 */
public class DocumentBook extends Book {

    private final Document document;

    public DocumentBook(Document document) {
        this.document = document;
        parse();
    }

    private void parse() {
        MyIterator<jav.correctionBackend.Page> it = document.pageIterator();
        while (it.hasNext()) {
            this.add(new DocumentPage(it.next(), document));
        }
    }
}
