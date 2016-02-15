package jav.gui.error;

import jav.correctionBackend.OcrErrorInfo;
import jav.correctionBackend.Token;
import jav.gui.events.MessageCenter;
import jav.gui.events.concordance.ConcordanceEvent;
import jav.gui.events.concordance.ConcordanceType;
import jav.gui.filter.OCRFehlerFilter;
import jav.gui.main.MainController;
import jav.logging.log4j.Log;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import javax.swing.Timer;

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
 */
public class OCRFehlerDefaultMode implements OCRFehlerMode {

    private OCRFehlerLabel lastSelectedError = null;
    private int clickDelay = 200;
    private Timer clickTimer;
    private boolean docLoaded = false;

    public OCRFehlerDefaultMode() {
    }

    @Override
    public MouseListener getMouseListener() {
        MouseListener ml = new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getSource() instanceof OCRFehlerLabel) {

                    final OCRFehlerLabel pl = (OCRFehlerLabel) e.getSource();
                    final OcrErrorInfo info = pl.getErrorInfo();

                    // single click = selection / deselection
                    if (e.getClickCount() == 1) {
                        clickTimer = new Timer(clickDelay, new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if (lastSelectedError == null) {
                                    if (info.getOccurencesN() != info.getCorrected()) {
                                        pl.setSelected(true);
                                        if (docLoaded) {
                                            OCRFehlerTopComponent.findInstance().setKonkordanzButton(true);
                                        }
                                    }
                                    lastSelectedError = pl;
                                } else if (lastSelectedError.equals(pl)) {
                                    lastSelectedError.setSelected(false);
                                    lastSelectedError = null;
                                    OCRFehlerTopComponent.findInstance().setKonkordanzButton(false);
                                } else {
                                    lastSelectedError.setSelected(false);
                                    lastSelectedError = pl;
                                    if (info.getOccurencesN() != info.getCorrected()) {
                                        pl.setSelected(true);
                                        if (docLoaded) {
                                            OCRFehlerTopComponent.findInstance().setKonkordanzButton(true);
                                        }
                                    }
                                }
                            }
                        });
                        clickTimer.setRepeats(false);
                        clickTimer.start();
                        // double click = correction
                    } else if (e.getClickCount() == 2) {
                        clickTimer.stop();
                        if (info.getOccurencesN() != info.getCorrected()) {
                            if (!pl.isSelected()) {
                                pl.setSelected(true);
                                if (lastSelectedError != null) {
                                    lastSelectedError.setSelected(false);
                                }
                            }
                            lastSelectedError = pl;
                            concordanceAction();
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        };
        return ml;
    }

    @Override
    public OcrErrorInfo getSelectedOCRError() {
        if (lastSelectedError != null) {
            return lastSelectedError.getErrorInfo();
        } else {
            return null;
        }
    }

    @Override
    public void concordanceAction() {

        MainController.changeCursorWaitStatus(true);
        SwingWorker<ArrayList<Token>, Object> worker = new SwingWorker<ArrayList<Token>, Object>() {

            @Override
            protected ArrayList<Token> doInBackground() {

                ArrayList<Token> result = null;
                try {
                    OCRFehlerFilter f = new OCRFehlerFilter(lastSelectedError.getText(), "");
                    result = f.applyFilter(MainController.findInstance().getCorrectionSystem().getDocument().tokenIterator());
                } catch (Exception e) {
                }
                return result;
            }

            @Override
            protected void done() {
                try {
                    ArrayList<Token> result = get();
                    if (result != null) {
                        MessageCenter.getInstance().fireConcordanceEvent(new ConcordanceEvent(this, ConcordanceType.CLONE, result, lastSelectedError.getText()));
                    } else {
                    }
                    MainController.changeCursorWaitStatus(false);
                } catch (ExecutionException | InterruptedException | CancellationException ex) {
                    Log.error(this, ex);
                }
            }
        };
        worker.execute();
    }

    @Override
    public Color getSelectedColor() {
        return Color.RED;
    }

    @Override
    public Color getUnselectedColor() {
        return Color.BLACK;
    }

    @Override
    public void setDocLoaded(Boolean b) {
        this.docLoaded = b;
    }
}
