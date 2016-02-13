/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import jav.correctionBackend.Document;
import jav.correctionBackend.MyIterator;
import java.util.ArrayList;

/**
 *
 * @author flo
 */
public class DocumentBook extends Book {

    private final Document document;
    private final ArrayList<DocumentPage> pages;

    public DocumentBook(Document document) {
        this.document = document;
        pages = new ArrayList<>();
        parse();
    }

    @Override
    public int getNumberOfPages() {
        return pages.size();
    }

    @Override
    public DocumentPage getPageAt(int i) {
        return pages.get(i);
    }

    private void parse() {
        MyIterator<jav.correctionBackend.Page> it = document.pageIterator();
        while (it.hasNext()) {
            pages.add(new DocumentPage(it.next(), document));
        }
    }
}
