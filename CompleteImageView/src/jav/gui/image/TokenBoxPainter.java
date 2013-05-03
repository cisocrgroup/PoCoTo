package jav.gui.image;  

import jav.correctionBackend.Token;
import jav.correctionBackend.TokenImageInfoBox;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JScrollPane;
import jpl.mipl.jade.JadeDisplay;
import jpl.mipl.jade.OverlayPainter;

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
 * paints overlay rectangles to mark the current token in the complete image
 */
public class TokenBoxPainter implements OverlayPainter {

    private JadeDisplay _disp;
    private JScrollPane _sp;
    private Rectangle _oldRect;
    private Rectangle _newRect;
    private boolean hasPainter = false;

    public TokenBoxPainter(JadeDisplay disp, JScrollPane sp) {
        this._disp = disp;
        this._sp = sp;
        _oldRect = null;
        _newRect = null;
        _disp.addOverlayPainter(this);
        this.hasPainter = true;
    }

    @Override
    public void paintOverlay(Graphics g) {
        if (_newRect != null) {
            g.setColor(Color.red);
            g.drawRect(_newRect.x, _newRect.y, _newRect.width, _newRect.height);
        }
    }

    public void paintTokenBox(Token t, double scale) {
        if( !this.hasPainter ) {
            _disp.addOverlayPainter(this);
        }
        TokenImageInfoBox tiib = t.getTokenImageInfoBox();
        if (tiib != null) {
            int left = (int) (tiib.getCoordinateLeft() * scale);
            int right = (int) (tiib.getCoordinateRight() * scale);
            int top = (int) (tiib.getCoordinateTop() * scale);
            int bottom = (int) (tiib.getCoordinateBottom() * scale);

            int width = right - left;
            int height = bottom - top;
            _newRect = new Rectangle(left, top, width, height);

            if (!_sp.getViewport().getViewRect().contains(_newRect)) {
                Rectangle visible = _sp.getViewport().getVisibleRect();
                int p_left = left - ((visible.width - width) / 2);
                if (p_left < 0) {
                    p_left = 0;
                }
                int p_top = top - ((visible.height - height) / 2);
                if (p_top < 0) {
                    p_top = 0;
                }
                Point p = new Point(p_left, p_top);               
                _sp.getViewport().setViewPosition(p);
            }

            if (_oldRect == null) {
                _oldRect = new Rectangle(left, top, width, height);
                Rectangle pRec = new Rectangle(left, top, width + 1, height + 1);
                _disp.paintImmediately(pRec); //.paintNoErase(pRec);
            } else {
                Rectangle combRect = new Rectangle();
                Rectangle pRec = new Rectangle(left, top, width + 1, height + 1);
                combRect.add(pRec);
                Rectangle pORec = new Rectangle(_oldRect.x, _oldRect.y,
                        _oldRect.width + 1, _oldRect.height + 1);
                combRect.add(pORec);
                _disp.paintNoErase(combRect);

                _oldRect = _newRect;
            }
        }
    }
    
    public void setHasPainter(boolean b) {
        this.hasPainter = b;
    }
}
