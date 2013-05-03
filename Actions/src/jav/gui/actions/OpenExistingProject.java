package jav.gui.actions;

import jav.gui.cookies.CorrectionSystemReadyCookie;
import jav.gui.dialogs.UnsavedChangesDialog;
import jav.gui.main.MainController;
import java.io.File;
import java.io.IOException;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
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
public class OpenExistingProject extends ContextAction<CorrectionSystemReadyCookie> {

    public OpenExistingProject() {
        this(Utilities.actionsGlobalContext());
    }

    public OpenExistingProject(Lookup context) {
        super(context);
        putValue(NAME, java.util.ResourceBundle.getBundle("jav/gui/actions/Bundle").getString("openproject"));
        putValue("iconBase", "jav/gui/actions/openProject.png");
    }


    @Override
    public Class<CorrectionSystemReadyCookie> contextClass() {
        return CorrectionSystemReadyCookie.class;
    }

    @Override
    public void performAction(CorrectionSystemReadyCookie context) {
        if(MainController.findInstance().hasUnsavedChanges()) {
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

    private void showDialog() {
        JFileChooser jfc = new JFileChooser();
//        AbstractButton button = SwingUtils.getDescendantOfType(AbstractButton.class, jfc, "Icon", UIManager.getIcon("FileChooser.detailsViewIcon"));
//        button.doClick();

        jfc.setDialogTitle(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("ocrproj_file"));
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".ocrproject") || f.isDirectory();
            }

            @Override 
            public String getDescription() {
                return java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("ocrproj_file");
            }
        });

        if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = jfc.getSelectedFile();
                String doc = file.getCanonicalPath();
                MainController.findInstance().loadDocument(doc);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public boolean enable(CorrectionSystemReadyCookie context) {
        return true;
    }

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        return new OpenExistingProject(lkp);
    }

}