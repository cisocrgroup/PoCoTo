package jav.gui.error.profiler;

import jav.concordance.control.ConcordanceEntry;
import jav.correctionBackend.Pattern;
import jav.correctionBackend.PatternOccurrence;
import jav.correctionBackend.Token;
import jav.gui.events.MessageCenter;
import jav.gui.events.concordance.ConcordanceEvent;
import jav.gui.events.concordance.ConcordanceType;
import jav.gui.filter.PatternFilter;
import jav.gui.main.MainController;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import javax.swing.Timer;

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
public class PatternDefaultMode implements PatternMode {

    private PatternLabel lastSelectedPattern = null;
    private int clickDelay = 200;
    private Timer clickTimer;
    private boolean docLoaded = false;
    LinkedHashMap<Integer, ConcordanceEntry> tokens;

    public PatternDefaultMode() {
//        MessageCenter.getInstance().addTokenStatusEventListener(this);
    }

    @Override
    public MouseListener getMouseListener() {
        MouseListener ml = new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getSource() instanceof PatternLabel) {

                    final PatternLabel pl = (PatternLabel) e.getSource();

                    // single click = selection / deselection
                    if (e.getClickCount() == 1) {
                        clickTimer = new Timer(clickDelay, new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if (lastSelectedPattern == null) {
                                    pl.setSelected(true);
                                    if (pl.getPattern().getOccurencesN() != pl.getPattern().getCorrected()) {
                                        if (docLoaded) {
                                            PatternTopComponent.findInstance().setKonkordanzButton(true);
                                        }
                                    }
                                    lastSelectedPattern = pl;
                                } else {
                                    if (lastSelectedPattern.equals(pl)) {
                                        lastSelectedPattern.setSelected(false);
                                        lastSelectedPattern = null;
                                        PatternTopComponent.findInstance().setKonkordanzButton(false);
                                    } else {
                                        lastSelectedPattern.setSelected(false);
                                        pl.setSelected(true);
                                        lastSelectedPattern = pl;
                                        if (pl.getPattern().getOccurencesN() != pl.getPattern().getCorrected()) {
                                            if (docLoaded) {
                                                PatternTopComponent.findInstance().setKonkordanzButton(true);
                                            }
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
                        if (pl.getPattern().getOccurencesN() != pl.getPattern().getCorrected()) {
                            if (!pl.isSelected()) {
                                pl.setSelected(true);
                                if (lastSelectedPattern != null) {
                                    lastSelectedPattern.setSelected(false);
                                }
                            }
                            lastSelectedPattern = pl;
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
    public Color getSelectedColor() {
        return Color.RED;
    }

    @Override
    public Color getUnselectedColor() {
        return Color.BLACK;
    }

    @Override
    public void concordanceAction() {

        MainController.changeCursorWaitStatus(true);
        SwingWorker<ArrayList<Token>, Object> worker = new SwingWorker<ArrayList<Token>, Object>() {

            @Override
            protected ArrayList<Token> doInBackground() {

                ArrayList<Token> result = null;
                try {
                    tokens = new LinkedHashMap<>();
                    Iterator<PatternOccurrence> i = lastSelectedPattern.getPattern().getOccurences().iterator();
                    HashMap<String, PatternOccurrence> types = new HashMap<>();
                    while (i.hasNext()) {
                        PatternOccurrence po = i.next();
                        types.put(po.getWOCR_LC(), po);
                    }

                    PatternFilter pF = new PatternFilter(types, "");
                    result = pF.applyFilter(MainController.findInstance().getDocument().tokenIterator());

                    Iterator<Token> it = result.iterator();
                    while (it.hasNext()) {
                        Token tok = it.next();
                        ConcordanceEntry ke = new ConcordanceEntry(tok);
                        PatternOccurrence po = types.get(tok.getWOCR_lc());

                        // pattern suggestions are always lowercase, if wocr is uppercase then make first letter of suggestion uppercase
                        if (Character.isUpperCase(tok.getWOCR().charAt(0))) {
                            po.setWSuggestion(po.getWSuggestion().substring(0, 1).toUpperCase() + po.getWSuggestion().substring(1));
                        }
                        ke.setCandidateString(po.getWSuggestion());
                        tokens.put(tok.getID(), ke);
                    }
                } catch (Exception e) {
                }
                return result;
            }

            @Override
            protected void done() {
                try {
                    ArrayList<Token> result = get();
                    if (result != null) {
                        MessageCenter.getInstance().fireConcordanceEvent(new ConcordanceEvent(this, ConcordanceType.DIVERSE, result, lastSelectedPattern.getPattern().getLeft() + " --> " + lastSelectedPattern.getPattern().getRight()));
                    } else {
                    }
                    MainController.changeCursorWaitStatus(false);
                } catch (ExecutionException ex) {
                } catch (InterruptedException ex) {
                } catch (CancellationException ex) {
                }
            }
        };
        worker.execute();

    }

    @Override
    public Pattern getSelectedPattern() {
        if (lastSelectedPattern != null) {
            return lastSelectedPattern.getPattern();
        } else {
            return null;
        }
    }

//    @Override
//    public void dispatchEvent(TokenStatusEvent e) {
//        if (lastSelectedPattern != null && e.getType().equals(TokenStatusType.CORRECTED)) {
//            final Integer tok = e.getTokenIndex();
//            EventQueue.invokeLater(new Runnable() {
//
//                @Override
//                public void run() {
//                    if (tokens.containsKey(tok)) {
//                        lastSelectedPattern.getPattern().setCorrected(true);
//                        lastSelectedPattern.setToolTipText(java.util.ResourceBundle.getBundle("jav/gui/error/profiler/Bundle").getString("pat_frq") + lastSelectedPattern.getPattern().getOccurencesN() + java.util.ResourceBundle.getBundle("jav/gui/error/profiler/Bundle").getString("occurences") + lastSelectedPattern.getPattern().getOccurencesN() + "</br>" + java.util.ResourceBundle.getBundle("jav/gui/error/profiler/Bundle").getString("corrected") + lastSelectedPattern.getPattern().getCorrected() + "</br></html>");
//                        if (lastSelectedPattern.getPattern().getOccurencesN() == lastSelectedPattern.getPattern().getCorrected()) {
//                            PatternTopComponent.findInstance().setKonkordanzButton(false);
//                        }
//                    }
//                }
//            });
//        }
//    }
    @Override
    public void disconnect() {
//        MessageCenter.getInstance().removeTokenStatusEventListener(this);
    }

    @Override
    public void setDocLoaded(boolean b) {
        this.docLoaded = b;
    }
}
