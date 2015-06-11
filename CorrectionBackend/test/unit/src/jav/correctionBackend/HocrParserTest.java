/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend;

import java.net.URL;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author finkf
 */
public class HocrParserTest {
    private MockDocument document;
    
    @Before
    public void setUp() {
        URL url = getClass().getResource("/data/test.hocr");
        document = new MockDocument();
        HocrParser parser = new HocrParser(document);
        parser.parse(url.getFile(), "null", "Utf8");
    }

    /**
     * Test of parse method, of class HocrParser.
     */
    @Test
    public void shouldContainRightToken() {
        Token token = document.findFirstToken("εἶχον");
        assertNotNull(token);
        TokenImageInfoBox box = token.getTokenImageInfoBox();
        assertNotNull(box);
        assertEquals(1316, box.getCoordinateLeft());
        assertEquals(238, box.getCoordinateTop());
        assertEquals(1474, box.getCoordinateRight());
        assertEquals(314, box.getCoordinateBottom());
    }
    @Test
    public void shouldContainRightLastToken() {
        Token token = document.findLastToken("Κῦρος");
        assertNotNull(token);
        TokenImageInfoBox box = token.getTokenImageInfoBox();
        assertNotNull(box);
        assertEquals(2031, box.getCoordinateLeft());
        assertEquals(2282, box.getCoordinateTop());
        assertEquals(2215, box.getCoordinateRight());
        assertEquals(2362, box.getCoordinateBottom());
    }
}
