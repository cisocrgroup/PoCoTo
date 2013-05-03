package jav.concordance.view;

import info.clearthought.layout.TableLayout;
import jav.concordance.control.ConcordanceEntry;
import jav.correctionBackend.Token;
import jav.correctionBackend.TokenImageInfoBox;
import jav.gui.dialogs.CustomErrorDialog;
import jav.gui.events.MessageCenter;
import jav.gui.events.tokenNavigation.TokenNavigationEvent;
import jav.gui.events.tokenNavigation.TokenNavigationType;
import jav.gui.events.tokenStatus.TokenStatusType;
import jav.gui.main.AbstractTokenVisualization;
import jav.gui.main.MainController;
import jav.gui.token.behaviour.TokenVisualizationConcordanceMode;
import jav.gui.token.behaviour.TokenVisualizationMode;
import jav.gui.token.display.ImageTokenVisualization;
import jav.gui.token.display.OnlyTextTokenVisualization;
import jav.gui.token.display.PseudoImageTokenVisualization;
import jav.gui.token.display.TokenVisualization;
import jav.gui.token.tools.ImageProcessor;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

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
public class ConcordancePage extends JPanel {

    private TableLayout tl;
    private Border inactiveLeft;
    private Border inactiveCenter;
    private Border inactiveRight;
    private Border activeLeft;
    private Border activeCenter;
    private Border activeRight;
    private ItemListener itemListener;
    private ImageProcessor ipc;
    private double p = TableLayout.PREFERRED;
    ConcordanceTopComponent parent;
    private TokenVisualizationMode tvMode;

    ConcordancePage(final ConcordanceTopComponent par, int start, int len) {
        super();

        tvMode = new TokenVisualizationConcordanceMode();

        this.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.parent = par;
        this.ipc = new ImageProcessor();
        inactiveLeft = BorderFactory.createMatteBorder(2, 2, 2, 0, Color.WHITE);
        inactiveCenter = BorderFactory.createMatteBorder(2, 0, 2,
                0, Color.WHITE);
        inactiveRight = BorderFactory.createMatteBorder(2, 0, 2,
                2, Color.WHITE);

        activeLeft = BorderFactory.createMatteBorder(2, 2, 2, 0,
                Color.CYAN);
        activeCenter = BorderFactory.createMatteBorder(2, 0, 2, 0,
                Color.CYAN);
        activeRight = BorderFactory.createMatteBorder(2, 0, 2, 2,
                Color.CYAN);

        tl = new TableLayout();
        tl.setVGap(10);
////        tl.setHGap(20);

        double[] size = {p, p, p};
        tl.setColumn(size);
        this.setLayout(tl);
        this.setBackground(Color.white);

        itemListener = new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {

                Component component = (Component) e.getSource();
                int row = tl.getConstraints(component.getParent()).row1;

                if (e.getStateChange() == ItemEvent.SELECTED) {
                    parent.addSelected(1);
                    
                    for (JPanel p : parent.getConcordanceRegistry().get(row)) {
                        if (p.getName().equals("rightc")) {
                            p.setBorder(activeRight);
                        } else if (p.getName().equals("word")) {
                            p.setBorder(activeCenter);
                            TokenVisualization tv = (TokenVisualization) p.getComponents()[1];
                            parent.setSelected(tv.getTokenID(), true);
                        } else if (p.getName().equals("leftc")) {
                            p.setBorder(activeLeft);
                        }
                    }
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {

                    parent.removeSelected(1);
                    for (JPanel p : parent.getConcordanceRegistry().get(row)) {
                        if (p.getName().equals("rightc")) {
                            p.setBorder(inactiveRight);
                        } else if (p.getName().equals("word")) {
                            p.setBorder(inactiveCenter);
                            TokenVisualization tv = (TokenVisualization) p.getComponents()[1];
                            parent.setSelected(tv.getTokenID(), false);
                        } else if (p.getName().equals("leftc")) {
                            p.setBorder(inactiveLeft);
                        }
                    }
                }
            }
        };


        int counter = 0;
        Object[] data = parent.getEntryRegistry().keySet().toArray();
        while (counter < len) {
            Integer tok = (Integer) data[start];
            ConcordanceEntry cce = parent.getEntryRegistry().get(tok);
            if (!cce.isDisabled()) {
                this.addRow(tok, cce.getCandidateString(), cce.isSelected(), cce.isCorrected(), cce.isDisabled());
            }
            start++;
            counter++;
        }
    }

