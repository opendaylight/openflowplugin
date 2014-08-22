/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.IncompleteStructureException;
import org.opendaylight.of.lib.MessageParseException;
import org.opendaylight.of.lib.OfPacketReader;
import org.opendaylight.of.lib.mp.MBodyPortDesc;
import org.opendaylight.of.lib.mp.MpBodyFactory;
import org.opendaylight.of.lib.mp.MultipartBody;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.MacAddress;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.mp.MultipartType.PORT_DESC;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REPLY;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REQUEST;
import static org.opendaylight.of.lib.msg.PortConfig.*;
import static org.opendaylight.of.lib.msg.PortFeature.*;
import static org.opendaylight.of.lib.msg.PortState.LINK_DOWN;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for OfmMultipartRequest and OfmMultipartReply messages
 * of type MultipartType.PORT_DESC.
 *
 * @author Simon Hunt
 */
public class OfmMultipartPortDescTest extends OfmMultipartTest {

    // Test files...
    private static final String TF_REQ_PD_13 = "v13/mpRequestPortDesc";
    private static final String TF_REP_PD_13 = "v13/mpReplyPortDesc";
    private static final String TF_REP_PD_13_TWICE = "v13/mpReplyPortDescTwice";

    // === Expected values
    private static final BigPortNumber EXP_PNUM_0 = bpn(258);
    private static final MacAddress EXP_MAC_0 = mac("114477:112233");
    private static final String EXP_NAME_0 = "Two";
    private static final Set<PortConfig> EXP_CFG_0 =
            new TreeSet<PortConfig>(Arrays.asList(NO_RECV, NO_FWD, NO_PACKET_IN));
    private static final Set<PortState> EXP_STATE_0 =
            new TreeSet<PortState>(Arrays.asList(LINK_DOWN));

    private static final BigPortNumber EXP_PNUM_1 = bpn(259);
    private static final MacAddress EXP_MAC_1 = mac("114477:22ab42");
    private static final String EXP_NAME_1 = "Three";
    private static final Set<PortConfig> EXP_CFG_1 =
            new TreeSet<PortConfig>(Arrays.asList(NO_FWD));
    private static final Set<PortState> EXP_STATE_SET_1 =
            new TreeSet<PortState>();

    private static final Set<PortFeature> EXP_CURR =
            new TreeSet<PortFeature>(Arrays.asList(RATE_1GB_FD, FIBER, AUTONEG));
    private static final Set<PortFeature> EXP_ADV =
            new TreeSet<PortFeature>(Arrays.asList(RATE_1GB_FD, FIBER));
    private static final Set<PortFeature> EXP_SUPP =
            new TreeSet<PortFeature>(Arrays.asList(RATE_1GB_FD, FIBER, AUTONEG));
    private static final Set<PortFeature> EXP_PEER = null;

    private static final long EXP_CURR_SPEED = 1100000;
    private static final long EXP_MAX_SPEED = 3000000;


    // ========================================================= PARSING ====

    @Test
    public void mpRequestPortDesc() {
        print(EOL + "mpRequestPortDesc()");
        OfmMultipartRequest msg = (OfmMultipartRequest)
                verifyMsgHeader(TF_REQ_PD_13, V_1_3, MULTIPART_REQUEST, 16);
        verifyMpHeader(msg, PORT_DESC);
    }

    @Test
    public void mpReplyPortDesc() throws MessageParseException {
        print(EOL + "mpReplyPortDesc()");
        OfPacketReader pkt = getOfmTestReader(TF_REP_PD_13);
        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyPortDesc(m);
    }

    @Test
    public void mpReplyPortDescTwice() throws MessageParseException {
        print(EOL + "mpReplyPortDescTwice()");
        OfPacketReader pkt = getOfmTestReader(TF_REP_PD_13_TWICE);

        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyPortDesc(m);

        m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyPortDesc(m);
    }

