/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.util;

/**
 *
 * @author flo
 */
public class Tokenization {

    private Tokenization() {

    }

    public static boolean isWordCharacter(int codepoint) {
        return Character.isAlphabetic(codepoint)
                || Character.isDigit(codepoint)
                || Character.getType(codepoint) == Character.NON_SPACING_MARK;
    }

    public static boolean isWhitespaceCharacter(int codepoint) {
        return Character.isWhitespace(codepoint);
    }
}