    private void addRow(int tokenIndex, String cand, boolean selected, boolean corrected, boolean disabled) {

        tl.insertRow(tl.getNumRow(), p);

        JCheckBox b = new JCheckBox();

        JPanel leftC = new JPanel();
        leftC.setLayout(new BoxLayout(leftC, BoxLayout.LINE_AXIS));
        leftC.setAlignmentX(Component.RIGHT_ALIGNMENT);
        leftC.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        leftC.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        leftC.setName("leftc");

        JPanel rightC = new JPanel();
        rightC.setLayout(new BoxLayout(rightC, BoxLayout.X_AXIS));
        rightC.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightC.setAlignmentY(BOTTOM_ALIGNMENT);
        rightC.setName("rightc");

        JPanel word = new JPanel();
        word.setLayout(new BoxLayout(word, BoxLayout.X_AXIS));
        word.setAlignmentX(Component.CENTER_ALIGNMENT);
        word.setAlignmentY(Component.CENTER_ALIGNMENT);
        word.setName("word");

        if (corrected) {
            b.setEnabled(false);
            b.setBackground(new Color(229, 236, 255));

            leftC.setBackground(new Color(229, 236, 255));
            word.setBackground(new Color(229, 236, 255));
            rightC.setBackground(new Color(229, 236, 255));

        } else if (disabled) {
            b.setEnabled(false);
            b.setBackground(Color.lightGray);

            leftC.setToolTipText(java.util.ResourceBundle.getBundle("jav/gui/concordance/Bundle").getString("mergeDamage"));
            word.setToolTipText(java.util.ResourceBundle.getBundle("jav/gui/concordance/Bundle").getString("mergeDamage"));
            rightC.setToolTipText(java.util.ResourceBundle.getBundle("jav/gui/concordance/Bundle").getString("mergeDamage"));

            leftC.setBackground(Color.lightGray);
            word.setBackground(Color.lightGray);
            rightC.setBackground(Color.lightGray);

        } else {
            b.setSelected(selected);
            b.addItemListener(itemListener);
            b.setBackground(Color.WHITE);

            leftC.setBackground(Color.WHITE);
            rightC.setBackground(Color.WHITE);
            word.setBackground(Color.WHITE);
        }

        if (!disabled) {
            for (int i = 1; i <= parent.getLeftContextSize(); i++) {
                if (tokenIndex - i >= 0) {
                    Token tt = MainController.findInstance().getDocument().getTokenByIndex(tokenIndex - i);
                    TokenVisualization tv = this.createTokenVisualization(tt);
                    if (corrected) {
                        tv.setBackground(new Color(229, 236, 255));
                    } else {
                        tv.setBackground(Color.white);
                    }
                    tv.setMode(tvMode);
                    parent.getTVRegistry().add(tt, tv);
                    leftC.add(tv);
                }
            }
        }

        if (selected) {
            leftC.setBorder(activeLeft);
        } else {
            leftC.setBorder(inactiveLeft);
        }

        this.add(leftC, "0, " + (tl.getNumRow() - 1) + ", f, f");

        word.add(Box.createHorizontalStrut(35)); //new JLabel("    "));

        Token tt = MainController.findInstance().getDocument().getTokenByID(tokenIndex);
        TokenVisualization tv = this.createTokenVisualization(tt);

        if (corrected) {
            tv.setBackground(new Color(229, 236, 255));
        } else {
            tv.setBackground(Color.white);
        }

        if (!disabled) {
            tv.setMode(tvMode, tt);
            parent.getTVRegistry().add(tokenIndex, tv);
        } else {
            tv.isolate();
        }
        tv.setAlignmentY(CENTER_ALIGNMENT);
        word.add(tv);

        b.setAlignmentY(CENTER_ALIGNMENT);
        word.add(b);
        FastCorrectionButton topcandb = new FastCorrectionButton(cand, tv.getTokenID());
        if (cand.equals("")) {
            topcandb.setEnabled(false);
        }
        topcandb.setName("topcandb");
        topcandb.setAlignmentY(CENTER_ALIGNMENT);

//        JLabel topcandl = new JLabel(" -> " + cand);
//        topcandl.setForeground(Color.BLUE);
//        topcandl.setName("topcandl");
//        topcandl.setAlignmentY(CENTER_ALIGNMENT);
        word.add(topcandb);
        word.add(Box.createHorizontalStrut(35)); //new JLabel("    "));

        if (selected) {
            word.setBorder(activeCenter);
        } else {
            word.setBorder(inactiveCenter);
        }

        this.add(word, "1, " + (tl.getNumRow() - 1));

        if (!disabled) {
            for (int i = 1; i <= parent.getRightContextSize(); i++) {
                if (tokenIndex + i < MainController.findInstance().getDocument().getNumberOfTokens()) {
                    Token ttt = MainController.findInstance().getDocument().getTokenByIndex(tokenIndex + i);
                    TokenVisualization tokv = this.createTokenVisualization(ttt);
                    if (corrected) {
                        tokv.setBackground(new Color(229, 236, 255));
                    } else {
                        tokv.setBackground(Color.white);
                    }
                    tokv.setMode(tvMode, ttt);
                    parent.getTVRegistry().add(ttt, tokv);

                    rightC.add(tokv);
                }
            }
        }

        if (selected) {
            rightC.setBorder(activeRight);
        } else {
            rightC.setBorder(inactiveRight);
        }
        this.add(rightC, "2, " + (tl.getNumRow() - 1) + ", f, f");
        parent.getConcordanceRegistry().add(tl.getNumRow() - 1, leftC, word, rightC);
    }

