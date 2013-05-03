package jav.gui.actions;

import cis.profiler.client.ProfilerWebServiceStub.GetTransactionsRequest;
import cis.profiler.client.ProfilerWebServiceStub.GetTransactionsRequestType;
import cis.profiler.client.ProfilerWebServiceStub.GetTransactionsResponse;
import cis.profiler.client.ProfilerWebServiceStub.GetTransactionsResponseType;
import jav.gui.cookies.ProfilerIDCookie;
import jav.gui.dialogs.CustomErrorDialog;
import jav.gui.main.MainController;
import java.awt.BorderLayout;
import java.rmi.RemoteException;
import java.util.Vector;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressRunnable;
import org.netbeans.api.progress.ProgressUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
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
id = "jav.gui.wizard.profiler.GetTransactions")
@ActionRegistration(displayName = "#CTL_GetTransactions")
@ActionReferences({
    @ActionReference(path = "Menu/Profiler", position = 1009)
})
public class GetTransactionsAction extends ContextAction<ProfilerIDCookie> {

    public GetTransactionsAction() {
        this(Utilities.actionsGlobalContext());
    }

    public GetTransactionsAction(Lookup context) {
        super(context);
        putValue(NAME, java.util.ResourceBundle.getBundle("jav/gui/actions/Bundle").getString("CTL_GetTransactions"));
    }

    @Override
    public Class<ProfilerIDCookie> contextClass() {
        return ProfilerIDCookie.class;
    }

    @Override
    public void performAction(ProfilerIDCookie context) {
        ProgressRunnable<String[]> r = new TransactionsGetter();
        String[] transactions = ProgressUtils.showProgressDialogAndRun(r, java.util.ResourceBundle.getBundle("jav/gui/actions/Bundle").getString("getting_transactions"), true);

        JPanel panel = new JPanel();
        panel.setSize(200, 200);
        panel.setLayout(new BorderLayout());

        if (transactions != null) {
            if (transactions[0].equals("empty")) {
                JLabel label = new JLabel(java.util.ResourceBundle.getBundle("jav/gui/actions/Bundle").getString("no_transactions"));
                panel.add(label, BorderLayout.CENTER);
            } else {
                Vector<Vector> data = new Vector<Vector>();
                Vector<String> headers = new Vector<String>();

                headers.add(("date"));
                headers.add("ip");
                headers.add("docinfo");
                headers.add("profilerinfo");

                for (int i = 0; i < transactions.length; i++) {
                    Vector<String> row = new Vector<String>();
                    String raw = transactions[i];
                    String[] temp = raw.split("#");
                    for (int j = 0; j < temp.length; j++) {
                        row.add(temp[j]);
                    }
                    data.add(row);
                }

                JTable table = new JTable(data, headers) {

                    @Override
                    public boolean isCellEditable(int rowIndex, int vColIndex) {
                        return false;
                    }
                };

                JScrollPane tableContainer = new JScrollPane(table);

                panel.add(tableContainer, BorderLayout.CENTER);
            }
            Object[] options = {DialogDescriptor.OK_OPTION};

            DialogDescriptor d = new DialogDescriptor(panel, java.util.ResourceBundle.getBundle("jav/gui/actions/Bundle").getString("transactions"), true, options, DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, null, null);
            DialogDisplayer.getDefault().notify(d);
        }
    }

    @Override
    public boolean enable(ProfilerIDCookie context) {
        return true;
    }

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        return new GetTransactionsAction();
    }

    private class TransactionsGetter implements ProgressRunnable<String[]> {

        @Override
        public String[] run(ProgressHandle ph) {
            try {
                ph.progress(java.util.ResourceBundle.getBundle("jav/gui/actions/Bundle").getString("getting_transactions"));
                ph.setDisplayName(java.util.ResourceBundle.getBundle("jav/gui/actions/Bundle").getString("getting_transactions"));

                GetTransactionsRequest grq = new GetTransactionsRequest();
                GetTransactionsRequestType grqt = new GetTransactionsRequestType();

                grqt.setUserid(MainController.findInstance().getProfilerUserID());
                grq.setGetTransactionsRequest(grqt);

                try {
                    GetTransactionsResponse resp = MainController.findInstance().getProfilerWebServiceStub().getTransactions(grq);
                    GetTransactionsResponseType rst = resp.getGetTransactionsResponse();
                    if (rst.getReturncode() == 0) {
                        if (rst.getMessage().equals("empty")) {
                            String[] retv = new String[]{"empty"};
                            return retv;
                        } else {
                            return rst.getTransactions();
                        }
                    } else {
                        new CustomErrorDialog().showDialog(rst.getMessage());
                        return null;
                    }
                } catch (RemoteException ex) {
                    new CustomErrorDialog().showDialog(ex.getMessage());
                    return null;
                }
            } catch (Exception e) {
                new CustomErrorDialog().showDialog(e.getMessage());
                return null;
            }
        }
    }
}