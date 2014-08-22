/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.of.mockswitch.MockOpenflowSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.opendaylight.of.lib.msg.MessageType.BARRIER_REPLY;
import static org.opendaylight.of.lib.msg.MessageType.ECHO_REPLY;

/**
 * A basic mock switch, where the DPID is defined via the constructor,
 * overriding the DPID declared in the definition file.
 *
 * @author Simon Hunt
 */
public class BasicSwitch extends MockOpenflowSwitch {

    /** A handy logger, should one wish to log stuff from the switch. */
    protected final Logger log = LoggerFactory.getLogger(MockOpenflowSwitch.class);

    private static final String TRUE = "true";
    private static final String MOCK_OF_SW_PROP = "mockofsw.output";

    private static boolean showSwitchOutput() {
        String ep = System.getProperty(MOCK_OF_SW_PROP);
        // Translate empty string value to 'true' for the property.
        if (ep != null && ep.length() == 0)
            ep = TRUE;
        return ep != null && ep.equals(TRUE);
    }

    /**
     * Constructs a mock switch with the given datapath ID, using the
     * specified definition file for configuration. If dpid is null, the dpid
     * in the definition file will be used.
     *
     * @param dpid the datapath ID (null to use dpid from def file)
     * @param defPath the switch definition file
     * @throws IOException if there was an issue reading switch configuration
     */
    public BasicSwitch(DataPathId dpid, String defPath) throws IOException {
        super(defPath, showSwitchOutput()); 
        if (dpid != null)
            this.dpid = dpid;
    }
    
    @Override
    protected void msgRx(OpenflowMessage msg) {
        switch (msg.getType()) {
            case BARRIER_REQUEST:
                handleBarrier((OfmBarrierRequest) msg);
                break;
            case ECHO_REQUEST:
                handleEcho((OfmEchoRequest) msg);
                break;
            default:
                // ignore stuff we don't directly care about
                break;
        }
    }

    /**
     * Default handling of a barrier request message; send out a barrier reply.
     *
     * @param request the request
     */
    protected void handleBarrier(OfmBarrierRequest request) {
        send(MessageFactory.create(request, BARRIER_REPLY).toImmutable());
    }

    /**
     * Default handling of an echo request message; send out an echo reply,
     * with the data copied.
     *
     * @param request the request
     */
    protected void handleEcho(OfmEchoRequest request) {
        OfmMutableEchoReply me = (OfmMutableEchoReply)
                MessageFactory.create(request, ECHO_REPLY);
        byte[] data = me.getData();
        if (data != null)
            me.data(request.getData());
        send(me.toImmutable());
    }

}
