/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

/**
 *
 * @author finkf
 */
class HocrChar {

    private HocrToken token;
    private final HocrLine line;
    private int i;

    public HocrChar(HocrToken token, HocrLine line, int i) {
        this.token = token;
        this.line = line;
        this.i = i;
    }

    public void substitute(char c) {

    }

    public char getChar() {
        return token.toString().charAt(i);
    }
}
