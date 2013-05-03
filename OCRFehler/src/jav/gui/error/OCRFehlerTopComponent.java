package jav.gui.error;

import jav.correctionBackend.OCRErrorInfo;
import jav.gui.cookies.FontZoomCookie;
import jav.gui.events.MessageCenter;
import jav.gui.events.documentChanged.DocumentChangedEvent;
import jav.gui.events.documentChanged.DocumentChangedEventSlot;
import jav.gui.events.tokenStatus.CorrectedEvent;
import jav.gui.events.tokenStatus.TokenStatusEvent;
import jav.gui.events.tokenStatus.TokenStatusEventSlot;
import jav.gui.events.tokenStatus.TokenStatusType;
import jav.gui.main.AbstractMyTopComponent;
import jav.gui.main.MainController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.IOProvider;
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
 */
@ConvertAsProperties(dtd = "-//jav.gui.error//OCRFehler//EN",
autostore = false)
public final class OCRFehlerTopComponent extends AbstractMyTopComponent implements FontZoomCookie, TokenStatusEventSlot, DocumentChangedEventSlot {

    private static OCRFehlerTopComponent instance;
    private static final String PREFERRED_ID = "OCRFehlerTopComponent";
    private Preferences node;
    private InstanceContent content = new InstanceContent();
    private int fontSize;
    private OCRFehlerMode olm = new OCRFehlerDefaultMode();
    private HashMap<String, OCRErrorInfo> errormap = null;

    public OCRFehlerTopComponent() {
//        initComponents();

        this.setFocusable(true);

        associateLookup(new AbstractLookup(content));
        node = NbPreferences.forModule(this.getClass());

        initialize();

        jPanel1.setLayout(new BoxLayout(jPanel1,
                BoxLayout.Y_AXIS));
        jPanel1.setBackground(Color.white);

        // show concordance button
        jButton2.setEnabled(false);
        jButton2.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                olm.concordanceAction();
            }
        });

        setName(NbBundle.getMessage(OCRFehlerTopComponent.class, "CTL_OCRFehlerTopComponent"));
        setToolTipText(NbBundle.getMessage(OCRFehlerTopComponent.class, "HINT_OCRFehlerTopComponent"));
