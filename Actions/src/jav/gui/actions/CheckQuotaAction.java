package jav.gui.actions;

import cis.profiler.client.ProfilerWebServiceStub;
import cis.profiler.client.ProfilerWebServiceStub.CheckQuotaRequest;
import cis.profiler.client.ProfilerWebServiceStub.CheckQuotaRequestType;
import cis.profiler.client.ProfilerWebServiceStub.CheckQuotaResponse;
import cis.profiler.client.ProfilerWebServiceStub.CheckQuotaResponseType;
import jav.gui.cookies.ProfilerIDCookie;
import jav.gui.dialogs.CustomErrorDialog;
import jav.gui.dialogs.CustomInformationDialog;
import jav.gui.main.MainController;
import jav.logging.log4j.Log;
import java.rmi.RemoteException;
import javax.swing.Action;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressRunnable;
import org.netbeans.api.progress.ProgressUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
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
id = "jav.gui.wizard.profiler.CheckQuota")
@ActionRegistration(displayName = "#CTL_CheckQuota")
@ActionReferences({
    @ActionReference(path = "Menu/Profiler", position = 1008)
})
public class CheckQuotaAction extends ContextAction<ProfilerIDCookie> {

    public CheckQuotaAction() {
        this(Utilities.actionsGlobalContext());
    }

    public CheckQuotaAction(Lookup context) {
        super(context);
        putValue(NAME, java.util.ResourceBundle.getBundle("jav/gui/actions/Bundle").getString("CTL_CheckQuota"));
    }

    @Override
    public Class<ProfilerIDCookie> contextClass() {
        return ProfilerIDCookie.class;
    }

    @Override
    public void performAction(ProfilerIDCookie context) {
        ProgressRunnable<Integer> r = new QuotaChecker();
        int retval = ProgressUtils.showProgressDialogAndRun(r, java.util.ResourceBundle.getBundle("jav/gui/actions/Bundle").getString("checking_quota"), true);
        if( retval != -1) {
            new CustomInformationDialog().showDialog(java.util.ResourceBundle.getBundle("jav/gui/actions/Bundle").getString("quota_left") + " " + retval);
        }
    }

    @Override
    public boolean enable(ProfilerIDCookie context) {
        return true;
    }

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        return new CheckQuotaAction();
    }

    private class QuotaChecker implements ProgressRunnable<Integer> {

        public QuotaChecker() {
        }

        @Override
        public Integer run(ProgressHandle ph) {
            Log.info(
                    this, 
                    "checkQuota '%s' '%s'", 
                    MainController.findInstance().getProfilerServiceUrl(), 
                    MainController.findInstance().getProfilerUserId()
            );
            try {
                ph.progress(java.util.ResourceBundle.getBundle("jav/gui/actions/Bundle").getString("checking_quota"));
                ph.setDisplayName(java.util.ResourceBundle.getBundle("jav/gui/actions/Bundle").getString("checking_quota"));

                CheckQuotaRequest req = new CheckQuotaRequest();
                CheckQuotaRequestType reqt = new CheckQuotaRequestType();
                reqt.setUserid(MainController.findInstance().getProfilerUserId());
                req.setCheckQuotaRequest(reqt);
                try {
                    ProfilerWebServiceStub stub = 
                            MainController.findInstance().getProfilerWebServiceStub();
                    CheckQuotaResponse resp = stub.checkQuota(req);
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