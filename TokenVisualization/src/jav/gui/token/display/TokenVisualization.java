package jav.gui.token.display;

import jav.correctionBackend.Candidate;
import jav.correctionBackend.SpecialSequenceType;
import jav.correctionBackend.Token;
import jav.correctionBackend.TokenImageInfoBox;
import jav.gui.events.MessageCenter;
import jav.gui.events.tokenNavigation.TokenNavigationEvent;
import jav.gui.events.tokenNavigation.TokenNavigationType;
import jav.gui.main.AbstractTokenVisualization;
import jav.gui.main.MainController;
import jav.gui.token.behaviour.TokenVisualizationMode;
import jav.gui.token.edit.ComboBoxEntry;
import jav.gui.token.edit.ComboBoxEntryType;
import jav.gui.token.edit.MyEditCustomComboBox;
import jav.gui.token.tools.ImageProcessor;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.LinkedHashSet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
 * abstract class for building visual representations of tokens all methods that
 * are independent from implementation are here, all abstract methods have to be
 * implemented
 *
 */
public abstract class TokenVisualization extends AbstractTokenVisualization {

    int tokenID;
    Token temptoken = null;
    boolean isEditing = false;
    TokenTextLabel tokenTextLabel;
    MyEditCustomComboBox box;
    TokenVisualizationMode tvm = null;
    boolean hasImage;
    TokenVisualization instance = this;
    boolean isSelected;
    boolean isMultiSelected;
    KeyListenerImpl keyListener = new KeyListenerImpl();
    String delete = java.util.ResourceBundle.getBundle("jav/gui/token/display/Bundle").getString("delete");
    String setcorrect = java.util.ResourceBundle.getBundle("jav/gui/token/display/Bundle").getString("setcorr");
    String merger = java.util.ResourceBundle.getBundle("jav/gui/token/display/Bundle").getString("mergeR");
    String focusInMain = java.util.ResourceBundle.getBundle("jav/gui/token/display/Bundle").getString("jumpto");
    FocusListener fl;

    public void setFontSize(int fontSize) {
        Font f = tokenTextLabel.getFont();
        float nS = (float) fontSize;
        Font deriveFont = f.deriveFont(nS);
        tokenTextLabel.setFont(deriveFont);
    }

    public Point getCentroid() {
        if (this.isSpace()) {
            return new Point(this.getX(), this.getY());
        } else {
            return new Point((this.getX() + (this.getWidth() / 2)), (this.getY() + (this.getHeight() / 2)));
        }
    }

    public abstract void calculateSizeNormMode();

    public abstract void calculateSizeEditMode();

