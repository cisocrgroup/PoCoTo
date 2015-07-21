/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend;

import jav.correctionBackend.CompoundSet.Compound;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author finkf
 */
public class CompoundSet implements Iterable<Compound> {

    static public class Node {
        private final org.w3c.dom.Node node;
        private final TokenImageInfoBox tiib;
        public Node(org.w3c.dom.Node node, TokenImageInfoBox tiib) {
            this.node = node;
            this.tiib = tiib;
        }
        public TokenImageInfoBox getTokenImageInfoBox() {
            return tiib;
        }
        public org.w3c.dom.Node getNode() {
            return node;
        }
    }
    public class Compound implements Iterable<Token> {
        private final Node node;
        private final Set<Token> tokens;
        
        public Compound(Node node) {
            assert(node != null);
            this.node = node;
            tokens = new HashSet<>();
        }
        public boolean overlaps(Token token) {
            return node.getTokenImageInfoBox()
                    .overlapsWith(token.getTokenImageInfoBox());
        }
        public int getNumberOfTokens() {
            return tokens.size();
        }
        public void add(Token token) {
            tokens.add(token);
        }
        public Node getNode() {
            return node;
        }
        @Override
        public Iterator<Token> iterator() {
            return tokens.iterator();
        }
    }
    
    private final ArrayList<Compound> compounds;
    public CompoundSet() {
        compounds = new ArrayList<>();
    }
    public Compound get(int index) {
        return compounds.get(index);
    }
    public int size() {
        return compounds.size();
    }
    @Override
    public Iterator<Compound> iterator() {
        return compounds.iterator();
    }
    public void add(Node node, Iterable<Token> tokens) {
        Compound x = null;
        for (Compound c: compounds) {
            if (c.overlaps(node)) {
                x = c;
                break;
            }
        }
        if (x == null) {
            x = new Compound();
            compounds.add(x);
        }
        add(x, node, tokens);
    }
    private void add(Compound c, Node node, Iterable<Token> tokens) {
        c.add(node);
        for (Token token: tokens) {
            if (c.overlaps(token)) {
                c.add(token);
            }
        }
    }
}
