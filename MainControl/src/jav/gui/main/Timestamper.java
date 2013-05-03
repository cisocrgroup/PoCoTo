package jav.gui.main;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Exceptions;
import org.openide.windows.IOProvider;

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
 * class for tool evaluation by measuring corrections over points of time
 * @deprecated 
 */
// TODO change implementation to db format
@Deprecated
public class Timestamper {

    public Timestamper() {
    }

    public void saveTimeStamp() throws IOException {

        SwingWorker<String, Object> worker = new SwingWorker<String, Object>() {

            ProgressHandle p = ProgressHandleFactory.createHandle("Saving");

            @Override
            protected String doInBackground() {

                String retval = null;
                try {
                    p.start();
                    p.progress("Saving");
                    p.setDisplayName("Saving");

                    retval = MainController.findInstance().getCorrectionSystem().getDocument().getProjectFilename().replace(".ocrczip", "__" + DateUtils.now("yyyy_MM_dd_kk_mm") + ".ocrczip");
                    MainController.findInstance().getSaver().saveAs(retval, false);

                } catch (Exception e) {
                } catch (UnsatisfiedLinkError e) {
                    retval = null;
                    p.finish();
                }

                return retval;
            }

            @Override
            protected void done() {
                try {
                    String retval = get();
                    if (retval != null) {
//                        IOProvider.getDefault().getIO("Nachrichten", false).getOut().println("Save complete " + retval);
                        p.finish();
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (CancellationException ex) {
                }
            }
        };
        worker.execute();
    }
}
