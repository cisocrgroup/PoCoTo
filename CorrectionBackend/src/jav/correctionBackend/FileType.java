package jav.correctionBackend;

import jav.correctionBackend.export.AbbyyXmlPageParser;
import jav.correctionBackend.export.HocrPageParser;
import jav.correctionBackend.export.PageParser;
import java.io.File;
import java.io.FilenameFilter;

/**
 * Copyright (c) 2012, IMPACT working group at the Centrum für Informations- und
 * Sprachverarbeitung, University of Munich. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * This file is part of the ocr-postcorrection tool developed by the IMPACT
 * working group at the Centrum für Informations- und Sprachverarbeitung,
 * University of Munich. For further information and contacts visit
 * http://ocr.cis.uni-muenchen.de/
 *
 * @author thorsten (thorsten.vobl@googlemail.com)
 */
public enum FileType {

    ABBYY_XML_DIR(), HOCR();
    private static final String ABBYY_STR = "ABBYY_XML";
    private static final String HOCR_STR = "HOCR";

    private FileType() {
    }

    public static FileType fromString(String fileType) {
        switch (fileType) {
            case ABBYY_STR:
                return ABBYY_XML_DIR;
            case HOCR_STR:
                return HOCR;
            default:
                return ABBYY_XML_DIR;
        }
    }

    @Override
    public String toString() {
        switch (this) {
            case HOCR:
                return HOCR_STR;
            case ABBYY_XML_DIR:
            default:
                return ABBYY_STR;
        }
    }

    public FilenameFilter getFilenameFilter() {
        switch (this) {
            case HOCR:
                return new FilenameFilter() {
                    @Override
                    public boolean accept(File f, String name) {
                        return name.endsWith(".html") || name.endsWith(".hocr");
                    }
                };
            case ABBYY_XML_DIR: // fall through
            default:
                return new FilenameFilter() {
                    @Override
                    public boolean accept(File f, String name) {
                        return name.endsWith(".xml");
                    }
                };
        }
    }

    public PageParser getPageParser() {
        switch (this) {
            case HOCR:
                return new HocrPageParser();
            case ABBYY_XML_DIR:
            default:
                return new AbbyyXmlPageParser();
        }
    }
}
