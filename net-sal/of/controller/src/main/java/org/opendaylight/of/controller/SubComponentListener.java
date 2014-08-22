/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller;

import org.opendaylight.of.controller.impl.AbstractSubComponent;

/**
 * Implemented by classes that interact with a controller
 * {@link AbstractSubComponent sub-component}, to be notified when datapath
 * or message events occur.
 *
 * @see SubComponentService
 *
 * @author Simon Hunt
 */
public interface SubComponentListener {

    /** Callback invoked when a datapath event occurs.
     *
     * @param event the datapath event
     */
    void event(DataPathEvent event);

    /** Callback invoked when a message event occurs.
     *
     * @param event the message event
     */
    void event(MessageEvent event);
}
