package jav.concordance.control;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

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
public class ConcordanceGraphicsRegistry {
   public LinkedHashMap<Integer, ArrayList<JPanel>> registry;
    
    public ConcordanceGraphicsRegistry() {
        registry = new LinkedHashMap<>();
    }
    
    public void add( int tokenId, JPanel leftC, JPanel center, JPanel rightC ) {
        ArrayList<JPanel> val = new ArrayList<>();
        val.add(leftC);
        val.add(center);
        val.add(rightC);
        registry.put( tokenId, val);
    }
    
    public Integer[] getEntries() {
        return (Integer[]) registry.keySet().toArray();
    }
    
    
    public ArrayList<JPanel> get( int tokenId ) {
        return registry.get( tokenId );
    }
    
    public JCheckBox getCheckBox( int tokenId ) {
        return (JCheckBox) registry.get( tokenId ).get(1).getComponent(2);
    }
    
    public JPanel getLeftContext( int tokenId ) {
        return registry.get( tokenId ).get(0);
    }
    
    public JPanel getWord( int tokenId ) {
        return registry.get( tokenId ).get(1);
    }

    public JPanel getRightContext( int tokenId ) {
        return registry.get( tokenId ).get(2);
    }
    
    public void remove( int tokenId ) {
        registry.remove( tokenId );
    }
    
    public void clear() {
        registry.clear();
    } 
}
