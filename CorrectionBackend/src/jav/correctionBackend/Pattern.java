package jav.correctionBackend;

import java.util.Comparator;
import java.util.HashSet;

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
public class Pattern {

    private final String left, right;
    private final HashSet<PatternOccurrence> occs;
    private int corrected;
    private int occsN;
    private final int patternID;

    public Pattern(int i, String l, String r, int o, int c) {
        this.patternID = i;
        this.occs = new HashSet<>();
        this.left = l;
        this.right = r;
        this.occsN = o;
        this.corrected = c;
    }

    public void addOccurence(PatternOccurrence po, boolean adjustvalues) {
        if (!this.occs.contains(po)) {
            this.occs.add(po);
            if (adjustvalues) {
                this.corrected += po.getCorrected();
                this.occsN += po.getOccurencesN();
            }
        }
    }

    public int getPatternID() {
        return this.patternID;
    }

    public HashSet<PatternOccurrence> getOccurences() {
        return occs;
    }

    public String getLeft() {
        return this.left;
    }

    public String getRight() {
        return this.right;
    }

    public int getOccurencesN() {
        return this.occsN;
    }

    public int getCorrected() {
        return this.corrected;
    }

    public void setCorrected(boolean b) {
        if (b) {
            this.corrected++;
        } else {
            this.corrected--;
        }
    }

    @Override
    public String toString() {
        return String.format("{id: %d, left: %s, right: %s}", patternID, left, right);
    }
}

class MyComparator implements Comparator<PatternOccurrence> {

    @Override
    public int compare(PatternOccurrence po1, PatternOccurrence po2) {
        int o1 = po1.getOccurencesN();
        int o2 = po2.getOccurencesN();

        if (o1 < o2) {
            return 1;
        } else if (o1 > o2) {
            return -1;
        } else {
            return po1.getWOCR_LC().compareToIgnoreCase(po2.getWOCR_LC());
        }
    }
}
