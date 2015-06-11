/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend;

import java.net.URL;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author finkf
 */
public class AbbyyXmlParserTest {
    MockDocument document;
    
    @Before
    public void setUp() {
        URL url = getClass().getResource("/data/test.abbyy.xml");
        document = new MockDocument();
        AbbyyXmlParser parser = new AbbyyXmlParser(document);
        parser.parse(url.getFile(), "null", null);
    }

    @Test
    public void shouldContainRightToken() {
        Token token = document.findFirstToken("Cap");
        assertNotNull(token);
        TokenImageInfoBox box = token.getTokenImageInfoBox();
        assertNotNull(box);
        assertEquals(300, box.getCoordinateLeft());
        assertEquals(125, box.getCoordinateTop());
        assertEquals(413, box.getCoordinateRight());
        assertEquals(207, box.getCoordinateBottom());
    }
    
    @Test
    public void shouldContainRightLastToken() {
        Token token = document.findLastToken("deni");
        assertNotNull(token);
        TokenImageInfoBox box = token.getTokenImageInfoBox();
        assertNotNull(box);
        assertEquals(1780, box.getCoordinateLeft());
        assertEquals(2700, box.getCoordinateTop());
        assertEquals(1877, box.getCoordinateRight());
        assertEquals(2752, box.getCoordinateBottom());
    }
}
