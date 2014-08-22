/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller;

import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.msg.OpenflowMessage;

/**
 * Defines an API that may be implemented by controller sub-components to
 * allow "helper" classes to interact with them. The
 * {@link SubComponentListener} API defines callbacks that may be invoked for
 * datapath events and message events. It is up to the
 * {@link SubComponentService} implementation to determine which events
 * received from the lower controller components should be forwarded to its
 * sub-component listeners.
 * 
 * @author Simon Hunt
 */
public interface SubComponentService {

    /** Adds the specified listener to the sub-component service.
     *
     * @param listener the listener to be added
     * @throws NullPointerException if listener is null
     */
    void addListener(SubComponentListener listener);

    /** Removes the specified listener from the sub-component service.
     *
     * @param listener the listener to be removed
     * @throws NullPointerException if listener is null
     */
    void removeListener(SubComponentListener listener);

    /** Sends the specified message to the specified datapath, via the
     * sub-component.
     *
     * @param msg the OpenFlow message to send
     * @param dpid the OpenFlow datapath to which the message is to be sent
     * @throws NullPointerException if either parameter is null
     * @throws IllegalArgumentException if msg is mutable
     * @throws OpenflowException if there was a problem encoding or sending
     *              the message
     */
    void send(OpenflowMessage msg, DataPathId dpid) throws OpenflowException;
}
