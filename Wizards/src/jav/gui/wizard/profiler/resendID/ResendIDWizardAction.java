package jav.gui.wizard.profiler.resendID;

import cis.profiler.client.ProfilerWebServiceStub.ResendIDRequest;
import cis.profiler.client.ProfilerWebServiceStub.ResendIDRequestType;
import cis.profiler.client.ProfilerWebServiceStub.ResendIDResponse;
import cis.profiler.client.ProfilerWebServiceStub.ResendIDResponseType;
import jav.gui.dialogs.CustomErrorDialog;
import jav.gui.dialogs.CustomInformationDialog;
import jav.gui.main.MainController;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import javax.swing.JComponent;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressRunnable;
import org.netbeans.api.progress.ProgressUtils;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
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
 */
@ActionID(category = "Profiler",
id = "jav.gui.wizard.profiler.resendID.ResendID")
@ActionRegistration(displayName = "#CTL_ResendID")
@ActionReferences({
    @ActionReference(path = "Menu/Profiler", position = 1007)
})
public final class ResendIDWizardAction implements ActionListener {

    private WizardDescriptor.Panel[] panels;

    public @Override
    void actionPerformed(ActionEvent e) {
        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setOptions(new Object[] {WizardDescriptor.FINISH_OPTION,WizardDescriptor.CANCEL_OPTION});
        wizardDescriptor.setTitle("Your wizard dialog title here");
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            ProgressRunnable<Integer> r = new IDResender(wizardDescriptor.getProperty("Email").toString());
            ProgressUtils.showProgressDialogAndRun(r, java.util.ResourceBundle.getBundle("jav/gui/wizard/profiler/resendID/Bundle").getString("resending"), true);
        }
    }

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        panels = new WizardDescriptor.Panel[]{
            new ResendIDWizardPanel1()
        };
        String[] steps = new String[panels.length];
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            // Default step name to component name of panel. Mainly useful
            // for getting the name of the target chooser to appear in the
            // list of steps.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Sets step number of a component
                // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_*:
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                // Sets steps names for a panel
                jc.putClientProperty("WizardPanel_contentData", steps);
                // Turn on subtitle creation on each step
                jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.FALSE);
                // Show steps on the left side with the image on the background
                jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.FALSE);
                // Turn on numbering of all steps
                jc.putClientProperty("WizardPanel_contentNumbered", Boolean.FALSE);
            }
        }
        return panels;
    }

    private class IDResender implements ProgressRunnable<Integer> {

        private String email;

        public IDResender(String e) {
            this.email = e;
        }

        @Override
        public Integer run(ProgressHandle ph) {
            try {
                ph.progress(java.util.ResourceBundle.getBundle("jav/gui/wizard/profiler/resendID/Bundle").getString("resending"));
                ph.setDisplayName(java.util.ResourceBundle.getBundle("jav/gui/wizard/profiler/resendID/Bundle").getString("resending"));

                ResendIDRequest rrq = new ResendIDRequest();
                ResendIDRequestType rrqt = new ResendIDRequestType();

                rrqt.setEmail(email);
                rrq.setResendIDRequest(rrqt);

                ResendIDResponse rrs = null;
                try {
                    rrs = MainController.findInstance().getProfilerWebServiceStub().resendID(rrq);
                    ResendIDResponseType rrst = rrs.getResendIDResponse();
                    if (rrst.getReturncode() == 0) {
                        NbPreferences.forModule(MainController.class).put("profiler_user_id", "");
                        MainController.findInstance().refreshID();
                        new CustomInformationDialog().showDialog(java.util.ResourceBundle.getBundle("jav/gui/wizard/profiler/resendID/Bundle").getString("resendID_success"));
                    } else {
                        new CustomErrorDialog().showDialog(rrst.getMessage());
                    }
                } catch (RemoteException ex) {
                    new CustomErrorDialog().showDialog(ex);
                }
                return 0;
            } catch (Exception e) {
                return -1;
            }
        }
    }
}
