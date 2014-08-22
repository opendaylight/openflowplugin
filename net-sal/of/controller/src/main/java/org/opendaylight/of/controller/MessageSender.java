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
import org.opendaylight.of.lib.msg.MessageFuture;
import org.opendaylight.of.lib.msg.OpenflowMessage;

import java.util.List;

/**
 * Defines the API for sending OpenFlow messages to datapaths.
 *
 * @author Simon Hunt
 */
public interface MessageSender {

    /** Sends the specified message to the specified datapath.
     * A message future is returned, to allow the caller to discover the
     * outcome of the request once it has been satisfied.
     * <p>
     * <strong>Important Note:</strong><br/>
     * The sending of <em>FlowMod</em> messages via this method is disallowed;
     * use {@link ControllerService#sendFlowMod} instead.
     *
     * @param msg the OpenFlow message to send
     * @param dpid the OpenFlow datapath to which the message is to be sent
     * @return a message future
     * @throws NullPointerException if either parameter is null
     * @throws IllegalArgumentException if msg is mutable, or if it is a
     *          <em>FlowMod</em>
     * @throws OpenflowException if there was a problem encoding or sending
     *              the message
     */
    MessageFuture send(OpenflowMessage msg, DataPathId dpid)
            throws OpenflowException;

    /** Sends the specified list of messages (in order) to the specified
     * datapath. A list of message futures (one for each message) is returned,
     * to allow the caller to discover the outcome of the requests once they
     * have been satisfied.
     * <p>
     * <strong>Important Note:</strong><br/>
     * The sending of <em>FlowMod</em> messages via this method is disallowed;
     * use {@link ControllerService#sendFlowMod} instead.
     *
     * @param msgs the OpenFlow messages to send
     * @param dpid the OpenFlow datapath to which the messages are to be sent
     * @return a list of message futures
     * @throws NullPointerException if either parameter is null, or if any
     *          element in the list is null
     * @throws IllegalArgumentException if any message in the list is mutable,
     *          or is a <em>FlowMod</em>
     * @throws OpenflowException if there was a problem encoding or sending
     *              the messages
     */
    List<MessageFuture> send(List<OpenflowMessage> msgs, DataPathId dpid)
            throws OpenflowException;

}
