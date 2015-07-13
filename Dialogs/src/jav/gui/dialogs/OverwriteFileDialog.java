package jav.gui.dialogs;

import jav.logging.log4j.Log;
import java.io.File;
import java.util.ResourceBundle;
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
public class OverwriteFileDialog {
    public enum Result {YES, NO, ALL};
    private final NotifyDescriptor descriptor;
    private final Object[] buttonText = {
        ResourceBundle.getBundle("jav/gui/dialogs/Bundle").getString("overwriteyes"), 
        ResourceBundle.getBundle("jav/gui/dialogs/Bundle").getString("overwriteno"), 
        ResourceBundle.getBundle("jav/gui/dialogs/Bundle").getString("overwriteall"),
    };

    public OverwriteFileDialog(File file) {
        String desc = String.format(
                ResourceBundle.getBundle("jav/gui/dialogs/Bundle").getString("overwrite"),
                file.getName()
        );
        descriptor = new NotifyDescriptor(
                desc, 
                "Obacht", NotifyDescriptor.YES_NO_CANCEL_OPTION, 
                NotifyDescriptor.WARNING_MESSAGE, buttonText, null);
    }

    public Result showDialogAndGetResult() {
        Object res = DialogDisplayer.getDefault().notify(descriptor);
        if (buttonText[0].equals(res))
            return Result.YES;
        else if (buttonText[1].equals(res))
            return Result.NO;
        else if (buttonText[2].equals(res))
            return Result.ALL;
        else
            return Result.NO;
    }
}
