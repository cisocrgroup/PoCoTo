package jav.gui.token.edit;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.LinkedHashSet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;

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
public class MyEditCustomComboBox extends JComboBox {

    private boolean layingOut = false;
    private String type;
    private int widestLengh = 0;
    private boolean wide = false;

    public MyEditCustomComboBox(LinkedHashSet<ComboBoxEntry> entries, Font f, Dimension d, final long numofcands, JButton b) {
        super(entries.toArray());
//        Object[] objectentries = ;
//        for (Object o : objectentries) {
//            this.addItem(o);
//        }
        
        MyComboBoxUI mcui = new MyComboBoxUI();
//        LargerComboBoxUI lcui = new LargerComboBoxUI();
        MyEditCustomComboBoxEditor ed = new MyEditCustomComboBoxEditor(f, d, b);

//        this.setSize(new Dimension(d.width+b.getSize().width,d.height));
        this.setPreferredSize(new Dimension(d.width+b.getSize().width+80,d.height));
        this.setFont(f);
        this.setEditable(true);
        this.setUI(mcui);
        this.setEditor(ed);
        this.setRenderer(new ComboSeparatorsRenderer(this.getRenderer()) {

            @Override
            protected boolean addSeparatorAfter(JList list, Object value, int index) {
                return index == 0 || index == numofcands;
            }
        });
    }

//    @Override
//    public void processKeyEvent(KeyEvent e) {
//        System.out.println(e.getKeyCode());
//        if( e.getKeyCode() != KeyEvent.VK_TAB) {
//            super.processKeyEvent(e);
//        }
//    }
    
    public boolean isWide() {
        return wide;
    }
    
    //Setting the JComboBox wide
    public void setWide(boolean wide) {
        this.wide = wide;
        widestLengh = getWidestItemWidth();

    }

    @Override
    public Dimension getSize() {
        Dimension dim = super.getSize();
        if (!layingOut && isWide()) {
            dim.width = Math.max(widestLengh, dim.width);
        }
        return dim;
    }

    public int getWidestItemWidth() {

        int numOfItems = this.getItemCount();
        Font font = this.getFont();
        FontMetrics metrics = this.getFontMetrics(font);
        int widest = 0;
        for (int i = 0; i < numOfItems; i++) {
            Object item = this.getItemAt(i);
            int lineWidth = metrics.stringWidth(item.toString());
            widest = Math.max(widest, lineWidth);
        }

        return widest + 5;
    }

    @Override
    public void doLayout() {
        try {
            layingOut = true;
            super.doLayout();
        } finally {
            layingOut = false;
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String t) {
        type = t;
    }
}