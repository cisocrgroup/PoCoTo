package jav.gui.main;

import jav.gui.events.documentChanged.DocumentChangedEventSlot;
import jav.gui.events.tokenDeselection.TokenDeselectionEventSlot;
import jav.gui.events.tokenSelection.TokenSelectionEventSlot;
import jav.gui.events.tokenStatus.TokenStatusEventSlot;
import jav.gui.cookies.FontZoomCookie;
import jav.gui.cookies.ImageZoomCookie;
import jav.gui.cookies.PageCookie;
import jav.gui.events.special.multiselection.MultiSelectionEvent;

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
 * @description abstract class that defines basic properties of topcomponents that go in the editor mode
 *              (MainView and Konkordanz)
 */
public abstract class AbstractEditorViewTopComponent extends AbstractMyTopComponent implements DocumentChangedEventSlot, TokenSelectionEventSlot, TokenDeselectionEventSlot, TokenStatusEventSlot, FontZoomCookie, ImageZoomCookie, PageCookie {
    public abstract GlobalActions getGlobalActions();
    public abstract void dispatchMultiSelectionEvent( MultiSelectionEvent e);
    public abstract boolean isEditing();
//    public abstract TokenVisualizationRegistry getTokenVisualizationRegistry();
}
