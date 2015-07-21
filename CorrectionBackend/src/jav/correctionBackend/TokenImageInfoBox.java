package jav.correctionBackend;

import java.util.Arrays;

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
public class TokenImageInfoBox {

    private int coordinate_left;
    private int coordinate_right;
    private int coordinate_top;
    private int coordinate_bottom;
    private String imageFileName;

    public TokenImageInfoBox(int l, int t, int r, int b, String img) {
        coordinate_left = l;
        coordinate_top = t;
        coordinate_right = r;
        coordinate_bottom = b;        
        imageFileName = img;
    }
    
    public TokenImageInfoBox(int l, int t, int r, int b) {
        this(l, t, r, b, "");
    }
    public TokenImageInfoBox() {
        this(0, 0, 0, 0, "");
    }

    public int getCoordinateLeft() {
        if( coordinate_left < 0 ) {
            return 1;
        } else {
            return coordinate_left;
        }
    }

    public void setCoordinateLeft(int i) {
        if( i < 0 ) {
            this.coordinate_left = 1;
        } else {
            this.coordinate_left = i;
        }
    }

    public int getCoordinateRight() {
        if( coordinate_right < 0 ) {
            return 1;
        } else {
            return coordinate_right;
        }
    }

    public void setCoordinateRight(int i) {
        if( i < 0 ) {
            this.coordinate_right = 1;
        } else {
            this.coordinate_right = i;
        }
    }

    public int getCoordinateTop() {
        if( coordinate_top < 0 ) {
            return 1;
        } else {
            return coordinate_top;            
        }
    }

    public void setCoordinateTop(int i) {
        if( i < 0 ) {
            this.coordinate_top = 1;
        } else {
            this.coordinate_top = i;
        }
    }

    public int getCoordinateBottom() {
        if( coordinate_bottom < 0 ) {
            return 1;
        } else {
            return coordinate_bottom;
        }
    }

    public void setCoordinateBottom(int i) {
        if( i < 0 ) {
            this.coordinate_bottom = 1;
        } else {
            this.coordinate_bottom = i;
        }
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String s) {
        this.imageFileName = s;
    }
    
    public boolean overlapsWith(TokenImageInfoBox other) {
        if (other == null)
            return false;
        if (!imageFileName.equals(other.imageFileName))
            return false;
        if (coordinate_right < other.coordinate_left)
            return false;
        if (coordinate_left > other.coordinate_right)
            return false;
        if (coordinate_bottom < other.coordinate_top)
            return false;
        if (coordinate_top > other.coordinate_bottom)
            return false;
        return true;
    }
    
    public TokenImageInfoBox calculateOverlappingBox(TokenImageInfoBox other) {
        if (! overlapsWith(other))
            return new TokenImageInfoBox();
        assert(overlapsWith(other));
        int h[] = {coordinate_left, coordinate_right, other.coordinate_left, other.coordinate_right};
        int v[] = {coordinate_top, coordinate_bottom, other.coordinate_top, other.coordinate_bottom};
        Arrays.sort(h);
        Arrays.sort(v);
        return new TokenImageInfoBox(h[1], v[1], h[2], v[2], imageFileName);
    }
    
    public int getArea() {
        return Math.abs(coordinate_left - coordinate_right) *
                Math.abs(coordinate_top - coordinate_bottom);
    }
    
    @Override
    public String toString() {
        return new StringBuilder()
                .append(imageFileName)
                .append(' ')
                .append(coordinate_left)
                .append(' ')                
                .append(coordinate_top)
                .append(' ')
                .append(coordinate_right)
                .append(' ')
                .append(coordinate_bottom)
                .toString();
    }
}
