package jav.gui.events;

import jav.gui.events.cancel.CancelEvent;
import jav.gui.events.cancel.CancelEventGenerator;
import jav.gui.events.cancel.CancelEventSlot;
import jav.gui.events.concordance.ConcordanceEvent;
import jav.gui.events.concordance.ConcordanceEventGenerator;
import jav.gui.events.concordance.ConcordanceEventSlot;
import jav.gui.events.documentChanged.DocumentChangedEvent;
import jav.gui.events.documentChanged.DocumentChangedEventGenerator;
import jav.gui.events.documentChanged.DocumentChangedEventSlot;
import jav.gui.events.pageChanged.PageChangedEvent;
import jav.gui.events.pageChanged.PageChangedEventGenerator;
import jav.gui.events.pageChanged.PageChangedEventSlot;
import jav.gui.events.saved.SavedEvent;
import jav.gui.events.saved.SavedEventGenerator;
import jav.gui.events.saved.SavedEventSlot;
import jav.gui.events.tokenDeselection.TokenDeselectionEvent;
import jav.gui.events.tokenDeselection.TokenDeselectionEventGenerator;
import jav.gui.events.tokenDeselection.TokenDeselectionEventSlot;
import jav.gui.events.tokenMultiDeselection.TokenMultiDeselectionEvent;
import jav.gui.events.tokenMultiDeselection.TokenMultiDeselectionEventGenerator;
import jav.gui.events.tokenMultiDeselection.TokenMultiDeselectionEventSlot;
import jav.gui.events.tokenMultiSelection.TokenMultiSelectionEvent;
import jav.gui.events.tokenMultiSelection.TokenMultiSelectionEventGenerator;
import jav.gui.events.tokenMultiSelection.TokenMultiSelectionEventSlot;
import jav.gui.events.tokenNavigation.TokenNavigationEvent;
import jav.gui.events.tokenNavigation.TokenNavigationEventGenerator;
import jav.gui.events.tokenNavigation.TokenNavigationEventSlot;
import jav.gui.events.tokenSelection.TokenSelectionEvent;
import jav.gui.events.tokenSelection.TokenSelectionEventGenerator;
import jav.gui.events.tokenSelection.TokenSelectionEventSlot;
import jav.gui.events.tokenStatus.TokenStatusEvent;
import jav.gui.events.tokenStatus.TokenStatusEventGenerator;
import jav.gui.events.tokenStatus.TokenStatusEventSlot;

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
public class MessageCenter {

    private static MessageCenter instance;
    private DocumentChangedEventGenerator documentChangedEventGenerator;
    private TokenSelectionEventGenerator tokenSelectionEventGenerator;
    private PageChangedEventGenerator pageChangedEventGenerator;
    private TokenStatusEventGenerator tokenStatusEventGenerator;
    private TokenDeselectionEventGenerator tokenDeselectionEventGenerator;
    private SavedEventGenerator savedEventGenerator;
    private ConcordanceEventGenerator concordanceEventGenerator;
    private TokenNavigationEventGenerator tokenNavigationEventGenerator;
    private TokenMultiSelectionEventGenerator tokenMultiSelectionEventGenerator;
    private TokenMultiDeselectionEventGenerator tokenMultiDeselectionEventGenerator;
    private CancelEventGenerator cancelEventGenerator;

    private MessageCenter() {
        documentChangedEventGenerator = new DocumentChangedEventGenerator();
        tokenSelectionEventGenerator = new TokenSelectionEventGenerator();
        pageChangedEventGenerator = new PageChangedEventGenerator();
        tokenStatusEventGenerator = new TokenStatusEventGenerator();
        tokenDeselectionEventGenerator = new TokenDeselectionEventGenerator();
        savedEventGenerator = new SavedEventGenerator();
        concordanceEventGenerator = new ConcordanceEventGenerator();
        tokenNavigationEventGenerator = new TokenNavigationEventGenerator();
        tokenMultiSelectionEventGenerator = new TokenMultiSelectionEventGenerator();
        tokenMultiDeselectionEventGenerator = new TokenMultiDeselectionEventGenerator();
        cancelEventGenerator = new CancelEventGenerator();
    }

    public static MessageCenter getInstance() {
        if (instance == null) {
            instance = new MessageCenter();
        }
        return instance;
    }

