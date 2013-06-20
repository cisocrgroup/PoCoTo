package jav.gui.error.profiler;

import jav.correctionBackend.Pattern;
import jav.gui.cookies.FontZoomCookie;
import jav.gui.events.MessageCenter;
import jav.gui.events.documentChanged.DocumentChangedEvent;
import jav.gui.events.documentChanged.DocumentChangedEventSlot;
import jav.gui.events.tokenStatus.TokenStatusEvent;
import jav.gui.events.tokenStatus.TokenStatusEventSlot;
import jav.gui.main.AbstractMyTopComponent;
import jav.gui.main.MainController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.BoxLayout;
import javax.swing.SwingWorker;
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
 */
@ConvertAsProperties(dtd = "-//jav.gui.ocrerror_profiler//PatternView//EN",
autostore = false)
public final class PatternTopComponent extends AbstractMyTopComponent implements DocumentChangedEventSlot, TokenStatusEventSlot, FontZoomCookie {

    private ArrayList<Pattern> patterns;
    private Preferences node;
    private InstanceContent content = new InstanceContent();
    private static PatternTopComponent instance;
    private int fontSize;
    private PatternMode plm = new PatternDefaultMode();
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "PatternTopComponent";

    public PatternTopComponent() {
//        initComponents();

        initialize();
        this.setFocusable(true);

        associateLookup(new AbstractLookup(content));
        node = NbPreferences.forModule(this.getClass());

        jPanel1.setLayout(new BoxLayout(jPanel1,
                BoxLayout.Y_AXIS));
        jPanel1.setBackground(Color.white);

//        jButton1.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                JFileChooser jfc = new JFileChooser();
//                jfc.setDialogTitle(java.util.ResourceBundle.getBundle("jav/gui/error/profiler/Bundle").getString("choose_profiler_xml"));
//                jfc.setFileFilter(new FileFilter() {
//
//                    @Override
//                    public boolean accept(File f) {
//                        return f.getName().toLowerCase().endsWith(".xml") || f.isDirectory();
//                    }
//
//                    @Override
//                    public String getDescription() {
//                        return java.util.ResourceBundle.getBundle("jav/gui/error/profiler/Bundle").getString("profiler_xml");
//                    }
//                });
//
//                if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
//                    File file = jfc.getSelectedFile();
//                    patterns = parseFile(file);
//                    display();
//                }
//            }
//        });

        jButton2.setEnabled(false);
        jButton2.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                plm.concordanceAction();
            }
        });

        setName(NbBundle.getMessage(PatternTopComponent.class, "CTL_PatternTopComponent"));
        setToolTipText(NbBundle.getMessage(PatternTopComponent.class, "HINT_PatternTopComponent"));
