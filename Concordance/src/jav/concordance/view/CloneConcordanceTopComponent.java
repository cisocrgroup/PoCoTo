package jav.concordance.view;

import jav.concordance.control.ConcordanceEntry;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JToolBar;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

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
public class CloneConcordanceTopComponent extends ConcordanceTopComponent {
    
    private JButton jB;

    @Override
    public void setSelected( int i, boolean b ) {
        if( !b ) {
            super.setSelected(i, b);
            if (this.toDo == 0) {
                jB.setEnabled(false);
            }
        } else {
            super.setSelected( i, b);
        }
    }

    @Override
    public JToolBar getToolBar() {
        JToolBar bar = super.getToolBar();

        jB = new JButton();
        jB.setText(java.util.ResourceBundle.getBundle("jav/concordance/Bundle").getString("changeCandidate"));
        jB.setFocusable(false);
        jB.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jB.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        /*
         * set the correction Candidate
         */
        jB.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                NotifyDescriptor.InputLine d = new NotifyDescriptor.InputLine(java.util.ResourceBundle.getBundle("jav/concordance/Bundle").getString("newcand"), java.util.ResourceBundle.getBundle("jav/concordance/Bundle").getString("candidateRep"));

                String candidateString = tokens.get(tokens.keySet().iterator().next()).getCandidateString();
                d.setInputText(candidateString);
                Object retval = DialogDisplayer.getDefault().notify(d);
                if (retval.equals(NotifyDescriptor.OK_OPTION)) {
                    candidateString = d.getInputText();
                    if (!candidateString.equals("")) {
                        for (ConcordanceEntry cce : tokens.values()) {
                            if (!cce.isCorrected() & !cce.isDisabled()) {
                                cce.setCandidateString(candidateString);
                            }
                        }

                        cp.setCandidateString(candidateString);
                        if (!candidateString.equals("") && numSelected > 0) {
                            jButton1.setEnabled(true);
                        }
                    }
                }
            }
        });

        bar.add(jB, 4);
        bar.add(new javax.swing.JToolBar.Separator(), 5);

        return bar;
    }
}
