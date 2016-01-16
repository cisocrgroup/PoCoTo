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
        this(node);
        setChar(c);
    }

    public AbbyyXmlChar(Node node) {
        this.node = node;
        this.letter = node.getFirstChild().getNodeValue();
    }

    public char getChar() {
        return letter.charAt(0);
    }

    public void substitute(char c) {
        setChar(c);
    }

    public void delete() {
        node.getParentNode().removeChild(node);
    }

    public Node getNode() {
        return node;
    }

    private void setChar(char c) {
        char tmp[] = {c};
        letter = new String(tmp);
        node.getFirstChild().setNodeValue(letter);
    }
}
