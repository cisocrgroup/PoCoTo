/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import jav.correctionBackend.Document;
import jav.correctionBackend.MyIterator;
import jav.correctionBackend.SpecialSequenceType;
import jav.correctionBackend.Token;

/**
 *
 * @author flo
 */
public class DocumentPage extends Page {

    private final Document document;
    private final jav.correctionBackend.Page page;

    public DocumentPage(jav.correctionBackend.Page page, Document doc) {
        this.page = page;
        this.document = doc;
        parse();
    }

    private void parse() {
        MyIterator<Token> it = document.tokenIterator(page);
        boolean prevWasNewline = false;
        DocumentParagraph paragraph = new DocumentParagraph(document);
        DocumentLine line = new DocumentLine(document);
        while (it.hasNext()) {
            Token token = it.next();
            if (SpecialSequenceType.NEWLINE.equals(token.getSpecialSeq())) {
                if (prevWasNewline) { // end of paragraph
                    this.add(paragraph);
                    paragraph = new DocumentParagraph(document);
                } else {
                    prevWasNewline = true;
                    paragraph.add(line);
                    line = new DocumentLine(document);
                }
            } else {
                prevWasNewline = false;
                line.add(token);
            }
        }
    }
}
