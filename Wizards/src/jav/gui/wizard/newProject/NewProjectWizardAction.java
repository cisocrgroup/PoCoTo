package jav.gui.wizard.newProject;

import jav.correctionBackend.FileType;
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
 */
public final class NewProjectWizardAction extends ContextAction<CorrectionSystemReadyCookie> {

    public NewProjectWizardAction() {
        this(Utilities.actionsGlobalContext());
    }

    public NewProjectWizardAction(Lookup context) {
        super(context);
        putValue(NAME, NbBundle.getMessage(NewProjectWizardAction.class, "CTL_NewProjectWizardAction"));
        putValue("iconBase", "jav/gui/wizard/newProject/newProject.png");
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
        String xmlDirName = null;
        String imgDirName = null;
        String propertiespath = null;
        String configuration = null;
        FileType inputType = null;
        String encoding = null;

        NewProjectWizardDescriptor descriptor = new NewProjectWizardDescriptor();
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        dialog.toFront();

        if (descriptor.getValue() == WizardDescriptor.FINISH_OPTION) {
            Map<String, Object> props = descriptor.getProperties();
            if (props.containsKey(NewProjectVisualPanel1.PROP_XML_DIRNAME)) {
                xmlDirName = (String) props.get(NewProjectVisualPanel1.PROP_XML_DIRNAME);
            }
            if (props.containsKey(NewProjectVisualPanel2.PROP_IMG_DIRNAME)) {
                imgDirName = (String) props.get(NewProjectVisualPanel2.PROP_IMG_DIRNAME);
            }
            if (props.containsKey("CompleteProjectPath")) {
                propertiespath = (String) props.get("CompleteProjectPath");
            }
            if (props.containsKey("InputType")) {
                //inputType = FileType.fromString((String) props.get("InputType"));
                inputType = (FileType) props.get("InputType");
            }
            if (props.containsKey(NewProjectVisualPanel0.PROP_DIRNAME)) {
                projectpath = (String) props.get(NewProjectVisualPanel0.PROP_DIRNAME);
            }
            if (props.containsKey(NewProjectVisualPanel0.PROP_NAME)) {
                projectname = (String) props.get(NewProjectVisualPanel0.PROP_NAME);
            }
            if (props.containsKey("Configuration")) {
                configuration = (String) props.get("Configuration");
                if (configuration.equals("")) {
                    configuration = null;
                }
            }
            if (props.containsKey("Encoding")) {
                encoding = (String) props.get("Encoding");
            }

            // document with images and without simple analysis
            if (imgDirName != null && xmlDirName != null && projectpath != null && configuration == null) {
                MainController.findInstance().createDocumentNoAnalysis(xmlDirName, imgDirName, inputType, encoding, propertiespath, projectpath, projectname);

                // document without images but without simple analysis
            } else if (imgDirName == null && xmlDirName != null && projectpath != null && configuration == null) {
                MainController.findInstance().createDocumentNoAnalysis(xmlDirName, inputType, encoding, propertiespath, projectpath, projectname);

                // document with images but with simple analysis
            } else if (imgDirName != null && xmlDirName != null && projectpath != null && configuration != null) {
                MainController.findInstance().createDocumentAnalysis(xmlDirName, imgDirName, inputType, encoding, propertiespath, projectpath, projectname, configuration);

                // document without images and with simple analysis
            } else if (imgDirName == null && xmlDirName != null && projectpath != null && configuration != null) {
                MainController.findInstance().createDocumentAnalysis(xmlDirName, inputType, encoding, propertiespath, projectpath, projectname, configuration);

            } else {
                new CustomErrorDialog().showDialog("NewProjectWizardAction:: impossible Selection");
            }
        }
    }

    @Override
    public boolean enable(CorrectionSystemReadyCookie context) {
        return true;
    }

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        return new NewProjectWizardAction(lkp);
    }
}
