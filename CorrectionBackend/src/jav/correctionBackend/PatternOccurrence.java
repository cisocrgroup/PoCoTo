package jav.correctionBackend;

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
public class PatternOccurrence {

    private final String tokenString;
    private String suggestion;
    private final int occsN;
    private final int patternID;
    private final int part;
    private int corrected;

    public PatternOccurrence(int i, int p, String t, String s, int d, int c) {
        this.patternID = i;
        this.part = p;
        this.tokenString = t;
        this.suggestion = s;
        this.occsN = d;
        this.corrected = c;
    }

    public int getPatternID() {
        return this.patternID;
    }

    public int getPart() {
        return this.part;
    }

    public void setCorrected(boolean b) {
        if (b) {
            this.corrected++;
        } else {
            this.corrected--;
        }
    }

    public String getWOCR_LC() {
        return tokenString;
    }

    public String getWSuggestion() {
        return suggestion;
    }

    public int getOccurencesN() {
        return occsN;
    }

    public int getCorrected() {
        return this.corrected;
    }

    public void setWSuggestion(String s) {
        this.suggestion = s;
    }

    @Override
    public String toString() {
        return String.format("{id:%d, token:%s, suggestion:%s}", patternID, tokenString, suggestion);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final PatternOccurrence other = (PatternOccurrence) obj;
        if (other.getWOCR_LC().equals(this.getWOCR_LC())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.tokenString != null ? this.tokenString.hashCode() : 0);
        return hash;
    }
}
