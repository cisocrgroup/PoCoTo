/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.util;

import java.io.File;

/**
 *
 * @author flo
 */
public class FilePath {

    /**
     * Removes all extensions from a file's name.
     *
     * @param file The file
     * @return The name of the file with all it's extensions removed.
     */
    public static String removeAllExtensionsFromFileName(File file) {
        return removeAllExtensionsFromFileName(file.getName());
    }

    /**
     * Removes all extensions from a file name.
     *
     * @param fileName the name of a file
     * @return the name of the file with all its extensions removed.
     */
    public static String removeAllExtensionsFromFileName(String fileName) {
        final int i = fileName.indexOf(".");
        return i != -1 ? fileName.substring(0, i) : fileName;
    }

    private FilePath() {
    }
}
