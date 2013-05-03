package jav.gui.events;

import java.util.ArrayList;
import org.openide.windows.IOProvider;

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
public abstract class EventGenerator<ET extends Event, EST extends EventSlot> {

    /** list of event slots registered for updates */
    public final ArrayList<EST> listeners = new ArrayList<EST>();
//    List<EST> listeners = Collections.synchronizedList( new ArrayList<EST>());

    /**
     * registers an event slot (listener) for receiving updates. Typically this
     * method is called by the respective object itself.
     *
     * @param listener
     *            the object to be called back for updates on events
     */
    public void addListener(EST listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * unregisters an event slot (listener) from receiving updates. Typically
     * this method is called by the respective object itself.
     *
     * @param listener
     *            the listener to remove
     */
    public void removeListener(EST listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public int getNumListeners() {
        return listeners.size();
    }

    /**
     * notifies all listeners that an event happened.
     * NOTE: The method takes the combined time of all listeners reacting
     * to the event to finish
     * Listeners performing time consuming tasks can block the sending of the event
     * 
     * Listeners that want to perform complex tasks should consider threading them to
     * not block the event sending
     * 
     * {@link EventGenerator#fireEvent(EventSlot, Event)} is called.
     *
     * @param e
     *            object describing the event that happened
     */
    public void fireEvent(ET e) {
        synchronized (listeners) {
            for (EST listener : listeners) {
                fireEvent(listener, e);
            }
        }
    }

    /*
     */
    public void fireThreadedEvent(ET e) {
        synchronized (listeners) {
            for (EST listener : listeners) {
                new EventThread(listener, e);
            }
        }
    }

    /**
     * notifies the given listener, that the given event happened.
     * <p>
     * This method is abstract and has to be implemented by subclasses. Usally
     * it calls the method in the listener, that is enforced by the appropriate
     * EventSlot interface.
     *
     * @param Listener
     *            the listener to notify
     * @param e
     *            description of the event that happened
     */
    public abstract void fireEvent(EST Listener, ET e);

    /*
     * ensures that the individual message recipient cannot slow down
     * the execution of the whole program
     */
    class EventThread implements Runnable {

        private EST listener;
        private ET e;

        public EventThread(EST l, ET e) {
            this.listener = l;
            this.e = e;
            new Thread(this).start();
        }

        @Override
        public void run() {
            fireEvent(listener, e);
        }
    }
}
