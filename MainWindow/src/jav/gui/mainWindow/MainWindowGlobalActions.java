package jav.gui.mainWindow;

import jav.gui.cookies.PageCookie;
import jav.gui.cookies.ShowImagesCookie;
import jav.gui.cookies.TokenNavigationCookie;
import jav.gui.main.GlobalActions;

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
public class MainWindowGlobalActions extends GlobalActions implements ShowImagesCookie, PageCookie, TokenNavigationCookie {

    private MainTopComponent topc;

    public MainWindowGlobalActions( MainTopComponent tc) {
        this.topc = tc;
    }

    @Override
    public boolean isReady() {
        return topc.isReady();
    }

    @Override
    public int getPageN() {
        return topc.getPageN();
    }

    @Override
    public int getMaxPages() {
        return topc.getMaxPages();
    }

    @Override
    public void gotoPage(int p) {
        topc.gotoPage(p, null);
    }

    
    @Override
    public int getActualTokenID() {
        return topc.getActualTokenID();
    }

    @Override
    public int getNumLastToken() {
        return topc.getNumLastToken();
    }

    @Override
    public void goToNextToken() {
        topc.goToNextNormalToken();
    }

    @Override
    public void goToPreviousToken() {
        topc.goToPreviousNormalToken();
    }

    @Override
    public void goToNextLine() {
        topc.goToNextLine();
    }

    @Override
    public void goToPreviousLine() {
        topc.goToPreviousLine();
    }

    @Override
    public void goToNextSuspiciousToken() {
        topc.goToNextSuspiciousToken();
    }

    @Override
    public void goToPreviousSuspiciousToken() {
        topc.goToPreviousSuspiciousToken();
    }

    @Override
    public boolean getShowImages() {
        return topc.getShowImages();
    }

    @Override
    public void setShowImages(boolean b) {
        topc.setShowImages(b);
    }

    @Override
    public boolean hasImage() {
        return topc.hasImage();
    }

    @Override
    public void toggleImages(boolean b) {
        topc.toggleImages(b);
    }

    @Override
    public int getNumFirstSuspToken() {
        return topc.getNumFirstSuspToken();
    }

    @Override
    public int getNumLastSuspToken() {
        return topc.getNumLastSuspToken();
    }
}
