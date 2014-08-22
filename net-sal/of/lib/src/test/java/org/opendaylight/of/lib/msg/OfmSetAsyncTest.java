/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.VersionMismatchException;
import org.opendaylight.of.lib.VersionNotSupportedException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.MessageType.SET_ASYNC;
import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;

/**
 * Unit tests for the {@link OfmSetAsync} message.
 *
 * @author Scott Simes
 */
public class OfmSetAsyncTest extends OfmTest {

    // test files
    private static final String TF_SAC_13 = "v13/setAsync";

    // expected data
    private static final PacketInReason[] EXP_MSTR_PKT_IN =
            {PacketInReason.NO_MATCH, PacketInReason.ACTION};
    private static final Set<PacketInReason> EXP_MSTR_PKT_IN_SET =
            new HashSet<PacketInReason>(Arrays.asList(EXP_MSTR_PKT_IN));

    private static final PacketInReason[] EXP_SLV_PKT_IN = {};
    private static final Set<PacketInReason> EXP_SLV_PKT_IN_SET =
            new HashSet<PacketInReason>(Arrays.asList(EXP_SLV_PKT_IN));

    private static final PortReason[] EXP_MSTR_PORT_STATUS =
            {PortReason.ADD, PortReason.MODIFY, PortReason.DELETE};
    private static final Set<PortReason> EXP_MSTR_PORT_STATUS_SET =
            new HashSet<PortReason>(Arrays.asList(EXP_MSTR_PORT_STATUS));

    private static final PortReason[] EXP_SLV_PORT_STATUS =
            {PortReason.ADD, PortReason.MODIFY, PortReason.DELETE};
    private static final Set<PortReason> EXP_SLV_PORT_STATUS_SET =
            new HashSet<PortReason>(Arrays.asList(EXP_SLV_PORT_STATUS));

    private static final FlowRemovedReason[] EXP_MSTR_FLOW_REMOVED =
            {FlowRemovedReason.IDLE_TIMEOUT, FlowRemovedReason.HARD_TIMEOUT,
                    FlowRemovedReason.DELETE, FlowRemovedReason.GROUP_DELETE};
    private static final Set<FlowRemovedReason> EXP_MSTR_FLOW_REMOVED_SET =
            new HashSet<FlowRemovedReason>(Arrays.asList(EXP_MSTR_FLOW_REMOVED));

    private static final FlowRemovedReason[] EXP_SLV_FLOW_REMOVED = {};
    private static final Set<FlowRemovedReason> EXP_SLV_FLOW_REMOVED_SET =
            new HashSet<FlowRemovedReason>(Arrays.asList(EXP_SLV_FLOW_REMOVED));

    private MutableMessage mm;

    // ========================================================= PARSING ====

    @Test
    public void setAsync13() {
        print(EOL + "setAsync13()");
        OfmSetAsync msg = (OfmSetAsync) verifyMsgHeader(TF_SAC_13,
                V_1_3, SET_ASYNC, 32);
        verifyFlags(msg.getPacketInMask(), EXP_MSTR_PKT_IN);
        verifyFlags(msg.getSlavePacketInMask(), EXP_SLV_PKT_IN);

        verifyFlags(msg.getPortStatusMask(), EXP_MSTR_PORT_STATUS);
        verifyFlags(msg.getSlavePortStatusMask(), EXP_SLV_PORT_STATUS);

        verifyFlags(msg.getFlowRemovedMask(), EXP_MSTR_FLOW_REMOVED);
        verifyFlags(msg.getSlaveFlowRemovedMask(), EXP_SLV_FLOW_REMOVED);
    }

    // NOTE: SET_ASYNC not supported in 1.0, 1,1 or 1.2

    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodeSetAsync13() {
        print(EOL + "encodeSetAsync13()");
        mm = MessageFactory.create(V_1_3, SET_ASYNC);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_3, SET_ASYNC, 0);
        OfmMutableSetAsync mut = (OfmMutableSetAsync) mm;
        mut.pktInMask(EXP_MSTR_PKT_IN_SET)
           .pktInMaskSlave(EXP_SLV_PKT_IN_SET)
           .portStatusMask(EXP_MSTR_PORT_STATUS_SET)
           .portStatusMaskSlave(EXP_SLV_PORT_STATUS_SET)
           .flowRemovedMask(EXP_MSTR_FLOW_REMOVED_SET)
           .flowRemovedMaskSlave(EXP_SLV_FLOW_REMOVED_SET);
        encodeAndVerifyMessage(mm.toImmutable(), TF_SAC_13);
    }

    // NOTE: SET_ASYNC not supported in 1.0, 1,1 or 1.2

    @Test(expected = VersionNotSupportedException.class)
    public void encodeSetAsync12() {
        mm = MessageFactory.create(V_1_2, SET_ASYNC);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeSetAsync11() {
        mm = MessageFactory.create(V_1_1, SET_ASYNC);
    }

    @Test(expected = VersionMismatchException.class)
    public void encodeSetAsync10() {
        mm = MessageFactory.create(V_1_0, SET_ASYNC);
    }
}