    public void selectAll() {

        Component[] comp = this.getComponents();
        int counter = 0;
        while (counter < comp.length) {
            JPanel word = (JPanel) comp[counter + 1];
            JCheckBox b = (JCheckBox) word.getComponent(2);

            if (b.isEnabled() && !b.isSelected()) {
                b.setSelected(true);
            }
            counter += 3;
        }
    }

    public void deselectAll() {
        Component[] comp = this.getComponents();
        int counter = 0;
        while (counter < comp.length) {
            JPanel word = (JPanel) comp[counter + 1];
            JCheckBox b = (JCheckBox) word.getComponent(2);

            if (b.isEnabled() && b.isSelected()) {
                b.setSelected(false);
            }
            counter += 3;
        }
    }

    private TokenVisualization createTokenVisualization(Token t) {

        TokenVisualization tv;
        TokenImageInfoBox tiib = t.getTokenImageInfoBox();
        if (tiib != null && parent.showImage() && parent.hasImage()) {
            // if no image loaded or token has different source image than last one
            if (ipc.getImageString() == null || !ipc.getImageString().equals(t.getImageFilename())) {
                ipc.setImageInput(t.getImageFilename());
            }

            int left = tiib.getCoordinateLeft();
            int right = tiib.getCoordinateRight();
            int top = tiib.getCoordinateTop();
            int bottom = tiib.getCoordinateBottom();
            int width = right - left;
            int height = bottom - top;

            BufferedImage bi = ipc.getTokenImage(left, top, width, height, parent.getScale());

            if (t.getWOCR().equals(" ")) {
                tv = new OnlyTextTokenVisualization(t, parent.getFontSize());
            } else {
                tv = new ImageTokenVisualization(bi, t, parent.getFontSize());
            }
        } else {
            tv = new OnlyTextTokenVisualization(t, parent.getFontSize());
        }

        return tv;
    }

