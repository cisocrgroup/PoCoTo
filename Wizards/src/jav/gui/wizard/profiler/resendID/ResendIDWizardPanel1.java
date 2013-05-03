package jav.gui.wizard.profiler.resendID;

import cis.profiler.client.ProfilerWebServiceStub.ValidateEmailRequest;
import cis.profiler.client.ProfilerWebServiceStub.ValidateEmailRequestType;
import cis.profiler.client.ProfilerWebServiceStub.ValidateEmailResponse;
import cis.profiler.client.ProfilerWebServiceStub.ValidateEmailResponseType;
import jav.gui.main.MainController;
import java.rmi.RemoteException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

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
public class ResendIDWizardPanel1 implements WizardDescriptor.ValidatingPanel<WizardDescriptor>, ChangeListener {

    private ResendIDVisualPanel1 component;
    private WizardDescriptor model;
    private ChangeSupport changeSupport = new ChangeSupport(this);

    @Override
    public ResendIDVisualPanel1 getComponent() {
        if (component == null) {
            component = new ResendIDVisualPanel1(this);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isValid() {
        if( getComponent().getEmail().equals("") || !getComponent().getEmail().matches(".*@.*")) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    @Override
    public void readSettings(WizardDescriptor data) {
        this.model = data;        
    }

    @Override
    public void storeSettings(WizardDescriptor data) {
        model.putProperty("Email", getComponent().getEmail());
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }

    @Override
    public void validate() throws WizardValidationException {
         ValidateEmailRequest vrq = new ValidateEmailRequest();
         ValidateEmailRequestType vrqt = new ValidateEmailRequestType();
         vrqt.setEmail(getComponent().getEmail());
         vrq.setValidateEmailRequest(vrqt);
        try {
            ValidateEmailResponse vrs = MainController.findInstance().getProfilerWebServiceStub().validateEmail(vrq);
            ValidateEmailResponseType vrst = vrs.getValidateEmailResponse();
            /*
             * if there is no such email registered in the database, throw exception
             * (valid means that an email has not been registered yet)
             */
            if (vrst.getReturncode() == 0 && vrst.getIsvalid()) {
                component.setMessage(java.util.ResourceBundle.getBundle("jav/gui/wizard/profiler/resendID/Bundle").getString("no_such_email"));
                throw new WizardValidationException(component, "", "");
            } else if(vrst.getReturncode() != 0){
                component.setMessage(vrst.getMessage());
                throw new WizardValidationException(component, "", "");        
            }        } catch (RemoteException ex) {
            component.setMessage(ex.getMessage());
            throw new WizardValidationException(component, "", "");
        }
    }
}