package jav.correctionBackend;

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
public class Page {
    private int index;
    private int tokenIndexFrom;
    private int tokenIndexTo;
    private String imageCanonical;
    private String imageFilename;
    
    protected Page( int i ) {
        this.index = i;
    }
    
    protected void setImageFilename( String s ) {
        this.imageFilename = s;
    }
    
    protected void setImageCanonical( String s ) {
        this.imageCanonical = s;
    }
    
    protected void setStartIndex( int i ) {
        this.tokenIndexFrom = i;
    }
    
    protected void setEndIndex( int i ) {
        this.tokenIndexTo = i;
    }
    
    public int getIndex() {
        return this.index;
    }
    
    public int getStartIndex() {
        return this.tokenIndexFrom;
    }
    
    public int getEndIndex() {
        return this.tokenIndexTo;
    }
    
    public String getImageFilename() {
        return this.imageFilename;
    }
    
    public String getImageCanonical() {
        return this.imageCanonical;
    }
    
    public boolean hasImage() {
        return !this.imageFilename.equals("");
    }
    
    public boolean hasTokens() {
        if( this.getEndIndex() > this.getStartIndex() ) {
            return true;
        } else {
            return false;
        }
    }
}
