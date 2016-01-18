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
public class Token {

    private String wOCR = "";
    private String wCOR = "";
    private int tokenID;
    private int orig_id;
    private int indexInDocument;
    private boolean isNormal;
    private boolean isCorrected;
    private boolean isSuspicious = false;
    private int numCands;
    private SpecialSequenceType special_seq;
    private TokenImageInfoBox tiib;
    private int pageIndex;
    private int topcandDLev = -1;
    private String topSuggestion = "";

    public Token(String wocr) {
        this.orig_id = -1;
        this.tokenID = -1;
        this.wOCR = wocr;
    }

    public void setPageIndex(int i) {
        this.pageIndex = i;
    }

    public void setNumberOfCandidates(int i) {
        this.numCands = i;
    }

    public void setIndexInDocument(int index) {
        this.indexInDocument = index;
    }

    public void setWOCR(String s) {
        this.wOCR = s;
    }

    public void setWCOR(String s) {
        this.wCOR = s;
    }

    public void setTopCandDLev(int i) {
        this.topcandDLev = i;
    }

    public void setIsNormal(boolean b) {
        this.isNormal = b;
    }

    public void setIsCorrected(boolean b) {
        this.isCorrected = b;
    }

    public void setIsSuspicious(boolean b) {
        this.isSuspicious = b;
    }

    public void setSpecialSeq(SpecialSequenceType s) {
        this.special_seq = s;
    }

    public void setTokenImageInfoBox(TokenImageInfoBox b) {
        this.tiib = b;
    }

    public void setTopSuggestion(String s) {
        this.topSuggestion = s;
    }

    public void setId(int i) {
        this.tokenID = i;
    }

    public void setOrigID(int i) {
        this.orig_id = i;
    }

    public int getOrigID() {
        return this.orig_id;
    }

    public int getID() {
        return this.tokenID;
    }

    public int getIndexInDocument() {
        return this.indexInDocument;
    }

    public String getWOCR() {
        return this.wOCR;
    }

    public String getWCOR() {
        if (wCOR == null) {
            return "";
        } else {
            return this.wCOR;
        }
    }

    public String getWOCR_lc() {
        return this.wOCR.toLowerCase();
    }

    public int getTopCandDLev() {
        return this.topcandDLev;
    }

    public String getWCOR_lc() {
        if (wCOR == null) {
            return "";
        } else {
            return this.wCOR.toLowerCase();
        }
    }

    public boolean isNormal() {
        return this.isNormal;
    }

    public boolean isCorrected() {
        return this.isCorrected;
    }

    public boolean isSuspicious() {
        if (getWOCR().length() <= 3) { // rely on abbyy's judgment
            return this.isSuspicious;
        } else if (this.topSuggestion.equals("")) {
            // return true;
            return this.isSuspicious;
        } else {
            return this.topcandDLev > 0;
        }
    }

//    bool Token::isSuspicious() const {
//        if( getWOCR().length() <=3 ) { // rely on abbyy's judgment
//            return getAbbyySpecifics().isSuspicious();
//        }
//        else if( !hasTopCandidate() ) { // always suspicious
//            return true;
//        }
//        else {
//            return getTopCandidate().getLevDistance() > 0;
//        }
//    }
    public int getNumberOfCandidates() {
        return this.numCands;
    }

    public SpecialSequenceType getSpecialSeq() {
        return this.special_seq;
    }

    public TokenImageInfoBox getTokenImageInfoBox() {
        return this.tiib;
    }

    public String getImageFilename() {
        if (tiib != null) {
            return this.tiib.getImageFileName();
        } else {
            return "";
        }
    }

    public int getPageIndex() {
        return this.pageIndex;
    }

    public String getTopSuggestion() {
        return this.topSuggestion;
    }

    public String getWDisplay() {
        if (this.wCOR.equals("")) {
            return this.wOCR;
        } else {
            return this.wCOR;
        }
    }

    @Override
    public String toString() {
        return getWCOR() + " " + getID() + " " + getIndexInDocument();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Token other = (Token) obj;
        if (this.tokenID == other.tokenID) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + this.tokenID;
        return hash;
    }
}
