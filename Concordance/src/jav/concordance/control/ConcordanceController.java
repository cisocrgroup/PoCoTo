package jav.concordance.control;

import jav.concordance.view.CloneConcordanceTopComponent;
import jav.concordance.view.ConcordanceTopComponent;
import jav.correctionBackend.Token;
import jav.gui.dialogs.CustomErrorDialog;
import jav.gui.events.MessageCenter;
import jav.gui.events.concordance.ConcordanceEvent;
import jav.gui.events.concordance.ConcordanceEventSlot;
import jav.gui.events.concordance.ConcordanceType;
import jav.gui.main.MainController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

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
public class ConcordanceController implements ConcordanceEventSlot {
    
    private TokenLexOrderComparator comparator;
    
    public ConcordanceController() {
        comparator = new TokenLexOrderComparator();
        MessageCenter.getInstance().addConcordanceEventListener(this);
    }

    @Override
    public void dispatchEvent(final ConcordanceEvent e) {
        MainController.findInstance().addToLog("KonkordanzKontroller # Konkordanz erzeugt # "+e.getConcordanceType()+" # "+e.getName());
        if (e.getConcordanceType() == ConcordanceType.CLONE) {

            final ProgressHandle p = ProgressHandleFactory.createHandle("initConcordance");

            SwingWorker<Boolean, Object> worker = new SwingWorker<Boolean, Object>() {

                @Override
                protected Boolean doInBackground() {

                    boolean retval = false;
                    p.start();
                    p.progress("Create Concordance");
                    p.setDisplayName("Building Concordance");

                    retval = true;

                    return retval;
                }

                @Override
                protected void done() {
                    try {
                        boolean retval = get();
                        if (retval) {
                            CloneConcordanceTopComponent comp = new CloneConcordanceTopComponent();
                            comp.init(e.getArray(), e.getName());
                            comp.open();
                            comp.requestActive();
                            p.finish();

                        } else {
                            new CustomErrorDialog().showDialog("KonkordanzController::dispatchEvent(ConcordanceEvent)");
                        }
                    } catch ( ExecutionException | InterruptedException | CancellationException ex) {
                    }
                }
            };
            worker.execute();
        } else if (e.getConcordanceType() == ConcordanceType.DIVERSE) {

            final ProgressHandle p = ProgressHandleFactory.createHandle("initConcordance");

            SwingWorker<ArrayList<Token>, Object> worker = new SwingWorker<ArrayList<Token>, Object>() {

                @Override
                protected ArrayList<Token> doInBackground() {

                    ArrayList<Token> retval = e.getArray();
                    Collections.sort(retval, comparator);
                    
                    p.start();
                    p.progress("Create Concordance");
                    p.setDisplayName("Building Concordance");

                    return retval;
                }

                @Override
                protected void done() {
                    try {
                        ArrayList<Token> retval = get();
                        if (retval != null ) {
                            ConcordanceTopComponent comp = new ConcordanceTopComponent();
                            comp.init(e.getArray(), e.getName());
                            comp.open();
                            comp.requestActive();
                            p.finish();

                        } else {
                            new CustomErrorDialog().showDialog("KonkordanzController::dispatchEvent(ConcordanceEvent)");
                        }
                    } catch (ExecutionException | InterruptedException | CancellationException ex) {
                    }
                }
            };
            worker.execute();
        }
    }
}
