package jav.gui.actions;

import jav.gui.cookies.ShowImagesCookie;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import org.openide.awt.Actions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.actions.BooleanStateAction;

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
public final class ToggleImages extends BooleanStateAction implements LookupListener {

    private JCheckBoxMenuItem menuButton;
    private Lookup context = null;
    private Lookup.Result<ShowImagesCookie> result = null;

    public ToggleImages() {
        this(Utilities.actionsGlobalContext());
    }

    public ToggleImages(Lookup lkp) {
        super();
        this.setEnabled(false);
        init(lkp);
    }

    private void init(Lookup lkp) {
        this.context = lkp;

        menuButton = new JCheckBoxMenuItem(java.util.ResourceBundle.getBundle("jav/gui/actions/Bundle").getString("CTL_ToggleImages"));
        Actions.connect(menuButton, this, true);
        setBooleanState(true);

        result = context.lookupResult(contextClass());
        result.addLookupListener(this);
        resultChanged(null);
    }

    public Class<ShowImagesCookie> contextClass() {
        return ShowImagesCookie.class;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        super.actionPerformed(event);
        boolean state = getBooleanState();

        actionP(result.allInstances().iterator().next(), state);
    }

    @Override
    public JMenuItem getMenuPresenter() {
        return menuButton;
    }

    @Override
    public String getName() {
        return java.util.ResourceBundle.getBundle("jav/gui/actions/Bundle").getString("CTL_ToggleImages"); //""; //menuButton.getText();
    }

    @Override
    public HelpCtx getHelpCtx() {
        HelpCtx bla = new HelpCtx("Go away!");
        return bla;
    }

    @Override
    public void resultChanged(LookupEvent le) {
        if (!result.allItems().isEmpty()) {
            setEnabled(enable(result.allInstances().iterator().next()));
        } else {
            setEnabled(false);
        }
    }

    public boolean enable(ShowImagesCookie context) {
        if( context.isReady() && context.hasImage()) {
            this.setBooleanState(context.getShowImages());
            return true;
        } else {
            return false;
        }
    }

//    @Override
//    public Action createContextAwareInstance(Lookup lkp) {
//        return new ToggleImages(lkp);
//    }

    public void actionP( ShowImagesCookie context, boolean b) {
        if(context.isReady()) {
            context.toggleImages(b);
        }
    }
}