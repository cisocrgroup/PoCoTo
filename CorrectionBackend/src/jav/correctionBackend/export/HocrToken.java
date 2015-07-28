/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

import jav.correctionBackend.Token;
import jav.correctionBackend.TokenImageInfoBox;

/**
 *
 * @author finkf
 */
public class HocrToken implements HasTokenImageInfoBox {
    private final Token token;
    HocrToken(Token token) {
        this.token = token;
    }
    
    @Override
    public TokenImageInfoBox getTokenImageInfoBox() {
        return token.getTokenImageInfoBox();
    }
    public Token getToken() {
        return token;
    }
    @Override 
    public String toString() {
        return new StringBuilder(token.getWOCR())
                .append('(')
                .append(token.getWCOR())
                .append(')')
                .toString();
    }
}
