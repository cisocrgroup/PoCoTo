package jav.gui.mainWindow;

import jav.correctionBackend.Document;
import jav.correctionBackend.MyIterator;
import jav.correctionBackend.Page;
import jav.correctionBackend.Token;
import jav.gui.dialogs.CustomErrorDialog;
import jav.gui.dialogs.EndOfPageDialog;
import jav.gui.dialogs.StartOfPageDialog;
import jav.gui.events.MessageCenter;
import jav.gui.events.documentChanged.DocumentChangedEvent;
import jav.gui.events.pageChanged.PageChangedEvent;
import jav.gui.events.special.multiselection.MultiSelectionEvent;
import jav.gui.events.special.multiselection.MultiSelectionEventType;
import jav.gui.events.tokenDeselection.TokenDeselectionEvent;
import jav.gui.events.tokenMultiDeselection.TokenMultiDeselectionEvent;
import jav.gui.events.tokenMultiSelection.TokenMultiSelectionEvent;
import jav.gui.events.tokenNavigation.TokenNavigationEvent;
import jav.gui.events.tokenNavigation.TokenNavigationEventSlot;
import jav.gui.events.tokenNavigation.TokenNavigationType;
import jav.gui.events.tokenSelection.TokenSelectionEvent;
import jav.gui.events.tokenSelection.TokenSelectionType;
import jav.gui.events.tokenStatus.*;
import jav.gui.layer.MouseDrawingUI;
import jav.gui.main.*;
import jav.gui.token.display.TokenVisualization;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingWorker;
import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.ext.LockableUI;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

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
 * MainView
 */
@ConvertAsProperties(dtd = "-//jav.gui.mainWindow//Main//EN",
        autostore = false)
public final class MainTopComponent extends AbstractEditorViewTopComponent implements TokenNavigationEventSlot {

    private static MainTopComponent instance;
    private static final String PREFERRED_ID = "MainTopComponent";
    private ArrayList<TokenVisualization> multiSelection = null;
    private ArrayList<Integer> multiToken = null;
    private boolean docOpened = false;
    private PageView pv = null;
    private boolean hasImage = false;
    private boolean showImages = true;
    private int currentPageIndex = -1;
    private int currentTokenID = -1;
    private Preferences node;
    private boolean isActive = true;
    private int fontSize;
    private double imgScale;
    private InstanceContent content = new InstanceContent();
    private GlobalActions globalActions;
    private TokenVisualizationRegistry tokenRegistry;
    private int horizontalcent;
    private int vertical;
    private int multivertical;
    private LockableUI lockableUI;
    private MouseDrawingUI mouseDrawingUI;
    
