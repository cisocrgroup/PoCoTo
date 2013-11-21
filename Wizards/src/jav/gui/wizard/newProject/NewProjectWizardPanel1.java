package jav.gui.wizard.newProject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
public class NewProjectWizardPanel1 implements WizardDescriptor.Panel<WizardDescriptor>, PropertyChangeListener {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0
    private NewProjectVisualPanel1 view = null;
    private WizardDescriptor model = null;
    private boolean isValid = false;
    private ResourceBundle bundle = NbBundle.getBundle(NewProjectWizardPanel1.class);
    private JFileChooser fileChooser;

    public NewProjectWizardPanel1(JFileChooser jfc) {
        fileChooser = jfc;
    }
    
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public NewProjectVisualPanel1 getComponent() {
        if (view == null) {
            view = new NewProjectVisualPanel1(fileChooser);
            view.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(1));
            view.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
            view.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
            view.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
        }

        return view;
    }

    public String getName() {
        return bundle.getString("Panel1.Name");
    }

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    private boolean checkValidity( PropertyChangeEvent e ) {
        if (!getComponent().getXMLDirName().equals("") &! (getComponent().getInputType() == null) &! (getComponent().getEncoding() == null)) {
            setMessage(null);
            return true;
        } else {
            setMessage(bundle.getString("Panel1.Error1"));
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

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    @Override
    public void readSettings(WizardDescriptor model) {
        this.model = model;
        getComponent().addPropertyChangeListener(this);
    }

    @Override
    public void storeSettings(WizardDescriptor model) {
        model.putProperty(NewProjectVisualPanel1.PROP_XML_DIRNAME, getComponent().getXMLDirName());
        model.putProperty("InputType", getComponent().getInputType());
        model.putProperty("Encoding", getComponent().getEncoding());
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