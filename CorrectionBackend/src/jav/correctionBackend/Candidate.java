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
public class Candidate {

    private final int tokenID;
    private final int rank;
    private final String suggestion;
    private final String interpretation;
    private final double voteweight;
    private final int dlev;

    protected Candidate(int i, int r, String s, String interp, double v, int l) {
        this.tokenID = i;
        this.rank = r;
        this.suggestion = s;
        this.interpretation = interp;
        this.voteweight = v;
        this.dlev = l;
    }

    public String getInterpretation() {
        return this.interpretation;
    }

    public int getTokenID() {
        return this.tokenID;
    }

    public int getRank() {
        return this.rank;
    }

    public String getSuggestion() {
        return this.suggestion;
    }

    public int getDlev() {
        return this.dlev;
    }

    public double getVoteweight() {
        return this.voteweight;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("suggestion: ")
                .append(getSuggestion())
                .append(" interpretation: ")
                .append(getInterpretation())
                .append(" id: ")
                .append(getTokenID())
                .append(" rank: ")
                .append(getRank())
                .append(" getDlev: ")
                .append(getDlev())
                .append(" voteweight: ")
                .append(getVoteweight())
                .toString();
    }
}
