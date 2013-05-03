package jav.gui.token.display;

import com.sun.media.jai.widget.DisplayJAI;
import jav.correctionBackend.Token;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import javax.swing.Box;
import javax.swing.BoxLayout;

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
public class ImageTokenVisualization extends TokenVisualization {

    private DisplayJAI djai = null;
    private int imageHeight = 0;

    public ImageTokenVisualization(BufferedImage bi, Token t, int fontSize) {
        super();
        this.tokenID = t.getID();
        this.init( t, fontSize, bi);
    }

    private void init(Token t, int fontSize, BufferedImage bi) {
        this.hasImage = true;

        this.setBackground(Color.white);

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setAlignmentY(Component.BOTTOM_ALIGNMENT);

        djai = new DisplayJAI(bi);
        djai.setBackground(Color.white);
        this.add(djai);

        this.tokenTextLabel = new TokenTextLabel(t);
        this.tokenTextLabel.setBackground(Color.white);
        this.tokenTextLabel.setFont(new Font("DejaVu Sans", Font.PLAIN, fontSize));
        
        this.add(Box.createVerticalGlue());

        this.setFontSize(fontSize);
        this.add(tokenTextLabel);
        this.calculateSizeNormMode();
        this.imageHeight = djai.getPreferredSize().height;
        instance = this;
    }

    @Override
    public void calculateSizeNormMode() {
        Dimension img = djai.getPreferredSize();
        Dimension txt = tokenTextLabel.getPreferredSize();

        Dimension returnDim = new Dimension();
        returnDim.height = img.height + txt.height;
        if (img.width > txt.width) {
            returnDim.width = img.width;
        } else {
            returnDim.width = txt.width;
        }

        this.setPreferredSize(returnDim);
        this.setMaximumSize(returnDim);
    }

    @Override
    public void calculateSizeEditMode() {
        Dimension img = djai.getPreferredSize();
        Dimension txt = box.getPreferredSize();

        Dimension returnDim = new Dimension();
        returnDim.height = img.height + txt.height;
        if (img.width > txt.width) {
            returnDim.width = img.width;
        } else {
            returnDim.width = txt.width;
        }

        this.setPreferredSize(returnDim);
        this.setMaximumSize(returnDim);
    }

    public BufferedImage getImage() {
        return (BufferedImage) this.djai.getSource();
    }

    public int getImageHeight() {
        return this.imageHeight;
    }

    public void setImage(BufferedImage bi) {
        djai.set(bi);
        this.hasImage = true;
        djai.revalidate();
        this.calculateSizeNormMode();
        this.revalidate();
        this.imageHeight = djai.getPreferredSize().height;
    }

    public void clearImage() {
        djai = new DisplayJAI();
        djai.setBackground(Color.white);
        this.removeAll();
        this.add(djai);
        this.add(tokenTextLabel);
        this.calculateSizeNormMode();
        this.revalidate();
    }

    @Override
    public void update( String s ) {
        super.update( s );
        tvm.setTokenVisualizationStyle(instance);
        this.calculateSizeNormMode();
        this.revalidate();
    }

    public void update( BufferedImage bi, String s ) {
        this.update( s );
        this.setImage(bi);
    }
}