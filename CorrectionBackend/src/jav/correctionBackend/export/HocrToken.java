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
public class HocrToken {

    public final StringBuilder token;
    public final Node node;
    public HocrToken next;

    public HocrToken(String token, Node node) {
        this.token = new StringBuilder(token);
        this.node = node;
        next = null;
    }
}
