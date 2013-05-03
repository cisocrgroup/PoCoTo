package jav.gui.actions;


import jav.gui.cookies.PageCookie;
import jav.gui.events.MessageCenter;
import jav.gui.events.tokenDeselection.TokenDeselectionEvent;
import jav.gui.events.tokenDeselection.TokenDeselectionEventSlot;
import jav.gui.main.MainController;
import java.awt.event.*;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.*;

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
 * textfield that holds an integer representing page numbers
 * when the page changes, the displayed number is refreshed
 * 
 * the user can edit the displayed number to trigger a page change
 * 
 */
public class SeitenFenster extends JTextField implements LookupListener, TokenDeselectionEventSlot {

    private SeitenFenster instance;
    private int display;
    private IntTextDocument doc;
    private Lookup context = null;
    private Lookup.Result<PageCookie> result = null;

    public SeitenFenster() {
        this(Utilities.actionsGlobalContext());
    }

    public SeitenFenster(Lookup lkp) {
        super();
        this.setColumns(3);
        doc = new IntTextDocument();
        this.setDocument(doc);
        this.setEnabled(false);
        instance = this;
        this.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                //instance.setText("");
                instance.selectAll();
                instance.setEditable(true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                instance.setText(display + "");
                instance.setEditable(false);
            }
        });
        MessageCenter.getInstance().addTokenDeselectionEventListener(this);

        this.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                String s;
                try {
                    s = doc.getText(0, doc.getLength());
                    int i = Integer.parseInt(s);

                    if (i > 0 && i <= MainController.findInstance().getDocument().getNumberOfPages()) {
                        actionP(result.allInstances().iterator().next(), i - 1);
                    } else {
                        NotifyDescriptor nd = new NotifyDescriptor("No such page", "Obacht", NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE, null, null);
                        DialogDisplayer.getDefault().notify(nd);
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }

            }
        });
        this.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    instance.setFocusable(false);
                    instance.setFocusable(true);
                    instance.setEditable(false);
                    instance.setText(display + "");
                }
            }
        });
        init(lkp);
    }

    private void init(Lookup lkp) {
        this.context = lkp;

        result = context.lookupResult(contextClass());
        result.addLookupListener(this);
        resultChanged(null);
    }

    public Class<PageCookie> contextClass() {
        return PageCookie.class;
    }

    public void actionP(PageCookie context, int i) {
        context.gotoPage(i);
    }

    public boolean enable(PageCookie context) {
        if (context.isReady() && context.getMaxPages() > 1) {
            this.setEnabled(true);
            display = context.getPageN();
            instance.setFocusable(false);
            instance.setFocusable(true);
            instance.setEditable(false);
            instance.setText(display + "");
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void resultChanged(LookupEvent le) {
        if (!result.allItems().isEmpty()) {
            setEnabled(enable(result.allInstances().iterator().next()));
        } else {
            setEnabled(false);
        }
    }

    @Override
    public void dispatchEvent(TokenDeselectionEvent e) {
        instance.setFocusable(false);
        instance.setFocusable(true);
    }
}