    public void zoomFont(int i) {
        for (Component ca : this.getComponents()) {
            if (ca instanceof JPanel) {
                JPanel tokenVisContainer = (JPanel) ca;
                for (Component cb : tokenVisContainer.getComponents()) {
                    if (cb instanceof TokenVisualization) {
                        TokenVisualization tv = (TokenVisualization) cb;
                        tv.zoomFont(i);
                    }

                    if (cb instanceof JLabel) {
                        JLabel jl = (JLabel) cb;
                        Font f = jl.getFont();
                        float nS = (float) i;
                        Font deriveFont = f.deriveFont(nS);
                        jl.setFont(deriveFont);
                    }
                }
            }
        }
    }

    public void zoomImg(double scale) {
        for (Component ca : this.getComponents()) {
            if (ca instanceof JPanel) {
                JPanel tokenVisContainer = (JPanel) ca;
                for (Component cb : tokenVisContainer.getComponents()) {
                    if (cb instanceof ImageTokenVisualization) {
                        ImageTokenVisualization tv = (ImageTokenVisualization) cb;

                        // load image file containing token if not already loaded
                        Token tok = MainController.findInstance().getDocument().getTokenByID(tv.getTokenID());
                        if (!tok.getImageFilename().equals(ipc.getImageString())) {
                            ipc.setImageInput(tok.getImageFilename());
                        }
                        TokenImageInfoBox tiib = tok.getTokenImageInfoBox();
                        if (tiib != null) {
                            int left = tiib.getCoordinateLeft();
                            int right = tiib.getCoordinateRight();
                            int top = tiib.getCoordinateTop();
                            int bottom = tiib.getCoordinateBottom();
                            int width = right - left;
                            int height = bottom - top;
                            tv.setImage(ipc.getTokenImage(left, top, width, height, scale));
                        }


                    }
                }
            }
        }
    }

    public void toggleImages(boolean on) {
        for (Component ca : this.getComponents()) {
            if (ca instanceof JPanel) {
                JPanel container = (JPanel) ca;
                for (Component cb : container.getComponents()) {
                    if (cb instanceof ImageTokenVisualization) {
                        ImageTokenVisualization tv = (ImageTokenVisualization) cb;
                        if (on) {
                            TokenImageInfoBox tiib = MainController.findInstance().getDocument().getTokenByID(tv.getTokenID()).getTokenImageInfoBox();
                            if (tiib != null) {
                                int left = tiib.getCoordinateLeft();
                                int right = tiib.getCoordinateRight();
                                int top = tiib.getCoordinateTop();
                                int bottom = tiib.getCoordinateBottom();
                                int width = right - left;
                                int height = bottom - top;

                                BufferedImage bi = ipc.getTokenImage(left, top, width, height, parent.getScale());
                                tv.setImage(bi);
                            }
                        } else {
                            if (tv.hasImage()) {
                                tv.clearImage();
                            }
                        }
                    } else if (cb instanceof PseudoImageTokenVisualization) {
                        ((PseudoImageTokenVisualization) cb).toggleImage(on);
                    }
                }
            }
        }
    }

    public TokenVisualizationMode getVisualizationMode() {
        return this.tvMode;
    }

    // sets the candidate for all tokens, only to be used in clone concordance
    public void setCandidateString(String s) {
        for (Component ca : this.getComponents()) {
            if (ca instanceof JPanel) {
                if (ca.getName().equals("word")) {
                    JPanel wordContainer = (JPanel) ca;
                    TokenVisualization tokv = (TokenVisualization) wordContainer.getComponent(1);
                    ConcordanceEntry cce = parent.getEntryRegistry().get(tokv.getTokenID());
                    if (!cce.isCorrected() && !cce.isDisabled()) {
                        JButton wordcandb = (JButton) wordContainer.getComponent(3);
                        //JLabel wordcandl = (JLabel) wordContainer.getComponent(3);
                        wordcandb.setText(s);
                    }
                }
            }
        }
    }

