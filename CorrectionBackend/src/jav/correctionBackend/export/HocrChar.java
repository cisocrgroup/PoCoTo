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

    private final HocrToken token;
    private final HocrLine line;

    public HocrChar(HocrToken token, HocrLine line) {
        this.token = token;
        this.line = line;
    }

    public void substitute(char c) {

    }
}
