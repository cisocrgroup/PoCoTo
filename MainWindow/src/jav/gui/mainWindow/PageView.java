package jav.gui.mainWindow;

import jav.correctionBackend.MyIterator;
import jav.correctionBackend.Page;
import jav.correctionBackend.Token;
import jav.correctionBackend.TokenImageInfoBox;
import jav.gui.events.MessageCenter;
import jav.gui.events.tokenSelection.TokenSelectionEvent;
import jav.gui.events.tokenSelection.TokenSelectionType;
import jav.gui.events.tokenStatus.TokenStatusType;
import jav.gui.main.MainController;
import jav.gui.token.behaviour.TokenVisualizationDefaultMode;
import jav.gui.token.behaviour.TokenVisualizationMode;
import jav.gui.token.display.ImageTokenVisualization;
import jav.gui.token.display.OnlyTextTokenVisualization;
import jav.gui.token.display.PseudoImageTokenVisualization;
import jav.gui.token.display.TokenVisualization;
import jav.gui.token.tools.ImageProcessor;
import java.awt.Color;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import se.datadosen.component.RiverLayout;

/**
 * Copyright (c) 2012, IMPACT working group at the Centrum für Informations- und
 * Sprachverarbeitung, University of Munich. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * This file is part of the ocr-postcorrection tool developed by the IMPACT
 * working group at the Centrum für Informations- und Sprachverarbeitung,
 * University of Munich. For further information and contacts visit
 * http://ocr.cis.uni-muenchen.de/
 *
 * @author thorsten (thorsten.vobl@googlemail.com)
 */
public class PageView extends JPanel {
    
    private TokenVisualizationMode tvMode = new TokenVisualizationDefaultMode();
    private ImageProcessor ip = null;
    private MainTopComponent parent;
    private int lineheight;
    private RiverLayout rl;
    
    public PageView(MainTopComponent p, MyIterator<Token> it, int fontSize) {
        super();
        
        this.parent = p;
        this.setBorder(new EmptyBorder(10, 10, 10, 10));
        rl = new RiverLayout(0, 25);
        this.setLayout(rl);
        this.setBackground(Color.white);
        
        if (!it.hasNext()) {
            JLabel bla = new JLabel(java.util.ResourceBundle.getBundle("jav/gui/mainWindow/Bundle").getString("no_token"));
            this.add(bla);
        } else {
            it.reset();
            boolean newline = false;
            while (it.hasNext()) {
                Token tok = it.next();
                TokenVisualization tv = new OnlyTextTokenVisualization(tok, fontSize);
                tv.setMode(tvMode);
                tv.setAlignmentY(Component.BOTTOM_ALIGNMENT);

                // two newlines following each other
                if (newline && tv.isNewline()) {
                    this.add("br", tv);
                    // the token after a newline
                } else if (newline && !tv.isNewline()) {
                    this.add("br", tv);
                    newline = false;
                    // newline after not newline
                } else if (!newline && tv.isNewline()) {
                    this.add(tv);
                    newline = true;
                    // regular tokens in a row
                } else {
                    this.add(tv);
                }
                parent.getTokenVisualizationRegistry().addtoRegistry(tok, tv);
            }
        }
    }
    
