package jav.gui.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.autoupdate.*;
import org.netbeans.api.autoupdate.InstallSupport.Validator;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

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
public class AutoUpdater implements Runnable {

    @Override
    public void run() {
        System.out.println("updater started");
        RequestProcessor.getDefault().post(new AutoUpdaterImpl(), 1000);
    }

    private static class AutoUpdaterImpl implements Runnable {

        private List<UpdateElement> install = new ArrayList<UpdateElement>();
        private List<UpdateElement> update = new ArrayList<UpdateElement>();
        private boolean isRestartRequested = false;

        @Override
        public void run() {            
            this.searchNewAndUpdatedModules();
            
            OperationContainer<InstallSupport> installContainer = this.addToContainer(OperationContainer.createForInstall(), install);
            this.installModules(installContainer);
            
            OperationContainer<InstallSupport> updateContainer = this.addToContainer(OperationContainer.createForUpdate(), update);
            this.installModules(updateContainer);
        }

        private void searchNewAndUpdatedModules() {

            for (UpdateUnitProvider provider : UpdateUnitProviderFactory.getDefault().getUpdateUnitProviders(false)) {
                try {
                    provider.refresh(null, true);
                } catch (IOException ex) {
                }
            }

            for (UpdateUnit unit : UpdateManager.getDefault().getUpdateUnits()) {
                if (!unit.getAvailableUpdates().isEmpty()) {
                    if (unit.getInstalled() == null) {
                        System.out.println("install " + unit.getCodeName());
                        install.add(unit.getAvailableUpdates().get(0));
                    } else {
                        System.out.println("update " + unit.getCodeName());                        
                        update.add(unit.getAvailableUpdates().get(0));
                    }
                }
            }
        }

        private OperationContainer<InstallSupport> addToContainer(OperationContainer<InstallSupport> c, List<UpdateElement> modules) {

            for (UpdateElement e : modules) {
                if (c.canBeAdded(e.getUpdateUnit(), e)) {
                    OperationInfo<InstallSupport> operationInfo = c.add(e);

                    if (operationInfo != null) {
                        c.add(operationInfo.getRequiredElements());
                    }
                }
            }
            return c;
        }

        private void installModules(OperationContainer<InstallSupport> container) {
            try {
                InstallSupport support = container.getSupport();
                if (support != null) {
                    Validator vali = support.doDownload(null, true, true);
                    InstallSupport.Installer inst = support.doValidate(vali, null);
                    Restarter restarter = support.doInstall(inst, null);
                    
                    if( restarter != null) {
                        support.doRestartLater(restarter);
                        if( !isRestartRequested) {
                            NotificationDisplayer.getDefault().notify(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("actualize"), null, java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("restart"), new RestartAction(support,restarter));
                            isRestartRequested = true;
                        }
                    }
                }
            } catch (OperationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    private static final class RestartAction implements ActionListener {
        private InstallSupport support;
        private OperationSupport.Restarter restarter;
        
        public RestartAction( InstallSupport sup, OperationSupport.Restarter rest) {
            this.support = sup;
            this.restarter = rest;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                support.doRestart(restarter, null);
            } catch (OperationException ex) {
                Exceptions.printStackTrace(ex);
            }
            
        }
    }
}