    public void grayOut(TokenVisualization tv) {
        ArrayList<JPanel> line = parent.getConcordanceRegistry().get(tl.getConstraints(tv.getParent()).row1);
        JPanel leftC = line.get(0);
        JPanel word = line.get(1);
        JPanel rightC = line.get(2);
        JCheckBox b = (JCheckBox) word.getComponent(2);
        JButton jb = (JButton) word.getComponent(3);
        jb.setEnabled(false);

        leftC.setBorder(inactiveRight);
        word.setBorder(inactiveCenter);
        rightC.setBorder(inactiveLeft);

        b.setSelected(false);
        b.setEnabled(false);
        for (Component ca : leftC.getComponents()) {
            if (ca instanceof TokenVisualization) {
                ca.setBackground(new Color(229, 236, 255));
            }
        }
        for (Component ca : rightC.getComponents()) {
            if (ca instanceof TokenVisualization) {
                ca.setBackground(new Color(229, 236, 255));
            }
        }
        leftC.setBackground(new Color(229, 236, 255));
        word.setBackground(new Color(229, 236, 255));
        rightC.setBackground(new Color(229, 236, 255));
        tv.setBackground(new Color(229, 236, 255));
        b.setBackground(new Color(229, 236, 255));
    }

    private void disableEntry(final TokenVisualization tv, int index, ArrayList<JPanel> rowpa) {
        parent.getConcordanceRegistry().remove(tl.getConstraints(tv.getParent()).row1);
        parent.getEntryRegistry().get(index).setDisabled(true);

        for (Component ca : rowpa.get(0).getComponents()) {
            if (ca instanceof TokenVisualization) {
                TokenVisualization tokv = (TokenVisualization) ca;
                tokv.setBackground(Color.lightGray);
                tokv.isolate();
            }
        }

        for (Component ca : rowpa.get(1).getComponents()) {
            if (ca instanceof TokenVisualization) {
                TokenVisualization tokv = (TokenVisualization) ca;
                tokv.setBackground(Color.lightGray);
                tokv.isolate();
            }
        }

        for (Component ca : rowpa.get(2).getComponents()) {
            if (ca instanceof TokenVisualization) {
                TokenVisualization tokv = (TokenVisualization) ca;
                tokv.setBackground(Color.lightGray);
                tokv.isolate();
            }
        }

        parent.addDisabled();
        JCheckBox b = (JCheckBox) rowpa.get(1).getComponent(2);
        b.setEnabled(false);
        b.setSelected(false);

        FastCorrectionButton fb = (FastCorrectionButton) rowpa.get(1).getComponent(3);
        fb.setEnabled(false);

        MouseListener listener = new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent me) {
            }

