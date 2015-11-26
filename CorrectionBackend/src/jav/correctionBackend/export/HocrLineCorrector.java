/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

import java.util.ArrayList;
import org.w3c.dom.Node;

/**
 *
 * @author finkf
 */
public class HocrLineCorrector {

    private final Node node;
    private final ArrayList<HocrChar> chars;

    public HocrLineCorrector(Node node) {
        this.node = node;
        chars = new ArrayList<>();
        readChars();
    }

    public void substitute(int i, char c) {
        chars.get(i).substitute(c);
    }

    public void delete(int i) {

    }

    public void insert(int i, char c) {

    }

    private void readChars() {

    }
}
