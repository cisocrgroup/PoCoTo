/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

import java.util.ArrayList;

/**
 * Base class for token based Char implementations.
 *
 * @author finkf
 */
public abstract class AbstractToken<T extends Char> extends ArrayList<T> {

    /**
     * Convenience function.
     *
     * @return the first Char of this token.
     */
    public Char getFirstChar() {
        assert (!isEmpty());
        return this.get(0);
    }

    /**
     * Simple convenience function
     *
     * @return the last char of this token.
     */
    public Char getLastChar() {
        assert (!isEmpty());
        return this.get(this.size() - 1);
    }

    /**
     * Called whenever any of the characters were changed. Should rebuild the
     * token and write the info to the node.
     */
    public abstract void update();

    /**
     * Called if this token should be deleted from the xml tree. Should remove
     * the xml node from its parent e.g:
     * <code>this.getNode().getParent().removeChild(this.getNode())</code>
     */
    public abstract void removeFromTree();

    public void delete(T c) {
        int i = indexOf(c);
        if (i != -1) {
            remove(i);
            if (!isEmpty()) {
                update();
            } else {
                removeFromTree();
            }
        }
    }

    /**
     * Merges this token with its right neighbour.
     *
     * @param on the token on which the two token are merge or null if there is
     * no such token.
     * @param right the right token that get merged with this one. The right
     * token is deleted from the xml node.
     */
    public void mergeRightward(T on, AbstractToken right) {
        if (on != null) {
            add(on);
        }
        addAll(right);
        right.removeFromTree();
        update();
    }

    /**
     * Appends a new Character.
     *
     * @param at the character where the new Character is appended
     * @param newChar the new Character that is appended after at.
     */
    public void append(T at, T newChar) {
        final int i = indexOf(at);
        if (i != -1) {
            add(i + 1, newChar);
            update();
        }
    }

    /**
     * Prepends a new Character
     *
     * @param at the character where the new Character is prepended.
     * @param newChar the new character that is appended before at.
     */
    public void prepend(T at, T newChar) {
        final int i = indexOf(at);
        if (i != -1) {
            add(i, newChar);
            update();
        }
    }
}
