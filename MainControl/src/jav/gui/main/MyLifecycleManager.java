package jav.gui.main;

import java.io.IOException;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

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
@ServiceProvider(service = LifecycleManager.class, position = 1)
public class MyLifecycleManager extends LifecycleManager {

    @Override
    public void saveAll() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void exit() {
        if (MainController.findInstance().hasUnsavedChanges()) {
            Object[] buttonText = {java.util.ResourceBundle.getBundle("jav/gui/dialogs/Bundle").getString("save"), java.util.ResourceBundle.getBundle("jav/gui/dialogs/Bundle").getString("discard"), java.util.ResourceBundle.getBundle("jav/gui/dialogs/Bundle").getString("cancel")};
            NotifyDescriptor d = new NotifyDescriptor(java.util.ResourceBundle.getBundle("jav/gui/dialogs/Bundle").getString("Unsaved"), "Obacht", NotifyDescriptor.YES_NO_CANCEL_OPTION, NotifyDescriptor.WARNING_MESSAGE, buttonText, null);
            Object retval = DialogDisplayer.getDefault().notify(d);
            if (retval.equals(java.util.ResourceBundle.getBundle("jav/gui/dialogs/Bundle").getString("save"))) {
                try {
                    MainController.findInstance().getSaver().save();
                    MainController.findInstance().shutDown();
                    quit();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else if (retval.equals(java.util.ResourceBundle.getBundle("jav/gui/dialogs/Bundle").getString("discard"))) {
                MainController.findInstance().undoAll();
                MainController.findInstance().shutDown();
                quit();
            }
        } else {
            MainController.findInstance().shutDown();
            quit();
        }
    }

    public void quit() {
        for (LifecycleManager manager : Lookup.getDefault().lookupAll(LifecycleManager.class)) {
            if (manager != this) {
                MainController.findInstance().addToLog("Programmende");
                manager.exit();
            }
        }
    }
}
