package jav.gui.main;

import jav.gui.dialogs.UnsavedChangesDialog;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.Exceptions;

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
 * @source http://wiki.netbeans.org/AddingMRUList
 */
public class MRUFilesMenu extends JMenu implements DynamicMenuContent {

    private static MRUFilesMenu instance;

    static {
        instance = new MRUFilesMenu();
        instance.updateMenu();
        instance.setenabled(false);
    }

    public static MRUFilesMenu findInstance() {
        return instance;
    }

    public MRUFilesMenu() {
        super(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("CTL_MRUFiles"));

        MRUFilesOptions opts = MRUFilesOptions.getInstance();
        opts.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                if (!evt.getPropertyName().equals(MRUFilesOptions.MRU_FILE_LIST_PROPERTY)) {
                    return;
                }
                updateMenu();
            }
        });
    }

    private void updateMenu() {
        removeAll();
        MRUFilesOptions opts = MRUFilesOptions.getInstance();
        List<String> list = opts.getMRUFileList();
        for (String name : list) {
            Action action = createAction(name);
            action.putValue(Action.NAME, name);
            JMenuItem menuItem = new JMenuItem(action);
            MRUFilesMenu.findInstance().add(menuItem);
        }
    }

    private Action createAction(String actionCommand) {
        Action action = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                menuItemActionPerformed(e);
            }
        };

        action.putValue(Action.ACTION_COMMAND_KEY, actionCommand);
        return action;
    }

    private void menuItemActionPerformed(ActionEvent evt) {
        final String command = evt.getActionCommand();

        if (MainController.findInstance().hasUnsavedChanges()) {
            Object retval = new UnsavedChangesDialog().showDialog();

            // save
            if (retval.equals(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("save"))) {
                try {
                    MainController.findInstance().getSaver().save();
                    MainController.findInstance().loadDocument(command);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                // discard
            } else if (retval.equals(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("discard"))) {
                MainController.findInstance().loadDocument(command);
            }
            // no unsaved changes
        } else {
            MainController.findInstance().loadDocument(command);
        }
    }

    public void setenabled(boolean b) {
        instance.setEnabled(b);
    }

    @Override
    public JComponent[] getMenuPresenters() {
        return new JComponent[]{instance};
    }

    @Override
    public JComponent[] synchMenuPresenters(JComponent[] jcs) {
        return getMenuPresenters();
    }
}
