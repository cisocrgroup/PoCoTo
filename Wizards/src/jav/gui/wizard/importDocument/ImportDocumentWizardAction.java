package jav.gui.wizard.importDocument;

import jav.gui.actions.ContextAction;
import jav.gui.cookies.CorrectionSystemReadyCookie;
import jav.gui.dialogs.CustomErrorDialog;
import jav.gui.dialogs.UnsavedChangesDialog;
import jav.gui.main.MainController;
import java.awt.Dialog;
import java.io.IOException;
import java.util.Map;
import javax.swing.Action;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
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
public final class ImportDocumentWizardAction extends ContextAction<CorrectionSystemReadyCookie> {

    public ImportDocumentWizardAction() {
        this(Utilities.actionsGlobalContext());
    }

    public ImportDocumentWizardAction(Lookup context) {
        super(context);
        putValue(NAME, NbBundle.getMessage(ImportDocumentWizardAction.class, "CTL_ImportDocumentWizardAction"));
        putValue("iconBase", "jav/gui/wizard/importDocument/newProject.png");
    }

    @Override
    public Class<CorrectionSystemReadyCookie> contextClass() {
        return CorrectionSystemReadyCookie.class;
    }

    @Override
    public void performAction(CorrectionSystemReadyCookie context) {
        if (MainController.findInstance().hasUnsavedChanges()) {
            Object retval = new UnsavedChangesDialog().showDialog();
            if (retval.equals(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("save"))) {
                try {
                    MainController.findInstance().getSaver().save();
                    showWizard();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else if (retval.equals(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("discard"))) {
                showWizard();
            }
        } else {
            showWizard();
        }
    }

    private void showWizard() {
        String projectpath = null;
        String projectname = null;
        String ocrcxmlFilename = null;
        String imgDirName = null;
        String propertiespath = null;
        String profilename = null;

        ImportDocumentWizardDescriptor descriptor = new ImportDocumentWizardDescriptor();
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        dialog.toFront();

        if (descriptor.getValue() == WizardDescriptor.FINISH_OPTION) {
            Map<String, Object> props = descriptor.getProperties();
            if (props.containsKey(ImportDocumentVisualPanel1.PROP_XML_DIRNAME)) {
                ocrcxmlFilename = (String) props.get(ImportDocumentVisualPanel1.PROP_XML_DIRNAME);
            }
            if (props.containsKey(ImportDocumentVisualPanel2.PROP_IMG_DIRNAME)) {
                imgDirName = (String) props.get(ImportDocumentVisualPanel2.PROP_IMG_DIRNAME);
            }
            if (props.containsKey("CompleteProjectPath")) {
                propertiespath = (String) props.get("CompleteProjectPath");
            }
            if (props.containsKey(ImportDocumentVisualPanel0.PROP_DIRNAME)) {
                projectpath = (String) props.get(ImportDocumentVisualPanel0.PROP_DIRNAME);
            }
            if (props.containsKey(ImportDocumentVisualPanel0.PROP_NAME)) {
                projectname = (String) props.get(ImportDocumentVisualPanel0.PROP_NAME);
            }
            if (props.containsKey(ImportDocumentVisualPanel3.PROP_PROFILE_NAME)) {
                profilename = (String) props.get(ImportDocumentVisualPanel3.PROP_PROFILE_NAME);
            }
            
            if( ocrcxmlFilename != null ) {
                MainController.findInstance().importOCRCXMLasNewProject(ocrcxmlFilename, imgDirName, propertiespath, projectpath, projectname, profilename);
            } else {
                new CustomErrorDialog().showDialog("ImportDocumentWizardAction::EmptyFilename");
            }
        }
    }

    @Override
    public boolean enable(CorrectionSystemReadyCookie context) {
        return true;
    }

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        return new ImportDocumentWizardAction(lkp);
    }
}