//        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
    }

    private void initialize() {

        this.setLayout(new BorderLayout());
        
        jToolBar1 = new javax.swing.JToolBar();
//        jButton1 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jButton2 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();

        jToolBar1.setRollover(true);

//        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(PatternTopComponent.class, "PatternTopComponent.jButton1.text")); // NOI18N
//        jButton1.setFocusable(false);
//        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
//        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
//        jToolBar1.add(jButton1);
//        jToolBar1.add(jSeparator1);

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(PatternTopComponent.class, "PatternTopComponent.jButton2.text")); // NOI18N
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton2);

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setWheelScrollingEnabled(true);

        this.add(jToolBar1, BorderLayout.PAGE_START);
        this.add(jScrollPane1, BorderLayout.CENTER);

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jButton2 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();

        jToolBar1.setRollover(true);

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(PatternTopComponent.class, "PatternTopComponent.jButton1.text")); // NOI18N
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton1);
        jToolBar1.add(jSeparator1);

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(PatternTopComponent.class, "PatternTopComponent.jButton2.text")); // NOI18N
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton2);

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setDoubleBuffered(true);
        jScrollPane1.setMaximumSize(new java.awt.Dimension(0, 0));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(0, 0));

        jPanel1.setMaximumSize(new java.awt.Dimension(0, 0));
        jPanel1.setMinimumSize(new java.awt.Dimension(0, 0));
        jPanel1.setPreferredSize(new java.awt.Dimension(0, 0));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 382, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 267, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized PatternTopComponent getDefault() {
        if (instance == null) {
            instance = new PatternTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the PatternTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized PatternTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(PatternTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof PatternTopComponent) {
            return (PatternTopComponent) win;
        }
        Logger.getLogger(PatternTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public void componentOpened() {
        MessageCenter.getInstance().addDocumentChangedEventListener(this);
        if( MainController.findInstance().getDocOpen()) {
            this.displayPatterns();
        }
//        if( MainController.findInstance().getTempProfileFilename() != null) {
//            patterns = parseFile(new File(MainController.findInstance().getTempProfileFilename()));
//            display();
//        }
    }

    @Override
    public void componentClosed() {
        plm.disconnect();
        MessageCenter.getInstance().removeDocumentChangedEventListener(this);
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

    private void display() {

        jPanel1.removeAll();
        jPanel1.setLayout(new BoxLayout( jPanel1, BoxLayout.Y_AXIS));
        Iterator<Pattern> i = patterns.iterator();
        while (i.hasNext()) {
            Pattern p = i.next();
//            System.out.println("Pattern "+p.getLeft()+"_"+p.getRight()+" "+p.getOccurencesN()+" "+p.getOccurences().size());
            if (p.getOccurencesN() >= 1) {
                PatternPanel pp = new PatternPanel( p, plm );
                jPanel1.add(pp);
            }
        }
        jScrollPane1.setViewportView(jPanel1);
        content.add(this);
        this.repaint();
        this.revalidate();
    }

    public void setKonkordanzButton(boolean b) {
        jButton2.setEnabled(b);
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
            if (c instanceof PatternLabel) {
                PatternLabel pl = (PatternLabel) c;
                pl.zoomFont(i);
            }
        }
        this.fontSize = i;
        MainController.findInstance().getDocumentProperties().setProperty("profilerFontSize", ""+this.fontSize);
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
    
    public void displayPatterns() {
        SwingWorker<Boolean, Object> w = new SwingWorker<Boolean, Object>() {

            @Override
            protected Boolean doInBackground() throws Exception {
                patterns = MainController.findInstance().getPatternList();
                return true;
            }
            
            @Override
            protected void done() {
                if( patterns.isEmpty() ) {
                    jPanel1.removeAll();
                } else {
                    display();
                }
                plm.setDocLoaded(true);
            }   
        };
        w.execute();
    }

    @Override
    public void dispatchEvent(DocumentChangedEvent e) {
        fontSize = Integer.parseInt(MainController.findInstance().getDocumentProperties().getProperty("profilerFontSize"));
        this.displayPatterns();
//
//        patterns = MainController.findInstance().getPatternList();
//        if( patterns.isEmpty() ) {
//            jPanel1.removeAll();
//        } else {
//            display();
//        }
////        if( MainController.findInstance().getTempProfileFilename() != null) {
////            patterns = parseFile(new File(MainController.findInstance().getTempProfileFilename()));
////            display();
////        } else {
////            jPanel1.removeAll();
////        }
//        plm.setDocLoaded(true);
    }

    @Override
    public void dispatchEvent(TokenStatusEvent e) {
        SwingWorker<Boolean, Object> w = new SwingWorker<Boolean, Object>() {

            @Override
            protected Boolean doInBackground() throws Exception {
                patterns = MainController.findInstance().getPatternList();
                return true;
            }
            
            @Override
            protected void done() {
                if( patterns.isEmpty() ) {
                    jPanel1.removeAll();
                } else {
                    display();
                }
                plm.setDocLoaded(true);
            }   
        };
        w.execute();
    }
}
