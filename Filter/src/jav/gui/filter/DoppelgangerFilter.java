package jav.gui.filter;

import jav.correctionBackend.Token;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *Copyright (c) 2012, IMPACT working group at the Centrum für Informations- und Sprachverarbeitung, University of Munich.
 *All rights reserved.

 *Redistribution and use in source and binary forms, with or without
 *modification, are permitted provided that the following conditions are met:

 *Redistributions of source code must retain the above copyright
 *notice, this list of conditions and the following disclaimer.
 *Redistributions in binary form must reproduce the above copyright
 *notice, this list of conditions and the following disclaimer in the
 *documentation and/or other materials provided with the distribution.

 *THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 *IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 *TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 *PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This file is part of the ocr-postcorrection tool developed
 * by the IMPACT working group at the Centrum für Informations- und Sprachverarbeitung, University of Munich.
 * For further information and contacts visit http://ocr.cis.uni-muenchen.de/
 * 
 * @author thorsten (thorsten.vobl@googlemail.com)
 */
public class DoppelgangerFilter implements AbstractTokenFilter {

    private Token param;
    private String name;
    private boolean retVal;
    private ArrayList<Token> tokRL;

    public DoppelgangerFilter(Token t, String n) {
        this.param = t;
        this.name = n;
    }

    @Override
    public void setName(String n) {
        this.name = n;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setParam(Token t) {
        this.param = t;
    }

    public Token getParam() {
        return this.param;
    }

    @Override
    public boolean applies(Token t) {
        retVal = false;
        if (this.param.getWDisplay().equals(t.getWDisplay()) & !t.isCorrected()) {
            retVal = true;
        } else {
            retVal = false;
        }
        return retVal;
    }

    @Override
    public ArrayList<Token> applyFilter(Iterator<Token> i) {
        try {
            tokRL = new ArrayList<Token>();
            while (i.hasNext()) {
                if (!Thread.currentThread().isInterrupted()) {
                    Token tok = i.next();
                    if (!Thread.currentThread().isInterrupted() && this.applies(tok)) {
                        tokRL.add(tok);
                    }
                } else {
                    return null;
                }
            }
            return tokRL;
        } catch (Error e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public ArrayList<Token> applyFilter(ArrayList<Token> a) {
        tokRL = new ArrayList<Token>();
        for (Token tok : a) {
            if (this.applies(tok)) {
                tokRL.add(tok);
            }
        }
        return tokRL;
    }
}
