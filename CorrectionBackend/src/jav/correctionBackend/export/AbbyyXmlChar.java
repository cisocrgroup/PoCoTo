/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

import org.w3c.dom.Node;

/**
 *
 * @author finkf
 */
public class AbbyyXmlChar {

    private final Node node;
    private String letter;

    public AbbyyXmlChar(char c, Node node) {
        this.node = node;
        letter = "";
        substitute(c);
    }

    public AbbyyXmlChar(Node node) {
        this.node = node;
        this.letter = parseLetter();
    }

    public char getChar() {
        return letter.charAt(0);
    }

    public void substitute(char c) {
        letter = "" + c;
        node.getFirstChild().setNodeValue(letter);
    }

    public void delete() {
        node.getParentNode().removeChild(node);
    }

    public Node getNode() {
        return node;
    }

    private String parseLetter() {
        return node.getFirstChild().getNodeValue();
    }

}
