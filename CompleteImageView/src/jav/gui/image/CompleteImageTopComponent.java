package jav.gui.image;

import jav.correctionBackend.Page;
import jav.correctionBackend.Token;
import jav.gui.cookies.ImageZoomCookie;
import jav.gui.events.MessageCenter;
import jav.gui.events.documentChanged.DocumentChangedEvent;
import jav.gui.events.documentChanged.DocumentChangedEventSlot;
import jav.gui.events.pageChanged.PageChangedEvent;
import jav.gui.events.pageChanged.PageChangedEventSlot;
import jav.gui.events.tokenDeselection.TokenDeselectionEvent;
import jav.gui.events.tokenDeselection.TokenDeselectionEventSlot;
import jav.gui.events.tokenSelection.TokenSelectionEvent;
import jav.gui.events.tokenSelection.TokenSelectionEventSlot;
import jav.gui.main.AbstractMyTopComponent;
import jav.gui.main.MainController;
import java.awt.EventQueue;
import java.awt.RenderingHints;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;
import jpl.mipl.jade.JadeDisplay;
import jpl.mipl.jade.MouseScroller;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

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
 * topcomponent that uses the jadedisplay to show an image
 *
 */
@ConvertAsProperties(dtd = "-//jav.gui.image//CompleteImage//EN",
autostore = false)
public final class CompleteImageTopComponent extends AbstractMyTopComponent implements TokenSelectionEventSlot, TokenDeselectionEventSlot, DocumentChangedEventSlot, PageChangedEventSlot, ImageZoomCookie {

    private int currentTokenIndex;
    private boolean hasImage = false;
    private Preferences node;
    private JScrollPane sp = null;
    private double scale;
    private String actualPageString;
    private boolean imageOpened = false;
    private JadeDisplay _jadeDisplay = null;
    private ParameterBlockJAI _loadPB, _zoomPB;
    private RenderedOp _loadImage, _zoomImage, _finalImage;
    private TokenBoxPainter _tokenAnMaler = null;
    private InstanceContent content = new InstanceContent();
    private static CompleteImageTopComponent instance;
    private SwingWorker worker = null;
    /**
     * path to the icon used by the component and its open action
     */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "CompleteImageTopComponent";

