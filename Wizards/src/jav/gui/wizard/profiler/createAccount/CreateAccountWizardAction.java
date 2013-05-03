package jav.gui.wizard.profiler.createAccount;

import cis.profiler.client.ProfilerWebServiceStub.CreateAccountRequest;
import cis.profiler.client.ProfilerWebServiceStub.CreateAccountRequestType;
import cis.profiler.client.ProfilerWebServiceStub.CreateAccountResponse;
import cis.profiler.client.ProfilerWebServiceStub.CreateAccountResponseType;
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
id = "jav.gui.wizard.profiler.createAccount.CreateAccount")
@ActionRegistration(displayName = "#CTL_CreateAccount")
@ActionReferences({
    @ActionReference(path = "Menu/Profiler", position = 1005, separatorAfter = 1502)
})
public final class CreateAccountWizardAction implements ActionListener {

    private WizardDescriptor.Panel<WizardDescriptor>[] panels;

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
            ProgressRunnable<Integer> r = new AccountCreator(wizardDescriptor.getProperty("Email").toString(),wizardDescriptor.getProperty("Name").toString(), wizardDescriptor.getProperty("Institution").toString());
            ProgressUtils.showProgressDialogAndRun(r, java.util.ResourceBundle.getBundle("jav/gui/wizard/profiler/createAccount/Bundle").getString("creating"), true);
            panels = null;
        }
    }

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel<WizardDescriptor>[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[]{
                new CreateAccountWizardPanel1()
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
        }
        return panels;
    }
    
    private class AccountCreator implements ProgressRunnable<Integer> {
        
        private String email;
        private String name;
        private String institution;
        
        public AccountCreator(String e, String n, String i) {
            this.email = e;
            this.name = n;
            this.institution = i;
        }

        @Override
        public Integer run(ProgressHandle ph) {
            try {
                ph.progress(java.util.ResourceBundle.getBundle("jav/gui/wizard/profiler/createAccount/Bundle").getString("creating"));
                ph.setDisplayName(java.util.ResourceBundle.getBundle("jav/gui/wizard/profiler/createAccount/Bundle").getString("creating"));

                CreateAccountRequest crq = new CreateAccountRequest();
                CreateAccountRequestType crqt = new CreateAccountRequestType();

            crqt.setEmail(this.email);
            crqt.setUsername(this.name);
            crqt.setInstitution(this.institution);
            crq.setCreateAccountRequest(crqt);
            
            CreateAccountResponse crs;
            try {
                crs = MainController.findInstance().getProfilerWebServiceStub().createAccount(crq);
                CreateAccountResponseType crst = crs.getCreateAccountResponse();
                if( crst.getReturncode() == 0) {
                    new CustomInformationDialog().showDialog(java.util.ResourceBundle.getBundle("jav/gui/wizard/profiler/createAccount/Bundle").getString("account_success"));
                } else {
                    new CustomErrorDialog().showDialog(crst.getMessage());
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
