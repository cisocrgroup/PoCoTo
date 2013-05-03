package jav.gui.token.behaviour;

import com.jidesoft.swing.StyleRange;
import jav.correctionBackend.Candidate;
import jav.correctionBackend.Token;
import jav.gui.events.MessageCenter;
import jav.gui.events.tokenDeselection.TokenDeselectionEvent;
import jav.gui.events.tokenSelection.TokenSelectionEvent;
import jav.gui.events.tokenSelection.TokenSelectionType;
import jav.gui.main.MainController;
import jav.gui.token.display.TokenVisualization;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.prefs.Preferences;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.Timer;
import org.openide.util.NbPreferences;

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
 * default implementation of TokenVisualisation behaviour, also showcase of how
 * to implement new modes
 *
 */
public class TokenVisualizationDefaultMode implements TokenVisualizationMode {

    private Preferences node;
    private TokenVisualization lastSelectedTv = null;
    private int clickDelay = 250;
    private int candNum = 0;
    private Timer clickTimer;
    private MouseListener ml;
    private KeyListener kl;
    private static final HashMap<String, Color> definedTagColors = new HashMap<String, Color>() {

        {
            put("normalToken", Color.white);
            put("correctedTrue", Color.green);
            put("lexicalFalse", Color.red);
            put("normalFalse", Color.orange);
        }
    };

    public TokenVisualizationDefaultMode() {
        node = NbPreferences.forModule(this.getClass());
        candNum = node.getInt("candNum", 10);

        ml = new MouseListener() {

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getSource() instanceof TokenVisualization) {

                    final TokenVisualization tv = (TokenVisualization) e.getSource();

                    if (e.getClickCount() == 1) {
                        // single left click = selection / deselection
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            clickTimer = new Timer(clickDelay, new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {

                                    if (lastSelectedTv == null) {
                                        lastSelectedTv = tv;
                                        lastSelectedTv.setSelected(true);
                                        MessageCenter.getInstance().fireTokenSelectionEvent(new TokenSelectionEvent(lastSelectedTv, tv.getTokenID(), TokenSelectionType.NORMAL));
                                    } else {
                                        if (lastSelectedTv.equals(tv)) {
                                            lastSelectedTv.setSelected(false);
                                            MessageCenter.getInstance().fireTokenDeselectionEvent(new TokenDeselectionEvent(lastSelectedTv, lastSelectedTv.getTokenID()));
                                            lastSelectedTv = null;
                                            // deselection event
                                        } else {
                                            // selecting other token while last one still selected
//                                            MessageCenter.getInstance().fireTokenDeselectionEvent(new TokenDeselectionEvent(lastSelectedTv, tv.getToken().getIndexInDocument()));
                                            lastSelectedTv.setSelected(false);
                                            tv.setSelected(true);
                                            lastSelectedTv = tv;
                                            MessageCenter.getInstance().fireTokenSelectionEvent(new TokenSelectionEvent(lastSelectedTv, tv.getTokenID(), TokenSelectionType.NORMAL));
                                        }
                                    }
                                }
                            });
                            clickTimer.setRepeats(false);
                            clickTimer.start();

                        } // single right click == select and show popup
                        else if (e.getButton() == MouseEvent.BUTTON3) {
                            if (lastSelectedTv != null) {
//                                if (!lastSelectedTv.equals(tv)) {
                                    // selecting other token while last one still selected
//                                    MessageCenter.getInstance().fireTokenDeselectionEvent(new TokenDeselectionEvent(this, lastSelectedTv.getToken().getIndexInDocument()));
                                    lastSelectedTv.setSelected(false);
                                    if( lastSelectedTv.isEditing()) {
                                        lastSelectedTv.abortTokenEditing();
                                    }
//                                }
                            }

                            tv.setSelected(true);
                            lastSelectedTv = tv;
                            MessageCenter.getInstance().fireTokenSelectionEvent(new TokenSelectionEvent(lastSelectedTv, tv.getTokenID(), TokenSelectionType.NORMAL));
                            lastSelectedTv.startTokenEditing();
                        }
                        // double left click = correction
                    } else if (e.getClickCount() == 2) {
                        // double left click
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            clickTimer.stop();
                            if (lastSelectedTv != null) {
                                if (!lastSelectedTv.equals(tv)) {
                                    // selecting other token while last one still selected
//                                    MessageCenter.getInstance().fireTokenDeselectionEvent(new TokenDeselectionEvent(this, lastSelectedTv.getToken().getIndexInDocument()));
                                    lastSelectedTv.setSelected(false);
                                }
                            }

                            tv.setSelected(true);
                            lastSelectedTv = tv;
                            MessageCenter.getInstance().fireTokenSelectionEvent(new TokenSelectionEvent(lastSelectedTv, tv.getTokenID(), TokenSelectionType.NORMAL));
                            lastSelectedTv.startTokenEditing();
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        };