    public CompleteImageTopComponent() {

        associateLookup(new AbstractLookup(content));

        node = NbPreferences.forModule(this.getClass());
        init();
        sp = new JScrollPane();
        this.setDoubleBuffered(true);
        sp.setDoubleBuffered(false);
        this.add(sp);

        setName(NbBundle.getMessage(CompleteImageTopComponent.class, "CTL_CompleteImageTopComponent"));
        setToolTipText(NbBundle.getMessage(CompleteImageTopComponent.class, "HINT_CompleteImageTopComponent"));
//        setIcon(ImageUtilities.loadImage(ICON_PATH, true));

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setVerifyInputWhenFocusTarget(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files
     * only, i.e. deserialization routines; otherwise you could get a
     * non-deserialized instance. To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized CompleteImageTopComponent getDefault() {
        if (instance == null) {
            instance = new CompleteImageTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the CompleteImageTopComponent instance. Never call {@link #getDefault}
     * directly!
     */
    public static synchronized CompleteImageTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(CompleteImageTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof CompleteImageTopComponent) {
            return (CompleteImageTopComponent) win;
        }
        Logger.getLogger(CompleteImageTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public void componentOpened() {
        MessageCenter.getInstance().addPageChangedEventListener(this);
        MessageCenter.getInstance().addTokenSelectionEventListener(this);
        MessageCenter.getInstance().addTokenDeselectionEventListener(this);
        MessageCenter.getInstance().addDocumentChangedEventListener(this);
    }

    @Override
    public void componentClosed() {
        MessageCenter.getInstance().removeDocumentChangedEventListener(this);
        MessageCenter.getInstance().removePageChangedEventListener(this);
        MessageCenter.getInstance().removeTokenSelectionEventListener(this);
        MessageCenter.getInstance().removeTokenDeselectionEventListener(this);
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

    private void init() {
        this.setDoubleBuffered(true);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    @Override
    public void dispatchEvent(final PageChangedEvent e) {

        final Page p = MainController.findInstance().getPage(e.getPageNum());
        if (p != null && p.hasImage()) {
            setName(java.util.ResourceBundle.getBundle("jav/gui/image/Bundle").getString("page") + (e.getPageNum()+1));
            EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    actualPageString = p.getImageCanonical();
                    if (p.hasImage()) {
                        hasImage = true;
                        loadImage(actualPageString, scale);
                    }
                }
            });
        }
    }

    private void loadImage(String filename, double scalef) {

        System.out.println("imageload" + filename);
        if (sp != null) {
            content.remove(this);
        }

        _loadPB = new ParameterBlockJAI("fileload");
        _loadPB.setParameter("filename", filename);
        _loadImage = JAI.create("fileload", _loadPB, new RenderingHints(JAI.KEY_TILE_CACHE, null));

        // z00m the image
        _zoomPB = new ParameterBlockJAI("scale");
        _zoomPB.setSource(_loadImage, 0);
        float s = (float) scalef;
        _zoomPB.setParameter("xScale", s);
        _zoomPB.setParameter("yScale", s);
//        _zoomPB.setParameter("interpolation", javax.media.jai.Interpolation.getInstance(javax.media.jai.Interpolation.INTERP_BICUBIC));

        _zoomImage = JAI.create("scale", _zoomPB);
        _finalImage = _zoomImage;

        _jadeDisplay = new JadeDisplay(_finalImage);
        _jadeDisplay.setDisableDoubleBuffering(false);
        _jadeDisplay.setDoubleBuffered(false);
        _jadeDisplay.setRepaintPolicy(JadeDisplay.REPAINT_DEFERRED);

        sp.getVerticalScrollBar().setUnitIncrement(60);
        sp.getVerticalScrollBar().setBlockIncrement(180);
        sp.setViewportView(_jadeDisplay);
        new MouseScroller(sp.getViewport());

        _tokenAnMaler = new TokenBoxPainter(_jadeDisplay, sp);

        imageOpened = true;
        content.add(this);
//        content.set(Collections.singleton(this), null);
    }

    @Override
    public boolean isReady() {
        return this.imageOpened;
    }

    @Override
    public void zoomImg(double s) {

        this.scale = s;
        this.loadImage(this.actualPageString, s);

        Token temp = MainController.findInstance().getDocument().getTokenByID(currentTokenIndex);
        if (temp != null) {
            _tokenAnMaler.paintTokenBox(temp, s);
        }
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
        return this.scale;
    }

    @Override
    public void dispatchEvent(final TokenSelectionEvent e) {
        currentTokenIndex = e.getTokenID();
        if (worker != null && worker.getState().equals(StateValue.STARTED)) {
            worker.cancel(true);
        }
        if (this.hasImage) {
            worker = new SwingWorker<Boolean, Object>() {

                @Override
                protected Boolean doInBackground() throws Exception {
                    Token temp = MainController.findInstance().getDocument().getTokenByID(currentTokenIndex);
                    if (!temp.getImageFilename().equals("")) {
                        if (actualPageString.equals(temp.getImageFilename())) {
                            _tokenAnMaler.paintTokenBox(temp, scale);
                        } else {
                            loadImage(temp.getImageFilename(), scale);
                            actualPageString = temp.getImageFilename();
                            _tokenAnMaler.paintTokenBox(temp, scale);
                        }
                    }
                    return true;
                }
            };
            worker.execute();
            setName(java.util.ResourceBundle.getBundle("jav/gui/image/Bundle").getString("page") + (MainController.findInstance().getDocument().getTokenByID(currentTokenIndex).getPageIndex()+1));
        }
    }

    @Override
    public void dispatchEvent(TokenDeselectionEvent e) {
        if (worker != null && worker.getState().equals(StateValue.STARTED)) {
            worker.cancel(true);
        }
        currentTokenIndex = -1;
        if (this._jadeDisplay != null && this._tokenAnMaler != null) {
            this._jadeDisplay.removeOverlayPainter(this._tokenAnMaler);
            _tokenAnMaler.setHasPainter(false);
        }
    }

    @Override
    public boolean hasImage() {
        return this.hasImage;
    }

    @Override
    public boolean showImage() {
        return hasImage();
    }

    @Override
    public void dispatchEvent(DocumentChangedEvent e) {
        scale = Double.parseDouble(MainController.findInstance().getDocumentProperties().getProperty("completeImageScale"));
    }
}