    private void validateReplyPortDesc(OpenflowMessage m) {
        print(m.toDebugString());

        assertEquals(AM_NEQ, V_1_3, m.getVersion());
        assertEquals(AM_NEQ, MULTIPART_REPLY, m.getType());
        assertEquals(AM_UXS, 144, m.length());

        OfmMultipartReply msg = (OfmMultipartReply) m;

        MBodyPortDesc.Array body = (MBodyPortDesc.Array)
                verifyMpHeader(msg, PORT_DESC);
        Iterator<MBodyPortDesc> pdIt = body.getList().iterator();

        List<Port> accumulated = new ArrayList<Port>();
        Port p;
        p = pdIt.next().getPort();
        accumulated.add(p);
        verifyPort(p, EXP_PNUM_0, EXP_MAC_0, EXP_NAME_0,
                EXP_CFG_0, EXP_STATE_0,
                EXP_CURR, EXP_ADV, EXP_SUPP, EXP_PEER,
                EXP_CURR_SPEED, EXP_MAX_SPEED);
        p = pdIt.next().getPort();
        accumulated.add(p);
        verifyPort(p, EXP_PNUM_1, EXP_MAC_1, EXP_NAME_1,
                EXP_CFG_1, EXP_STATE_SET_1,
                EXP_CURR, EXP_ADV, EXP_SUPP, EXP_PEER,
                EXP_CURR_SPEED, EXP_MAX_SPEED);
        assertFalse(AM_HUH, pdIt.hasNext());

        // check that we can extract the list of ports directly
        List<Port> extracted = body.getPorts();
        assertEquals(AM_NEQ, accumulated, extracted);
    }


    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodeMpRequestPortDesc() {
        print(EOL + "encodeMpRequestPortDesc()");
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_3, MULTIPART_REQUEST);
        req.clearXid();
        req.type(PORT_DESC);

        // now encode and verify
        encodeAndVerifyMessage(req.toImmutable(), TF_REQ_PD_13);
    }

    @Test
    public void encodeMpRequestPortDescWithMpType() {
        print(EOL + "encodeMpRequestPortDescWithMpType()");
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_3, MULTIPART_REQUEST, PORT_DESC);
        req.clearXid();
        encodeAndVerifyMessage(req.toImmutable(), TF_REQ_PD_13);
    }

    @Test
    public void encodeMpReplyPortDesc() throws IncompleteStructureException {
        print(EOL + "encodeMpReplyPortDesc()");
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
                MessageFactory.create(V_1_3, MULTIPART_REPLY);
        rep.clearXid();

        MBodyPortDesc.MutableArray array = (MBodyPortDesc.MutableArray)
                        MpBodyFactory.createReplyBody(V_1_3, PORT_DESC);
        fillOutArray(array);
        rep.body((MultipartBody) array.toImmutable());
        // now encode and verify
        encodeAndVerifyMessage(rep.toImmutable(), TF_REP_PD_13);
    }

    @Test
    public void encodeMpReplyPortDescWithMpType()
            throws IncompleteStructureException {
        print(EOL + "encodeMpReplyPortDescWithMpType()");
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
                MessageFactory.create(V_1_3, MULTIPART_REPLY, PORT_DESC);
        rep.clearXid();
        MBodyPortDesc.MutableArray array =
                (MBodyPortDesc.MutableArray) rep.getBody();
        fillOutArray(array);
        encodeAndVerifyMessage(rep.toImmutable(), TF_REP_PD_13);
    }

    private void fillOutArray(MBodyPortDesc.MutableArray array)
            throws IncompleteStructureException {
        // TODO - reuse mutable port
        //  PortFactory.copy(port)
        MutablePort port = PortFactory.createPort(V_1_3)
                .portNumber(EXP_PNUM_0).hwAddress(EXP_MAC_0).name(EXP_NAME_0)
                .config(EXP_CFG_0).state(EXP_STATE_0)
                .current(EXP_CURR).advertised(EXP_ADV)
                .supported(EXP_SUPP).peer(EXP_PEER)
                .currentSpeed(EXP_CURR_SPEED).maxSpeed(EXP_MAX_SPEED);
        array.addPort((Port) port.toImmutable());

        port = PortFactory.createPort(V_1_3)
                .portNumber(EXP_PNUM_1).hwAddress(EXP_MAC_1).name(EXP_NAME_1)
                .config(EXP_CFG_1).state(EXP_STATE_SET_1)
                .current(EXP_CURR).advertised(EXP_ADV)
                .supported(EXP_SUPP).peer(EXP_PEER)
                .currentSpeed(EXP_CURR_SPEED).maxSpeed(EXP_MAX_SPEED);
        array.addPort((Port) port.toImmutable());
    }

}
