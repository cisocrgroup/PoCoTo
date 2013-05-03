package jav.gui.token.actions;

import jav.correctionBackend.MyIterator;
import jav.correctionBackend.Token;
import jav.gui.events.MessageCenter;
import jav.gui.events.cancel.CancelEvent;
import jav.gui.events.cancel.CancelEventSlot;
import jav.gui.events.concordance.ConcordanceEvent;
import jav.gui.events.concordance.ConcordanceType;
import jav.gui.events.pageChanged.PageChangedEvent;
import jav.gui.events.pageChanged.PageChangedEventSlot;
import jav.gui.events.tokenDeselection.TokenDeselectionEvent;
import jav.gui.events.tokenDeselection.TokenDeselectionEventSlot;
import jav.gui.events.tokenMultiDeselection.TokenMultiDeselectionEvent;
import jav.gui.events.tokenMultiDeselection.TokenMultiDeselectionEventSlot;
import jav.gui.events.tokenMultiSelection.TokenMultiSelectionEvent;
import jav.gui.events.tokenMultiSelection.TokenMultiSelectionEventSlot;
import jav.gui.events.tokenSelection.TokenSelectionEvent;
import jav.gui.events.tokenSelection.TokenSelectionEventSlot;
import jav.gui.filter.DoppelgangerFilter;
import jav.gui.main.MainController;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

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
@ConvertAsProperties(dtd = "-//jav.gui.token.actions//TokenActions//EN",
autostore = false)
@TopComponent.Description(preferredID = "TokenActionsTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "explorer", openAtStartup = true)
@ActionID(category = "Window", id = "jav.gui.token.actions.TokenActionsTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_TokenActionsAction",
preferredID = "TokenActionsTopComponent")
public final class TokenActionsTopComponent extends TopComponent implements CancelEventSlot, TokenSelectionEventSlot, TokenDeselectionEventSlot, PageChangedEventSlot, TokenMultiSelectionEventSlot, TokenMultiDeselectionEventSlot {

    private SwingWorker<Boolean, Object> worker;
    private ArrayList<Integer> multiToken;
    private ArrayList<Token> result;
    private int resultSize;
    private int currentTokenID;
    private MyIterator<Token> tokenit;

    public TokenActionsTopComponent() {
        initComponents();
        this.setBackground(Color.gray);
        setName(NbBundle.getMessage(TokenActionsTopComponent.class, "CTL_TokenActionsTopComponent"));
        setToolTipText(NbBundle.getMessage(TokenActionsTopComponent.class, "HINT_TokenActionsTopComponent"));
        //putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);

        jButton1.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                MessageCenter.getInstance().fireConcordanceEvent(new ConcordanceEvent(this, ConcordanceType.CLONE, result, MainController.findInstance().getDocument().getTokenByIndex(currentTokenID).getWDisplay()));
            }
        });

        jButton2.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                MainController.findInstance().mergeRightward( MainController.findInstance().getDocument().getTokenByID(multiToken.get(0)).getIndexInDocument(), multiToken.size() - 1 );
            }
        });
        jButton3.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                int begin = multiToken.get(0);
                if( MainController.findInstance().getDocument().getTokenByID(multiToken.get(0)-1).getWDisplay().equals(" ") && MainController.findInstance().getDocument().getTokenByID(multiToken.get(multiToken.size()-1)+1).getWDisplay().equals(" ")) {
                    multiToken.add(MainController.findInstance().getDocument().getTokenByID(multiToken.get(multiToken.size()-1) +1).getIndexInDocument());
                }
                int afterend = multiToken.get(multiToken.size() - 1) + 1;
                MainController.findInstance().deleteToken(begin, afterend);
            }
        });
        jButton1.setEnabled(false);
        jButton2.setEnabled(false);
        jButton3.setEnabled(false);

        jXTaskPane1.setTitle(org.openide.util.NbBundle.getMessage(TokenActionsTopComponent.class, "TokenActionsTopComponent.jXTaskPane1.text"));
        jXTaskPane1.add(jPanel1);

        jXTaskPane2.setTitle(org.openide.util.NbBundle.getMessage(TokenActionsTopComponent.class, "TokenActionsTopComponent.jXTaskPane2.text"));
        jXTaskPane2.add(jPanel2);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jXBusyLabel1 = new org.jdesktop.swingx.JXBusyLabel();
        jPanel2 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jXTaskPaneContainer1 = new org.jdesktop.swingx.JXTaskPaneContainer();
        jXTaskPane1 = new org.jdesktop.swingx.JXTaskPane();
        jXTaskPane2 = new org.jdesktop.swingx.JXTaskPane();

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(TokenActionsTopComponent.class, "TokenActionsTopComponent.jButton1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jXBusyLabel1, org.openide.util.NbBundle.getMessage(TokenActionsTopComponent.class, "TokenActionsTopComponent.jXBusyLabel1.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
            .addComponent(jXBusyLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jXBusyLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1))
        );

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(TokenActionsTopComponent.class, "TokenActionsTopComponent.jButton2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(TokenActionsTopComponent.class, "TokenActionsTopComponent.jButton3.text")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
            .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3))
        );

        jXTaskPaneContainer1.add(jXTaskPane1);
        jXTaskPaneContainer1.add(jXTaskPane2);

        jScrollPane1.setViewportView(jXTaskPaneContainer1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private org.jdesktop.swingx.JXBusyLabel jXBusyLabel1;
    private org.jdesktop.swingx.JXTaskPane jXTaskPane1;
    private org.jdesktop.swingx.JXTaskPane jXTaskPane2;
    private org.jdesktop.swingx.JXTaskPaneContainer jXTaskPaneContainer1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        MessageCenter.getInstance().addTokenSelectionEventListener(this);
        MessageCenter.getInstance().addTokenDeselectionEventListener(this);
        MessageCenter.getInstance().addPageChangedEventListener(this);
        MessageCenter.getInstance().addTokenMultiDeselectionEventListener(this);
        MessageCenter.getInstance().addTokenMultiSelectionEventListener(this);
        MessageCenter.getInstance().addCancelEventListener(this);
    }

    @Override
    public void componentClosed() {
        MessageCenter.getInstance().removeTokenSelectionEventListener(this);
        MessageCenter.getInstance().removeTokenDeselectionEventListener(this);
        MessageCenter.getInstance().removePageChangedEventListener(this);
        MessageCenter.getInstance().removeTokenMultiDeselectionEventListener(this);
        MessageCenter.getInstance().removeTokenMultiSelectionEventListener(this);
        MessageCenter.getInstance().removeCancelEventListener(this);
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    public void dispatchEvent(TokenSelectionEvent e) {

        currentTokenID = e.getTokenID();
        if (worker != null && worker.getState().equals(StateValue.STARTED)) {
            worker.cancel(true);
            try {
                Thread.sleep(1L);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        worker = new SwingWorker<Boolean, Object>() {
            
            @Override
            protected Boolean doInBackground() {

                boolean retval = false;
                try {
                    DoppelgangerFilter f = new DoppelgangerFilter( MainController.findInstance().getDocument().getTokenByID(currentTokenID), "");
                    tokenit = MainController.findInstance().getDocument().tokenIterator();
                    result = f.applyFilter( tokenit );
                    if( result != null ) {
                        resultSize = result.size();
                        retval = true;
                    } else {
                        retval = false;
                    }
                } catch (Exception e) {
                    retval = false;
                } catch (Error e) {
                    retval = false;
                }
                return retval;
            }

            @Override
            protected void done() {
                try {
                    boolean retval = get();
                    if (retval) {
                        jXBusyLabel1.setText((resultSize + java.util.ResourceBundle.getBundle("jav/gui/token/actions/Bundle").getString("occ")));
                        if (resultSize > 1) {
                            jButton1.setEnabled(true);
                        } else {
                            jButton1.setEnabled(false);
                        }
                        jXBusyLabel1.setBusy(false);

                    } else {
                    }
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (CancellationException ex) {
                }
            }
        };
        jXBusyLabel1.setText(java.util.ResourceBundle.getBundle("jav/gui/token/actions/Bundle").getString("calc"));
        jXBusyLabel1.setBusy(true);
        worker.execute();
    }

    @Override
    public void dispatchEvent(TokenDeselectionEvent e) {
        if (worker != null && worker.getState().equals(StateValue.STARTED)) {
            worker.cancel(true);
            tokenit.cancel();
        }
        this.currentTokenID = -1;
        jXBusyLabel1.setBusy(false);
        jXBusyLabel1.setText(java.util.ResourceBundle.getBundle("jav/gui/token/actions/Bundle").getString("occ"));
        jButton1.setEnabled(false);
    }

    @Override
    public void dispatchEvent(PageChangedEvent e) {
        jXBusyLabel1.setText(java.util.ResourceBundle.getBundle("jav/gui/token/actions/Bundle").getString("occ"));
        jButton1.setEnabled(false);
    }

    @Override
    public void dispatchEvent(TokenMultiSelectionEvent e) {
        this.multiToken = e.getTokenIDs();
        this.jButton2.setEnabled(true);
        this.jButton3.setEnabled(true);
    }

    @Override
    public void dispatchEvent(TokenMultiDeselectionEvent e) {
        this.jButton2.setEnabled(false);
        this.jButton3.setEnabled(false);
    }

    @Override
    public void dispatchEvent(CancelEvent e) {
        if (worker != null && worker.getState().equals(StateValue.STARTED)) {
            worker.cancel(true);
            tokenit.cancel();
            try {
                Thread.sleep(1L);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        jXBusyLabel1.setBusy(false);
        jXBusyLabel1.setText(java.util.ResourceBundle.getBundle("jav/gui/token/actions/Bundle").getString("occ"));
        jButton1.setEnabled(false);
    }
}