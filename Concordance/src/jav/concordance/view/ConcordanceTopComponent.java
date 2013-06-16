package jav.concordance.view;

import jav.concordance.control.ConcordanceEntry;
import jav.concordance.control.ConcordanceGraphicsRegistry;
import jav.correctionBackend.Token;
import jav.gui.dialogs.CustomErrorDialog;
import jav.gui.events.MessageCenter;
import jav.gui.events.documentChanged.DocumentChangedEvent;
import jav.gui.events.pageChanged.PageChangedEvent;
import jav.gui.events.special.multiselection.MultiSelectionEvent;
import jav.gui.events.tokenDeselection.TokenDeselectionEvent;
import jav.gui.events.tokenMultiDeselection.TokenMultiDeselectionEvent;
import jav.gui.events.tokenSelection.TokenSelectionEvent;
import jav.gui.events.tokenSelection.TokenSelectionType;
import jav.gui.events.tokenStatus.CorrectedEvent;
import jav.gui.events.tokenStatus.DeleteEvent;
import jav.gui.events.tokenStatus.InsertEvent;
import jav.gui.events.tokenStatus.MergeEvent;
import jav.gui.events.tokenStatus.SetCorrectedEvent;
import jav.gui.events.tokenStatus.SplitEvent;
import jav.gui.events.tokenStatus.TokenStatusEvent;
import jav.gui.events.tokenStatus.TokenStatusType;
import jav.gui.main.AbstractEditorViewTopComponent;
import jav.gui.main.AbstractTokenVisualization;
import jav.gui.main.GlobalActions;
import jav.gui.main.MainController;
import jav.gui.main.TokenVisualizationMultiRegistry;
import jav.gui.token.display.TokenVisualization;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressRunnable;
import org.netbeans.api.progress.ProgressUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;

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
public class ConcordanceTopComponent extends AbstractEditorViewTopComponent {

    ConcordancePage cp = null;
    private InstanceContent content = new InstanceContent();
    private Preferences node;
    private boolean isOpen = false;
    private int leftContextSize;
    private int rightContextSize;
    private int tokensPerPage;
    private String name;
    int numSelected = 0;
    private int corrected = 0;
    private int disabled = 0;
    int toDo = 0;
    private int tokensInPage = 0;
    private int fontSize;
    private double imgScale;
    private int actualPage = -1;
    private int currentTokenID = -1;
    private boolean showImages;
    private boolean isActive;
    private ConcordanceTopComponent instance;
    private ConcordanceGlobalActions globalActions;
    private TokenVisualizationMultiRegistry tvRegistry;
    private ConcordanceGraphicsRegistry concGRegistry;
    LinkedHashMap<Integer, ConcordanceEntry> tokens;