    public PageView(MainTopComponent p, MyIterator<Token> it, String imageFile, int fontSize, double imgScale) {
        super();
        
        this.parent = p;
        
        ip = new ImageProcessor();
        ip.setImageInput(imageFile);
        
        this.setBorder(new EmptyBorder(10, 10, 10, 10));
        rl = new RiverLayout(0, 25);
        this.setLayout(rl);
        this.setBackground(Color.white);

        // for empty pages, display a message
        if (!it.hasNext()) {
            JLabel bla = new JLabel(java.util.ResourceBundle.getBundle("jav/gui/mainWindow/Bundle").getString("no_token"));
            this.add(bla);
        } else {
            it.reset();
            boolean newline = false;
            while (it.hasNext()) {
                Token tok = it.next();
                if (tok == null) {
                    System.out.println("NULL");
                }
                TokenImageInfoBox tiib = tok.getTokenImageInfoBox();
                TokenVisualization tv;
                if (tiib != null) {
                    int left = tiib.getCoordinateLeft();
                    int right = tiib.getCoordinateRight();
                    int top = tiib.getCoordinateTop();
                    int bottom = tiib.getCoordinateBottom();
                    int width = right - left;
                    int height = bottom - top;
                    
                    BufferedImage bi = ip.getTokenImage(left, top, width, height, imgScale);
                    tv = new ImageTokenVisualization(bi, tok, fontSize);
                    lineheight = ((ImageTokenVisualization) tv).getImageHeight();
                } else {
                    if (tok.isNormal()) {
                        tv = new PseudoImageTokenVisualization(tok, fontSize, lineheight);
                    } else {
                        tv = new OnlyTextTokenVisualization(tok, fontSize);
                    }
                }
                
                tv.setMode(tvMode, tok);
                tv.setAlignmentY(Component.BOTTOM_ALIGNMENT);

                // two newlines following each other
                if (newline && tv.isNewline()) {
                    this.add("br", tv);
                    // the token after a newline
                } else if (newline && !tv.isNewline()) {
                    this.add("br", tv);
                    newline = false;
                    // newline after not newline
                } else if (!newline && tv.isNewline()) {
                    this.add(tv);
                    newline = true;
                    // regular tokens in a row
                } else {
                    this.add(tv);
                }
                parent.getTokenVisualizationRegistry().addtoRegistry(tok, tv);
            }
        }
    }
    
    public void zoomFont(int i) {
        for (Component ca : this.getComponents()) {
            if (ca instanceof TokenVisualization) {
                TokenVisualization tv = (TokenVisualization) ca;
                tv.zoomFont(i);
            }
        }
    }
    
    public void zoomImg(double scale) {
        for (Component ca : this.getComponents()) {
            if (ca instanceof ImageTokenVisualization) {
                ImageTokenVisualization tv = (ImageTokenVisualization) ca;
                TokenImageInfoBox tiib = MainController.findInstance().getDocument().getTokenByID(tv.getTokenID()).getTokenImageInfoBox();
                if (tiib != null) {
                    int left = tiib.getCoordinateLeft();
                    int right = tiib.getCoordinateRight();
                    int top = tiib.getCoordinateTop();
                    int bottom = tiib.getCoordinateBottom();
                    int width = right - left;
                    int height = bottom - top;
                    tv.setImage(ip.getTokenImage(left, top, width, height, scale));
                }
            } else if (ca instanceof PseudoImageTokenVisualization) {
                ((PseudoImageTokenVisualization) ca).zoomFiller(scale);
            }
        }
    }
    
    public void toggleImages(boolean on) {
        for (Component ca : this.getComponents()) {
            if (ca instanceof ImageTokenVisualization) {
                ImageTokenVisualization tv = (ImageTokenVisualization) ca;
                if (on) {
                    TokenImageInfoBox tiib = MainController.findInstance().getDocument().getTokenByID(tv.getTokenID()).getTokenImageInfoBox();
                    if (tiib != null) {
                        int left = tiib.getCoordinateLeft();
                        int right = tiib.getCoordinateRight();
                        int top = tiib.getCoordinateTop();
                        int bottom = tiib.getCoordinateBottom();
                        int width = right - left;
                        int height = bottom - top;
                        
                        BufferedImage bi = ip.getTokenImage(left, top, width, height, parent.getScale());
                        tv.setImage(bi);
                    }
                } else {
                    if (tv.hasImage()) {
                        tv.clearImage();
                    }
                }
            } else if (ca instanceof PseudoImageTokenVisualization) {
                ((PseudoImageTokenVisualization) ca).toggleImage(on);
            }
        }
    }
    
