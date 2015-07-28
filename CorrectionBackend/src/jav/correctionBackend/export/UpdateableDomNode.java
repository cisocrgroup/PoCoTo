/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

import jav.correctionBackend.Token;

/**
 *
 * @author finkf
 */
public interface UpdateableDomNode extends HasTokenImageInfoBox {
    public void correctToken(Token token);
    public String getContent();
    public void mergeWith(Iterable<UpdateableDomNode> ns);
}
