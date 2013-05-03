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
import javax.swing.JPanel;

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
 * 
 * @brief dummy class for displaying disabled token visualizations
 */
public class DummyTokenVisualization extends JPanel {

    private int tokenIndex;
    private TokenTextLabel tokenTextLabel;
    private DisplayJAI djai;
    private boolean hasImage;

    public DummyTokenVisualization( Token t, String s, int fontSize) {
        super();
        this.hasImage = false;
        this.tokenIndex = t.getIndexInDocument();
        init( s, fontSize);
        
    }

    public DummyTokenVisualization( Token t, String s, int fontSize, BufferedImage b) {
        super();
        this.hasImage = true;
        this.tokenIndex = t.getIndexInDocument();
        init( s, fontSize, b);
    }
    
    private void setFontSize(int fontSize) {
        Font f = tokenTextLabel.getFont();
        float nS = (float) fontSize;
        Font deriveFont = f.deriveFont(nS);
        tokenTextLabel.setFont(deriveFont);
    }

    private void init( String s, int fontSize) {
        this.setBackground(Color.lightGray);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        this.add(Box.createVerticalGlue());
        this.tokenTextLabel = new TokenTextLabel(s);
        this.tokenTextLabel.setBackground(Color.lightGray);
        this.tokenTextLabel.setFont(new Font("DejaVu Sans", Font.PLAIN, fontSize));
        this.add(tokenTextLabel);
    }

    private void init(String s, int fontSize, BufferedImage bi) {
        this.setBackground(Color.lightGray);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        this.add(Box.createVerticalGlue());

        djai = new DisplayJAI(bi);
        djai.setBackground(Color.lightGray);
        this.add(djai);

        this.tokenTextLabel = new TokenTextLabel(s);
        this.tokenTextLabel.setBackground(Color.lightGray);

        this.tokenTextLabel.setFont(new Font("DejaVu Sans", Font.PLAIN, fontSize));
        this.add(tokenTextLabel);
        this.calculateSize();        
    }

    private void calculateSize() {
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

    public boolean isNewline() {
        if (tokenTextLabel.getText().equals("¶")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hasImage() {
        return hasImage;
    }

    public BufferedImage getImage() {
        if (this.hasImage) {
            return (BufferedImage) djai.getSource();
        } else {
            return null;
        }
    }

    public void setImage(BufferedImage bi) {
        djai.set(bi);
        this.hasImage = true;
        djai.revalidate();
        this.calculateSize();
        this.revalidate();

    }

    public void clearImage() {
        djai = new DisplayJAI();
        djai.setBackground(Color.lightGray);
        this.removeAll();
        this.add(djai);
        this.add(tokenTextLabel);
        this.calculateSize();
        this.revalidate();
    }

    public void zoomFont(int newSize) {
        if (!tokenTextLabel.isSpace()) {
            Font f = tokenTextLabel.getFont();
            float nS = (float) newSize;
            Font deriveFont = f.deriveFont(nS);
            tokenTextLabel.setFont(deriveFont);
            tokenTextLabel.revalidate();

            if (this.hasImage) {
                this.calculateSize();
                this.revalidate();
            }
        }
    }

    public String getTokenTextLabelText() {
        return tokenTextLabel.getText();
    }

    public int getTokenIndex() {
        return this.tokenIndex;
    }
}