/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author finkf
 */
public class AbbyyXmlChar extends AbstractBaseChar {

    private final Node node;
    private int letter;
    private final BoundingBox bb;

    public AbbyyXmlChar(Line line, Node node, BoundingBox bb) {
        super(line);
        this.node = node;
        this.letter = node.getFirstChild().getNodeValue().codePointAt(0);
        this.bb = bb;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return bb;
    }

    @Override
    public boolean isSuspicious() {
        Node s = node.getAttributes().getNamedItem("suspicious");
        return s != null && "1".equals(s.getNodeValue());
    }

    @Override
    public int getChar() {
        return letter;
    }

    @Override
    public void substitute(int c) {
        setAttribute("pocotoSubstitution", new String(Character.toChars(letter)));
        letter = c;
        node.getFirstChild().setNodeValue(new String(Character.toChars(letter)));
    }

    @Override
    public void delete() {
        final int i = getIndexInLine();
        if (i != -1) {
            node.getParentNode().removeChild(node);
            getLine().remove(i);
        }
    }

    @Override
    public void prepend(int c) {
        final int i = getIndexInLine();
        if (i != -1) {
            AbbyyXmlChar clone = clone(c);
            clone.setAttribute("pocotoPrepend", new String(Character.toChars(c)));
            node.getParentNode().insertBefore(clone.node, this.node);
            getLine().add(i, clone);
        }
    }

    @Override
    public void append(int c) {
        final int i = getIndexInLine();
        if (i != -1) {
            AbbyyXmlChar clone = clone(c);
            clone.setAttribute("pocotoAppend", new String(Character.toChars(c)));
            node.getParentNode().appendChild(clone.node);
            getLine().add(clone);
        }
    }

    @Override
    public String toString() {
        return new String(Character.toChars(letter));
    }

    private AbbyyXmlChar clone(int c) {
        Node clone = node.cloneNode(true);
        clone.getFirstChild().setNodeValue(new String(Character.toChars(c)));
        return new AbbyyXmlChar(getLine(), clone, bb);
    }

    private void setAttribute(String key, String val) {
        NamedNodeMap attrs = node.getAttributes();
        Node attr = node.getOwnerDocument().createAttribute(key);
        attr.setNodeValue(val);
        attrs.setNamedItem(attr);
    }
}