    public boolean isNewline() {
        if (tokenTextLabel.getText().equals("¶")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isSpace() {
        if (tokenTextLabel.getText().equals(" ")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hasImage() {
        return hasImage;
    }

    public void setTokenID(int i) {
        this.tokenID = i;
    }

    public int getTokenID() {
        return this.tokenID;
    }

    public void zoomFont(int newSize) {
        if (!tokenTextLabel.isSpace()) {
            Font f = tokenTextLabel.getFont();
            float nS = (float) newSize;
            Font deriveFont = f.deriveFont(nS);
            tokenTextLabel.setFont(deriveFont);
            tokenTextLabel.revalidate();
            if (this.hasImage) {
                this.calculateSizeNormMode();
                this.revalidate();
            }
        }
    }

    public void setMode(TokenVisualizationMode n) {
        this.tvm = n;
        this.tokenTextLabel.setForeground(tvm.getUnselectedColor());
        this.addMouseListener(tvm.getMouseListener());
        this.addKeyListener(tvm.getKeyListener());
        tvm.setTokenVisualizationStyle(this);
        this.calculateSizeNormMode();
        this.revalidate();
    }

    public void setMode(TokenVisualizationMode n, Token t) {
        this.tvm = n;
        this.tokenTextLabel.setForeground(tvm.getUnselectedColor());
        this.addMouseListener(tvm.getMouseListener());
        this.addKeyListener(tvm.getKeyListener());
        tvm.setTokenVisualizationStyle(this, t);
        this.calculateSizeNormMode();
        this.revalidate();
    }

    public void deactivate() {
        if (tvm != null) {
            this.removeMouseListener(tvm.getMouseListener());
            this.removeKeyListener(keyListener);
            this.setBackground(Color.lightGray);
        }
    }

    public void activate() {
        if (tvm != null) {
            this.addMouseListener(tvm.getMouseListener());
            this.addKeyListener(keyListener);
            this.setBackground(Color.white);
        }
    }

    public void setSelected(boolean b) {
        this.isSelected = b;
        this.grabFocus();
        if (tvm != null) {
            tvm.setTokenVisualizationStyle(this);
        }
    }

    public void setMultiSelected(boolean b) {
        this.isMultiSelected = b;
        if (!this.isSpace() && tvm != null) {
            tvm.setTokenVisualizationStyle(this);
        }
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public boolean isMultiSelected() {
        return this.isMultiSelected;
    }

    public TokenTextLabel getTokenTextLabel() {
        return this.tokenTextLabel;
    }

    public String getTokenTextLabelText() {
        return tokenTextLabel.getText();
    }

    public void update(String newtext) {
        tokenTextLabel.setText(newtext);
        if (tvm != null) {
            tvm.setTokenVisualizationStyle(instance);
        }
        this.calculateSizeNormMode();
        this.revalidate();
    }

    public void update(String newtext, boolean corrected) {
        tokenTextLabel.setText(newtext);
        this.setCorrected(corrected);
        this.calculateSizeNormMode();
        this.revalidate();
    }

    public void setCorrected(boolean b) {
        if (tvm != null) {
            tvm.setCorrected(instance, b);
        }
    }

    public void startTokenEditing() {

        this.isEditing = true;

        LinkedHashSet<ComboBoxEntry> cands = new LinkedHashSet<>();

        temptoken = MainController.findInstance().getDocument().getTokenByID(tokenID);

        cands.add(new ComboBoxEntry(this.getTokenTextLabelText(), ComboBoxEntryType.NORMAL));

        Iterator<Candidate> iterator = MainController.findInstance().getDocument().candidateIterator(tokenID);
        int maxcands = 5;
        while (iterator.hasNext() && maxcands > 1) {
            maxcands--;
            cands.add(new ComboBoxEntry(iterator.next().getSuggestion(), ComboBoxEntryType.NORMAL));
        }

        cands.add(new ComboBoxEntry(setcorrect, ComboBoxEntryType.SETCORRECTED));
        cands.add(new ComboBoxEntry(delete, ComboBoxEntryType.DELETE));
        cands.add(new ComboBoxEntry(merger, ComboBoxEntryType.MERGE));

        if (tvm != null && tvm.getClass().getSimpleName().equals("TokenVisualizationConcordanceMode")) {
            cands.add(new ComboBoxEntry(focusInMain, ComboBoxEntryType.FOCUS_IN_MAIN));
        }

        JButton submitButton = new JButton("↵");
        submitButton.setFocusable(false);
        submitButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                endTokenEditing(box.getEditor().getItem().toString());
            }
        });

        box = new MyEditCustomComboBox(cands, tokenTextLabel.getFont(), new Dimension(tokenTextLabel.getPreferredSize().width, tokenTextLabel.getPreferredSize().height), temptoken.getNumberOfCandidates(), submitButton);
        box.setWide(true);
        ((JPanel) box.getEditor().getEditorComponent()).getComponent(0).addKeyListener(keyListener);
//        box.getEditor().getEditorComponent().addKeyListener(keyListener);

        fl = new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                if (e.getComponent().getParent().getParent().getClass().getName().equals("jav.gui.token.edit.MyEditCustomComboBox")) {
                    JComboBox b = (JComboBox) e.getComponent().getParent().getParent();
                    if (!b.isPopupVisible()) {
                        b.showPopup();
                        b.setPopupVisible(true);
                        b.getEditor().selectAll();
                    }
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                abortTokenEditing();
            }
        };
        ((JPanel) box.getEditor().getEditorComponent()).getComponent(0).addFocusListener(fl);

//        box.getEditor().getEditorComponent().addFocusListener(fl);
//        BoundsPopupMenuListener lis = new BoundsPopupMenuListener( false , true ) {
//            @Override
//            public void popupMenuCanceled(PopupMenuEvent e) {
//                abortTokenEditing();
//            }
//        };
//        box.addPopupMenuListener( lis );
//        box.addPopupMenuListener(new PopupMenuListener() {
//
//            @Override
//            public void popupMenuWillBecomeVisible(PopupMenuEvent pme) {
//                JComboBox comboBox = (JComboBox) pme.getSource();
//		if (comboBox.getItemCount() == 0) return;
//		Object child = comboBox.getAccessibleContext().getAccessibleChild(0);
//            }
//
//            @Override
//            public void popupMenuWillBecomeInvisible(PopupMenuEvent pme) {
//            }
//
//            @Override
//            public void popupMenuCanceled(PopupMenuEvent pme) {
////                abortTokenEditing();
//            }
//        });
        this.remove(tokenTextLabel);
        this.add(box);
        this.calculateSizeEditMode();
        this.repaint();
        this.revalidate();
        ((JTextField) ((JPanel) box.getEditor().getEditorComponent()).getComponent(0)).grabFocus();
    }

    /*
     * correct Token with String in editor Textfield
     */
    public void endTokenEditing(String editString) {
        // TODO check implementation
//        boolean b = Pattern.matches("([^ ]+ [^ ]+)*", editString);
//        if (b) {
        if (editString.contains(" ")) {
            MainController.findInstance().splitToken(this.tokenID, editString);
//            MainController.findInstance().addToLog(MainController.findInstance().getLastFocusedTCName() + " # split # " + temptoken.getWOCR() + " # " + retval);
        } else {
            //if (!editString.equals(tokenTextLabel.getText())) {
            MainController.findInstance().correctTokenByString(this.tokenID, editString);
//            MainController.findInstance().addToLog(MainController.findInstance().getLastFocusedTCName() + " # correct # " + temptoken.getWOCR() + " # " + editString);
//            } else {
//                abortTokenEditing();
//            }
        }
//            if (editString.contains(" ") && !editString.startsWith(" ") && !editString.endsWith(" ")) {
////            if (editString.matches("\\p{Alnum}.*\\W+.*\\p{Alnum}*")) {
//                int retval = this.token.handleSplit(editString);
//                MainController.findInstance().addToLog(MainController.findInstance().getLastFocusedTCName()+" # split # "+this.token.getWOCR()+" # "+retval);
//                MessageCenter.getInstance().fireTokenStatusEvent(new TokenStatusEvent(this.token, TokenStatusType.SPLIT, retval));
//            } else if (editString.contains(" ") && (editString.startsWith(" ") || editString.endsWith(" "))) {
//                abortTokenEditing();
//            } else {
//                MainController.findInstance().correctTokenByString(token, editorf.getText());
//                MainController.findInstance().addToLog(MainController.findInstance().getLastFocusedTCName()+ " # correct # "+token.getWOCR()+" # "+editorf.getText());
//            }
//        this.remove(editorf);
        box.getEditor().getEditorComponent().removeFocusListener(fl);
        this.remove(box);
        this.add(tokenTextLabel);
        this.calculateSizeNormMode();
        this.repaint();
        this.revalidate();
        this.grabFocus();
        this.isEditing = false;
    }

    /*
     * abort editing, keep original string
     */
    public void abortTokenEditing() {
        box.getEditor().getEditorComponent().removeFocusListener(fl);
        this.remove(box);
        this.add(tokenTextLabel);
        this.calculateSizeNormMode();
        this.repaint();
        this.revalidate();
        this.grabFocus();
        this.isEditing = false;
    }

    public void processEdit(ComboBoxEntry cbe) {
        if (cbe.getType() == ComboBoxEntryType.NORMAL) {
            endTokenEditing(box.getEditor().getItem().toString());
        } else if (cbe.getType() == ComboBoxEntryType.MERGE) {
            abortTokenEditing();
            MainController.findInstance().mergeRightward(tokenID);
            MainController.findInstance().addToLog(MainController.findInstance().getLastFocusedTCName() + " # merge # " + " # " + temptoken.getWOCR() + " # " + temptoken.getWOCR());
        } else if (cbe.getType() == ComboBoxEntryType.SETCORRECTED) {
            abortTokenEditing();
            if (!temptoken.isCorrected()) {
                MainController.findInstance().setCorrected(tokenID, true);
            }
            instance.revalidate();
            MainController.findInstance().addToLog(MainController.findInstance().getLastFocusedTCName() + " # set as corrected # " + temptoken.getWOCR());

            // if delete, check if prev and next token are spaces, if the case delete one of them to not produce sequent spaces
        } else if (cbe.getType() == ComboBoxEntryType.DELETE) {

            abortTokenEditing();
            MainController.findInstance().deleteToken(tokenID);

        } else if (cbe.getType() == ComboBoxEntryType.FOCUS_IN_MAIN) {
            abortTokenEditing();
            MessageCenter.getInstance().fireTokenNavigationEvent(new TokenNavigationEvent(this, tokenID, TokenNavigationType.FOCUS_IN_MAIN));
        }
    }

    public boolean isEditing() {
        return this.isEditing;
    }

    private class KeyListenerImpl implements KeyListener {

        public KeyListenerImpl() {
        }

        @Override
        public void keyTyped(KeyEvent ke) {
        }

        @Override
        public void keyPressed(KeyEvent ke) {
            if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
                ComboBoxEntry cbe = (ComboBoxEntry) box.getSelectedItem();
                processEdit(cbe);
            } else if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
                abortTokenEditing();
            }
        }

