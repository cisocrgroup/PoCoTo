package jav.gui.main;

import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *Copyright (c) 2012, IMPACT working group at the Centrum für Informations- und Sprachverarbeitung, University of Munich.
 *All rights reserved.

 *Redistribution and use in source and binary forms, with or without
 *modification, are permitted provided that the following conditions are met:

 *Redistributions of source code must retain the above copyright
 *notice, this list of conditions and the following disclaimer.
 *Redistributions in binary form must reproduce the above copyright
 *notice, this list of conditions and the following disclaimer in the
 *documentation and/or other materials provided with the distribution.

 *THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 *IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 *TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 *PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This file is part of the ocr-postcorrection tool developed
 * by the IMPACT working group at the Centrum für Informations- und Sprachverarbeitung, University of Munich.
 * For further information and contacts visit http://ocr.cis.uni-muenchen.de/
 * 
 * @author thorsten (thorsten.vobl@googlemail.com)
 */
public class ProfileController {
    
    private DocumentBuilderFactory factory;
    private DocumentBuilder builder;
    private XPath xpath;
    private Document document = null;
    
    public ProfileController() {
        factory = DocumentBuilderFactory.newInstance();
        try {
            builder = factory.newDocumentBuilder();
            xpath = XPathFactory.newInstance().newXPath();
        } catch (ParserConfigurationException ex) {
            
        }
    }
    
    public void loadProfile( String xmlin ) {
        try {
            document = builder.parse(xmlin);
        } catch (SAXException ex) {
        } catch (IOException ex) {
        }
    }
        
    public void makeUnique( String xmlout ) {
        try {
            XPathExpression expr = xpath.compile("//pattern[pattern_occurences]");
        } catch (XPathExpressionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }    
}

//
//        // Prepare the DOM document for writing
//        Source source = new DOMSource(doc);
//transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//        // Prepare the output file
//        File file = new File(filename);
//        Result result = new StreamResult(file);
//
//        // Write the DOM document to the file
//        Transformer xformer = TransformerFactory.newInstance().newTransformer();
//        xformer.transform(source, result);

//
//    private DocumentBuilderFactory factory;
//    private DocumentBuilder builder;
//    private XPath xpath;
//    private FileOutputStream fos;
//    private OutputStreamWriter out;
//    
//    public PageParser2TextWriter() {
//        factory = DocumentBuilderFactory.newInstance();
//        try {
//            builder = factory.newDocumentBuilder();
//            xpath = XPathFactory.newInstance().newXPath();
//        } catch (ParserConfigurationException ex) {
//            
//        }
//    }
//    
//    public int page2txt( String xmlin, String txtout ) {
//        
//        
//        int retval;
//        try {
//            Document document = builder.parse(xmlin);
//            fos = new FileOutputStream(txtout); 
//            out = new OutputStreamWriter(fos, "UTF-8");
//            
//            NodeList regions = document.getElementsByTagName("RegionRefIndexed");
//            for( int i = 0 ; i < regions.getLength() ; i++ ) {
//                String rid = regions.item(i).getAttributes().getNamedItem("regionRef").getNodeValue();
//                try {
//                    XPathExpression expr = xpath.compile("//TextRegion[@id = '" + rid + "'][1]/TextEquiv/Unicode");
//                    Object result = expr.evaluate( document, XPathConstants.NODE);
//                    NodeList nodes = (NodeList) result;
//                    for (int j = 0; j < nodes.getLength(); j++) {
//                        String content = nodes.item(j).getTextContent();
//                        if( content != null) {
//                            out.write(content + "\n");
//                        }
//                    }                    
//                } catch (XPathExpressionException ex) {
//                }                                
//            }
//            
//            out.flush();
//            out.close();
//            retval = 0;
//        } catch (SAXException ex) {
//            retval = -2;
//        } catch (IOException ex) {
//            retval = -3;
//        }        
//        return retval;
//    }