//        setIcon(ImageUtilities.loadImage(ICON_PATH, true));

    }

    private void initialize() {

        this.setLayout(new BorderLayout());

        jToolBar1 = new javax.swing.JToolBar();
        jButton2 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();

        jToolBar1.setRollover(true);

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(OCRFehlerTopComponent.class, "OCRFehlerTopComponent.jButton2.text")); // NOI18N
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton2);

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setWheelScrollingEnabled(true);

        this.add(jToolBar1, BorderLayout.PAGE_START);
        this.add(jScrollPane1, BorderLayout.CENTER);

    }
    private javax.swing.JButton jButton2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

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
    public static synchronized OCRFehlerTopComponent getDefault() {
        if (instance == null) {
            instance = new OCRFehlerTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the OCRFehlerTopComponent instance. Never call {@link #getDefault}
     * directly!
     */
    public static synchronized OCRFehlerTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(OCRFehlerTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof OCRFehlerTopComponent) {
            return (OCRFehlerTopComponent) win;
        }
        Logger.getLogger(OCRFehlerTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
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
    public void componentOpened() {
        if (MainController.findInstance().getDocOpen()) {
            displayErrorMap();
            olm.setDocLoaded(true);
        }
        MessageCenter.getInstance().addDocumentChangedEventListener(this);
        MessageCenter.getInstance().addTokenStatusEventListener(this);
    }

    @Override
    public void componentClosed() {
        MessageCenter.getInstance().removeDocumentChangedEventListener(this);
        MessageCenter.getInstance().removeTokenStatusEventListener(this);
    }

    @Override
    public void dispatchEvent(DocumentChangedEvent e) {
        fontSize = Integer.parseInt(MainController.findInstance().getDocumentProperties().getProperty("ocrerrorFontSize"));
        displayErrorMap();
        olm.setDocLoaded(true);
    }

    public void displayErrorMap() {

        SwingWorker<TreeMap<String, OCRErrorInfo>, Object> worker = new SwingWorker<TreeMap<String, OCRErrorInfo>, Object>() {

            @Override
            protected TreeMap<String, OCRErrorInfo> doInBackground() {

                TreeMap<String, OCRErrorInfo> result = null;
                try {
                    HashMap<String, OCRErrorInfo> errmap = MainController.findInstance().computeErrorFreqList();
                    errormap = errmap;
                    MyComparator comp = new MyComparator(errmap);
                    result = new TreeMap<>(comp);
                    result.putAll(errmap);
                } catch (Exception e) {
//                    IOProvider.getDefault().getIO("", false).getOut().println(e.getMessage());
                }
                return result;
            }

            @Override
            protected void done() {
                try {
                    TreeMap<String, OCRErrorInfo> result = get();

                    if (result != null) {
                        content.remove(instance);
                        jPanel1.removeAll();
                        jPanel1.setLayout(new BoxLayout(jPanel1, BoxLayout.Y_AXIS));

                        Iterator<String> i = result.keySet().iterator();
                        while (i.hasNext()) {
                            String key = i.next();
                            OCRErrorInfo info = result.get(key);
                            if (info.getOccurencesN() > 1) {
                                OCRFehlerPanel panel = new OCRFehlerPanel(key, info, olm);
                                jPanel1.add(panel);
                            }
                        }

                        jScrollPane1.setViewportView(jPanel1);
                        content.add(instance);
                    } else {
                    }
                } catch (ExecutionException ex) {
                } catch (InterruptedException ex) {
                } catch (CancellationException ex) {
                }
            }
        };

        jPanel1.setLayout(new BoxLayout(jPanel1, BoxLayout.Y_AXIS));
        jPanel1.add(new JLabel("loading..."));
        jScrollPane1.setViewportView(jPanel1);
        worker.execute();
//        this.revalidate();
    }

    @Override
    public boolean isReady() {
        if (jPanel1.getComponentCount() > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void zoomFont(int i) {
        content.remove(this);
        for (Component c : jPanel1.getComponents()) {
            if (c instanceof OCRFehlerPanel) {
                OCRFehlerPanel pl = (OCRFehlerPanel) c;
                pl.zoomFont(i);
            }
        }
        this.fontSize = i;
        MainController.findInstance().getDocumentProperties().setProperty("ocrerrorFontSize", "" + this.fontSize);
        content.add(this);
        jScrollPane1.revalidate();
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

    public void setKonkordanzButton(boolean b) {
        jButton2.setEnabled(b);
    }

    @Override
    public void dispatchEvent(final TokenStatusEvent e) {

        if (e.getType().equals(TokenStatusType.CORRECTED)) {
            CorrectedEvent ce = (CorrectedEvent) e;
            if (errormap != null && errormap.containsKey(ce.getNewText())) {
                OCRErrorInfo info = errormap.get(ce.getNewText());
                if (ce.getSetTo()) {
                    info.addCorrected();
                } else {
                    info.removeCorrected();
                }
                for (Component ca : jPanel1.getComponents()) {
                    JPanel p = (JPanel) ca;
                    OCRFehlerLabel l = (OCRFehlerLabel) p.getComponent(0);
                    if (ce.getNewText().equals(l.getText())) {
                        JLabel numl = (JLabel) p.getComponent(1);
                        int n = info.getOccurencesN() - info.getCorrected();
                        numl.setText("" + n);
                        break;
                    }
                }
            }
        } else if(e.getType().equals(TokenStatusType.DELETE)) {
            
        } else if(e.getType().equals(TokenStatusType.INSERT)) {
            
        } else if(e.getType().equals(TokenStatusType.MERGED_RIGHT)) {
            
        } else if(e.getType().equals(TokenStatusType.SPLIT)) {
            
        }

//        if (e.getType().equals(TokenStatusType.CORRECTED) || e.getType().equals(TokenStatusType.MERGED_RIGHT) || e.getType().equals(TokenStatusType.SPLIT)) {

//            EventQueue.invokeLater(new Runnable() {
//
//                @Override
//                public void run() {
//                    String s = MainController.findInstance().getToken(e.getTokenIndex()).getWOCR();
//                    if (errormap != null && s != null) {
//                        if (errormap.containsKey(s)) {
//                            OCRErrorInfo info = errormap.get(s);
//                            info.addCorrected();
//                            if (info.getCorrected() == info.getOccurencesN()) {
//                                for (Component ca : jPanel1.getComponents()) {
//                                    JPanel p = (JPanel) ca;
//                                    OCRFehlerLabel l = (OCRFehlerLabel) p.getComponent(0);
//                                    if (s.equals(l.getText())) {
//                                        jPanel1.remove(ca);
//                                        jPanel1.revalidate();
//                                        break;
//                                    }
//                                }
//                            } else {
//                                for (Component ca : jPanel1.getComponents()) {
//                                    JPanel p = (JPanel) ca;
//                                    OCRFehlerLabel l = (OCRFehlerLabel) p.getComponent(0);
//                                    if (s.equals(l.getText())) {
//                                        JLabel numl = (JLabel) p.getComponent(1);
//                                        int n = info.getOccurencesN() - info.getCorrected();
//                                        numl.setText("" + n);
//                                        break;
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            });
//        }
    }
}

class MyComparator implements Comparator<String> {

    HashMap<String, OCRErrorInfo> theMapToSort;

    public MyComparator(HashMap<String, OCRErrorInfo> theMapToSort) {
        this.theMapToSort = theMapToSort;
    }

    @Override
    public int compare(String s1, String s2) {
        int val1 = theMapToSort.get(s1).getOccurencesN();
        int val2 = theMapToSort.get(s2).getOccurencesN();
        if (val1 < val2) {
            return 1;
        } else if (val1 > val2) {
            return -1;
        } else {
            return s1.compareTo(s2);
        }
    }
}
