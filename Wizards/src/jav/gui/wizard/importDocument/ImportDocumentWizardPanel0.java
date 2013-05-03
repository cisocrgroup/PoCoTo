package jav.gui.wizard.importDocument;

import jav.gui.dialogs.ExistsDialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

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
public class ImportDocumentWizardPanel0 implements WizardDescriptor.Panel<WizardDescriptor>, PropertyChangeListener {

    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0
    private ImportDocumentVisualPanel0 view = null;
    private WizardDescriptor model = null;
    private boolean isValid = false;
    private ResourceBundle bundle = NbBundle.getBundle(ImportDocumentWizardPanel0.class);
    private String completeProjectPath;

    @Override
    public ImportDocumentVisualPanel0 getComponent() {
        if (view == null) {
            view = new ImportDocumentVisualPanel0();
            view.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(0));
            view.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
            view.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
            view.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
        }
        return view;
    }

    public String getName() {
        return bundle.getString("Panel0.Name");
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    private boolean checkValidity(PropertyChangeEvent event) {
        if (!getComponent().getProjPath().equals("") && !getComponent().getProjName().equals("")) {
            String s = getComponent().getProjName();
            if (!s.startsWith(File.separator)) {
                s = File.separator + s;
            }
            File f = new File(getComponent().getProjPath() + s + ".ocrproject");
            if (f.exists()) {
                ExistsDialog d = new ExistsDialog();
                Object retval = d.showDialog();
                if (retval.equals(NotifyDescriptor.YES_OPTION)) {
                    setMessage(null);
                    getComponent().setInfoText(getComponent().getProjPath() + s + ".ocrproject");
                    getComponent().appendInfoText(getComponent().getProjPath() + s + ".h2.db");
                    getComponent().appendInfoText(getComponent().getProjPath() + s + ".trace.db");
                    completeProjectPath = getComponent().getProjPath() + s + ".ocrproject";
                    return true;
                } else {
                    getComponent().clearName();
                    getComponent().setInfoText("");
                    setMessage(bundle.getString("Panel0.Error0"));
                    return false;
                }
            } else {
                getComponent().setInfoText(getComponent().getProjPath() + s + ".ocrproject");
                getComponent().appendInfoText(getComponent().getProjPath() + s + ".h2.db");
                getComponent().appendInfoText(getComponent().getProjPath() + s + ".trace.db");
                setMessage(null);
                completeProjectPath = getComponent().getProjPath() + s + ".ocrproject";
                return true;
            }
        } else {
            getComponent().setInfoText("");
            setMessage(bundle.getString("Panel0.Error0"));
            return false;
        }
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    protected final void fireChangeEvent(Object source, boolean oldState, boolean newState) {
        if (oldState != newState) {
            Iterator<ChangeListener> it;
            synchronized (listeners) {
                it = new HashSet<ChangeListener>(listeners).iterator();
            }
            ChangeEvent ev = new ChangeEvent(source);
            while (it.hasNext()) {
                it.next().stateChanged(ev);
            }
        }
    }

    @Override
    public void readSettings(WizardDescriptor model) {
        this.model = model;
        getComponent().addPropertyChangeListener(this);
    }

    @Override
    public void storeSettings(WizardDescriptor model) {
        model.putProperty(ImportDocumentVisualPanel0.PROP_DIRNAME, getComponent().getProjPath());
        model.putProperty(ImportDocumentVisualPanel0.PROP_NAME, getComponent().getProjName());
        model.putProperty("CompleteProjectPath", completeProjectPath);
    }

    private void setMessage(String message) {
        model.putProperty("WizardPanel_errorMessage", message);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (!event.getPropertyName().equals("ancestor")) {
            boolean oldState = isValid;
            isValid = checkValidity(event);
            fireChangeEvent(this, oldState, isValid);
        }
    }
}
