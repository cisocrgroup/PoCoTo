package jav.gui.events.concordance;

import jav.correctionBackend.Token;
import jav.gui.events.Event;
import java.util.ArrayList;

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
public class ConcordanceEvent extends Event {

    private final ArrayList<Token> arrTok;
    private final String name;
    private final ConcordanceType ct;

    public ConcordanceEvent(Object s, ConcordanceType t, ArrayList<Token> a, String n) {
        super(s);
        if (a != null) {
            this.arrTok = a;
        } else {
            arrTok = new ArrayList<>();
        }
        this.name = n;
        this.ct = t;
    }

    public String getName() {
        return this.name;
    }

    public ArrayList<Token> getArray() {
        return this.arrTok;
    }

    public ConcordanceType getConcordanceType() {
        return this.ct;
    }
}
