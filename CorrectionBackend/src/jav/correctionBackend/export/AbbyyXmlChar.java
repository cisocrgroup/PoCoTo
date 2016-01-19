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
        return bb;
    }

    public void setBoundingBox(BoundingBox bb) {
        this.bb = bb;
    }

    @Override
    public boolean isSuspicious() {
        Node s = node.getAttributes().getNamedItem("suspicious");
        return s != null && "1".equals(s.getNodeValue());
    }

    @Override
    public String getChar() {
        return letter;
    }

    @Override
    public Char substitute(String c) {
        setAttribute("pocotoSubstitution", letter);
        letter = c;
        node.getFirstChild().setNodeValue(letter);
        return this;
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
        nc.setAttribute("pocotoInsertion", letter);
        nc.next = this.next;
        nc.prev = this;
        this.next = nc;
        nc.node.getFirstChild().setNodeValue(str);
        return nc;
    }

    @Override
    public Char prepend(String str) {
        Node clone = node.cloneNode(true);
        node.getParentNode().insertBefore(clone, node);
        AbbyyXmlChar nc = new AbbyyXmlChar(clone);
        nc.setAttribute("pocotoInsertion", letter);
        nc.prev = this.prev;
        nc.next = this;
        this.prev = nc;
        nc.node.getFirstChild().setNodeValue(str);
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
}