    public void update(TokenStatusType t, int affectedID, ArrayList<Integer> affectedTokens) {
        
        if (t.equals(TokenStatusType.MERGED_RIGHT)) {
            
            TokenVisualization affectedTv;
            if ((affectedTv = (TokenVisualization) parent.getTokenVisualizationRegistry().getTokenVisualization(affectedID)) != null) {
                Token tok = MainController.findInstance().getDocument().getTokenByID(affectedTokens.get(0));
                affectedTv.setTokenID(tok.getID());
                if (affectedTv.hasImage()) {
                    TokenImageInfoBox tiib = tok.getTokenImageInfoBox();
                    if (tiib != null) {
                        int left = tiib.getCoordinateLeft();
                        int right = tiib.getCoordinateRight();
                        int top = tiib.getCoordinateTop();
                        int bottom = tiib.getCoordinateBottom();
                        int width = right - left;
                        int height = bottom - top;
                        BufferedImage bi = ip.getTokenImage(left, top, width, height, parent.getScale());
                        ((ImageTokenVisualization) affectedTv).update(bi, tok.getWDisplay());
                        if (!parent.getShowImages()) {
                            ((ImageTokenVisualization) affectedTv).clearImage();
                        }
                    }
                } else {
                    affectedTv.update(tok.getWDisplay());
                }
                parent.getTokenVisualizationRegistry().removefromRegistry(affectedID);
                parent.getTokenVisualizationRegistry().addtoRegistry(tok.getID(), affectedTv);
            }
            
            for (int i = 1; i < affectedTokens.size(); i++) {
                TokenVisualization todelete;
                if ((todelete = (TokenVisualization) parent.getTokenVisualizationRegistry().getTokenVisualization(affectedTokens.get(i))) != null) {
                    parent.getTokenVisualizationRegistry().removefromRegistry(affectedTokens.get(i));
                    this.remove(todelete);
                }
            }            
            MessageCenter.getInstance().fireTokenSelectionEvent(new TokenSelectionEvent(affectedTv, affectedTokens.get(0), TokenSelectionType.NORMAL));
            
        } else if (t.equals(TokenStatusType.SPLIT)) {
            
            TokenVisualization affectedTv = (TokenVisualization) parent.getTokenVisualizationRegistry().getTokenVisualization(affectedID);
            if (affectedTv != null) {
                affectedTv.setSelected(false);
//                MessageCenter.getInstance().fireTokenDeselectionEvent(new TokenDeselectionEvent(test, test.getTokenIndex()));                
                Token tok = MainController.findInstance().getDocument().getTokenByID(affectedTokens.get(0));
                affectedTv.setTokenID(tok.getID());
                if (affectedTv.hasImage() && parent.getShowImages()) {
                    
                    TokenImageInfoBox tiib = tok.getTokenImageInfoBox();
                    if (tiib != null) {
                        int left = tiib.getCoordinateLeft();
                        int right = tiib.getCoordinateRight();
                        int top = tiib.getCoordinateTop();
                        int bottom = tiib.getCoordinateBottom();
                        int width = right - left;
                        int height = bottom - top;
                        BufferedImage bi = ip.getTokenImage(left, top, width, height, parent.getScale());
                        ((ImageTokenVisualization) affectedTv).update(bi, tok.getWDisplay());
                    }
                } else {
                    affectedTv.update(tok.getWDisplay());
                }
                parent.getTokenVisualizationRegistry().removefromRegistry(affectedID);
                parent.getTokenVisualizationRegistry().addtoRegistry(tok.getID(), affectedTv);
                
                int index = this.getIndexInContainer(affectedID);

                // create the additional tokenvis
                for (int i = 1; i < affectedTokens.size(); i++) {
                    tok = MainController.findInstance().getDocument().getTokenByID(affectedTokens.get(i));
                    TokenImageInfoBox tiib = tok.getTokenImageInfoBox();
                    
                    TokenVisualization tokv;
                    if (tiib != null) {
                        int left = tiib.getCoordinateLeft();
                        int right = tiib.getCoordinateRight();
                        int top = tiib.getCoordinateTop();
                        int bottom = tiib.getCoordinateBottom();
                        int width = right - left;
                        int height = bottom - top;
                        
                        BufferedImage bi = ip.getTokenImage(left, top, width, height, parent.getScale());
                        tokv = new ImageTokenVisualization(bi, tok, parent.getFontSize());
                        if (!parent.getShowImages()) {
                            ((ImageTokenVisualization) tokv).clearImage();
                        }
                    } else if (!tok.getImageFilename().equals("") && tok.isNormal()) {
                        tokv = new PseudoImageTokenVisualization(tok, parent.getFontSize(), ((ImageTokenVisualization) affectedTv).getImageHeight());
                        if (!parent.getShowImages()) {
                            ((PseudoImageTokenVisualization) tokv).toggleImage(false);
                        }
                    } else {
                        tokv = new OnlyTextTokenVisualization(tok, parent.getFontSize());
                    }
                    
                    tokv.setMode(tvMode);
                    this.add(tokv, index + i);
                    parent.getTokenVisualizationRegistry().addtoRegistry(tok, tokv);
                    if( i == affectedTokens.size()-1) {
                        tokv.setSelected(true);
                        tokv.grabFocus();
                        this.getVisualizationMode().setSelectedTokenVisualization(tokv);
                        MessageCenter.getInstance().fireTokenSelectionEvent(new TokenSelectionEvent(tokv, tokv.getTokenID(), TokenSelectionType.NORMAL));
                        
                        // TODO das nächste Token selektieren
                    }
                }
            }
//            int count = 0;
//            TokenVisualization tvToSelect = null;
//            for (Component c : this.getComponents()) {
//                if (c instanceof TokenVisualization) {
//                    TokenVisualization test = (TokenVisualization) c;
//
//                    // update the affected tokenvis
//                    if (test.getTokenID() == affectedIndex) {
//                        test.setSelected(false);
////                        MessageCenter.getInstance().fireTokenDeselectionEvent(new TokenDeselectionEvent(test, test.getTokenIndex()));
//
//                        Token tok = MainController.findInstance().getDocument().getTokenByID(test.getTokenID());
//                        if (test.hasImage() && parent.getShowImages()) {
//
//                            TokenImageInfoBox tiib = tok.getTokenImageInfoBox();
//                            if (tiib != null) {
//                                int left = tiib.getCoordinateLeft();
//                                int right = tiib.getCoordinateRight();
//                                int top = tiib.getCoordinateTop();
//                                int bottom = tiib.getCoordinateBottom();
//                                int width = right - left;
//                                int height = bottom - top;
//                                BufferedImage bi = ip.getTokenImage(left, top, width, height, parent.getScale());
//                                ((ImageTokenVisualization) test).update(bi, tok.getWDisplay());
//                            }
//                        } else {
//                            test.update(tok.getWDisplay());
//                        }
//                        this.updateTokenVisualizationIndices(affectedIndex, numAffected);
//
//                        // create the additional tokenvis
//                        for (int i = 1; i <= numAffected; i++) {
//                            tok = MainController.findInstance().getDocument().getTokenByID(affectedIndex + i);
//                            TokenImageInfoBox tiib = tok.getTokenImageInfoBox();
//
//                            TokenVisualization tokv;
//                            if (tiib != null) {
//                                int left = tiib.getCoordinateLeft();
//                                int right = tiib.getCoordinateRight();
//                                int top = tiib.getCoordinateTop();
//                                int bottom = tiib.getCoordinateBottom();
//                                int width = right - left;
//                                int height = bottom - top;
//
//                                BufferedImage bi = ip.getTokenImage(left, top, width, height, parent.getScale());
//                                tokv = new ImageTokenVisualization(bi, tok, parent.getFontSize());
//                                if (!parent.getShowImages()) {
//                                    ((ImageTokenVisualization) tokv).clearImage();
//                                }
//                            } else if (!tok.getImageFilename().equals("") && tok.isNormal()) {
//                                tokv = new PseudoImageTokenVisualization(tok, parent.getFontSize(), ((ImageTokenVisualization) test).getImageHeight());
//                                if (!parent.getShowImages()) {
//                                    ((PseudoImageTokenVisualization) tokv).toggleImage(false);
//                                }
//                            } else {
//                                tokv = new OnlyTextTokenVisualization(tok, parent.getFontSize());
//                            }
//
//                            tokv.setMode(tvMode);
//                            this.add(tokv, count + i);
//                            if (i == numAffected) {
//                                tvToSelect = tokv;
//                            }
//                        }
//                        this.updateTokenRegistry();
//
//                        tvToSelect.setSelected(true);
//                        tvToSelect.grabFocus();
//                        this.getVisualizationMode().setSelectedTokenVisualization(tvToSelect);
//                        MessageCenter.getInstance().fireTokenSelectionEvent(new TokenSelectionEvent(tvToSelect, tvToSelect.getTokenID(), TokenSelectionType.NORMAL));
//                        return;
//                    }
//                }
//                count++;
//            }
        } else if (t.equals(TokenStatusType.DELETE)) {

            for( int i = 0; i < affectedTokens.size(); i++) {
                TokenVisualization todelete;
                if ((todelete = (TokenVisualization) parent.getTokenVisualizationRegistry().getTokenVisualization(affectedTokens.get(i))) != null) {
                    parent.getTokenVisualizationRegistry().removefromRegistry(affectedTokens.get(i));
                    this.remove(todelete);
                }               
            }
            
        } else if (t.equals(TokenStatusType.INSERT)) {
            
            int index;
            if( affectedID == -1) {
                index = 0;
            } else {
                index = this.getIndexInContainer(affectedID)+1;
            }
            
            TokenVisualization test = (TokenVisualization) parent.getTokenVisualizationRegistry().getTokenVisualization( affectedID );
            
            for (int i = 0; i < affectedTokens.size(); i++) {
                Token tok = MainController.findInstance().getDocument().getTokenByID( affectedTokens.get(i));
                                TokenImageInfoBox tiib = tok.getTokenImageInfoBox();

                                TokenVisualization tokv;
                                if (tiib != null) {
                                    int left = tiib.getCoordinateLeft();
                                    int right = tiib.getCoordinateRight();
                                    int top = tiib.getCoordinateTop();
                                    int bottom = tiib.getCoordinateBottom();
                                    int width = right - left;
                                    int height = bottom - top;

                                    BufferedImage bi = ip.getTokenImage(left, top, width, height, parent.getScale());
                                    tokv = new ImageTokenVisualization(bi, tok, parent.getFontSize());
                                    if (!parent.getShowImages()) {
                                        ((ImageTokenVisualization) tokv).clearImage();
                                    }
                                } else if (!tok.getImageFilename().equals("") && tok.isNormal()) {
                                    if( test == null) {
                                        tokv = new PseudoImageTokenVisualization(tok, parent.getFontSize(), 10);
                                    } else {
                                        tokv = new PseudoImageTokenVisualization(tok, parent.getFontSize(), ((ImageTokenVisualization) test).getImageHeight());
                                    }
                                    if (!parent.getShowImages()) {
                                        ((PseudoImageTokenVisualization) tokv).toggleImage(false);
                                    }
                                } else {
                                    tokv = new OnlyTextTokenVisualization(tok, parent.getFontSize());
                                }

                                tokv.setMode(tvMode);

                                if (tok.getWDisplay().equals("\n") && test != null) {
                                    this.getLayoutConstraints().remove(test);
                                    this.getLayoutConstraints().put(tokv, "br");
                                }

                                this.add(tokv, (index + i));                                
            }
            
            
            System.out.println("INSERT POID: " + affectedID);
            for( int i = 0; i < affectedTokens.size(); i++ ) {
                System.out.println("INSERT: " + affectedTokens.get(i));
            }

//            Page p = MainController.findInstance().getPage(parent.getPageN() - 1);
//            if (p.getStartIndex() <= affectedIndex && affectedIndex <= p.getEndIndex()) {
//
//                int count = 0;
//                for (Component c : this.getComponents()) {
//                    if (c instanceof TokenVisualization) {
//                        TokenVisualization test = (TokenVisualization) c;
//
//                        if (test.getTokenID() + 1 == affectedIndex) {
//
//                            this.updateTokenVisualizationIndices(test.getTokenID(), numAffected);
//
//                            for (int i = 0; i < numAffected; i++) {
//                                Token tok = MainController.findInstance().getDocument().getTokenByID(affectedIndex + i);
//                                TokenImageInfoBox tiib = tok.getTokenImageInfoBox();
//
//                                TokenVisualization tokv;
//                                if (tiib != null) {
//                                    int left = tiib.getCoordinateLeft();
//                                    int right = tiib.getCoordinateRight();
//                                    int top = tiib.getCoordinateTop();
//                                    int bottom = tiib.getCoordinateBottom();
//                                    int width = right - left;
//                                    int height = bottom - top;
//
//                                    BufferedImage bi = ip.getTokenImage(left, top, width, height, parent.getScale());
//                                    tokv = new ImageTokenVisualization(bi, tok, parent.getFontSize());
//                                    if (!parent.getShowImages()) {
//                                        ((ImageTokenVisualization) tokv).clearImage();
//                                    }
//                                } else if (!tok.getImageFilename().equals("") && tok.isNormal()) {
//                                    tokv = new PseudoImageTokenVisualization(tok, parent.getFontSize(), ((ImageTokenVisualization) test).getImageHeight());
//                                    if (!parent.getShowImages()) {
//                                        ((PseudoImageTokenVisualization) tokv).toggleImage(false);
//                                    }
//                                } else {
//                                    tokv = new OnlyTextTokenVisualization(tok, parent.getFontSize());
//                                }
//
//                                tokv.setMode(tvMode);
//
//                                if (tok.getWDisplay().equals("\n")) {
//                                    this.getLayoutConstraints().remove(test);
//                                    this.getLayoutConstraints().put(tokv, "br");
//                                }
//
//                                this.add(tokv, (count + i + 1));
//                            }
//                            this.updateTokenRegistry();
//                            return;
//                        }
//                    }
//                    count++;
//                }
//            }
        }
    }
    