        @Override
        public void keyReleased(KeyEvent ke) {
        }
    }

    /**
     * Creates a new TokenVisualization Instance.
     *
     * @param token The token
     * @param scale the scale of the image
     * @param fontSize the size of the font
     * @param lineHeight the height of the line
     * @param ip the image processor
     * @return the appropriate TokeVisualization instance for the given token
     */
    public static TokenVisualization fromToken(
            Token token,
            double scale,
            int fontSize,
            int lineHeight,
            ImageProcessor ip
    ) {
        assert (token != null);
        TokenImageInfoBox tiib = token.getTokenImageInfoBox();
        TokenVisualization tv;
        if (tiib == null) {
            if (token.isNormal()) {
                tv = new PseudoImageTokenVisualization(token, fontSize, lineHeight);
            } else {
                tv = new OnlyTextTokenVisualization(token, fontSize);
            }
        } else if (SpecialSequenceType.NEWLINE.equals(token.getSpecialSeq())
                || SpecialSequenceType.SPACE.equals(token.getSpecialSeq())) {
            tv = new OnlyTextTokenVisualization(token, fontSize);
        } else {
            assert (tiib != null);
            final int left = tiib.getCoordinateLeft();
            final int right = tiib.getCoordinateRight();
            final int top = tiib.getCoordinateTop();
            final int bottom = tiib.getCoordinateBottom();
            final int width = right - left;
            final int height = bottom - top;
            BufferedImage bi = ip.getTokenImage(left, top, width, height, scale);
            tv = new ImageTokenVisualization(bi, token, fontSize);
        }
        return tv;
    }
}
