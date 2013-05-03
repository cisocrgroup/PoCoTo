package jav.gui.main;

import jav.correctionBackend.Token;
import java.util.ArrayList;
import java.util.HashMap;

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
public class TokenVisualizationMultiRegistry {
    private HashMap<Integer, ArrayList<AbstractTokenVisualization>> registry;
    
    public TokenVisualizationMultiRegistry() {
       registry = new HashMap<>();
    }
    
    public void add( Token t, AbstractTokenVisualization tv) {
        int i = t.getIndexInDocument();
        if( registry.containsKey(i)) {
            if(!registry.get(i).contains(tv)) {
                registry.get(i).add(tv);
            }
        } else {
            ArrayList<AbstractTokenVisualization> val = new ArrayList<>();
            val.add(tv);
            registry.put(i, val);
        }
    }
    
    public void add( int indexindoc, AbstractTokenVisualization tv) {
        if( registry.containsKey(indexindoc)) {
            if(!registry.get(indexindoc).contains(tv)) {
                registry.get(indexindoc).add(tv);
            }
        } else {
            ArrayList<AbstractTokenVisualization> val = new ArrayList<>();
            val.add(tv);
            registry.put(indexindoc, val);
        }
    }
    
    public boolean contains( Token t) {
        return registry.containsKey(t.getIndexInDocument());
    }
    
    public boolean contains( int indexInDoc ) {
        return registry.containsKey( indexInDoc );
    }
    
    public void remove( Token t, AbstractTokenVisualization tv) {
        int i = t.getIndexInDocument();
        if( registry.containsKey(i)) {
            if( registry.get(i).contains(tv)) {
                registry.get(i).remove(tv);
                if( registry.get(i).isEmpty()) {
                    registry.remove(i);
                }
            }
        }
    }
    
    public void remove( Token t) {
        registry.remove(t.getIndexInDocument());
    }
    
    public void remove( int indexindoc, AbstractTokenVisualization tv) {
        if( registry.containsKey(indexindoc)) {
            if( registry.get(indexindoc).contains(tv)) {
                registry.get(indexindoc).remove(tv);
                if( registry.get(indexindoc).isEmpty()) {
                    registry.remove(indexindoc);
                }
            }
        }
    }
    
    public void remove( int indexindoc) {
        registry.remove(indexindoc);
    }
    
    public ArrayList<AbstractTokenVisualization> getVisualizations( Token t) {
        return registry.get(t.getIndexInDocument());
    }
    
    public ArrayList<AbstractTokenVisualization> getVisualizations( int indexindoc) {
        return registry.get(indexindoc);
    }
    
    public void clear() {
        registry.clear();
    } 
}