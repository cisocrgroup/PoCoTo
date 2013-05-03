package jav.gui.main;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.event.EventListenerList;
import org.openide.util.NbPreferences;

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
 *
 * @source http://wiki.netbeans.org/AddingMRUList
 */
public class MRUFilesOptions {
    protected static String DEFAULT_NODE_NAME = "prefs";
    protected String nodeName = null;
    private EventListenerList listenerList;

    public static final String MRU_FILE_LIST_PROPERTY = "MRUFileList";

    private List<String> mruFileList;
    private int maxSize;

    private static MRUFilesOptions instance; // The single instance
    static {
        instance = new MRUFilesOptions();
    }

    /**
     * Returns the single instance, creating one if it's the
     * first time this method is called.
     *
     * @return The single instance.
     */
    public static MRUFilesOptions getInstance() {
        return instance;
    }

    /** {@inheritDoc} */
    protected MRUFilesOptions() {
        nodeName = "mrufiles";
        maxSize = 5; // default is 9
        mruFileList = new ArrayList<String>(maxSize);
        listenerList = new EventListenerList();
        retrieve();
    }

    public List<String> getMRUFileList() {
        return mruFileList;
    }

    public void setMRUFileList(List<String> list) {
        this.mruFileList.clear();
        this.mruFileList.addAll(list.subList(0, Math.min(list.size(), maxSize)));
        firePropertyChange(MRU_FILE_LIST_PROPERTY, null, mruFileList);
        store();
    }

    public void removeFile(String absolutePath) {
        mruFileList.remove(absolutePath);
        firePropertyChange(MRU_FILE_LIST_PROPERTY, null, mruFileList);
    }

    public void addFile(String absolutePath) {
        // remove the old
        mruFileList.remove(absolutePath);

        // add to the top
        mruFileList.add(0, absolutePath);
        while (mruFileList.size() > maxSize) {
            mruFileList.remove(mruFileList.size() - 1);
        }
        firePropertyChange(MRU_FILE_LIST_PROPERTY, null, mruFileList);
        store();
    }

    protected void store() {
        Preferences prefs = getPreferences();

        // clear the backing store
        try {
            prefs.clear();
        } catch (BackingStoreException ex) { }

        for (int i = 0; i < mruFileList.size(); i++) {
            String str = mruFileList.get(i);
            prefs.put(MRU_FILE_LIST_PROPERTY + i, str);
        }
    }

    protected void retrieve() {
        mruFileList.clear();
        Preferences prefs = getPreferences();

        for (int i = 0; i < maxSize; i++) {
            String str = prefs.get(MRU_FILE_LIST_PROPERTY + i, null);
            if (str != null) {
                File f = new File(str);
                if( f.exists()) {
                    mruFileList.add(str);
                }
            } else {
                break;
            }
        }
    }

    /** {@inheritDoc} */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listenerList.add(PropertyChangeListener.class, listener);
    }

    /** {@inheritDoc} */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listenerList.remove(PropertyChangeListener.class, listener);
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {

        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == PropertyChangeListener.class) {
                ((PropertyChangeListener) listeners[i+1]).propertyChange(event);
            }
        }
    }

    /** Return the backing store Preferences
     * @return Preferences
     */
    protected final Preferences getPreferences() {
        String name = DEFAULT_NODE_NAME;
        if (nodeName != null) {
            name = nodeName;
        }

        Preferences prefs = NbPreferences.forModule(this.getClass()).node("options").node(name);

        return prefs;
    }
}
