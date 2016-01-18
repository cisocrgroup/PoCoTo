/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

import jav.correctionBackend.SpreadIndexDocument;

/**
 *
 * @author finkf
 */
interface DocumentBuilder {

    public void append(Page page);

    public void append(Paragraph paragraph);

    public void append(Line line);

    public void append(Char c);

    public SpreadIndexDocument build();
}
