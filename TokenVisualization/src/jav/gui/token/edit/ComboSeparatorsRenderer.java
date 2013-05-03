package jav.gui.token.edit;

/**
 * MySwing: Advanced Swing Utilites
 * Copyright (C) 2005  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

import javax.swing.*;
import java.awt.*;

/**
 * @author Santhosh Kumar T
 * @email santhosh.tekuri@gmail.com
 */
public abstract class ComboSeparatorsRenderer implements ListCellRenderer{
    private ListCellRenderer delegate;
    private JPanel separatorPanel = new JPanel(new BorderLayout());
    private JSeparator separator = new JSeparator();

    public ComboSeparatorsRenderer(ListCellRenderer delegate){
        this.delegate = delegate;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus){
        Component comp = delegate.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        
        if(index!=-1 && addSeparatorAfter(list, value, index)){ // index==1 if renderer is used to paint current value in combo
            separatorPanel.removeAll();
            separatorPanel.add(comp, BorderLayout.CENTER);
            separatorPanel.add(separator, BorderLayout.SOUTH);
            return separatorPanel;
        }else
            return comp;
    }

    protected abstract boolean addSeparatorAfter(JList list, Object value, int index);
}