    public void init(ArrayList<Token> t, String n) {
        this.name = n;

        tvRegistry = new TokenVisualizationMultiRegistry();
        concGRegistry = new ConcordanceGraphicsRegistry();
        tokens = new LinkedHashMap<>();
        for (Token tok : t) {
            ConcordanceEntry temp = new ConcordanceEntry(tok);
            if (!tok.getTopSuggestion().equals("")) {
                temp.setCandidateString(tok.getTopSuggestion());
            } else {
                temp.setCandidateString("");
            }
            tokens.put(tok.getID(), temp);
        }
        instance = this;
        globalActions = new ConcordanceGlobalActions(this);
        this.setFocusable(true);

        MessageCenter.getInstance().addDocumentChangedEventListener(this);
        MessageCenter.getInstance().addTokenDeselectionEventListener(this);
        MessageCenter.getInstance().addTokenSelectionEventListener(this);
        MessageCenter.getInstance().addTokenStatusEventListener(this);

        associateLookup(new AbstractLookup(content));
        node = NbPreferences.forModule(this.getClass());
        leftContextSize = node.getInt("leftCLen", 7);
        rightContextSize = node.getInt("rightCLen", 7);
        tokensPerPage = node.getInt("tokensPerPage", 50);
        imgScale = Double.parseDouble(MainController.findInstance().getDocumentProperties().getProperty("concImageScale"));
        fontSize = Integer.parseInt(MainController.findInstance().getDocumentProperties().getProperty("concFontSize"));
        showImages = node.getBoolean("hasImages", true);

        initComponents();
        MainController.findInstance().customizeScrollPane(jScrollPane1);
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(60);
        jScrollPane1.getVerticalScrollBar().setBlockIncrement(180);

        jButton1.setEnabled(false);
        jButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NotifyDescriptor d = new NotifyDescriptor.Confirmation(java.util.ResourceBundle.getBundle("jav/concordance/Bundle").getString("WENN SIE FORTFAHREN WERDEN MEHRERE TOKEN KORRIGIERT!"), java.util.ResourceBundle.getBundle("jav/concordance/Bundle").getString("ACHTUNG"), NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.WARNING_MESSAGE);
                Object retval = DialogDisplayer.getDefault().notify(d);
                if (retval.equals(NotifyDescriptor.OK_OPTION)) {
                    correct();
                }
            }
        });

        ProgressRunnable bu = new Builder();
        ProgressUtils.showProgressDialogAndRun(bu, "Building", true);
        this.actualPage = 0;
        int display = actualPage + 1;
        setName(name + " " + display + "/" + getMaxPages());
        content.add(instance);
        MainController.findInstance().setLastFocusedTopComponent(instance);
    }

    private void initComponents() {

        jToolBar1 = this.getToolBar();
        jScrollPane1 = new javax.swing.JScrollPane();
//        jScrollPane1.setDoubleBuffered(true);

        totalentriesLabel = new JLabel("Entries Total: " + 0 + " ");
        numSelectedLabel = new JLabel("Entries Selected: " + 0 + " ");
        numCorrectedLabel = new JLabel("Entries Corrected: " + 0 + " ");
        numDisabledLabel = new JLabel("Entries Disabled: " + 0 + " ");
        numToDoLabel = new JLabel("Entries to process: " + 0 + " ");

        totalentriesLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        numSelectedLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        numCorrectedLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        numDisabledLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        numToDoLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);


        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(totalentriesLabel).addGap(18, 18, 18).addComponent(numSelectedLabel).addGap(18, 18, 18).addComponent(numCorrectedLabel).addGap(18, 18, 18).addComponent(numDisabledLabel).addGap(18, 18, 18).addComponent(numToDoLabel).addContainerGap(165, Short.MAX_VALUE)).addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(1, 1, 1).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(numSelectedLabel).addComponent(numCorrectedLabel).addComponent(numDisabledLabel).addComponent(numToDoLabel).addComponent(totalentriesLabel)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)));

    }
    javax.swing.JButton jButton1;
    javax.swing.JScrollPane jScrollPane1;
    javax.swing.JToolBar.Separator jSeparator1;
    javax.swing.JToolBar.Separator jSeparator2;
    javax.swing.JToolBar.Separator jSeparator3;
    javax.swing.JToggleButton jToggleButton1;
    javax.swing.JToggleButton jToggleButton2;
    javax.swing.JToolBar jToolBar1;
    javax.swing.JLabel totalentriesLabel;
    javax.swing.JLabel numSelectedLabel;
    javax.swing.JLabel numCorrectedLabel;
    javax.swing.JLabel numDisabledLabel;
    javax.swing.JLabel numToDoLabel;

    public JToolBar getToolBar() {
        JToolBar toolb = new JToolBar();
        jToggleButton1 = new javax.swing.JToggleButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jToggleButton2 = new javax.swing.JToggleButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jButton1 = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();

        toolb.setRollover(true);
        toolb.setFloatable(false);

        jToggleButton1.setText(java.util.ResourceBundle.getBundle("jav/concordance/Bundle").getString("pageSelect"));
        jToggleButton1.setFocusable(false);
        jToggleButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolb.add(jToggleButton1);
        toolb.add(jSeparator1);

        jToggleButton2.setText(java.util.ResourceBundle.getBundle("jav/concordance/Bundle").getString("allSelect"));
        jToggleButton2.setFocusable(false);
        jToggleButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolb.add(jToggleButton2);
        toolb.add(jSeparator2);

        toolb.add(jSeparator3);

        jButton1.setText(java.util.ResourceBundle.getBundle("jav/concordance/Bundle").getString("correct"));
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolb.add(jButton1);

        /*
         * select / deselect current Page
         */
        jToggleButton1.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {
                if (ie.getStateChange() == ItemEvent.SELECTED) {

                    Object[] o = tokens.keySet().toArray();
                    for (int i = actualPage * tokensPerPage; i < tokensInPage; i++) {
                        int tok = (Integer) o[i];
                        ConcordanceEntry cce = tokens.get(tok);
                        if (!cce.isCorrected() && !cce.isDisabled()) {
                            setSelected(tok, true);
                        }
                    }
                    cp.selectAll();
                } else {

                    Object[] o = tokens.keySet().toArray();
                    for (int i = actualPage * tokensPerPage; i < tokensInPage; i++) {
                        int tok = (Integer) o[i];
                        ConcordanceEntry cce = tokens.get(tok);
                        if (!cce.isCorrected() && !cce.isDisabled()) {
                            setSelected(tok, false);
                        }
                    }
                    cp.deselectAll();
                }
            }
        });

        /*
         * select / deselect everything
         */
        jToggleButton2.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {
                if (ie.getStateChange() == ItemEvent.SELECTED) {

                    jToggleButton1.setSelected(true);
                    Iterator<Integer> i = tokens.keySet().iterator();
                    while (i.hasNext()) {
                        Integer tokid = i.next();
                        ConcordanceEntry cce = tokens.get(tokid);
                        if (cce.isSelectable()) {
                            if (!tvRegistry.contains(tokid)) {
//                            if (count < actualPage * tokensPerPage || count >= actualPage * tokensPerPage + tokensInPage) {
                                setSelected(tokid, true);
                            }
                        }
                    }
                    cp.selectAll();
                } else {
                    jToggleButton1.setSelected(false);
                    Iterator<Integer> i = tokens.keySet().iterator();
                    while (i.hasNext()) {
                        Integer tokid = i.next();
                        ConcordanceEntry cce = tokens.get(tokid);
                        if (cce.isSelected() & !tvRegistry.contains(tokid)) {
                            setSelected(tokid, false);
                        }
                    }
                    cp.deselectAll();
                }
            }
        });

        jButton1.setEnabled(false);
        return toolb;
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void componentActivated() {
        this.isActive = true;
        this.requestFocusInWindow(true);
        if (MainController.findInstance().getLastFocusedTopComponent() != this) {
            MainController.findInstance().setLastFocusedTopComponent(this);
        }
        if (this.isReady()) {
            MessageCenter.getInstance().fireTokenMultiDeselectionEvent(new TokenMultiDeselectionEvent(this));
            if (currentTokenID != -1) {
                MessageCenter.getInstance().fireTokenSelectionEvent(new TokenSelectionEvent(this, currentTokenID, TokenSelectionType.NORMAL));
            } else {
                MessageCenter.getInstance().fireTokenDeselectionEvent(new TokenDeselectionEvent(this, currentTokenID));
            }
        }
    }

    @Override
    public void componentDeactivated() {
        this.isActive = false;
    }

    @Override
    public void dispatchEvent(TokenSelectionEvent e) {
        if (this.isActive) {
            this.currentTokenID = e.getTokenID();
        }
    }

    @Override
    public GlobalActions getGlobalActions() {
        return this.globalActions;
    }

    @Override
    public void dispatchMultiSelectionEvent(MultiSelectionEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void dispatchEvent(DocumentChangedEvent e) {
        this.close();
    }

    @Override
    public void dispatchEvent(TokenDeselectionEvent e) {
        if (this.isActive && this.currentTokenID == e.getTokenID()) {
            this.currentTokenID = -1;
        }
    }

    @Override
    public boolean isReady() {
        return this.isOpen;
    }

    @Override
    public void zoomFont(int i) {
        content.remove(this);
        cp.zoomFont(i);
        this.fontSize = i;
        MainController.findInstance().getDocumentProperties().setProperty("concFontSize", "" + this.fontSize);
        content.add(this);
        this.revalidate();
    }

    @Override
    public int getMaxFontSize() {
        return node.getInt("maxFontSize", 40);
    }

    @Override
    public int getMinFontSize() {
        return node.getInt("minFontSize", 5);
    }

    @Override
    public int getFontSize() {
        return this.fontSize;
    }

    @Override
    public boolean hasImage() {
        return true;
    }

    @Override
    public boolean showImage() {
        return this.showImages;
    }

    @Override
    public void zoomImg(double scale) {
        content.remove(this);
        cp.zoomImg(scale);
        this.imgScale = scale;
        MainController.findInstance().getDocumentProperties().setProperty("concImageScale", "" + this.imgScale);
        content.add(this);
        this.revalidate();
    }

    @Override
    public double getMaxScale() {
        return node.getDouble("maxScale", 1.5);
    }

    @Override
    public double getMinScale() {
        return node.getDouble("minScale", 0.2);
    }

    @Override
    public double getScale() {
        return this.imgScale;
    }

    @Override
    public int getPageN() {
        return this.actualPage + 1;
    }

    @Override
    public int getMaxPages() {
        return (tokens.size() / tokensPerPage) + 1;
    }

    public void setShowImages(boolean b) {
        this.showImages = b;
    }

    @Override
    public void gotoPage(final int p) {

        SwingWorker<Boolean, Object> worker = new SwingWorker<Boolean, Object>() {
            @Override
            protected Boolean doInBackground() {
                boolean retval = false;
                try {
                    tvRegistry = new TokenVisualizationMultiRegistry();
                    concGRegistry = new ConcordanceGraphicsRegistry();

                    if (p * tokensPerPage + tokensPerPage >= tokens.size()) {
                        tokensInPage = tokensPerPage - ((p * tokensPerPage + tokensPerPage) - tokens.size());
                        cp = new ConcordancePage(instance, p * tokensPerPage, tokensPerPage - ((p * tokensPerPage + tokensPerPage) - tokens.size()));
                    } else {
                        tokensInPage = tokensPerPage;
                        cp = new ConcordancePage(instance, p * tokensPerPage, tokensPerPage);
                    }

                    retval = true;

                } catch (Exception e) {
                    retval = false;
                }
                return retval;
            }

            @Override
            protected void done() {
                try {
                    boolean retval = get();
                    if (retval) {
                        jScrollPane1.setViewportView(cp);
                        actualPage = p;
                        int display = actualPage + 1;
                        setName(name + " " + display + "/" + getMaxPages());
                        MessageCenter.getInstance().firePageChangedEvent(new PageChangedEvent(instance, actualPage));
                        content.add(instance);
                        MainController.findInstance().addToLookup(globalActions);
                    } else {
                        new CustomErrorDialog().showDialog("CloneConcordanceView::GotoPage");
                    }
                } catch (ExecutionException ex) {
                } catch (InterruptedException ex) {
                } catch (CancellationException ex) {
                }
            }
        };
        MainController.findInstance().removeFromLookup(globalActions);
        this.concGRegistry.clear();
        content.remove(instance);
        this.tvRegistry.clear();
        worker.execute();
    }

    public int getActualTokenID() {
        return this.currentTokenID;
    }

    void goToNextRow() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    void goToPreviousRow() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     *
     * @param tokenId
     * @param b
     */
    public void setSelected(int tokenId, boolean b) {
        if (b && tokens.get(tokenId).isSelectable()) {
            this.numSelected++;
            numSelectedLabel.setText("Entries Selected: " + numSelected);
            if (this.numSelected > 0 & !tokens.get(tokenId).getCandidateString().equals("")) {
                jButton1.setEnabled(true);
            }
            tokens.get(tokenId).setSelected(b);
        } else if (!b && tokens.get(tokenId).isSelected()) {
            this.numSelected--;
            numSelectedLabel.setText("Entries Selected: " + numSelected);
            if (this.numSelected == 0) {
                jToggleButton1.setSelected(false);
                jToggleButton2.setSelected(false);
                jButton1.setEnabled(false);
            } else if (this.numSelected < 0) {
                new CustomErrorDialog().showDialog("ClassicConcordance::removeSelected < 0");
            }
            tokens.get(tokenId).setSelected(b);
        } else {
//            System.out.println( "ELSE " + b + " " + tokenId);
        }
    }

    public TokenVisualizationMultiRegistry getTVRegistry() {
        return this.tvRegistry;
    }

    public LinkedHashMap<Integer, ConcordanceEntry> getEntryRegistry() {
        return this.tokens;
    }

    @Override
    public boolean isEditing() {
        return false;
    }

    public int getLeftContextSize() {
        return this.leftContextSize;
    }

    public int getRightContextSize() {
        return this.rightContextSize;
    }

    public void addDisabled() {
        disabled++;
        toDo--;
        numToDoLabel.setText("Entries to process: " + toDo);
        numDisabledLabel.setText("Entries Disabled: " + disabled);
    }

    public void removeDisabled() {
        disabled--;
        toDo++;
        numToDoLabel.setText("Entries to process: " + toDo);
        numDisabledLabel.setText("Entries Disabled: " + disabled);
    }

    private void correct() {
        ProgressRunnable cr = new Corrector();
        ProgressUtils.showProgressDialogAndRun(cr, "Correcting", true);
        this.requestFocusInWindow(true);
    }

    void toggleImages(boolean b) {
        cp.toggleImages(b);
    }

    public ConcordanceGraphicsRegistry getConcordanceGraphicsRegistry() {
        return this.concGRegistry;
    }

    @Override
    public void dispatchEvent(TokenStatusEvent e) {
        TokenStatusType tst = e.getType();

        if (tst == TokenStatusType.SETCORRECTED) {
            ArrayList<AbstractTokenVisualization> tokvs = this.getTVRegistry().getVisualizations(e.getPOIID());
            if (tokvs != null) {
                Iterator<AbstractTokenVisualization> tokv_it = tokvs.iterator();
                while (tokv_it.hasNext()) {
                    TokenVisualization tv = (TokenVisualization) tokv_it.next();
                    tv.setCorrected(((SetCorrectedEvent) e).getSetTo());
                    if (tokens.containsKey(tv.getTokenID())) {
                        cp.grayOut(tv, ((SetCorrectedEvent) e).getSetTo());
                    }
                }
            }

        } else if (tst == TokenStatusType.CORRECTED) {
            ArrayList<AbstractTokenVisualization> tokvs = this.getTVRegistry().getVisualizations(e.getPOIID());
            if (tokvs != null) {
                CorrectedEvent cor = (CorrectedEvent) e;
                Iterator<AbstractTokenVisualization> tokv_it = tokvs.iterator();
                while (tokv_it.hasNext()) {
                    TokenVisualization tv = (TokenVisualization) tokv_it.next();
                    if (tokens.containsKey(tv.getTokenID())) {
                        tokens.get(tv.getTokenID()).setCorrected(cor.getSetTo());
                        if (cor.getSetTo()) {
                            corrected++;
                            toDo--;
                        } else {
                            corrected--;
                            toDo++;
                        }
                        if (corrected == tokens.size()) {
                            jButton1.setEnabled(false);
                            jToggleButton1.setEnabled(false);
                            jToggleButton2.setEnabled(false);
                        } else {
                            jButton1.setEnabled(true);
                            jToggleButton1.setEnabled(true);
                            jToggleButton2.setEnabled(true);                            
                        }
                        numToDoLabel.setText("Entries to process: " + toDo);
                        numCorrectedLabel.setText("Entries Corrected: " + corrected);
                        tv.update(cor.getNewText(), cor.getSetTo());
                        cp.grayOut(tv, cor.getSetTo());
                    } else {
                        tv.update(cor.getNewText(), cor.getSetTo());
                    }
                }
            }

        } else if (tst == TokenStatusType.MERGED_RIGHT) {
            cp.update(e.getType(), e.getPOIID(), ((MergeEvent) e).getAffectedTokenIds());

        } else if (tst == TokenStatusType.SPLIT) {
            cp.update(e.getType(), e.getPOIID(), ((SplitEvent) e).getAffectedTokenIds());

        } else if (tst == TokenStatusType.DELETE) {
            cp.update(e.getType(), e.getPOIID(), ((DeleteEvent) e).getAffectedTokenIds());

        } else if (tst == TokenStatusType.INSERT) {
            cp.update(e.getType(), e.getPOIID(), ((InsertEvent) e).getAffectedTokenIds());

            // TODO throw unknown tokenstatusType exception    
        } else {
        }
    }

    private class Builder implements ProgressRunnable {

        @Override
        public Object run(ProgressHandle ph) {
            ph.progress("Building Concordance");
            ph.setDisplayName("Concordance construction");

            if (tokens.size() < tokensPerPage) {
                tokensInPage = tokens.size();
                cp = new ConcordancePage(instance, 0, tokens.size());
            } else {
                tokensInPage = tokensPerPage;
                cp = new ConcordancePage(instance, 0, tokensPerPage);
            }

            ph.progress("Showing Concordance");

            jScrollPane1.setViewportView(cp);
            jScrollPane1.getHorizontalScrollBar().setValue(cp.getPreferredSize().width / 10);
            totalentriesLabel.setText("Entries Total: " + tokens.size());
            toDo = tokens.size();
            numToDoLabel.setText("Entries to process: " + toDo);
            isOpen = true;
            return 0;
        }
    }

    private class Corrector implements ProgressRunnable {

        @Override
        public Object run(ProgressHandle ph) {
            try {
                ph.progress("starting correction");

                Object[] o = tokens.keySet().toArray();
                HashMap<Integer, String> tokensToCorrect = new HashMap<>();
                int total = numSelected;
                int corrcount = 0;
                for (int i = 0; i < o.length; i++) {
                    int tokenId = (Integer) o[i];
                    ConcordanceEntry cce = tokens.get(tokenId);
                    if (cce.isSelected() && !cce.isCorrected()) {
                        corrcount++;
//                        if( !tvRegistry.contains(tokenId)) {
//                            setSelected(tokenId, false);
//                        }
                        if (corrcount % 20 == 0) {
                            ph.progress("correcting token " + corrcount + " of " + total);
                        }

                        tokensToCorrect.put(tokenId, cce.getCandidateString());
//                        MainController.findInstance().addToLog("CloneConcordanceView " + name + " # correct # " + tok.getWOCR() + " # " + cce.getCandidateString());
//                        cce.setCorrected(true);
//                        corrected++;
//                        toDo--;
                    } else {
                        System.out.println("corrector else " + tokenId);
                    }
                }

                MainController.findInstance().correctTokensByString(tokensToCorrect);

//                if (corrected == tokens.size()) {
//                    jButton1.setEnabled(false);
//                    jToggleButton1.setEnabled(false);
//                    jToggleButton2.setEnabled(false);
//                }
//                numToDoLabel.setText("Entries to process: " + toDo);
//                numCorrectedLabel.setText("Entries Corrected: " + corrected);
                return 1;
            } catch (Exception ex) {
                return 0;
            }
        }
    }
}