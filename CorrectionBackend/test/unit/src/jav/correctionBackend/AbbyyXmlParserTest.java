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
public class AbbyyXmlParserTest {
    String testFile;
    public AbbyyXmlParserTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        URL url = getClass().getResource("/data/test.abbyy.xml");
        testFile = url.getFile();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of parse method, of class AbbyyXmlParser.
     */
    @Test
    public void testParse() {
        MockDocument document = new MockDocument();
        AbbyyXmlParser parser = new AbbyyXmlParser(document);
        parser.parse(testFile, "null", null);
        Token token = document.findFirstToken("Cap");
        assertNotNull(token);
        TokenImageInfoBox box = token.getTokenImageInfoBox();
        assertNotNull(box);
        assertEquals(300, box.getCoordinateLeft());
        assertEquals(125, box.getCoordinateTop());
        assertEquals(413, box.getCoordinateRight());
        assertEquals(207, box.getCoordinateBottom());
    }   
}
