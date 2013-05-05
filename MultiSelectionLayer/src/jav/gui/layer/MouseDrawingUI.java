package jav.gui.layer;

/**
 * Copyright (c) 2008-2009, Piet Blok
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *   * Neither the name of the copyright holder nor the names of the
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * modifications to class made Sept. 2011 thorsten
 */
import jav.gui.events.special.multiselection.MultiSelectionEvent;
import jav.gui.events.special.multiselection.MultiSelectionEventType;
import jav.gui.main.AbstractEditorViewTopComponent;
import jav.gui.token.display.TokenVisualization;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.AbstractLayerUI;

public class MouseDrawingUI<V extends JComponent> extends AbstractLayerUI<V> {

    private Rectangle selection;
    private Point anchor;
    private AbstractEditorViewTopComponent parent;
    private boolean inselection = false;

    public MouseDrawingUI(AbstractEditorViewTopComponent in) {
        this.parent = in;
    }

    // override paintLayer(), not paint()
    @Override
    protected void paintLayer(Graphics2D g2, JXLayer<? extends V> l) {

        super.paintLayer(g2, l);

        if (selection != null) {
            g2.setColor(new Color(0, 255, 255, 32));
            g2.fill3DRect(selection.x, selection.y, selection.width, selection.height, true);
//                g2.fill(selection);
        }
    }

    // catch drag events
    @Override
    protected void processMouseMotionEvent(MouseEvent e, JXLayer<? extends V> l) {
        if (!parent.isEditing()) {
            super.processMouseMotionEvent(e, l);
            Point drag;
            if (e.getID() == MouseEvent.MOUSE_DRAGGED && inselection) {
                parent.grabFocus();
                if (e.getSource() instanceof TokenVisualization) {
                    TokenVisualization tv = (TokenVisualization) e.getSource();
                    drag = new Point(tv.getX() + e.getX(), tv.getY() + e.getY());
                } else {
                    drag = e.getPoint();
                }
                Rectangle newselection = new Rectangle(Math.min(anchor.x, drag.x), Math.min(anchor.y, drag.y), Math.abs(drag.x - anchor.x), Math.abs(drag.y - anchor.y));
                if (newselection.width < selection.width) {
                    parent.dispatchMultiSelectionEvent(new MultiSelectionEvent(newselection, MultiSelectionEventType.SMALLER));
                } else {
                    parent.dispatchMultiSelectionEvent(new MultiSelectionEvent(newselection, MultiSelectionEventType.LARGER));
                }
                selection = newselection;
                // mark the ui as dirty and needed to be repainted
                setDirty(true);
            }
        }
    }

    // catch MouseEvent.MOUSE_RELEASED and MouseEvent.MOUSE_PRESSED
    @Override
    protected void processMouseEvent(MouseEvent e, JXLayer<? extends V> l) {
        if (!parent.isEditing()) {
            super.processMouseEvent(e, l);
            if (e.getID() == MouseEvent.MOUSE_PRESSED && e.getButton() == MouseEvent.BUTTON1) {
                this.inselection = true;
                if (e.getSource() instanceof TokenVisualization) {
                    TokenVisualization tv = (TokenVisualization) e.getSource();
                    anchor = new Point(tv.getX() + e.getX(), tv.getY() + e.getY());
                } else {
                    anchor = e.getPoint();
                }
                selection = new Rectangle(anchor);
                parent.dispatchMultiSelectionEvent(new MultiSelectionEvent(selection, MultiSelectionEventType.START));
            } else if (e.getID() == MouseEvent.MOUSE_RELEASED && e.getButton() == MouseEvent.BUTTON1) {
                this.inselection = false;
                if ( selection != null && selection.width != 0) {
                    parent.dispatchMultiSelectionEvent(new MultiSelectionEvent(selection, MultiSelectionEventType.END));
                    // mark the ui as dirty and needed to be repainted
                    selection = null;
                    setDirty(true);
                } else {
                    selection = null;
                }
            }
        }
    }

    // clear overlay painting
    public void clear() {
        // mark the ui as dirty and needed to be repainted
        selection = null;
        setDirty(true);
    }
}
