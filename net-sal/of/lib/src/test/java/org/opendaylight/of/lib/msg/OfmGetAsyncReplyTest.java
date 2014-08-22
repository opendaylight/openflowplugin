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
import static org.opendaylight.of.lib.msg.MessageType.GET_ASYNC_REPLY;
import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;

/**
 * Unit tests for the {@link OfmGetAsyncReply} message.
 *
 * @author Scott Simes
 */
public class OfmGetAsyncReplyTest extends OfmTest {

    // test files
    private static final String TF_GACR_13 = "v13/getAsyncReply";

    // expected data
    private static final PacketInReason[] EXP_MSTR_PKT_IN =
            {PacketInReason.NO_MATCH, PacketInReason.INVALID_TTL};
    private static final Set<PacketInReason> EXP_MSTR_PKT_IN_SET =
            new HashSet<PacketInReason>(Arrays.asList(EXP_MSTR_PKT_IN));

    private static final PacketInReason[] EXP_SLV_PKT_IN =
            {PacketInReason.ACTION};
    private static final Set<PacketInReason> EXP_SLV_PKT_IN_SET =
            new HashSet<PacketInReason>(Arrays.asList(EXP_SLV_PKT_IN));

    private static final PortReason[] EXP_MSTR_PORT_STATUS =
            {PortReason.ADD, PortReason.DELETE};
    private static final Set<PortReason> EXP_MSTR_PORT_STATUS_SET =
            new HashSet<PortReason>(Arrays.asList(EXP_MSTR_PORT_STATUS));

    private static final PortReason[] EXP_SLV_PORT_STATUS =
            {PortReason.ADD, PortReason.MODIFY};
    private static final Set<PortReason> EXP_SLV_PORT_STATUS_SET =
            new HashSet<PortReason>(Arrays.asList(EXP_SLV_PORT_STATUS));

    private static final FlowRemovedReason[] EXP_MSTR_FLOW_REMOVED =
            {FlowRemovedReason.IDLE_TIMEOUT, FlowRemovedReason.HARD_TIMEOUT,
                    FlowRemovedReason.DELETE};
    private static final Set<FlowRemovedReason> EXP_MSTR_FLOW_REMOVED_SET =
            new HashSet<FlowRemovedReason>(Arrays.asList(EXP_MSTR_FLOW_REMOVED));

    private static final FlowRemovedReason[] EXP_SLV_FLOW_REMOVED =
            {FlowRemovedReason.DELETE, FlowRemovedReason.GROUP_DELETE};
    private static final Set<FlowRemovedReason> EXP_SLV_FLOW_REMOVED_SET =
            new HashSet<FlowRemovedReason>(Arrays.asList(EXP_SLV_FLOW_REMOVED));

    private MutableMessage mm;

    // ========================================================= PARSING ====

    @Test
    public void getAsyncConfigReply13() {
        print(EOL + "getAsyncConfigReply13()");
        OfmGetAsyncReply msg = (OfmGetAsyncReply) verifyMsgHeader(TF_GACR_13,
                V_1_3, GET_ASYNC_REPLY, 32);
        verifyFlags(msg.getPacketInMask(), EXP_MSTR_PKT_IN);
        verifyFlags(msg.getSlavePacketInMask(), EXP_SLV_PKT_IN);

        verifyFlags(msg.getPortStatusMask(), EXP_MSTR_PORT_STATUS);
        verifyFlags(msg.getSlavePortStatusMask(), EXP_SLV_PORT_STATUS);

        verifyFlags(msg.getFlowRemovedMask(), EXP_MSTR_FLOW_REMOVED);
        verifyFlags(msg.getSlaveFlowRemovedMask(), EXP_SLV_FLOW_REMOVED);
    }

    // NOTE: GET_ASYNC_REPLY not supported in 1.0, 1,1 or 1.2

    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodeAsyncConfigReply13() {
        print(EOL + "encodeAsyncConfigReply13()");
        mm = MessageFactory.create(V_1_3, GET_ASYNC_REPLY);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_3, GET_ASYNC_REPLY, 0);
        OfmMutableGetAsyncReply mut = (OfmMutableGetAsyncReply) mm;
        mut.pktInMask(EXP_MSTR_PKT_IN_SET)
           .pktInMaskSlave(EXP_SLV_PKT_IN_SET)
           .portStatusMask(EXP_MSTR_PORT_STATUS_SET)
           .portStatusMaskSlave(EXP_SLV_PORT_STATUS_SET)
           .flowRemovedMask(EXP_MSTR_FLOW_REMOVED_SET)
           .flowRemovedMaskSlave(EXP_SLV_FLOW_REMOVED_SET);
        encodeAndVerifyMessage(mm.toImmutable(), TF_GACR_13);
    }

    // NOTE: GET_ASYNC_REQUEST not supported in 1.0, 1,1 or 1.2

    @Test(expected = VersionNotSupportedException.class)
    public void encodeAsyncConfigReply12() {
        mm = MessageFactory.create(V_1_2, GET_ASYNC_REPLY);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeAsyncConfigReply11() {
        mm = MessageFactory.create(V_1_1, GET_ASYNC_REPLY);
    }

    @Test(expected = VersionMismatchException.class)
    public void encodeAsyncConfigReply10() {
        mm = MessageFactory.create(V_1_0, GET_ASYNC_REPLY);
    }
}
