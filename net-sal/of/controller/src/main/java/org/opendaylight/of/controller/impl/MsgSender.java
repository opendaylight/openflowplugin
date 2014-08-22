/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.controller.*;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.of.lib.msg.MessageType.*;

/**
 * Provides basic message future handling for outgoing OpenFlow messages.
 *
 * @author Simon Hunt
 */
class MsgSender extends AbstractSubComponent implements MessageSender {

    private static Logger log = LoggerFactory.getLogger(OpenflowController.class);

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            MsgSender.class, "msgSender");

    private static final String E_FLOW_MOD_DISALLOWED = RES
            .getString("e_flow_mod_disallowed");
    private static final String E_NO_WRITES = RES.getString("e_no_writes");
    private static final String E_NON_CONTROLLER_MSG = RES
            .getString("e_non_controller_msg");
    private static final String E_BAD_XID = RES.getString("e_bad_xid");

    // Message types that should NEVER be sent from the controller. */
    private static final Set<MessageType> NON_CONTROLLER_MSG_TYPES =
            new HashSet<MessageType>(Arrays.asList(
                    FEATURES_REQUEST, // (part of handshake only)
                    FEATURES_REPLY,
                    GET_CONFIG_REPLY,
                    PACKET_IN,
                    FLOW_REMOVED,
                    PORT_STATUS,
                    MULTIPART_REPLY,
                    BARRIER_REPLY,
                    QUEUE_GET_CONFIG_REPLY,
                    GET_ASYNC_REPLY
            ));

    // Controller initiated messages for which a reply (or error) is expected
    private static final Set<MessageType> REPLIES_EXPECTED_TYPES =
            new HashSet<MessageType>(Arrays.asList(
                    ECHO_REQUEST,
                    //EXPERIMENTER, /* FIXME: Review with Simon and update */
                    GET_CONFIG_REQUEST,
                    FLOW_MOD,
                    GROUP_MOD,
                    PORT_MOD,
                    TABLE_MOD,
                    MULTIPART_REQUEST,
                    BARRIER_REQUEST,
                    QUEUE_GET_CONFIG_REQUEST,
                    ROLE_REQUEST,
                    GET_ASYNC_REQUEST,
                    METER_MOD
            ));

    /* Controller initiated messages for which NO reply is expected
     *  SET_CONFIG
     *  PACKET_OUT
     *  SET_ASYNC
     */

    // Messages considered "write" operations - these are not allowed if we
    // are not the "master" for the destination datapath
    private static final Set<MessageType> WRITE_OPS =
            new HashSet<MessageType>(Arrays.asList(
                    SET_CONFIG,
                    // EXPERIMENTER? - too Draconian ?? // TODO Review
                    PACKET_OUT,
                    FLOW_MOD,
                    GROUP_MOD,
                    PORT_MOD,
                    TABLE_MOD,
                    SET_ASYNC,
                    METER_MOD
            ));

    /** Our interface to the core controller. */
    final MsgListener msgListener = new MsgListener();

    @Override
    protected MessageListener getMyMessageListener() {
        return msgListener;
    }

    // Cache of outstanding message requests
    // TODO: replace with AgeOutHashMap
    private final CopyOnWriteArraySet<MessageFuture> pendingFutures =
            new CopyOnWriteArraySet<MessageFuture>();

    // Locates the future with the specified xid
    private MessageFuture findFuture(long xid) {
        for (MessageFuture mf: pendingFutures)
            if (mf.xid() == xid)
                return mf;
        return null;
    }

    private void validateMsg(OpenflowMessage m, boolean noWrites) {
        MessageType mt = m.getType();
        // not able to write to the datapath if we are not the master
        if (noWrites && WRITE_OPS.contains(mt))
            throw new IllegalArgumentException(E_NO_WRITES + mt);

        // sending of flow-mods is disallowed
        if (mt == FLOW_MOD)
            throw new IllegalArgumentException(E_FLOW_MOD_DISALLOWED);
        // TODO: consider guarding against GROUP_MODS/METER_MODS

        // sending of "switch-initiated" messages is disallowed
        if (NON_CONTROLLER_MSG_TYPES.contains(mt))
            throw new IllegalArgumentException(E_NON_CONTROLLER_MSG + mt);

        // FIXME: consider guarding against PACKET_OUT with inPort != CONTROLLER

        // sending of message with XID <= 0 is disallowed
        if (m.getXid() <= 0)
            throw new IllegalArgumentException(E_BAD_XID + m.getXid());
    }

    /**
     * Sends an openflow message to the given datapath.
     *
     * @param msg the message to send
     * @param dpid the target datapath
     * @return a message future
     * @throws OpenflowException if there was a problem
     */
    @Override
    public MessageFuture send(OpenflowMessage msg, DataPathId dpid)
            throws OpenflowException {
        notNull(msg, dpid);
        boolean noWrites = !roleAdvisor.isMasterFor(dpid);
        validateMsg(msg, noWrites);

        MessageFuture future = new DefaultMessageFuture(msg);
        boolean pending = false;
        if (REPLIES_EXPECTED_TYPES.contains(msg.getType())) {
            pendingFutures.add(future);
            pending = true;
        }

        try {
            listenerService.send(msg, dpid);
        } catch (OpenflowException e) {
            pendingFutures.remove(future);
            future.setFailure(e);
            throw e;
        }
        if (!pending)
            future.setSuccess();

        return future;
    }

    /**
     * Sends a list of openflow messages to the given datapath.
     *
     * @param msgs the messages to send
     * @param dpid the target datapath
     * @return a list of message futures
     * @throws OpenflowException if there was a problem
     */
    @Override
    public List<MessageFuture> send(List<OpenflowMessage> msgs, DataPathId dpid)
            throws OpenflowException {
        notNull(msgs, dpid);

        // validate all messages up front - It's an all or nothing operation
        boolean noWrites = !roleAdvisor.isMasterFor(dpid);
        for (OpenflowMessage m: msgs)
            validateMsg(m, noWrites);

        // all messages are good to go, so send them
        List<MessageFuture> futures = new ArrayList<MessageFuture>(msgs.size());

        for (OpenflowMessage m: msgs) {
            MessageFuture future = new DefaultMessageFuture(m);
            boolean pending = false;
            if (REPLIES_EXPECTED_TYPES.contains(m.getType())) {
                pendingFutures.add(future);
                pending = true;
            }

            try {
                listenerService.send(m, dpid);
            } catch (OpenflowException e) {
                pendingFutures.remove(future);
                future.setFailure(e);
                // NOTE: don't rethrow exception - we want to process all msgs
            }
            if (!pending)
                future.setSuccess(); // no-op if already set as failure above
            futures.add(future);
        }

        return futures;
    }


    // =======================================================================
    private class MsgListener implements MessageListener {
        @Override
        public void queueEvent(QueueEvent event) {
            log.warn(event.toString());
        }

        @Override
        public void event(MessageEvent event) {
            if (event.type() == OpenflowEventType.MESSAGE_RX) {
                OpenflowMessage m = event.msg();
                MessageFuture f = findFuture(m.getXid());
                if (f != null) {
                    pendingFutures.remove(f);
                    if (m.getType() == MessageType.ERROR)
                        f.setFailure((OfmError) m);
                    else
                        f.setSuccess(m);
                }
            }
        }
    }
}