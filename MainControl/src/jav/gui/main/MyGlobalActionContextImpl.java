package jav.gui.main;

import java.beans.PropertyChangeEvent;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

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
@ServiceProvider(service = ContextGlobalProvider.class, position = 1)
public final class MyGlobalActionContextImpl implements ContextGlobalProvider, Lookup.Provider, java.beans.PropertyChangeListener {

    private TopComponent.Registry registry;
    private Lookup localLookup;

    public MyGlobalActionContextImpl() {
        this(TopComponent.getRegistry());
        localLookup = Lookup.EMPTY;
    }

    MyGlobalActionContextImpl(TopComponent.Registry r) {
        this.registry = r;
    }

    @Override
    public Lookup createGlobalContext() {
        registry.addPropertyChangeListener(this);
        return org.openide.util.lookup.Lookups.proxy(this);
//        return new ProxyLookup( this.getLookup() );
//        return new ProxyLookup( this.localLookup, MainController.findInstance().getLookup());
    }

    @Override
    public Lookup getLookup() {
        TopComponent tc = registry.getActivated();
        if (tc == null) {
            localLookup = Lookup.EMPTY;
        } else {
            localLookup = tc.getLookup();
        }
        return new ProxyLookup( localLookup, MainController.findInstance().getLookup());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Requests refresh of our lookup everytime component is changed.
        if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
            org.openide.util.Utilities.actionsGlobalContext().lookup(javax.swing.ActionMap.class);
        }
    }
}
///*
// *                 Sun Public License Notice
// *
// * The contents of this file are subject to the Sun Public License
// * Version 1.0 (the "License"). You may not use this file except in
// * compliance with the License. A copy of the License is available at
// * http://www.sun.com/
// *
// * The Original Code is NetBeans. The Initial Developer of the Original
// * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
// * Microsystems, Inc. All Rights Reserved.
// */
//import org.openide.util.Lookup;
//import org.openide.util.ContextGlobalProvider;
//import org.openide.windows.TopComponent;
//
///** An interface that can be registered in a lookup by subsystems
// * wish to provide a global context actions should react to.
// *
// * @author Jaroslav Tulach
//*/
//@ServiceProvider(service=ContextGlobalProvider.class, position=1)
////public final class MyGlobalActionContextImpl extends Object implements ContextGlobalProvider, Lookup.Provider, java.beans.PropertyChangeListener {
//    /** registry to work with */
//    private TopComponent.Registry registry;
//
//    public MyGlobalActionContextImpl () {
//        this(TopComponent.getRegistry());
//    }
//
//    public MyGlobalActionContextImpl (TopComponent.Registry r) {
//        this.registry = r;
//    }
//
//    /** Let's create the proxy listener that delegates to currently
//     * selected top component.
//     */
//    @Override
//    public Lookup createGlobalContext() {
//        registry.addPropertyChangeListener(this);
//        return org.openide.util.lookup.Lookups.proxy(this);
//    }
//
//    /** The current component lookup */
//    @Override
//    public Lookup getLookup() {
//        TopComponent tc = registry.getActivated();
//        return tc == null ? Lookup.EMPTY : tc.getLookup();
//    }
//
//    /** Requests refresh of our lookup everytime component is chagned.
//     */
//    @Override
//    public void propertyChange(java.beans.PropertyChangeEvent evt) {
//        if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
//            org.openide.util.Utilities.actionsGlobalContext().lookup(javax.swing.ActionMap.class);
//        }
//    }
//}

