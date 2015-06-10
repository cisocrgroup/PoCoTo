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
import org.xml.sax.Attributes;

/**
 *
 * @author finkf
 */
public class HocrParserTest {
    private MockDocument document;
    
    public HocrParserTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        URL url = getClass().getResource("/data/test.hocr");
        document = new MockDocument();
        HocrParser parser = new HocrParser(document);
        parser.parse(url.getFile(), "null", "Utf8");
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of parse method, of class HocrParser.
     */
    @Test
    public void testParse() {
        Token token = document.findFirstToken("εἶχον");
        assertNotNull(token);
        TokenImageInfoBox box = token.getTokenImageInfoBox();
        assertNotNull(box);
        assertEquals(1316, box.getCoordinateLeft());
        assertEquals(238, box.getCoordinateTop());
        assertEquals(1474, box.getCoordinateRight());
        assertEquals(314, box.getCoordinateBottom());
    }
}