    public void addDocumentChangedEventListener(final DocumentChangedEventSlot listener) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                documentChangedEventGenerator.addListener(listener);
            }
        });
        t.start();

    }

    public void removeDocumentChangedEventListener(final DocumentChangedEventSlot listener) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                documentChangedEventGenerator.removeListener(listener);
            }
        });
        t.start();
    }

    public void fireDocumentChangedEvent(DocumentChangedEvent e) {
        documentChangedEventGenerator.fireEvent(e);
    }

    public void addPageChangedEventListener(final PageChangedEventSlot listener) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                pageChangedEventGenerator.addListener(listener);
            }
        });
        t.start();

    }

    public void removePageChangedEventListener(final PageChangedEventSlot listener) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                pageChangedEventGenerator.removeListener(listener);
            }
        });
        t.start();

    }

    public void firePageChangedEvent(PageChangedEvent e) {
        pageChangedEventGenerator.fireEvent(e);
    }

    public void addTokenSelectionEventListener(final TokenSelectionEventSlot listener) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                tokenSelectionEventGenerator.addListener(listener);
            }
        });
        t.start();

    }

    public void removeTokenSelectionEventListener(final TokenSelectionEventSlot listener) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                tokenSelectionEventGenerator.removeListener(listener);
            }
        });
        t.start();

    }

    public void fireTokenSelectionEvent(TokenSelectionEvent e) {
        tokenSelectionEventGenerator.fireEvent(e);
    }

    public void addTokenDeselectionEventListener(final TokenDeselectionEventSlot listener) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                tokenDeselectionEventGenerator.addListener(listener);
            }
        });
        t.start();

    }

    public void removeTokenDeselectionEventListener(final TokenDeselectionEventSlot listener) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                tokenDeselectionEventGenerator.removeListener(listener);
            }
        });
        t.start();

    }

    public void fireTokenDeselectionEvent(TokenDeselectionEvent e) {
        tokenDeselectionEventGenerator.fireEvent(e);
    }

    public void addTokenStatusEventListener(final TokenStatusEventSlot listener) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                tokenStatusEventGenerator.addListener(listener);
            }
        });
        t.start();

    }

    public void removeTokenStatusEventListener(final TokenStatusEventSlot listener) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                tokenStatusEventGenerator.removeListener(listener);
            }
        });
        t.start();

    }

    public void fireTokenStatusEvent(TokenStatusEvent e) {
        tokenStatusEventGenerator.fireEvent(e);
    }

    public void addSavedEventListener(final SavedEventSlot listener) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                savedEventGenerator.addListener(listener);
            }
        });
        t.start();

    }

    public void removeSavedEventListener(final SavedEventSlot listener) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                savedEventGenerator.removeListener(listener);
            }
        });
        t.start();

    }

    public void fireSavedEvent(SavedEvent e) {
        savedEventGenerator.fireEvent(e);
    }

    public void addConcordanceEventListener(final ConcordanceEventSlot listener) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                concordanceEventGenerator.addListener(listener);
            }
        });
        t.start();

    }

    public void removeConcordanceEventListener(final ConcordanceEventSlot listener) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                concordanceEventGenerator.removeListener(listener);
            }
        });
        t.start();

    }

    public void fireConcordanceEvent(ConcordanceEvent e) {
        concordanceEventGenerator.fireEvent(e);
    }
    
    
    
    public void addTokenNavigationEventListener(final TokenNavigationEventSlot listener) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                tokenNavigationEventGenerator.addListener(listener);
            }
        });
        t.start();

    }

    public void removeTokenNavigationEventListener(final TokenNavigationEventSlot listener) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                tokenNavigationEventGenerator.removeListener(listener);
            }
        });
        t.start();

    }

    public void fireTokenNavigationEvent(TokenNavigationEvent e) {
        tokenNavigationEventGenerator.fireEvent(e);
    }
    
    
    
    public void addTokenMultiSelectionEventListener(final TokenMultiSelectionEventSlot listener) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                tokenMultiSelectionEventGenerator.addListener(listener);
            }
        });
        t.start();

    }

    public void removeTokenMultiSelectionEventListener(final TokenMultiSelectionEventSlot listener) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                tokenMultiSelectionEventGenerator.removeListener(listener);
            }
        });
        t.start();

    }

    public void fireTokenMultiSelectionEvent(TokenMultiSelectionEvent e) {
        tokenMultiSelectionEventGenerator.fireEvent(e);
    }
    

    
    public void addTokenMultiDeselectionEventListener(final TokenMultiDeselectionEventSlot listener) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                tokenMultiDeselectionEventGenerator.addListener(listener);
            }
        });
        t.start();

    }

    public void removeTokenMultiDeselectionEventListener(final TokenMultiDeselectionEventSlot listener) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                tokenMultiDeselectionEventGenerator.removeListener(listener);
            }
        });
        t.start();

    }

    public void fireTokenMultiDeselectionEvent(TokenMultiDeselectionEvent e) {
        tokenMultiDeselectionEventGenerator.fireEvent(e);
    }
    
    
    public void addCancelEventListener(final CancelEventSlot listener) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                cancelEventGenerator.addListener(listener);
            }
        });
        t.start();

    }

    public void removeCancelEventListener(final CancelEventSlot listener) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                cancelEventGenerator.removeListener(listener);
            }
        });
        t.start();

    }

    public void fireCancelEvent(CancelEvent e) {
        cancelEventGenerator.fireEvent(e);
    }
}
