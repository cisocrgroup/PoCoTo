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

    /**
     * Check if a given code point is a word character. Word characters are all
     * letters, numbers, and combining word characters.
     *
     * @param codepoint the given unicode code point
     * @return true if the given code point is a word character.
     */
    public static boolean isWordCharacter(int codepoint) {
        return Character.isAlphabetic(codepoint)
                || Character.isDigit(codepoint)
                || Character.getType(codepoint) == Character.NON_SPACING_MARK;
    }

    /**
     * Check if a given code point is white space
     *
     * @param codepoint the given unicode code point
     * @return true if the character is a whitespace character
     */
    public static boolean isWhitespaceCharacter(int codepoint) {
        return Character.isWhitespace(codepoint);
    }

    /**
     * Check if a given code point is neither a word character nor a whitespace
     * character.
     *
     * @param codepoint the given unicode code point
     * @return true if the character is neither a word character nor a
     * whitespace character
     */
    public static boolean isNonWordCharacter(int codepoint) {
        return !isWordCharacter(codepoint) && !isWhitespaceCharacter(codepoint);
    }
}