    public MainTopComponent() {

        MessageCenter.getInstance().addDocumentChangedEventListener(this);
        MessageCenter.getInstance().addTokenSelectionEventListener(this);
        MessageCenter.getInstance().addTokenDeselectionEventListener(this);
        MessageCenter.getInstance().addTokenStatusEventListener(this);
        MessageCenter.getInstance().addTokenNavigationEventListener(this);

        this.setFocusable(true);
        this.setDoubleBuffered(true);

        associateLookup(new AbstractLookup(content));
        globalActions = new MainWindowGlobalActions(this);
        node = NbPreferences.forModule(this.getClass());
        tokenRegistry = new TokenVisualizationRegistry();

        initComponents();

        MainController.findInstance().customizeScrollPane(jScrollPane1);
        setName(NbBundle.getMessage(MainTopComponent.class, "CTL_MainTopComponent"));
        setToolTipText(NbBundle.getMessage(MainTopComponent.class, "HINT_MainTopComponent"));
//        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 541, Short.MAX_VALUE));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE));
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files
     * only, i.e. deserialization routines; otherwise you could get a
     * non-deserialized instance. To obtain the singleton instance, use
     * {@link #findInstance}.
     */
    public static synchronized MainTopComponent getDefault() {
        if (instance == null) {
            instance = new MainTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the MainTopComponent instance. Never call {@link #getDefault}
     * directly!
     */
    public static synchronized MainTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(MainTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof MainTopComponent) {
            return (MainTopComponent) win;
        }
        Logger.getLogger(MainTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void componentOpened() {
//        MainController.findInstance().setLastFocusedTopComponent(this);
    }

    @Override
    public void componentClosed() {
        MessageCenter.getInstance().removeDocumentChangedEventListener(this);
        MessageCenter.getInstance().removeTokenSelectionEventListener(this);
        MessageCenter.getInstance().removeTokenDeselectionEventListener(this);
        MessageCenter.getInstance().removeTokenStatusEventListener(this);
        MessageCenter.getInstance().removeTokenNavigationEventListener(this);
    }

    @Override
    public void componentActivated() {
        if (this.isReady()) {
            this.isActive = true;
            if (MainController.findInstance().getLastFocusedTopComponent() != this) {
                MainController.findInstance().setLastFocusedTopComponent(this);
                if (this.currentTokenID != -1) {
                    this.selectToken((TokenVisualization) tokenRegistry.getTokenVisualization(this.currentTokenID), TokenSelectionType.NORMAL);
                    MessageCenter.getInstance().fireTokenSelectionEvent(new TokenSelectionEvent(this, this.currentTokenID, TokenSelectionType.NORMAL));
                    this.getTokenVisualizationRegistry().getTokenVisualization(this.currentTokenID).grabFocus();
                } else {
                    MessageCenter.getInstance().fireTokenDeselectionEvent(new TokenDeselectionEvent(this, this.currentTokenID));
                }
                if (this.multiToken != null) {
                    MessageCenter.getInstance().fireTokenMultiSelectionEvent(new TokenMultiSelectionEvent(this, this.multiToken));
                }
            }
        }
    }

    @Override
    public void componentDeactivated() {
        this.isActive = false;
//        MainController.findInstance().removeFromLookup(globalActions);
        if (this.currentTokenID != -1) {
            pv.getVisualizationMode().unSelect();
        } else if (this.multiToken != null) {
//            this.multiToken = null;
//            MessageCenter.getInstance().fireTokenMultiDeselectionEvent(new TokenMultiDeselectionEvent(this));
        }
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    @Override
    public void dispatchEvent(DocumentChangedEvent e) {

        fontSize = Integer.parseInt(MainController.findInstance().getDocumentProperties().getProperty("mainFontSize"));
        imgScale = Double.parseDouble(MainController.findInstance().getDocumentProperties().getProperty("mainImageScale"));

        if (this.currentTokenID != -1) {
            this.currentTokenID = -1;
        } else if (this.multiToken != null) {
            this.multiSelection = null;
            this.multiToken = null;
        }

        JFrame f = (JFrame) WindowManager.getDefault().getMainWindow();
        f.setTitle(MainController.findInstance().getCorrectionSystem().getDocument().getProjectFilename());
        final ProgressHandle p = ProgressHandleFactory.createHandle(java.util.ResourceBundle.getBundle("jav/gui/mainWindow/Bundle").getString("displaying"));
        this.vertical = 0;
        final Document doc = e.getDocument();

        Runnable r = new Runnable() {
            @Override
            public void run() {
                p.progress(java.util.ResourceBundle.getBundle("jav/gui/mainWindow/Bundle").getString("displaying"));
                Page page = doc.getPage(0);
                MyIterator<Token> tempit = doc.tokenIterator(page);
                currentPageIndex = 0;
                if (page.hasImage()) {
                    hasImage = true;
                    pv = new PageView(getDefault(), tempit, page.getImageCanonical(), fontSize, imgScale);
//                    if (!showImages) {
//                        pv.toggleImages(false);
//                    }
                } else {
                    hasImage = false;
                    pv = new PageView(getDefault(), tempit, fontSize);
                }

                mouseDrawingUI = new MouseDrawingUI(instance);
                JXLayer<JComponent> wrap = new JXLayer<JComponent>(pv, mouseDrawingUI);
                wrap.setDoubleBuffered(true);
                wrap.setLayerEventMask(AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);

                pv.setDoubleBuffered(true);
                jScrollPane1.setViewportView(wrap);
                jScrollPane1.getVerticalScrollBar().setUnitIncrement(17);
                jScrollPane1.getVerticalScrollBar().setBlockIncrement(180);

                docOpened = true;
                MainController.findInstance().addToLog("MainTopComponent # Document changed");
            }
        };
        try {
            if (docOpened) {
                docOpened = false;
                MainController.findInstance().removeFromLookup(globalActions);
                content.remove(this);
            }

            ProgressUtils.showProgressDialogAndRun(r, p, false);
            setName(java.util.ResourceBundle.getBundle("jav/gui/mainWindow/Bundle").getString("page") + " " + (currentPageIndex + 1) + java.util.ResourceBundle.getBundle("jav/gui/mainWindow/Bundle").getString("of") + " " + doc.getNumberOfPages());
            if (!this.isActive) {
                this.requestActive();
            }
            
            content.add(this);
            MainController.findInstance().setLastFocusedTopComponent(this);
            MainController.findInstance().addToLookup(globalActions);
            
//            MainController.findInstance().removeFromLookup(globalActions);
            MessageCenter.getInstance().firePageChangedEvent(new PageChangedEvent(instance, currentPageIndex));
        } catch (Exception | Error ex) {
//            IOProvider.getDefault().getIO("Fehler", false).getOut().println(ex.getLocalizedMessage());
        }
    }

    @Override
    public void zoomFont(int i) {
        content.remove(this);
        pv.zoomFont(i);
        this.fontSize = i;
        MainController.findInstance().getDocumentProperties().setProperty("mainFontSize", "" + this.fontSize);
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
    public boolean isReady() {
        return this.docOpened;
    }

    @Override
    public void zoomImg(double scale) {
        content.remove(this);
        pv.zoomImg(scale);
        this.imgScale = scale;
        MainController.findInstance().getDocumentProperties().setProperty("mainImageScale", "" + this.imgScale);
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
        return currentPageIndex + 1;
    }

    @Override
    public int getMaxPages() {
        return MainController.findInstance().getDocument().getNumberOfPages();
    }

    @Override
    public void gotoPage(final int p) {
        this.gotoPage(p, null);
    }

    public void gotoPage(final int p, PropertyChangeListener l) {

        SwingWorker<Boolean, Object> worker = new SwingWorker<Boolean, Object>() {
            @Override
            protected Boolean doInBackground() {

                boolean retval = false;
                try {
                    vertical = -1;
                    currentTokenID = -1;
                    Page page = MainController.findInstance().getPage(p);
                    long time = System.currentTimeMillis();
                    if (page.hasImage()) {
                        pv = new PageView(getDefault(), MainController.findInstance().getDocument().tokenIterator(page), page.getImageCanonical(), fontSize, imgScale);
                    } else {
                        pv = new PageView(getDefault(), MainController.findInstance().getDocument().tokenIterator(page), fontSize);
                    }
//                    System.out.println("time used to build pv: " + (System.currentTimeMillis() - time));

//                    lockableUI = new LockableUI();
                    mouseDrawingUI = new MouseDrawingUI(instance);

                    JXLayer<JComponent> wrap = new JXLayer<JComponent>(pv, mouseDrawingUI);
                    wrap.setLayerEventMask(AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
                    wrap.setDoubleBuffered(true);
//                    wrap = new JXLayer(wrap, lockableUI);
//                    wrap.setLayerEventMask(AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);

                    pv.setDoubleBuffered(true);
                    jScrollPane1.setViewportView(wrap);

                    currentPageIndex = p;
                    retval = true;

                } catch (Exception e) {
                    e.printStackTrace();
                    retval = false;
                }
                return retval;
            }

            @Override
            protected void done() {
                try {
                    boolean retval = get();
                    if (retval) {
                        int display = currentPageIndex + 1;
                        setName(java.util.ResourceBundle.getBundle("jav/gui/mainWindow/Bundle").getString("page") + " " + display + java.util.ResourceBundle.getBundle("jav/gui/mainWindow/Bundle").getString("of") + " " + MainController.findInstance().getDocument().getNumberOfPages());
                        instance.requestFocusInWindow(true);
                        MainController.findInstance().addToLookup(globalActions);
                        content.add(instance);
//                        lockableUI.setLocked(false);
                        MessageCenter.getInstance().firePageChangedEvent(new PageChangedEvent(instance, currentPageIndex));
                    } else {
                        new CustomErrorDialog().showDialog("MainTopComponent::GotoPageSelect");
                    }
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (CancellationException ex) {
                }
            }
        };
        MainController.findInstance().removeFromLookup(globalActions);
        content.remove(instance);
        if (l != null) {
            worker.addPropertyChangeListener(l);
        }
//        lockableUI.setLocked(true);
        worker.execute();
    }

    @Override
    public void dispatchEvent(TokenSelectionEvent e) {
//        IOProvider.getDefault().getIO("Nachrichten", false).getOut().println(System.currentTimeMillis() + "select " + e.getTokenIndex());
        if (this.isActive) {
            this.currentTokenID = e.getTokenID();
            this.setVertical(pv.getVisualizationMode().getSelectedTokenVisualization().getY());

            if (e.getSelectionType().equals(TokenSelectionType.NORMAL)) {
                this.setHorizontalCent(pv.getVisualizationMode().getSelectedTokenVisualization().getX() + (pv.getVisualizationMode().getSelectedTokenVisualization().getWidth() / 2));
            }
        }
    }

    @Override
    public void dispatchEvent(TokenDeselectionEvent e) {
        if (this.isActive && e.getTokenID() == this.currentTokenID) {
            this.currentTokenID = -1;
        }
    }

    public void toggleImages(boolean b) {
        if (b) {
            content.remove(instance);
            this.showImages = true;
            pv.toggleImages(b);
            content.add(instance);
        } else {
            content.remove(instance);
            this.showImages = false;
            pv.toggleImages(b);
            content.add(instance);
        }
        this.requestActive();
    }

    /*
     * TODO if documents without images are introduced change this
     */
//    @Override
//    public boolean hasImage() {
//        return this.hasImages;
//    }
    public void setShowImages(boolean b) {
        this.showImages = b;
    }

    public boolean getShowImages() {
        return this.hasImage;
    }

    @Override
    public void dispatchEvent(TokenStatusEvent e) {

        TokenVisualization tv = (TokenVisualization) this.getTokenVisualizationRegistry().getTokenVisualization(e.getPOIID());
        if (tv != null) {
            if (e.getType().equals(TokenStatusType.SETCORRECTED)) {
                tv.setCorrected(((SetCorrectedEvent) e).getSetTo());
            } else if (e.getType().equals(TokenStatusType.CORRECTED)) {
                CorrectedEvent cor = (CorrectedEvent) e;
                tv.update(cor.getNewText(), cor.getSetTo());
            } else if (e.getType().equals(TokenStatusType.MERGED_RIGHT)) {
                pv.update(e.getType(), e.getPOIID(), ((MergeEvent) e).getAffectedTokenIds());

                if (this.multiToken != null) {
                    if (this.multiToken.size() > 1) {
                        this.multiSelection = null;
                        this.multiToken = null;
                        MessageCenter.getInstance().fireTokenMultiDeselectionEvent(new TokenMultiDeselectionEvent(this));
                        this.requestActive();

                        if (tv != null) {
                            tv.setMultiSelected(false);
                        }
                    }
                }
            } else if (e.getType().equals(TokenStatusType.SPLIT)) {

                pv.update(e.getType(), e.getPOIID(), ((SplitEvent) e).getAffectedTokenIds());

                if (this.multiToken != null) {
                    if (this.multiToken.size() > 1) {
                        this.multiSelection = null;
                        this.multiToken = null;
                        MessageCenter.getInstance().fireTokenMultiDeselectionEvent(new TokenMultiDeselectionEvent(this));
                        this.requestActive();

                        if (tv != null) {
                            tv.setMultiSelected(false);
                        }
                    }
                }

            } else if (e.getType().equals(TokenStatusType.DELETE)) {

                DeleteEvent de = (DeleteEvent) e;
                // line ends have specific markers in the layout that have to be replaced if last token of line is deleted
                if (pv.getLayoutConstraints().containsKey(tv)) {
                    TokenVisualization newtv = (TokenVisualization) this.tokenRegistry.getTokenVisualization(de.getAffectedTokenIds().get(de.getAffectedTokenIds().size() - 1));
                    if (pv.getLayoutConstraints().containsKey(tv)) {
                        pv.getLayoutConstraints().remove(tv);
                        pv.getLayoutConstraints().put(newtv, "br");
                    }
                }

                pv.update(TokenStatusType.DELETE, de.getPOIID(), de.getAffectedTokenIds());
                pv.revalidate();
                pv.repaint();

//            if (this.currentTokenID == MainController.findInstance().getPage(this.currentPageIndex).getStartIndex() || (this.currentTokenID - ((DeleteEvent) e).getAffectedTokenIds().size()) < MainController.findInstance().getPage(this.currentPageIndex).getStartIndex()) {
//                this.currentTokenID = 0;
//            } else {
////                this.currentTokenIndex -= ((DeleteEvent) e).getNumberOfTokensAffected();
//            }

                this.goToNextNormalToken();

                if (this.multiToken != null) {
                    if (this.multiToken.size() > 1) {
                        this.multiSelection = null;
                        this.multiToken = null;
                        MessageCenter.getInstance().fireTokenMultiDeselectionEvent(new TokenMultiDeselectionEvent(this));
                        this.requestActive();
                    }
                }

            } else if (e.getType().equals(TokenStatusType.INSERT)) {

                pv.update(TokenStatusType.INSERT, e.getPOIID(), ((InsertEvent) e).getAffectedTokenIds());
                pv.validate();
//            pv.revalidate();
                pv.repaint();

//            this.currentTokenIndex -= ((InsertEvent) e).getNumberOfTokensAffected();
//            this.goToNextNormalToken();

                if (this.multiToken != null) {
                    if (this.multiToken.size() > 1) {
                        this.multiSelection = null;
                        this.multiToken = null;
                        MessageCenter.getInstance().fireTokenMultiDeselectionEvent(new TokenMultiDeselectionEvent(this));
                        this.requestActive();
                    }
                }
                // TODO throw undefined statustype exception
            } else {
            }
        }
    }

    public void goToNextToken() {
        /*
         * selects the prev token
         *
         */
        // token selected
        if (!this.isActive) {
            this.requestActive();
        }
        if (this.currentTokenID == -1) {
            Token next = MainController.findInstance().getDocument().getNextTokenByIndex(MainController.findInstance().getDocument().getPage(this.currentPageIndex).getStartIndex());
            if (next != null) {
                TokenVisualization tv = (TokenVisualization) tokenRegistry.getTokenVisualization(next.getID());
                if (tv != null) {
                    this.selectToken(tv, TokenSelectionType.NORMAL);
                }
            }
        } else {
            Token next = MainController.findInstance().getDocument().getNextToken(this.currentTokenID);
            if (next != null) {
                if (next.getPageIndex() == this.currentPageIndex) {
                    pv.getVisualizationMode().unSelect();
                    TokenVisualization toselect = (TokenVisualization) this.tokenRegistry.getTokenVisualization(next.getID());
                    this.selectToken(toselect, TokenSelectionType.NORMAL);
                } else {
                    EndOfPageDialog d = new EndOfPageDialog();
                    Object retval = d.showDialog();
                    if (retval.equals(NotifyDescriptor.OK_OPTION)) {
                        final Token tok = next;
                        this.gotoPage(next.getPageIndex(), new PropertyChangeListener() {
                            @Override
                            public void propertyChange(PropertyChangeEvent pce) {
                                if (pce.getNewValue().equals(SwingWorker.StateValue.DONE)) {
                                    TokenVisualization toselect = (TokenVisualization) tokenRegistry.getTokenVisualization(tok.getID());
                                    selectToken(toselect, TokenSelectionType.NORMAL);
                                }
                            }
                        });
                    }

                }

            }
        }
//        if (this.currentTokenID < MainController.findInstance().getDocument().getNumberOfTokens() - 1) {
//            if (this.currentTokenID != -1 || this.multiToken != null) {
//
//                int indextotest = this.currentTokenID;
//
//                if (indextotest + 1 < MainController.findInstance().getDocument().getNumberOfTokens()) {
//                    Token t = MainController.findInstance().getDocument().getTokenByID(indextotest + 1);
//                    if (t.getPageIndex() == this.currentPageIndex) {
//                        pv.getVisualizationMode().unSelect();
//                        TokenVisualization toselect = (TokenVisualization) this.tokenRegistry.getTokenVisualization(t);
//                        this.selectToken(toselect, TokenSelectionType.NORMAL);
//                    } else {
//                        EndOfPageDialog d = new EndOfPageDialog();
//                        Object retval = d.showDialog();
//                        if (retval.equals(NotifyDescriptor.OK_OPTION)) {
//                            final Token tok = t;
//                            this.gotoPage(t.getPageIndex(), new PropertyChangeListener() {
//                                @Override
//                                public void propertyChange(PropertyChangeEvent pce) {
//                                    if (pce.getNewValue().equals(SwingWorker.StateValue.DONE)) {
//                                        TokenVisualization toselect = (TokenVisualization) tokenRegistry.getTokenVisualization(tok);
//                                        selectToken(toselect, TokenSelectionType.NORMAL);
//                                    }
//                                }
//                            });
//                        }
//
//                    }
//                }
//                // no token selected
//            } else {
//                TokenVisualization tv = (TokenVisualization) tokenRegistry.getTokenVisualization(MainController.findInstance().getPage(this.currentPageIndex).getStartIndex());
//                this.selectToken(tv, TokenSelectionType.NORMAL);
//            }
//        }
    }

    public void goToNextNormalToken() {
        /*
         * selects the prev "normal" token (i.e. textual content, no space or
         * newline)
         *
         */
        // token selected
        if (!this.isActive) {
            this.requestActive();
        }
        if (this.currentTokenID == -1) {
            Token next = MainController.findInstance().getDocument().getNextNormalTokenByIndex(MainController.findInstance().getDocument().getPage(this.currentPageIndex).getStartIndex());
            if (next != null) {
                TokenVisualization tv = (TokenVisualization) tokenRegistry.getTokenVisualization(next.getID());
                if (tv != null) {
                    this.selectToken(tv, TokenSelectionType.NORMAL);
                }
            }
        } else {
            Token next = MainController.findInstance().getDocument().getNextToken(this.currentTokenID);
            if (next != null) {
                if (next.getPageIndex() == this.currentPageIndex) {
                    pv.getVisualizationMode().unSelect();
                    TokenVisualization toselect = (TokenVisualization) this.tokenRegistry.getTokenVisualization(next.getID());
                    this.selectToken(toselect, TokenSelectionType.NORMAL);
                } else {
                    EndOfPageDialog d = new EndOfPageDialog();
                    Object retval = d.showDialog();
                    if (retval.equals(NotifyDescriptor.OK_OPTION)) {
                        final Token tok = next;
                        this.gotoPage(next.getPageIndex(), new PropertyChangeListener() {
                            @Override
                            public void propertyChange(PropertyChangeEvent pce) {
                                if (pce.getNewValue().equals(SwingWorker.StateValue.DONE)) {
                                    TokenVisualization toselect = (TokenVisualization) tokenRegistry.getTokenVisualization(tok.getID());
                                    selectToken(toselect, TokenSelectionType.NORMAL);
                                }
                            }
                        });
                    }
                }
            }
        }
    }
    
    public void goToPreviousToken() {
        /*
         * selects the prev token
         *
         */
        // token selected
        if (!this.isActive) {
            this.requestActive();
        }
        if (this.currentTokenID == -1) {
            Token prev = MainController.findInstance().getDocument().getPreviousTokenByIndex(MainController.findInstance().getDocument().getPage(this.currentPageIndex).getStartIndex());
            if (prev != null) {
                TokenVisualization tv = (TokenVisualization) tokenRegistry.getTokenVisualization(prev.getID());
                if (tv != null) {
                    this.selectToken(tv, TokenSelectionType.NORMAL);
                }
            }
        } else {
            Token prev = MainController.findInstance().getDocument().getPreviousToken(this.currentTokenID);
            if (prev != null) {
                if (prev.getPageIndex() == this.currentPageIndex) {
                    pv.getVisualizationMode().unSelect();
                    TokenVisualization toselect = (TokenVisualization) this.tokenRegistry.getTokenVisualization(prev.getID());
                    this.selectToken(toselect, TokenSelectionType.NORMAL);
                } else {
                    StartOfPageDialog d = new StartOfPageDialog();
                    Object retval = d.showDialog();
                    if (retval.equals(NotifyDescriptor.OK_OPTION)) {
                        final Token tok = prev;
                        this.gotoPage(prev.getPageIndex(), new PropertyChangeListener() {
                            @Override
                            public void propertyChange(PropertyChangeEvent pce) {
                                if (pce.getNewValue().equals(SwingWorker.StateValue.DONE)) {
                                    TokenVisualization toselect = (TokenVisualization) tokenRegistry.getTokenVisualization(tok.getID());
                                    selectToken(toselect, TokenSelectionType.NORMAL);
                                }
                            }
                        });
                    }

                }

            }
        }
    }    

    public void goToPreviousNormalToken() {
        /*
         * selects the previous "normal" token (i.e. textual content, no space
         * or newline)
         *
         */
        // token selected
//        IOProvider.getDefault().getIO("Nachrichten", false).getOut().println(System.currentTimeMillis() + "prevtok");
        if (!this.isActive) {
            this.requestActive();
        }
        if (this.currentTokenID == -1) {
            Token prev = MainController.findInstance().getDocument().getPreviousNormalTokenByIndex(MainController.findInstance().getDocument().getPage(this.currentPageIndex).getStartIndex());
            if (prev != null) {
                TokenVisualization tv = (TokenVisualization) tokenRegistry.getTokenVisualization(prev.getID());
                if (tv != null) {
                    this.selectToken(tv, TokenSelectionType.NORMAL);
                }
            }
        } else {
            Token prev = MainController.findInstance().getDocument().getPreviousNormalToken(this.currentTokenID);
            if (prev != null) {
                if (prev.getPageIndex() == this.currentPageIndex) {
                    pv.getVisualizationMode().unSelect();
                    TokenVisualization toselect = (TokenVisualization) this.tokenRegistry.getTokenVisualization(prev.getID());
                    this.selectToken(toselect, TokenSelectionType.NORMAL);
                } else {
                    StartOfPageDialog d = new StartOfPageDialog();
                    Object retval = d.showDialog();
                    if (retval.equals(NotifyDescriptor.OK_OPTION)) {
                        final Token tok = prev;
                        this.gotoPage(prev.getPageIndex(), new PropertyChangeListener() {
                            @Override
                            public void propertyChange(PropertyChangeEvent pce) {
                                if (pce.getNewValue().equals(SwingWorker.StateValue.DONE)) {
                                    TokenVisualization toselect = (TokenVisualization) tokenRegistry.getTokenVisualization(tok.getID());
                                    selectToken(toselect, TokenSelectionType.NORMAL);
                                }
                            }
                        });
                    }

                }

            }
        }
    }

    /*
     * selects nearest token near horizontal position of current token in prev
     * line @returns 1 if successful, 0 else
     */
    private int selectNextLineToken() {
        int retval = 0;
        if (this.horizontalcent != 0) {
            SortableValueMap<TokenVisualization, Integer> lager = new SortableValueMap<>();
            int neuegrenze = 0;

            Token toTest;
            if (this.currentTokenID == -1) {
                toTest = MainController.findInstance().getDocument().getTokenByIndex(MainController.findInstance().getPage(this.currentPageIndex).getStartIndex());
            } else {
                toTest = MainController.findInstance().getDocument().getTokenByID(this.currentTokenID);
            }

            while( toTest.getPageIndex() == this.currentPageIndex) {
                            
                TokenVisualization testtv = (TokenVisualization) tokenRegistry.getTokenVisualization(toTest.getID());
                if (!testtv.isNewline() & !testtv.isSpace()) {

                    // new line
                    if (testtv.getY() > this.vertical && neuegrenze == 0) {
                        neuegrenze = testtv.getY();
                    }

                    // while in new line
                    if (testtv.getY() == neuegrenze) {
                        int hcz = testtv.getX() + (testtv.getWidth() / 2);
                        if (hcz > this.horizontalcent) {
                            lager.put(testtv, hcz - this.horizontalcent);
                        } else {
                            lager.put(testtv, this.horizontalcent - hcz);
                        }
                    }

                    // new line ended
                    if (testtv.getY() > neuegrenze && neuegrenze != 0) {
                        lager.sortByValue();
                        pv.getVisualizationMode().unSelect();
                        this.selectToken(lager.keySet().iterator().next(), TokenSelectionType.VERTICAL);
                        return 1;
                    }
                }
                toTest = MainController.findInstance().getDocument().getNextToken(toTest.getID());
            }

            // token in last line has to be selected
            if (lager.keySet().size() > 0) {
                lager.sortByValue();
                pv.getVisualizationMode().unSelect();
                this.selectToken(lager.keySet().iterator().next(), TokenSelectionType.VERTICAL);
                return 1;
            }
        } else {
            TokenVisualization toSelect = (TokenVisualization) tokenRegistry.getTokenVisualization(MainController.findInstance().getDocument().getTokenByIndex(MainController.findInstance().getPage(this.currentPageIndex).getStartIndex()));
            selectToken(toSelect, TokenSelectionType.NORMAL);
            return 1;
        }
        return retval;
    }

    /*
     * selects nearest token near horizontal position of current token in
     * previous line @returns 1 if successful, 0 else
     */
    private int selectPrevLineToken() {
        int retval = 0;
        if (this.horizontalcent != 0) {
            SortableValueMap<TokenVisualization, Integer> lager = new SortableValueMap<TokenVisualization, Integer>();
            int neuegrenze = 0;

            Token toTest;
            if (this.currentTokenID == -1) {
                toTest = MainController.findInstance().getDocument().getPreviousTokenByIndex(MainController.findInstance().getPage(this.currentPageIndex).getEndIndex());
            } else {
                toTest = MainController.findInstance().getDocument().getPreviousToken(this.currentTokenID);
            }

            if (this.vertical == 0) {
                this.vertical = tokenRegistry.getTokenVisualization(MainController.findInstance().getPage(this.currentPageIndex).getEndIndex() - 1).getY() + 1000;
            }

            while( toTest.getPageIndex() == this.currentPageIndex ) {

                TokenVisualization testtv = (TokenVisualization) tokenRegistry.getTokenVisualization(toTest.getID());
                if (!testtv.isNewline() & !testtv.isSpace()) {

                    // new line
                    if (testtv.getY() < this.vertical && neuegrenze == 0) {
                        neuegrenze = testtv.getY();
                    }

                    // while in new line
                    if (testtv.getY() == neuegrenze) {
                        int hcz = testtv.getX() + (testtv.getWidth() / 2);
                        if (hcz > this.horizontalcent) {
                            lager.put(testtv, hcz - this.horizontalcent);
                        } else {
                            lager.put(testtv, this.horizontalcent - hcz);
                        }
                    }

                    // new line ended
                    if (testtv.getY() < neuegrenze && neuegrenze != 0) {
                        lager.sortByValue();
                        pv.getVisualizationMode().unSelect();
                        this.selectToken(lager.keySet().iterator().next(), TokenSelectionType.VERTICAL);
                        return 1;
                    }
                }
                toTest = MainController.findInstance().getDocument().getPreviousToken(toTest.getID());
            }

            // token in last line has to be selected
            if (lager.keySet().size() > 0) {
                lager.sortByValue();
                pv.getVisualizationMode().unSelect();
                this.selectToken(lager.keySet().iterator().next(), TokenSelectionType.VERTICAL);
                return 1;
            }
        }
        return retval;
    }

    public void goToNextLine() {
        if (!this.isActive) {
            this.requestActive();
        }

        // fail
        if (this.selectNextLineToken() == 0) {
            // not last page of document
            if (this.currentPageIndex < MainController.findInstance().getDocument().getNumberOfPages() - 1) {
                EndOfPageDialog d = new EndOfPageDialog();
                Object retval = d.showDialog();
                if (retval.equals(NotifyDescriptor.OK_OPTION)) {
                    this.gotoPage(this.currentPageIndex + 1, new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent pce) {
                            if (pce.getNewValue().equals(SwingWorker.StateValue.DONE)) {
                                selectNextLineToken();
                            }
                        }
                    });
                }
            }
        }
    }

    public void goToPreviousLine() {
        if (!this.isActive) {
            this.requestActive();
        }

        // fail
        if (this.selectPrevLineToken() == 0 && this.horizontalcent != 0) {
            // not last page of document
            if (this.currentPageIndex > 0) {
                StartOfPageDialog d = new StartOfPageDialog();
                Object retval = d.showDialog();
                if (retval.equals(NotifyDescriptor.OK_OPTION)) {
                    this.gotoPage(this.currentPageIndex - 1, new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent pce) {
                            if (pce.getNewValue().equals(SwingWorker.StateValue.DONE)) {
                                selectPrevLineToken();
                            }
                        }
                    });
                }
            }
        }
    }

    public void goToNextSuspiciousToken() {
        /*
         * selects the prev suspicious token (= token with potential error)
         *
         */
        // token selected
        if (!this.isActive) {
            this.requestActive();
        }

        int indextotest;
        boolean end = false;
        if (this.currentTokenID < MainController.findInstance().getDocument().getNumberOfTokens() - 1) {
            if (currentTokenID != -1 || this.multiToken != null) {
//            MessageCenter.getInstance().fireTokenDeselectionEvent(new TokenDeselectionEvent(actualtv, actualtv.getToken().getIndexInDocument()));
                indextotest = this.currentTokenID;
            } else {
                indextotest = MainController.findInstance().getPage(this.currentPageIndex).getStartIndex();
            }


            int count = 1;

            while (!end && indextotest < MainController.findInstance().getDocument().getNumberOfTokens()) {
                indextotest = this.currentTokenID + count;
                Token t = MainController.findInstance().getDocument().getTokenByID(indextotest);
                if (t.isSuspicious()) {
                    end = true;
                    if (t.getPageIndex() == this.currentPageIndex) {
                        TokenVisualization toselect = (TokenVisualization) this.tokenRegistry.getTokenVisualization(t);
                        pv.getVisualizationMode().unSelect();
                        this.selectToken(toselect, TokenSelectionType.NORMAL);
                    } else {
                        EndOfPageDialog d = new EndOfPageDialog();
                        Object retval = d.showDialog();
                        if (retval.equals(NotifyDescriptor.OK_OPTION)) {
                            final Token tok = t;
                            this.gotoPage(t.getPageIndex(), new PropertyChangeListener() {
                                @Override
                                public void propertyChange(PropertyChangeEvent pce) {
                                    if (pce.getNewValue().equals(SwingWorker.StateValue.DONE)) {
                                        TokenVisualization toselect = (TokenVisualization) tokenRegistry.getTokenVisualization(tok);
                                        selectToken(toselect, TokenSelectionType.NORMAL);
                                    }
                                }
                            });
                        }
                    }
                } else {
                    count++;
                }
            }
        }
        // no token selected
    }

    public void goToPreviousSuspiciousToken() {
        /*
         * selects the previous suspicious token (= token with potential error)
         *
         */
        // token selected
        if (!this.isActive) {
            this.requestActive();
        }
        if (this.currentTokenID > 0) {
            if (currentTokenID != -1 || this.multiToken != null) {

//            MessageCenter.getInstance().fireTokenDeselectionEvent(new TokenDeselectionEvent(actualtv, actualtv.getToken().getIndexInDocument()));
                boolean end = false;
                int indextotest = this.currentTokenID;
                int count = 1;

                while (!end && indextotest > 0) {
                    indextotest = this.currentTokenID - count;
                    Token t = MainController.findInstance().getDocument().getTokenByID(indextotest);
                    if (t.isSuspicious()) {
                        end = true;
                        if (t.getPageIndex() == this.currentPageIndex) {
                            TokenVisualization toselect = (TokenVisualization) this.tokenRegistry.getTokenVisualization(t);
                            pv.getVisualizationMode().unSelect();
                            this.selectToken(toselect, TokenSelectionType.NORMAL);
                        } else {
                            StartOfPageDialog d = new StartOfPageDialog();
                            Object retval = d.showDialog();
                            if (retval.equals(NotifyDescriptor.OK_OPTION)) {
                                final Token tok = t;
                                this.gotoPage(t.getPageIndex(), new PropertyChangeListener() {
                                    @Override
                                    public void propertyChange(PropertyChangeEvent pce) {
                                        if (pce.getNewValue().equals(SwingWorker.StateValue.DONE)) {
                                            TokenVisualization toselect = (TokenVisualization) tokenRegistry.getTokenVisualization(tok);
                                            selectToken(toselect, TokenSelectionType.NORMAL);
                                        }
                                    }
                                });
                            }
                        }
                    } else {
                        count++;
                    }
                }
            }
        }
    }

    public void selectToken(TokenVisualization tvToSelect, TokenSelectionType tst) {
        tvToSelect.setSelected(true);
        tvToSelect.grabFocus();
        Rectangle rect = tvToSelect.getBounds();
//        rect.y = tvToSelect.getParent().getY();

        if (!jScrollPane1.getViewport().getViewRect().contains(rect)) {

            Rectangle visible = jScrollPane1.getViewport().getVisibleRect();
            int p_left = rect.x - visible.width / 3;
            if (p_left < 0) {
                p_left = 0;
            }
            int p_top = rect.y - visible.height / 3;
            if (p_top < 0) {
                p_top = 0;
            }
            Point p = new Point(p_left, p_top);
            jScrollPane1.getViewport().setViewPosition(p);
        }
        this.currentTokenID = tvToSelect.getTokenID();
        tvToSelect.grabFocus();
        pv.getVisualizationMode().setSelectedTokenVisualization(tvToSelect);
        MessageCenter.getInstance().fireTokenSelectionEvent(new TokenSelectionEvent(tvToSelect, tvToSelect.getTokenID(), tst));
    }

    public int getActualTokenID() {
        return this.currentTokenID;
    }

    // returns index of last token that is an actual token (no space or newline)
    public int getNumLastToken() {
        for (int i = MainController.findInstance().getDocument().getNumberOfTokens() - 1; i > 0; i--) {
            Token t = MainController.findInstance().getDocument().getTokenByID(i);
            if (t.isNormal()) {
                return i;
            }
        }
        return 0;
    }

    public int getNumLastSuspToken() {
        Document d = MainController.findInstance().getDocument();
        for (int i = d.getNumberOfTokens() - 1; i > 0; i--) {
            Token t = d.getTokenByIndex(i);
            if (t.isNormal() && !t.isSuspicious()) {
                return i;
            }
        }
        return 0;
    }

    public int getNumFirstSuspToken() {

        Document d = MainController.findInstance().getDocument();

        for (int i = 0; i < d.getNumberOfTokens() - 1; i++) {
            Token t = d.getTokenByIndex(i);
            if (t.isNormal() && !t.isSuspicious()) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public GlobalActions getGlobalActions() {
        return this.globalActions;
    }

    @Override
    public boolean showImage() {
        return this.showImages;
    }

    @Override
    public void dispatchEvent(TokenNavigationEvent e) {
        if (e.getType().equals(TokenNavigationType.FOCUS_IN_MAIN)) {
            if (!this.isActive) {
                this.requestActive();
            }

            final Token tok = MainController.findInstance().getDocument().getTokenByID(e.getTokenID());
            if (tok.getPageIndex() == this.currentPageIndex) {
                if (this.currentTokenID != -1) {
                    pv.getVisualizationMode().unSelect();
                }
                TokenVisualization tv = (TokenVisualization) tokenRegistry.getTokenVisualization(tok);
                selectToken(tv, TokenSelectionType.NORMAL);
            } else {
                this.gotoPage(tok.getPageIndex(), new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent pce) {
                        if (pce.getNewValue().equals(SwingWorker.StateValue.DONE)) {
                            TokenVisualization tv = (TokenVisualization) tokenRegistry.getTokenVisualization(tok);
                            selectToken(tv, TokenSelectionType.NORMAL);
                        }
                    }
                });
            }
        }
    }

    public TokenVisualizationRegistry getTokenVisualizationRegistry() {
        return tokenRegistry;
    }

    public void setVertical(int h) {
        this.vertical = h;
    }

    public void setHorizontalCent(int v) {
        this.horizontalcent = v;
    }

    @Override
    public void dispatchMultiSelectionEvent(MultiSelectionEvent e) {
        if (e.getType() == MultiSelectionEventType.START) {
            if (this.multiSelection != null) {
                for (TokenVisualization tv : this.multiSelection) {
                    tv.setMultiSelected(false);
                }
                MessageCenter.getInstance().fireTokenMultiDeselectionEvent(new TokenMultiDeselectionEvent(this));
            }
            this.multiSelection = new ArrayList<>();
            this.multiToken = new ArrayList<>();
            this.multivertical = 0;
        } else if (e.getType() == MultiSelectionEventType.END) {
            if (this.currentTokenID != -1) {
                pv.getVisualizationMode().unSelect();
                MessageCenter.getInstance().fireTokenDeselectionEvent(new TokenDeselectionEvent(this, currentTokenID));
            }

            this.updateMultiSelection(e.getSelectionBounds());

            for (Component c : pv.getComponents()) {
                if (c.getY() > e.getSelectionBounds().getMaxY()) {
                    break;
                }
                if (c instanceof TokenVisualization) {
                    TokenVisualization tv = (TokenVisualization) c;
                    if (tv.isMultiSelected()) {
                        this.multiSelection.add(tv);
                    }
                }
            }
            if (this.multiSelection.size() > 1) {
                for (int i = 0; i < multiSelection.size(); i++) {
                    TokenVisualization temp = multiSelection.get(i);
//                    if (temp.isSpace() && (i == 0)) { // || i == multiSelection.size() - 1)) {
//                    } else {
//                    IOProvider.getDefault().getIO("Nachrichten", false).getOut().println("Multi added " + temp.getTokenTextLabelText());
                    multiToken.add(temp.getTokenID());
//                    }
                }
//                if( MainController.findInstance().getDocument().getToken(multiToken.get(0).getIndexInDocument()-1).getWDisplay().equals(" ") && MainController.findInstance().getDocument().getToken(multiToken.get(multiToken.size()-1).getIndexInDocument()+1).getWDisplay().equals(" ")) {
//                    multiToken.add(MainController.findInstance().getDocument().getToken(multiToken.get(multiToken.size()-1).getIndexInDocument()+1));
//                }
                this.currentTokenID = multiToken.get(multiToken.size() - 1);
//                IOProvider.getDefault().getIO("Nachrichten", false).getOut().println("Anzahl der selektierten Token: " + multiToken.size() + ((TokenVisualization) tokenRegistry.getTokenVisualization(multiToken.get(multiToken.size() - 1))).getTokenTextLabelText());
                MessageCenter.getInstance().fireTokenMultiSelectionEvent(new TokenMultiSelectionEvent(this, multiToken));
            }
        } else if (e.getType() == MultiSelectionEventType.SMALLER) {
            for (Component c : pv.getComponents()) {
                if (c.getY() > e.getSelectionBounds().getMaxY()) {
                    break;
                }
                if (c instanceof TokenVisualization) {
                    TokenVisualization tv = (TokenVisualization) c;
                    if (tv.isMultiSelected()) {
                        if (!e.getSelectionBounds().contains(tv.getCentroid())) {
                            tv.setMultiSelected(false);
                        }
                    }
                }
            }
        } else if (e.getType() == MultiSelectionEventType.LARGER) {
            this.updateMultiSelection(e.getSelectionBounds());
        }
    }

    public void updateMultiSelection(Rectangle bounds) {
        for (Component c : pv.getComponents()) {
            if (c.getY() > bounds.getMaxY()) {
                break;
            }
            if (c instanceof TokenVisualization) {
                TokenVisualization tv = (TokenVisualization) c;
                if (!tv.isNewline() & !tv.isSpace()) {
                    if (multivertical == 0) {
                        if (bounds.contains(tv.getCentroid()) && !this.multiSelection.contains(tv)) {
                            this.multivertical = tv.getY();
                            tv.setMultiSelected(true);
                        }
                    } else {
                        if (bounds.contains(tv.getCentroid()) && !this.multiSelection.contains(tv) && tv.getY() == this.multivertical) {
                            tv.setMultiSelected(true);
                        }
                    }
                } else if (tv.isSpace()) {
                    if (bounds.contains(tv.getBounds())) {
                        tv.setMultiSelected(true);
                    }
                }
            }
        }
    }

    @Override
    public boolean isEditing() {
        if (this.currentTokenID != -1) {
            return false;
//            return ((TokenVisualization) this.tokenRegistry.getTokenVisualization(currentTokenIndex)).isEditing();
        } else {
            return false;
        }
    }

    @Override
    public boolean hasImage() {
        return this.hasImage;
    }
}
