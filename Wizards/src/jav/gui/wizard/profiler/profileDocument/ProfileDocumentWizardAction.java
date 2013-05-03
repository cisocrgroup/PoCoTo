package jav.gui.wizard.profiler.profileDocument;

import cis.profiler.client.ProfilerWebServiceStub.CheckQuotaRequest;
import cis.profiler.client.ProfilerWebServiceStub.CheckQuotaRequestType;
import cis.profiler.client.ProfilerWebServiceStub.CheckQuotaResponse;
import cis.profiler.client.ProfilerWebServiceStub.CheckQuotaResponseType;
import cis.profiler.client.ProfilerWebServiceStub.GetConfigurationsResponse;
import jav.gui.actions.ContextAction;
import jav.gui.cookies.ProfilerIDCookie;
import jav.gui.dialogs.CustomErrorDialog;
import jav.gui.dialogs.UnsavedChangesDialog;
import jav.gui.main.MainController;
import java.awt.Component;
import java.awt.Dialog;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import javax.swing.Action;
import javax.swing.JComponent;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressRunnable;
import org.netbeans.api.progress.ProgressUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

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
id = "jav.gui.wizard.profiler.ProfileDocument")
@ActionRegistration(displayName = "#CTL_ProfileDocument")
@ActionReferences({
    @ActionReference(path = "Menu/Profiler", position = 1011, separatorBefore = 1010)
})
public final class ProfileDocumentWizardAction extends ContextAction<ProfilerIDCookie> {

    public ProfileDocumentWizardAction() {
        this(Utilities.actionsGlobalContext());
    }

    public ProfileDocumentWizardAction(Lookup context) {
        super(context);
        putValue(NAME, java.util.ResourceBundle.getBundle("jav/gui/wizard/profiler/profileDocument/Bundle").getString("CTL_ProfileDocument"));
    }
    private String[] configurations;
    private WizardDescriptor.Panel[] panels;

    private void showDialog() {
        // check if the account used has sufficient quota
        ProgressRunnable<Integer> r = new QuotaChecker();
        int retval = ProgressUtils.showProgressDialogAndRun(r, java.util.ResourceBundle.getBundle("jav/gui/wizard/profiler/profileDocument/Bundle").getString("checking_quota"), true);
        if (retval > 0) {
            try {
                GetConfigurationsResponse gcr = MainController.findInstance().getProfilerWebServiceStub().getConfigurations();
                configurations = gcr.getGetConfigurationsResponse().getConfigurations();

                WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels(configurations));
                wizardDescriptor.setOptions(new Object[] {WizardDescriptor.FINISH_OPTION,WizardDescriptor.CANCEL_OPTION});
                wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
                wizardDescriptor.setTitle("Your wizard dialog title here");
                Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
                dialog.setVisible(true);
                dialog.toFront();
                boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
                if (!cancelled) {
                    MainController.findInstance().profileDocument(wizardDescriptor.getProperty("config").toString());
                }
            } catch (RemoteException ex) {
                new CustomErrorDialog().showDialog(ex.getMessage());
            }
        } else if (retval == 0) {
            new CustomErrorDialog().showDialog(java.util.ResourceBundle.getBundle("jav/gui/wizard/profiler/profileDocument/Bundle").getString("no_quota"));
        }
    }

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels(String[] configurations) {
        panels = new WizardDescriptor.Panel[]{
            new ProfileDocumentWizardPanel1(configurations)
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

    @Override
    public Class<ProfilerIDCookie> contextClass() {
        return ProfilerIDCookie.class;
    }

    @Override
    public boolean enable(ProfilerIDCookie context) {
        if (MainController.findInstance().getDocOpen()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        return new ProfileDocumentWizardAction();
    }

    @Override
    public void performAction(ProfilerIDCookie context) {
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(java.util.ResourceBundle.getBundle("jav/gui/dialogs/Bundle").getString("profile_obacht"), "Obacht",
                NotifyDescriptor.YES_NO_OPTION);
        if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.YES_OPTION) {
            if (MainController.findInstance().hasUnsavedChanges()) {
                Object retval = new UnsavedChangesDialog().showDialog();
                if (retval.equals(java.util.ResourceBundle.getBundle("jav/gui/dialogs/Bundle").getString("save"))) {
                    try {
                        MainController.findInstance().getSaver().save();
                        showDialog();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else if (retval.equals(java.util.ResourceBundle.getBundle("jav/gui/dialogs/Bundle").getString("discard"))) {
                    showDialog();
                }
            } else {
                showDialog();
            }
        }
    }

    private class QuotaChecker implements ProgressRunnable<Integer> {

        public QuotaChecker() {
        }

        @Override
        public Integer run(ProgressHandle ph) {
            try {
                ph.progress(java.util.ResourceBundle.getBundle("jav/gui/actions/Bundle").getString("checking_quota"));
                ph.setDisplayName(java.util.ResourceBundle.getBundle("jav/gui/actions/Bundle").getString("checking_quota"));

                CheckQuotaRequest req = new CheckQuotaRequest();
                CheckQuotaRequestType reqt = new CheckQuotaRequestType();
                reqt.setUserid(MainController.findInstance().getProfilerUserID());
                req.setCheckQuotaRequest(reqt);
                try {
                    CheckQuotaResponse resp = MainController.findInstance().getProfilerWebServiceStub().checkQuota(req);
                    CheckQuotaResponseType rst = resp.getCheckQuotaResponse();
                    if (rst.getReturncode() == 0) {
                        return rst.getQuota();
                    } else {
                        new CustomErrorDialog().showDialog(rst.getMessage());
                        return -1;
                    }
                } catch (RemoteException ex) {
                    new CustomErrorDialog().showDialog(ex.getMessage());
                    return -1;
                }
            } catch (Exception e) {
                return -1;
            }
        }
    }
}