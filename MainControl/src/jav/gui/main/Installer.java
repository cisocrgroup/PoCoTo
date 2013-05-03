package jav.gui.main;

import java.awt.EventQueue;
import javax.swing.JFrame;
import org.openide.modules.ModuleInstall;
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
 * 
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {

    @Override
    public void restored() {
        MainController.findInstance();
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {

            @Override
            public void run() {
                EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        MainController.findInstance().addToLog("Programmstart");
                        JFrame f = (JFrame) WindowManager.getDefault().getMainWindow();
                        f.setTitle("");
//                        MainController.findInstance().getCorrectionSystem().getProjectFilename();
//                        MainController.findInstance().initCorrectionSystem();
                    }
                });
            }
        }); 
    }

//    @Override
//    public boolean closing() {
//       if(MainController.findInstance().hasUnsavedChanges()) {
//            Object retval = new UnsavedChangesDialog().showDialog();
//            if (retval.equals(java.util.ResourceBundle.getBundle("jav/gui/dialogs/Bundle").getString("save"))) {
//                try {
//                    MainController.findInstance().getSaver().save();
//                    return true;
//                } catch (IOException ex) {
//                    return false;
//                }
//            } else if (retval.equals(java.util.ResourceBundle.getBundle("jav/gui/dialogs/Bundle").getString("discard"))) {
//                return true;
//            } else {
//                return false;
//            }
//        } else {
//            return true;
//        }
//    }
//    
//    @Override
//    public void close() {
//        IOProvider.getDefault().getIO("Nachrichten", false).getOut().println("Pfüati");
//    }
}
