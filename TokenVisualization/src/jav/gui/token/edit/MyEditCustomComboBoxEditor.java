package jav.gui.token.edit;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicComboBoxEditor;

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
public class MyEditCustomComboBoxEditor implements ComboBoxEditor {

    protected JPanel panel;
    protected JButton button;
    protected JTextField editor;

    public MyEditCustomComboBoxEditor(Font f, Dimension d, JButton b) {
        this.button = b;
        createEditorComponent(f, d);
    }

    private JPanel createEditorComponent(Font f, Dimension d) {
        panel = new JPanel();
        panel.setBackground(Color.white);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
//        panel.setLayout(new BorderLayout());
//        panel.setPreferredSize(new Dimension(d.width+button.getSize().width,d.height));
        editor = new BorderlessTextField("", 9);
        editor.setFont(f);
        editor.setSize(d);
        editor.setPreferredSize(d);
//        editor.setMaximumSize(d);
        button.setMaximumSize(button.getPreferredSize());
        panel.add(editor);//, BorderLayout.LINE_START);
        panel.add(button);//, BorderLayout.LINE_END);
        panel.setBorder(null);
//        panel.setFocusable(false);
        return panel;
    }

    @Override
    public Component getEditorComponent() {
        return panel;
    }

    @Override
    public void setItem(Object o) {
        if( o instanceof ComboBoxEntry) {
            ComboBoxEntry e = (ComboBoxEntry) o;
            if( e.getType() == ComboBoxEntryType.NORMAL ) {
                editor.setText(e.toString());
                editor.setEditable(true);
                editor.selectAll();
            } else {
                editor.setEditable(false);
                editor.setText("");
            }
        } else {
            editor.setText(o.toString());
        }
    }

    @Override
    public Object getItem() {
        return editor.getText();
        //                Object newValue = editor.getText();
//
//                if (oldValue != null && !(oldValue instanceof  String)) {
//                    // The original value is not a string. Should return the value in it's
//                    // original type.
//                    if (newValue.equals(oldValue.toString())) {
//                        return oldValue;
//                    } else {
//                        // Must take the value from the editor and get the value and cast it to the new type.
//                        Class cls = oldValue.getClass();
//                        try {
//                            Method method = cls.getMethod("valueOf",
//                                    new Class[] { String.class });
//                            newValue = method.invoke(oldValue,
//                                    new Object[] { editor.getText() });
//                        } catch (Exception ex) {
//                            // Fail silently and return the newValue (a String object)
//                        }
//                    }
//                }
//                return newValue;
    }

    @Override
    public void selectAll() {
        editor.selectAll();
//        editor.requestFocus();
    }

    @Override
    public void addActionListener(ActionListener al) {
        editor.addActionListener(al);
    }

    @Override
    public void removeActionListener(ActionListener al) {
        editor.removeActionListener(al);
    }

    static class BorderlessTextField extends JTextField {
        public BorderlessTextField(String value, int n) {
            super(value, n);
        }

        // workaround for 4530952
        @Override
        public void setText(String s) {
            if (getText().equals(s)) {
                return;
            }
            super.setText(s);
        }

        @Override
        public void setBorder(Border b) {
            if (!(b instanceof UIResource)) {
                super.setBorder(b);
            }
        }
    }

    public static class UIResource extends BasicComboBoxEditor implements javax.swing.plaf.UIResource {
    }
}