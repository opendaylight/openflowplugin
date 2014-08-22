/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.flow.impl;

import org.junit.Test;
import org.opendaylight.of.controller.flow.FlowEvent;
import org.opendaylight.of.controller.impl.AbstractTest;
import org.opendaylight.of.lib.dt.BufferId;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.msg.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.opendaylight.of.controller.flow.FlowEventType.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.MatchFactory.createMatch;
import static org.opendaylight.of.lib.msg.FlowModFlag.CHECK_OVERLAP;
import static org.opendaylight.of.lib.msg.FlowModFlag.SEND_FLOW_REM;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for FlowEvt.
 *
 * @author Simon Hunt
 */
public class FlowEvtTest extends AbstractTest {

    private static final DataPathId DPID = dpid("42/123456:789abc");

    private static final Match MATCH = (Match) createMatch(V_1_3).toImmutable();

    private static final Set<FlowModFlag> FM_FLAGS = new HashSet<FlowModFlag>(
            Arrays.asList(CHECK_OVERLAP, SEND_FLOW_REM)
    );

    private static final OfmFlowMod OFM_FLOW_MOD = createFlowMod();

    private static OfmFlowMod createFlowMod() {
        OfmMutableFlowMod m = (OfmMutableFlowMod) MessageFactory.create(V_1_3,
                MessageType.FLOW_MOD, FlowModCommand.ADD);
        m.clearXid();
        m.bufferId(BufferId.NO_BUFFER).outPort(bpn(3)).flowModFlags(FM_FLAGS)
                .match(MATCH);
        return (OfmFlowMod) m.toImmutable();
    }

    private static final OfmFlowRemoved OFM_FLOW_REMOVED = createFlowRemoved();

    private static OfmFlowRemoved createFlowRemoved() {
        OfmMutableFlowRemoved m = (OfmMutableFlowRemoved)
                MessageFactory.create(V_1_3, MessageType.FLOW_REMOVED,
                        FlowRemovedReason.IDLE_TIMEOUT);
        m.clearXid();
        m.tableId(tid(200)).match(MATCH);
        return (OfmFlowRemoved) m.toImmutable();
    }

    @Test
    public void flowModPushed() {
        print(EOL + "flowModPushed()");
        FlowEvent e = new FlowEvt(FLOW_MOD_PUSHED, DPID, OFM_FLOW_MOD);
        print(e);
        assertEquals(AM_NEQ, FLOW_MOD_PUSHED, e.type());
        assertEquals(AM_NEQ, DPID, e.dpid());
        assertEquals(AM_NEQ, OFM_FLOW_MOD, e.flowMod());
        assertEquals(AM_NEQ, null, e.flowRemoved());
    }

    @Test
    public void flowModPushFailed() {
        print(EOL + "flowModPushFailed()");
        FlowEvent e = new FlowEvt(FLOW_MOD_PUSH_FAILED, DPID, OFM_FLOW_MOD);
        print(e);
        assertEquals(AM_NEQ, FLOW_MOD_PUSH_FAILED, e.type());
        assertEquals(AM_NEQ, DPID, e.dpid());
        assertEquals(AM_NEQ, OFM_FLOW_MOD, e.flowMod());
        assertEquals(AM_NEQ, null, e.flowRemoved());
    }

    @Test
    public void flowModRemoved() {
        print(EOL + "flowModRemoved()");
        FlowEvent e = new FlowEvt(FLOW_REMOVED, DPID, OFM_FLOW_REMOVED);
        print(e);
        assertEquals(AM_NEQ, FLOW_REMOVED, e.type());
        assertEquals(AM_NEQ, DPID, e.dpid());
        assertEquals(AM_NEQ, null, e.flowMod());
        assertEquals(AM_NEQ, OFM_FLOW_REMOVED, e.flowRemoved());
    }
}