            @Override
            public void mousePressed(MouseEvent me) {
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                if (me.getButton() == MouseEvent.BUTTON3) {
                    JPopupMenu menu = new JPopupMenu();
                    JMenuItem jumpItem = new JMenuItem(java.util.ResourceBundle.getBundle("jav/gui/token/display/Bundle").getString("jumpto"));
                    jumpItem.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            MessageCenter.getInstance().fireTokenNavigationEvent(new TokenNavigationEvent(tv, tv.getTokenID(), TokenNavigationType.FOCUS_IN_MAIN));
                        }
                    });
                    menu.add(jumpItem);
                    menu.show(tv, 50, 50);
                }
            }

            @Override
            public void mouseEntered(MouseEvent me) {
            }

            @Override
            public void mouseExited(MouseEvent me) {
            }
        };

        rowpa.get(0).addMouseListener(listener);
        rowpa.get(1).addMouseListener(listener);
        rowpa.get(2).addMouseListener(listener);


        rowpa.get(0).setBorder(inactiveRight);
        rowpa.get(1).setBorder(inactiveCenter);
        rowpa.get(2).setBorder(inactiveLeft);

        rowpa.get(0).setBackground(Color.lightGray);
        rowpa.get(1).setBackground(Color.lightGray);
        rowpa.get(2).setBackground(Color.lightGray);

        b.setBackground(Color.lightGray);

        rowpa.get(0).setToolTipText(java.util.ResourceBundle.getBundle("jav/gui/concordance/Bundle").getString("mergeDamage"));
        rowpa.get(1).setToolTipText(java.util.ResourceBundle.getBundle("jav/gui/concordance/Bundle").getString("mergeDamage"));
        rowpa.get(2).setToolTipText(java.util.ResourceBundle.getBundle("jav/gui/concordance/Bundle").getString("mergeDamage"));

        this.updateTokenRegistry();
    }

    private void updateTokenRegistry() {
        parent.getTVRegistry().clear();
        for (Component ca : this.getComponents()) {
            if (ca instanceof JPanel) {
                JPanel container = (JPanel) ca;
                for (Component cb : container.getComponents()) {
                    if (cb instanceof TokenVisualization) {
                        TokenVisualization tv = (TokenVisualization) cb;
                        parent.getTVRegistry().add(tv.getTokenID(), tv);
                    }
                }
            }
        }
    }

    public void updateTokenVisualizationIndices(int startindex, int discr) {
        for (Component ca : this.getComponents()) {
            if (ca instanceof JPanel) {
                JPanel container = (JPanel) ca;
                for (Component cb : container.getComponents()) {
                    if (cb instanceof TokenVisualization) {
                        TokenVisualization tv = (TokenVisualization) cb;
                        if (tv.getTokenID() > startindex) {
//                            IOProvider.getDefault().getIO("Nachrichten", false).getOut().println(System.currentTimeMillis() + " cv tv update " + tv.getTokenTextLabelText() + " # " + tv.getTokenIndex() + " # " + discr + " # " + (tv.getTokenIndex() + discr));
                            tv.setTokenID(tv.getTokenID() + discr);
                        }
                    } else if (cb instanceof FastCorrectionButton) {
                        FastCorrectionButton fcb = (FastCorrectionButton) cb;
                        if (fcb.getTokenIndex() > startindex) {
                            fcb.setTokenIndex(fcb.getTokenIndex() + discr);
                        }
                    }
                }
            }
        }
    }

    /*
     * tv is the tokenvisualization that triggered the event
     */
    public void update(TokenStatusType tst, TokenVisualization tv, int tokenIndex, int numAffected, boolean updateIndices) {

        ArrayList<JPanel> rowpa = parent.getConcordanceRegistry().get(tl.getConstraints(tv.getParent()).row1);
        if (rowpa != null) {
            TokenVisualization mid = (TokenVisualization) rowpa.get(1).getComponent(1);

            if (tst == TokenStatusType.MERGED_RIGHT || tst == TokenStatusType.DELETE) {

                if (tv.getParent().getName().equals("leftc")) {
                    for (AbstractTokenVisualization atv : parent.getTVRegistry().getVisualizations(tokenIndex + numAffected)) {
                        TokenVisualization test = (TokenVisualization) atv;
                        if (tl.getConstraints(tv.getParent()).row1 == tl.getConstraints(test.getParent()).row1) {
                            /*
                             * if the deleted area affects the concordance
                             * entry, it has to be disabled because by being
                             * tampered with, it looses its specific features
                             * that made it belong to the concordance group
                             */
                            if (!test.getParent().getName().equals("leftc")) {
                                tv.update( MainController.findInstance().getDocument().getTokenByID(tv.getTokenID()).getWDisplay());
                                disableEntry(tv, mid.getTokenID(), rowpa);
                                rowpa.get(1).removeAll();
                                rowpa.get(2).removeAll();
                                parent.getEntryRegistry().remove(mid.getTokenID());

                                if( updateIndices ) {
                                    this.updateTokenVisualizationIndices(tokenIndex, 0 - numAffected);
                                }
                                
                                this.updateTokenRegistry();
                                //if the merging is inside the left context of the entry, update the context   
                            } else {
                                rowpa.get(0).removeAll();
                                this.updateTokenVisualizationIndices(tokenIndex, 0 - numAffected);
                                tokenIndex = mid.getTokenID();
                                for (int i = 1; i <= parent.getLeftContextSize(); i++) {
                                    if (tokenIndex - i >= 0) {
                                        Token t = MainController.findInstance().getDocument().getTokenByIndex(tokenIndex - i);
                                        TokenVisualization temp = this.createTokenVisualization(t);
                                        temp.setBackground(Color.white);
                                        temp.setMode(tvMode, t);
                                        rowpa.get(0).add(temp);
                                    }
                                }
                                rowpa.get(0).revalidate();
                                this.updateTokenRegistry();
                            }

                            break;
                        }
                    }
                } else if (tv.getParent().getName().equals("word")) {
                    disableEntry(tv, tokenIndex, rowpa);
                    tv.getParent().remove(tv);
                    parent.getEntryRegistry().remove(mid.getTokenID());
                    
                    if( updateIndices ) {
                        this.updateTokenVisualizationIndices(tokenIndex, 0 - numAffected);
                    }
                    
                    this.updateTokenRegistry();

                } else if (tv.getParent().getName().equals("rightc")) {
                    rowpa.get(2).removeAll();
                    this.updateTokenVisualizationIndices(tokenIndex, 0 - numAffected);
                    tokenIndex = mid.getTokenID();
                    for (int i = 1; i <= parent.getRightContextSize(); i++) {
                        if (tokenIndex + i < MainController.findInstance().getDocument().getNumberOfTokens()) {
                            Token t = MainController.findInstance().getDocument().getTokenByIndex(tokenIndex + i);
                            TokenVisualization temp = this.createTokenVisualization(t);
                            temp.setBackground(Color.white);
                            temp.setMode(tvMode, t);
                            rowpa.get(2).add(temp);
                        }
                    }
                    rowpa.get(2).revalidate();
                    this.updateTokenRegistry();
                } else {
                    new CustomErrorDialog().showDialog("ClassicConcordancePage::update undefined panel name");
                }
            } else if (tst == TokenStatusType.SPLIT) {

                if (tv.getParent().getName().equals("leftc")) {
                    rowpa.get(0).removeAll();
                    this.updateTokenVisualizationIndices(tokenIndex, numAffected);
                    tokenIndex = mid.getTokenID();
                    for (int i = 1; i <= parent.getLeftContextSize(); i++) {
                        if (tokenIndex - i >= 0) {
                            TokenVisualization temp = this.createTokenVisualization(MainController.findInstance().getDocument().getTokenByIndex(tokenIndex - i));
                            temp.setBackground(Color.white);
                            temp.setMode(tvMode);
                            rowpa.get(0).add(temp);
                        }
                    }
                    rowpa.get(0).revalidate();
                    this.updateTokenRegistry();
                } else if (tv.getParent().getName().equals("word")) {

                    tv.update( MainController.findInstance().getDocument().getTokenByID(tv.getTokenID()).getWDisplay());
                    disableEntry(tv, tv.getTokenID(), rowpa);
                    parent.getEntryRegistry().remove(tokenIndex);
                    
                    if( updateIndices ) {
                        this.updateTokenVisualizationIndices(tokenIndex, 0 - numAffected);
                    }
                    
                    this.updateTokenRegistry();

                } else if (tv.getParent().getName().equals("rightc")) {

                    rowpa.get(2).removeAll();
                    
                    if( updateIndices ) {
                        this.updateTokenVisualizationIndices(tokenIndex, 0 - numAffected);
                    }
                    
                    tokenIndex = mid.getTokenID();
                    for (int i = 1; i <= parent.getRightContextSize(); i++) {
                        if (tokenIndex + i < MainController.findInstance().getDocument().getNumberOfTokens()) {
                            Token t = MainController.findInstance().getDocument().getTokenByIndex(tokenIndex + i);
                            TokenVisualization temp = this.createTokenVisualization(t);
                            temp.setBackground(Color.white);
                            temp.setMode(tvMode);
                            rowpa.get(2).add(temp);
                        }
                    }
                    rowpa.get(2).revalidate();
                    this.updateTokenRegistry();
                } else {
                    new CustomErrorDialog().showDialog("ClassicConcordancePage::update undefined panel name");
                }
            }
        }
    }
}