    public int getIndexInContainer(int tokenID) {
        int count = 0;
        for (Component c : this.getComponents()) {
            if (c instanceof TokenVisualization) {
                TokenVisualization test = (TokenVisualization) c;
                if (test.getTokenID() == tokenID) {
                    return count;
                }
            }
            count++;
        }
        return 0;
    }
    
    public void checkPrint() {
        for (Component c : this.getComponents()) {
            if (c instanceof TokenVisualization) {
                TokenVisualization tv = (TokenVisualization) c;
                System.out.println(tv.getTokenTextLabelText() + " " + tv.getTokenID());
            }
        }
    }
    
    public TokenVisualizationMode getVisualizationMode() {
        return this.tvMode;
    }
    
    public HashMap getLayoutConstraints() {
        return rl.getConstraints();
    }
    
    private void updateTokenRegistry() {
        parent.getTokenVisualizationRegistry().clear();
        for (Component c : this.getComponents()) {
            if (c instanceof TokenVisualization) {
                TokenVisualization tv = (TokenVisualization) c;
                parent.getTokenVisualizationRegistry().addtoRegistry(tv.getTokenID(), tv);
            }
        }
    }
    
    private void updateTokenVisualizationIndices(int startindex, int discr) {
        for (Component c : this.getComponents()) {
            if (c instanceof TokenVisualization) {
                TokenVisualization tv = (TokenVisualization) c;
                if (tv.getTokenID() > startindex) {
                    tv.setTokenID(tv.getTokenID() + discr);
                }
            }
        }
    }
}