package jav.gui.token.display;

import jav.correctionBackend.Token;
import jav.gui.main.MainController;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;

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
 *
 * visualization that substitutes the space for the image by invisible spacers
 * to achieve alignment (java layouts suck at vertical aligning)
 */
public class PseudoImageTokenVisualization extends TokenVisualization {

    private Component filler;
    private int origFillerHeight;
    private int tempFillerHeight;

    public PseudoImageTokenVisualization(Token t, int fontSize, int fillerSize) {
        super();
        this.hasImage = false;
        this.tokenID = t.getID();
        init(t, fontSize, fillerSize);
    }

    private void init(Token t, int fontSize, int fillerSize) {

        this.setBackground(Color.white);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setAlignmentY(Component.BOTTOM_ALIGNMENT);

        this.tokenTextLabel = new TokenTextLabel(t);
        this.tokenTextLabel.setBackground(Color.white);
        this.tokenTextLabel.setFont(MainController.findInstance().getMainFont(fontSize));

        filler = Box.createVerticalStrut(fillerSize + 1);
        this.origFillerHeight = fillerSize;
        this.tempFillerHeight = fillerSize;
        this.add(filler);
        this.add(Box.createVerticalGlue());
        this.add(tokenTextLabel);

        this.calculateSizeNormMode();
        instance = this;
    }

    @Override
    public void calculateSizeNormMode() {
        if (filler == null) {
            this.setPreferredSize(tokenTextLabel.getPreferredSize());
            this.setMaximumSize(tokenTextLabel.getPreferredSize());
        } else {
            Dimension img = filler.getPreferredSize();
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
    }

    @Override
    public void calculateSizeEditMode() {
        if (filler == null) {
            this.setPreferredSize(box.getPreferredSize());
            this.setMaximumSize(box.getPreferredSize());
        } else {
            Dimension img = filler.getPreferredSize();
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
    }

    public int getImageHeight() {
        return filler.getHeight();
    }

    public void toggleImage(boolean on) {
        if (on) {
            filler = Box.createVerticalStrut(this.tempFillerHeight);
            this.add(filler, 0);
            this.calculateSizeNormMode();
            this.revalidate();
        } else {
            this.remove(filler);
            filler = Box.createVerticalStrut(1);
            this.add(filler, 0);
            this.calculateSizeNormMode();
            this.revalidate();
        }
    }

    public void zoomFiller(double scale) {
        double news = this.origFillerHeight * scale;
        this.tempFillerHeight = (int) (news + 0.5d);
        this.remove(filler);
        filler = Box.createVerticalStrut(tempFillerHeight);
        this.add(filler, 0);
        this.calculateSizeNormMode();
        this.revalidate();
    }
}
