/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend;

import java.io.File;
import java.io.FileOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author flo
 */
public class TeiXmlExporter {

    private static final String TEI_NS = "http://www.tei-c.org/ns/1.0";
    private final Document document;
    private org.w3c.dom.Document xml;

    public TeiXmlExporter(Document document) {
        this.document = document;
    }

    public void export(File output) throws Exception {
        build();
        write(output);
    }

    private void build() throws Exception {
        DocumentBuilderFactory dFact = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dFact.newDocumentBuilder();
        xml = builder.newDocument();
        Element tei = xml.createElementNS(TEI_NS, "TEI");
        xml.appendChild(tei);
        appenHeader(tei);
        appendBody(tei);
    }

    private void appenHeader(Element tei) {
        Element teiHeader = xml.createElement("teiHeader");
        tei.appendChild(teiHeader);
        Element fileDesc = xml.createElement("fileDesc");
        teiHeader.appendChild(fileDesc);
        fileDesc.appendChild(xml.createElement("titleStmt"));
        fileDesc.appendChild(xml.createElement("publicationStmt"));
        fileDesc.appendChild(xml.createElement("sourceDesc"));
        teiHeader.appendChild(xml.createElement("encodingDesc"));
        teiHeader.appendChild(xml.createElement("profileDesc"));
    }

    private void appendBody(Element tei) {
        Element text = xml.createElement("text");
        tei.appendChild(text);
        Element body = xml.createElement("body");
        text.appendChild(body);
        MyIterator<Page> pages = document.pageIterator();
        int i = 0;
        while (pages.hasNext()) {
            appendPage(body, pages.next(), ++i);
        }
    }

    private void appendPage(Element body, Page page, int i) {
        Element pb = xml.createElement("pb");
        body.appendChild(pb);
        setAttribute(pb, "n", Integer.toString(i));
        MyIterator<Token> tokens = document.tokenIterator(page);
        Element p = makeP(body);
        boolean lastWasNewline = false;
        while (tokens.hasNext()) {
            final Token token = tokens.next();
            if (SpecialSequenceType.NEWLINE.equals(token.getSpecialSeq())) {
                if (lastWasNewline) { // end of paragraph
                    p = makeP(body);
                    lastWasNewline = false;
                } else {
                    lastWasNewline = true;
                }
            } else {
                if (lastWasNewline) {
                    p.appendChild(xml.createElement("lb"));
                }
                if (token.isCorrected()) {
                    p.appendChild(xml.createTextNode(token.getWCOR()));
                } else {
                    p.appendChild(xml.createTextNode(token.getWOCR()));
                }
                lastWasNewline = false;
            }
        }
    }

    private Element makeP(Element body) {
        Element p = xml.createElement("p");
        body.appendChild(p);
        return p;
    }

    private void write(File output) throws Exception {
        TransformerFactory tFact = TransformerFactory.newInstance();
        Transformer trans = tFact.newTransformer();
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
        StreamResult result = new StreamResult(new FileOutputStream(output));
        trans.transform(new DOMSource(xml), result);

    }

    private void setAttribute(Element elem, String key, String val) {
        NamedNodeMap attrs = elem.getAttributes();
        Node attr = elem.getOwnerDocument().createAttribute(key);
        attr.setNodeValue(val);
        attrs.setNamedItem(attr);
    }
}
