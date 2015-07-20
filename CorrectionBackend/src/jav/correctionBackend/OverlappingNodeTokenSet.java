/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend;

import java.util.ArrayList;

/**
 *
 * @author finkf
 */
public class OverlappingNodeTokenSet {

    public class Node {
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
    public class Compound {
        private final ArrayList<Node> nodes;
        private final ArrayList<Token> tokens;
        public Compound() {
            nodes = new ArrayList<>();
            tokens = new ArrayList<>();
        }
        public boolean overlaps(Node node) {
            for (Token token: tokens) {
                if (token.getTokenImageInfoBox()
                        .overlapsWith(node.getTokenImageInfoBox())) {
                    return true;
                }
            }
            return false;
        }
        public boolean overlaps(Token token) {
            for (Node node: nodes) {
                if (node.getTokenImageInfoBox()
                        .overlapsWith(token.getTokenImageInfoBox())) {
                    return true;
                }
            }
            return false;
        }
        public int getNumberOfTokens() {
            return tokens.size();
        }
        public int getNumberOfNodes() {
            return nodes.size();
        }
        public void add(Node node) {
            nodes.add(node);
        }
        public void add(Token token) {
            tokens.add(token);
        }
        public Node getNodeAt(int index) {
            return nodes.get(index);
        }
        public Token getTokenAt(int index) {
            return tokens.get(index);
        }
    }
    
    private final ArrayList<Compound> compounds;
    public OverlappingNodeTokenSet() {
        compounds = new ArrayList<>();
    }
    public Compound get(int index) {
        return compounds.get(index);
    }
    public int size() {
        return compounds.size();
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
            if (c.overlaps(token))
                c.add(token);
        }
    }
}
