package jav.gui.token.edit;

import jav.gui.token.display.TokenVisualization;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;

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
public class MyComboBoxUI extends BasicComboBoxUI {

    @Override
    protected BasicComboPopup createPopup() {
        return new MyComboBoxUI.LargerComboPopup(comboBox);
    }

    /*
     * disable arrowbutton
     */
    @Override
    protected JButton createArrowButton() {
        return null;
    }

    @Override
    protected void installComponents() {
        arrowButton = createArrowButton();

        if (arrowButton != null) {
            comboBox.add(arrowButton);
            configureArrowButton();
        }
        if (comboBox.isEditable()) {
            addEditor();
        }
        comboBox.add(currentValuePane);
    }

    /**
     * The minumum size is the size of the display area plus insets plus the
     * button.
     */
    @Override
    public Dimension getMinimumSize(JComponent c) {
        if (!isMinimumSizeDirty) {
            return new Dimension(cachedMinimumSize);
        }
        Dimension size = getDisplaySize();
        Insets insets = getInsets();
        size.height += insets.top + insets.bottom;
        size.width += insets.left + insets.right;

        cachedMinimumSize.setSize(size.width, size.height);
        isMinimumSizeDirty = false;

        return new Dimension(size);
    }

    @Override
    protected Rectangle rectangleForCurrentValue() {
        int width = comboBox.getWidth();
        int height = comboBox.getHeight();
        Insets insets = getInsets();
        return new Rectangle(insets.left, insets.top, width - (insets.left + insets.right), height - (insets.top + insets.bottom));
    }

    /*
     * replace the standard action map to disable popup closing
     */
    @Override
    public void installKeyboardActions() {
        InputMap im = new InputMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "selectNext");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "selectPrevious");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enterPressed");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), "pageUpPassThrough");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), "pageDownPassThrough");
//        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "spacePopup");
//        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "hidePopup");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0), "selectNext");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0), "selectPrevious");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), "homePassThrough");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0), "endPassThrough");
        SwingUtilities.replaceUIInputMap(comboBox, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, im);

        ActionMap am = new ActionMap();
        am.put("selectNext", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                selectNextPossibleValue();
            }
        });
        am.put("selectPrevious", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                selectPreviousPossibleValue();
            }
        });
        am.put("enterPressed", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ae) {
            }
        });
        am.put("pageUpPassThrough", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                int listHeight = comboBox.getMaximumRowCount();
                int index = comboBox.getSelectedIndex() - listHeight;
                if (index < 0) {
                    index = 0;
                }
                if (index >= 0 && index < comboBox.getItemCount()) {
                    comboBox.setSelectedIndex(index);
                }
            }
        });
        am.put("pageDownPassThrough", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                int listHeight = comboBox.getMaximumRowCount();
                int index = comboBox.getSelectedIndex() + listHeight;
                int max = comboBox.getItemCount();
                if (index >= max) {
                    index = max - 1;
                }
                comboBox.setSelectedIndex(index);
            }
        });
        am.put("homePassThrough", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                comboBox.setSelectedIndex(0);
            }
        });
        am.put("endPassThrough", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                comboBox.setSelectedIndex(comboBox.getItemCount() - 1);
            }
        });
        SwingUtilities.replaceUIActionMap(comboBox, am);
    }

    public class LargerComboPopup extends BasicComboPopup {

        public LargerComboPopup(JComboBox comboBox) {
            super(comboBox);
        }
        
        /**
         * This method creates ListMouseListener to listen to mouse events
         * occuring in the combo box's item list.
         *
         * @return MouseListener to listen to mouse events occuring in the combo
         * box's items list.
         */
        @Override
        protected MouseListener createListMouseListener() {
            return new MyComboBoxUI.LargerComboPopup.ListMouseHandler();
        }

        protected class ListMouseHandler extends MouseAdapter {

            protected ListMouseHandler() {
            }

            @Override
            public void mousePressed(MouseEvent anEvent) {
            }

            @Override
            public void mouseReleased(MouseEvent anEvent) {
                TokenVisualization tv = (TokenVisualization) comboBox.getParent();
                if (anEvent.getButton() == MouseEvent.BUTTON1) {
                    int index = list.locationToIndex(anEvent.getPoint());
//                    if (((ComboBoxEntry) comboBox.getItemAt(index)).getType() != ComboBoxEntryType.NORMAL) {
                    comboBox.setSelectedIndex(index);
//                    }
                    tv.processEdit((ComboBoxEntry) comboBox.getSelectedItem());
                } else if (anEvent.getButton() == MouseEvent.BUTTON3) {
                    int index = list.locationToIndex(anEvent.getPoint());
                    comboBox.setSelectedIndex(index);
                    //tv.abortTokenEditing();
                }
            }
        }

        /**
         * Creates ListMouseMotionlistener to listen to mouse motion events
         * occuring in the combo box's list. This listener is responsible for
         * highlighting items in the list when mouse is moved over them.
         *
         * @return MouseMotionListener that handles mouse motion events occuring
         * in the list of the combo box.
         */
        @Override
        protected MouseMotionListener createListMouseMotionListener() {
            return new MyComboBoxUI.LargerComboPopup.ListMouseMotionHandler();
        }

        /**
         * ListMouseMotionHandler listens to mouse motion events occuring in the
         * combo box's list. This class is responsible for highlighting items in
         * the list when mouse is moved over them
         */
        protected class ListMouseMotionHandler extends MouseMotionAdapter {

            protected ListMouseMotionHandler() {
            }

            @Override
            public void mouseMoved(MouseEvent anEvent) {
                // Highlight list cells over which the mouse is located. 
                // This changes list model, but has no effect on combo box's data model
                int index = list.locationToIndex(anEvent.getPoint());
                list.setSelectedIndex(index);
                list.repaint();
//                if (((ComboBoxEntry) comboBox.getItemAt(index)).getType() == ComboBoxEntryType.NORMAL) {
//                    comboBox.setSelectedIndex(index);
//                }
            }
        }
    }

    class lm implements LayoutManager {

        @Override
        public void addLayoutComponent(String string, Component cmpnt) {
        }

        @Override
        public void removeLayoutComponent(Component cmpnt) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return parent.getPreferredSize();
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return parent.getMinimumSize();
        }

        @Override
        public void layoutContainer(Container parent) {

            Rectangle cvb;
            if (editor != null) {
                cvb = rectangleForCurrentValue();
                editor.setBounds(cvb);
            }
        }
    }
}