        kl = new KeyListener() {

            @Override
            public void keyTyped(KeyEvent ke) {
            }

            @Override
            public void keyPressed(KeyEvent ke) {
                if (lastSelectedTv != null) {
                    if (ke.getKeyCode() == KeyEvent.VK_ENTER & !lastSelectedTv.isEditing()) {
                        lastSelectedTv.startTokenEditing();
//                // if delete, check if prev and next token are spaces, if the case delete one of them to not produce sequent spaces
                    } else if (ke.getKeyCode() == KeyEvent.VK_DELETE) {
                        int position = lastSelectedTv.getTokenID();
                        MainController.findInstance().deleteToken(position);
                    }
                }

            }

            @Override
            public void keyReleased(KeyEvent ke) {
            }
        };
    }

    @Override
    public void unSelect() {
        if (this.lastSelectedTv != null) {
            this.lastSelectedTv.setSelected(false);
            this.lastSelectedTv = null;
        }
    }

    @Override
    public void setSelectedTokenVisualization(TokenVisualization tv) {
        this.lastSelectedTv = tv;
        this.setTokenVisualizationStyle(this.lastSelectedTv);
    }

    @Override
    public void setCorrected(TokenVisualization tv, boolean b) {
        Color fontColor = this.getUnselectedColor();
        if (tv.isSelected()) {
            fontColor = this.getSelectedColor();
        }
        Color tagColor;
        if (b) {
            tagColor = definedTagColors.get("correctedTrue");
        } else {
            tagColor = definedTagColors.get("normalToken");
        }
        tv.getTokenTextLabel().clearStyleRanges();
        tv.getTokenTextLabel().addStyleRange(new StyleRange(0, -1, Font.PLAIN, fontColor, StyleRange.STYLE_WAVED, tagColor));
    }

    @Override
    public TokenVisualization getSelectedTokenVisualization() {
        return this.lastSelectedTv;
    }

    @Override
    public MouseListener getMouseListener() {
        return ml;
    }

    @Override
    public Color getSelectedColor() {
        return Color.red;
    }

    @Override
    public Color getMultiSelectedColor() {
        return Color.orange;
    }

    @Override
    public Color getUnselectedColor() {
        return Color.black;
    }

    @Override
    public void setTokenVisualizationStyle(TokenVisualization tv) {
        Color fontColor = this.getUnselectedColor();
        if (tv.isSelected()) {
            fontColor = this.getSelectedColor();
        }
        if (tv.isMultiSelected()) {
            fontColor = this.getMultiSelectedColor();
        }

        tv.getTokenTextLabel().clearStyleRanges();
        Token token = MainController.findInstance().getDocument().getTokenByID(tv.getTokenID());
        if (token.isCorrected() && token.isNormal()) {
            Color tagColor = definedTagColors.get("correctedTrue");
            tv.getTokenTextLabel().addStyleRange(new StyleRange(0, -1, Font.PLAIN, fontColor, StyleRange.STYLE_WAVED, tagColor));
        } else if (token.isSuspicious() && token.isNormal()) {
            Color tagColor = definedTagColors.get("lexicalFalse");
            tv.getTokenTextLabel().addStyleRange(new StyleRange(0, -1, Font.PLAIN, fontColor, StyleRange.STYLE_WAVED, tagColor));
//        } else if (!tv.getToken().isNormal()) {
//            Color tagColor = definedTagColors.get("normalFalse");
//            tv.getTokenTextLabel().addStyleRange(new StyleRange(0, -1, Font.PLAIN, fontColor, StyleRange.STYLE_WAVED, tagColor));
        } else {
            Color tagColor = definedTagColors.get("normalToken");
            tv.getTokenTextLabel().addStyleRange(new StyleRange(0, -1, Font.PLAIN, fontColor, StyleRange.STYLE_WAVED, tagColor));
        }
    }

    @Override
    public void setTokenVisualizationStyle(TokenVisualization tv, Token token) {
        Color fontColor = this.getUnselectedColor();
        if (tv.isSelected()) {
            fontColor = this.getSelectedColor();
        }
        if (tv.isMultiSelected()) {
            fontColor = this.getMultiSelectedColor();
        }

        tv.getTokenTextLabel().clearStyleRanges();
        if (token.isCorrected() && token.isNormal()) {
            Color tagColor = definedTagColors.get("correctedTrue");
            tv.getTokenTextLabel().addStyleRange(new StyleRange(0, -1, Font.PLAIN, fontColor, StyleRange.STYLE_WAVED, tagColor));
        } else if (token.isSuspicious() && token.isNormal()) {
            Color tagColor = definedTagColors.get("lexicalFalse");
            tv.getTokenTextLabel().addStyleRange(new StyleRange(0, -1, Font.PLAIN, fontColor, StyleRange.STYLE_WAVED, tagColor));
//        } else if (!tv.getToken().isNormal()) {
//            Color tagColor = definedTagColors.get("normalFalse");
//            tv.getTokenTextLabel().addStyleRange(new StyleRange(0, -1, Font.PLAIN, fontColor, StyleRange.STYLE_WAVED, tagColor));
        } else {
            Color tagColor = definedTagColors.get("normalToken");
            tv.getTokenTextLabel().addStyleRange(new StyleRange(0, -1, Font.PLAIN, fontColor, StyleRange.STYLE_WAVED, tagColor));
        }
    }

    @Override
    public KeyListener getKeyListener() {
        return kl;
    }

    public JPopupMenu getPopupMenu() {

        JPopupMenu menu = new JPopupMenu();

        ActionListener editListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                lastSelectedTv.startTokenEditing();
                lastSelectedTv.repaint();
                lastSelectedTv.revalidate();
            }
        };
        JMenuItem editItem = new JMenuItem(java.util.ResourceBundle.getBundle("jav/gui/token/display/Bundle").getString("edit"));
        editItem.addActionListener(editListener);
        menu.add(editItem);
        menu.addSeparator();


        JMenuItem cor = new JMenuItem(java.util.ResourceBundle.getBundle("jav/gui/token/display/Bundle").getString("setcorr"));
        cor.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                MainController.findInstance().setCorrected(lastSelectedTv.getTokenID(), true);
            }
        });
        menu.add(cor);
        menu.addSeparator();

        ActionListener itemActionListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JMenuItem jm = (JMenuItem) e.getSource();
                MainController.findInstance().correctTokenByString(lastSelectedTv.getTokenID(), jm.getText());
                MainController.findInstance().addToLog(MainController.findInstance().getLastFocusedTCName() + " # correct # candidate # " + lastSelectedTv.getTokenTextLabelText() + " # " + jm.getText());
            }
        };
        // Create and add a menu item
        //CandidateSet cs = MainController.findInstance().getCorrectionSystem().computeCandidates(lastSelectedTv.getToken());
        //Iterator<Candidate> candIterator = cs.candIterator();
        Iterator<Candidate> candIterator = MainController.findInstance().getDocument().candidateIterator(lastSelectedTv.getTokenID());
        if (candIterator.hasNext()) {

            int counter = 0;
            while (counter < candNum && candIterator.hasNext()) {
                counter++;
                Candidate cand = candIterator.next();
                JMenuItem item = new JMenuItem(cand.getSuggestion());
//                item.setToolTipText(java.util.ResourceBundle.getBundle("jav/gui/token/display/Bundle").getString("lev_dist") + cand.getDlev()
//                        + java.util.ResourceBundle.getBundle("jav/gui/token/display/Bundle").getString("cand_frq") + cand.getFrequency()
//                        + "</html>");
                item.addActionListener(itemActionListener);
                menu.add(item);
            }

        }


        //////////////// add menu entry for merge to right
        menu.addSeparator();
        ActionListener mergeRightActionListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                MainController.findInstance().mergeRightward(lastSelectedTv.getTokenID());
                MainController.findInstance().addToLog(MainController.findInstance().getLastFocusedTCName() + " # merge # " + " # " + lastSelectedTv.getTokenTextLabelText() + " # " + lastSelectedTv.getTokenTextLabelText());
            }
        };
        JMenuItem mergeRightItem = new JMenuItem(java.util.ResourceBundle.getBundle("jav/gui/token/display/Bundle").getString("mergeR"));
        mergeRightItem.addActionListener(mergeRightActionListener);
        menu.add(mergeRightItem);

        ////////////  add menu entry for hyphenation
//        ActionListener mergeHyphenationActionListener = new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                JMenuItem jm = (JMenuItem) e.getSource();
//                // todo check for success of merge
//                lastSelectedTv.getToken().mergeHyphenation();
//                MessageCenter.getInstance().fireTokenStatusEvent(new TokenStatusEvent(lastSelectedTv.getToken(), TokenStatusType.MERGED_RIGHT));
//            }
//        };
        return menu;
    }
}
