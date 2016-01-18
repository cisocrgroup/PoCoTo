/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author finkf
 */
public class AbbyyXmlChar implements Char {

    private final Node node;
    private String letter;
    private BoundingBox bb;
    private AbbyyXmlChar prev, next;

    public AbbyyXmlChar(char c, Node node) {
        this(node);
        node.getFirstChild().setNodeValue(letter);
    }

    public AbbyyXmlChar(Node node) {
        this.node = node;
        this.letter = node.getFirstChild().getNodeValue();
        bb = null;
        prev = next = null;
    }

    @Override
    public Char getPrev() {
        return prev;
    }

    public void setPrev(AbbyyXmlChar left) {
        this.prev = left;
    }

    @Override
    public Char getNext() {
        return next;
    }

    public void setNext(AbbyyXmlChar right) {
        this.next = right;
    }

    @Override
    public BoundingBox getBoundingBox() {
        if (bb == null) {
            Node l = node.getAttributes().getNamedItem("l");
            Node t = node.getAttributes().getNamedItem("t");
            Node r = node.getAttributes().getNamedItem("r");
            Node b = node.getAttributes().getNamedItem("b");
            bb = new BoundingBox(getInt(l), getInt(t), getInt(r), getInt(b));
        }
        return bb;
    }

    @Override
    public String getChar() {
        return letter;
    }

    @Override
    public void substitute(String c) {
        setAttribute("pocotoSubstitute", letter);
        letter = c;
        node.getFirstChild().setNodeValue(letter);
    }

    @Override
    public void delete() {
        if (prev != null) {
            prev.next = this.next;
        }
        if (next != null) {
            next.prev = this.prev;
        }
        node.getParentNode().removeChild(node);
    }

    @Override
    public Char append(String str) {
        Node clone = node.cloneNode(true);
        node.getParentNode().insertBefore(clone, node.getNextSibling());
        AbbyyXmlChar nc = new AbbyyXmlChar(clone);
        nc.next = this.next;
        nc.prev = this;
        this.next = nc;
        return nc;
    }

    @Override
    public Char prepend(String str) {
        Node clone = node.cloneNode(true);
        node.getParentNode().insertBefore(clone, node);
        AbbyyXmlChar nc = new AbbyyXmlChar(clone);
        nc.prev = this.prev;
        nc.next = this;
        this.prev = nc;
        return nc;
    }

    public Node getNode() {
        return node;
    }

    @Override
    public String toString() {
        return letter;
    }

    private void setAttribute(String key, String val) {
        NamedNodeMap attrs = node.getAttributes();
        Node attr = node.getOwnerDocument().createAttribute(key);
        attr.setNodeValue(val);
        attrs.setNamedItem(attr);
    }

    private int getInt(Node node) {
        if (node != null) {
            return Integer.parseInt(node.getNodeValue());
        }
        return -1;
    }
}
