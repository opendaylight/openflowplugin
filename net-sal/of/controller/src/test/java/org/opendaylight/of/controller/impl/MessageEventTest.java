/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.of.controller.MessageEvent;
import org.opendaylight.of.controller.OpenflowEventType;
import org.opendaylight.of.lib.ExperimenterId;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.err.ErrorType;
import org.opendaylight.of.lib.mp.MultipartType;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.TimeUtils;
import org.opendaylight.util.test.FakeTimeUtils;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.dt.DataPathId.dpid;
import static org.opendaylight.of.lib.mp.MultipartType.DESC;
import static org.opendaylight.of.lib.msg.MessageFactory.create;
import static org.opendaylight.of.lib.msg.MessageType.*;
import static org.opendaylight.util.test.FakeTimeUtils.Advance;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for MessageEvent.
 *
 * @author Simon Hunt
 */
public class MessageEventTest {

    private static final OpenflowEventType CON =
            OpenflowEventType.DATAPATH_CONNECTED;
    private static final OpenflowEventType TX = OpenflowEventType.MESSAGE_TX;
    private static final OpenflowEventType RX = OpenflowEventType.MESSAGE_RX;

    private static final DataPathId DPID = dpid("1/123456:654321");
    private static final int AUX = 7;

//    private static OpenflowMessage msgHello;
//    private static OpenflowMessage msgMpDesc;

    private TimeUtils fakeTime;
    private long expTs;

    @BeforeClass
    public static void classSetUp() {
    }

    // Test message event that allows us to control the timestamp.
    private class TestMessageEvt extends MessageEvt {
        TestMessageEvt(OpenflowEventType type, OpenflowMessage msg,
                       DataPathId dpid, int auxId, ProtocolVersion pv) {
            super(type, msg, dpid, auxId, pv);
        }

        @Override protected TimeUtils time() { return fakeTime; }
    }

    @Before
    public void setUp() {
        // make sure XID assignment always starts from 101.
        MessageFactory.getTestSupport().reset(MessageFactory.TestReset.XID);
        fakeTime = FakeTimeUtils.getInstance(Advance.MANUAL).timeUtils();
    }

    private void validateMsg(OpenflowEventType dir, OpenflowMessage msg,
                             DataPathId dpid, int auxId, ProtocolVersion pv,
                             String exp) {
        MessageEvent me = new TestMessageEvt(dir, msg, dpid, auxId, pv);
        print(me);
        assertEquals(AM_NEQ, exp, me.toString());
        assertEquals(AM_NEQ, expTs, me.ts());
        assertEquals(AM_VMM, dir, me.type());
        assertEquals(AM_VMM, msg, me.msg());
        assertEquals(AM_VMM, dpid, me.dpid());
        assertEquals(AM_VMM, pv, me.negotiated());
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        OpenflowMessage msgHello = create(V_1_3, HELLO).toImmutable();
        OpenflowMessage msgMpDesc =
                create(V_1_3, MULTIPART_REQUEST, DESC).toImmutable();

        expTs = fakeTime.currentTimeMillis();
        validateMsg(CON, null, null, AUX, null,
                "{00:00:00.000 <TestMessageEvt> DATAPATH_CONNECTED,pv=null," +
                        "dpid=null,aux=7,msg=null}");
        validateMsg(TX, msgHello, null, AUX, null,
                "{00:00:00.000 <TestMessageEvt> MESSAGE_TX,pv=null,dpid=null," +
                        "aux=7,msg=[V_1_3,HELLO,len=8,xid=101]}");
        validateMsg(RX, msgHello, null, AUX, null,
                "{00:00:00.000 <TestMessageEvt> MESSAGE_RX,pv=null,dpid=null" +
                        ",aux=7,msg=[V_1_3,HELLO,len=8,xid=101]}");
        validateMsg(TX, msgHello, DPID, AUX, V_1_3,
                "{00:00:00.000 <TestMessageEvt> MESSAGE_TX,pv=V_1_3," +
                        "dpid=00:01:12:34:56:65:43:21,aux=7," +
                        "msg=[V_1_3,HELLO,len=8,xid=101]}");
        validateMsg(RX, msgHello, DPID, AUX, V_1_3,
                "{00:00:00.000 <TestMessageEvt> MESSAGE_RX,pv=V_1_3," +
                        "dpid=00:01:12:34:56:65:43:21,aux=7," +
                        "msg=[V_1_3,HELLO,len=8,xid=101]}");
        validateMsg(TX, msgMpDesc, DPID, AUX, V_1_3,
                "{00:00:00.000 <TestMessageEvt> MESSAGE_TX,pv=V_1_3," +
                        "dpid=00:01:12:34:56:65:43:21,aux=7," +
                        "msg=[V_1_3,MULTIPART_REQUEST/DESC,len=16,xid=102]}");
    }


    // Add some tests for the augmented message headers (showing subtypes)

    private MessageEvent mkEvt(OpenflowMessage msg) {
        return new TestMessageEvt(RX, msg, DPID, 0, V_1_3);
    }


    private String hdr(MessageEvent ms) {
        String s = ms.toString();
        int start = s.indexOf(",msg=") + 5;
        int end = s.lastIndexOf("}");
        return s.substring(start, end);
    }

    private OpenflowMessage mkMsg(MessageType mt, Enum<?> sub) {
        return create(V_1_3, mt, sub).toImmutable();
    }

    @Test
    public void headerFormatHello() {
        print(EOL + "headerFormatHello()");
        String header = hdr(mkEvt(mkMsg(HELLO, null)));
        print(header);
        assertEquals(AM_NEQ, "[V_1_3,HELLO,len=8,xid=101]", header);
    }

    @Test
    public void headerFormatError() {
        print(EOL + "headerFormatError()");
        String h = hdr(mkEvt(mkMsg(ERROR, ErrorType.BAD_ACTION)));
        print(h);
        assertEquals(AM_NEQ, "[V_1_3,ERROR/BAD_ACTION,len=12,xid=101]", h);
    }

    @Test
    public void headerFormatEchoRequest() {
        print(EOL + "headerFormatEchoRequest()");
        String h = hdr(mkEvt(mkMsg(ECHO_REQUEST, null)));
        print(h);
        assertEquals(AM_NEQ, "[V_1_3,ECHO_REQUEST,len=8,xid=101]", h);
    }

    @Test
    public void headerFormatEchoReply() {
        print(EOL + "headerFormatEchoReply()");
        String h = hdr(mkEvt(mkMsg(ECHO_REPLY, null)));
        print(h);
        assertEquals(AM_NEQ, "[V_1_3,ECHO_REPLY,len=8,xid=101]", h);
    }

    @Test
    public void headerFormatExperimenter() {
        print(EOL + "headerFormatExperimenter()");
        String h = hdr(mkEvt(mkMsg(EXPERIMENTER, ExperimenterId.BIG_SWITCH)));
        print(h);
        assertEquals(AM_NEQ, "[V_1_3,EXPERIMENTER/BIG_SWITCH,len=16,xid=101]", h);
    }

    @Test
    public void headerFormatFeaturesRequest() {
        print(EOL + "headerFormatFeaturesRequest()");
        String h = hdr(mkEvt(mkMsg(FEATURES_REQUEST, null)));
        print(h);
        assertEquals(AM_NEQ, "[V_1_3,FEATURES_REQUEST,len=8,xid=101]", h);
    }

    @Test
    public void headerFormatFeaturesReply() {
        print(EOL + "headerFormatFeaturesReply()");
        String h = hdr(mkEvt(mkMsg(FEATURES_REPLY, null)));
        print(h);
        assertEquals(AM_NEQ, "[V_1_3,FEATURES_REPLY,len=32,xid=101]", h);
    }

    @Test
    public void headerFormatGetConfigRequest() {
        print(EOL + "headerFormatGetConfigRequest()");
        String h = hdr(mkEvt(mkMsg(GET_CONFIG_REQUEST, null)));
        print(h);
        assertEquals(AM_NEQ, "[V_1_3,GET_CONFIG_REQUEST,len=8,xid=101]", h);
    }

    @Test
    public void headerFormatGetConfigReply() {
        print(EOL + "headerFormatGetConfigReply()");
        String h = hdr(mkEvt(mkMsg(GET_CONFIG_REPLY, null)));
        print(h);
        assertEquals(AM_NEQ, "[V_1_3,GET_CONFIG_REPLY,len=12,xid=101]", h);
    }

    @Test
    public void headerFormatSetConfig() {
        print(EOL + "headerFormatSetConfig()");
        String h = hdr(mkEvt(mkMsg(SET_CONFIG, null)));
        print(h);
        assertEquals(AM_NEQ, "[V_1_3,SET_CONFIG,len=12,xid=101]", h);
    }

    @Test
    public void headerFormatPacketIn() {
        print(EOL + "headerFormatPacketIn()");
        String h = hdr(mkEvt(mkMsg(PACKET_IN, PacketInReason.INVALID_TTL)));
        print(h);
        assertEquals(AM_NEQ, "[V_1_3,PACKET_IN/INVALID_TTL,len=26,xid=101]", h);
    }

    @Test
    public void headerFormatFlowRemoved() {
        print(EOL + "headerFormatFlowRemoved()");
        String h = hdr(mkEvt(mkMsg(FLOW_REMOVED, FlowRemovedReason.IDLE_TIMEOUT)));
        print(h);
        assertEquals(AM_NEQ, "[V_1_3,FLOW_REMOVED/IDLE_TIMEOUT,len=48,xid=101]", h);
    }

    @Test
    public void headerFormatPortStatus() {
        print(EOL + "headerFormatPortStatus()");
        String h = hdr(mkEvt(mkMsg(PORT_STATUS, PortReason.MODIFY)));
        print(h);
        assertEquals(AM_NEQ, "[V_1_3,PORT_STATUS/MODIFY,len=80,xid=101]", h);
    }

    @Test
    public void headerFormatPacketOut() {
        print(EOL + "headerFormatPacketOut()");
        String h = hdr(mkEvt(mkMsg(PACKET_OUT, null)));
        print(h);
        assertEquals(AM_NEQ, "[V_1_3,PACKET_OUT,len=24,xid=101]", h);
    }

    @Test
    public void headerFormatFlowMod() {
        print(EOL + "headerFormatFlowMod()");
        String h = hdr(mkEvt(mkMsg(FLOW_MOD, FlowModCommand.DELETE_STRICT)));
        print(h);
        assertEquals(AM_NEQ, "[V_1_3,FLOW_MOD/DELETE_STRICT,len=48,xid=101]", h);
    }

    @Test
    public void headerFormatGroupMod() {
        print(EOL + "headerFormatGroupMod()");
        String h = hdr(mkEvt(mkMsg(GROUP_MOD, GroupModCommand.ADD)));
        print(h);
        assertEquals(AM_NEQ, "[V_1_3,GROUP_MOD/ADD,len=16,xid=101]", h);
    }

    @Test
    public void headerFormatPortMod() {
        print(EOL + "headerFormatPortMod()");
        String h = hdr(mkEvt(mkMsg(PORT_MOD, null)));
        print(h);
        assertEquals(AM_NEQ, "[V_1_3,PORT_MOD,len=40,xid=101]", h);
    }

    @Test
    public void headerFormatTableMod() {
        print(EOL + "headerFormatTableMod()");
        String h = hdr(mkEvt(mkMsg(TABLE_MOD, null)));
        print(h);
        assertEquals(AM_NEQ, "[V_1_3,TABLE_MOD,len=16,xid=101]", h);
    }

    @Test
    public void headerFormatMultipartRequest() {
        print(EOL + "headerFormatMultipartRequest()");
        String h = hdr(mkEvt(mkMsg(MULTIPART_REQUEST, MultipartType.FLOW)));
        print(h);
        assertEquals(AM_NEQ, "[V_1_3,MULTIPART_REQUEST/FLOW,len=48,xid=101]", h);
    }

    @Test
    public void headerFormatMultipartReply() {
        print(EOL + "headerFormatMultipartReply()");
        String h = hdr(mkEvt(mkMsg(MULTIPART_REPLY, MultipartType.DESC)));
        print(h);
        assertEquals(AM_NEQ, "[V_1_3,MULTIPART_REPLY/DESC,len=1072,xid=101]", h);
    }

    @Test
    public void headerFormatBarrierRequest() {
        print(EOL + "headerFormatBarrierRequest()");
        String h = hdr(mkEvt(mkMsg(BARRIER_REQUEST, null)));
        print(h);
        assertEquals(AM_NEQ, "[V_1_3,BARRIER_REQUEST,len=8,xid=101]", h);
    }

    @Test
    public void headerFormatBarrierReply() {
        print(EOL + "headerFormatBarrierReply()");
        String h = hdr(mkEvt(mkMsg(BARRIER_REPLY, null)));
        print(h);
        assertEquals(AM_NEQ, "[V_1_3,BARRIER_REPLY,len=8,xid=101]", h);
    }

    @Test
    public void headerFormatQueueGetConfigRequest() {
        print(EOL + "headerFormatQueueGetConfigRequest()");
        String h = hdr(mkEvt(mkMsg(QUEUE_GET_CONFIG_REQUEST, null)));
        print(h);
        assertEquals(AM_NEQ, "[V_1_3,QUEUE_GET_CONFIG_REQUEST,len=16,xid=101]", h);
    }

    @Test
    public void headerFormatQueueGetConfigReply() {
        print(EOL + "headerFormatQueueGetConfigReply()");
        String h = hdr(mkEvt(mkMsg(QUEUE_GET_CONFIG_REPLY, null)));
        print(h);
        assertEquals(AM_NEQ, "[V_1_3,QUEUE_GET_CONFIG_REPLY,len=16,xid=101]", h);
    }

    @Test
    public void headerFormatRoleRequest() {
        print(EOL + "headerFormatRoleRequest()");
        String h = hdr(mkEvt(mkMsg(ROLE_REQUEST, ControllerRole.MASTER)));
        print(h);
        assertEquals(AM_NEQ, "[V_1_3,ROLE_REQUEST/MASTER,len=24,xid=101]", h);
    }

    @Test
    public void headerFormatRoleReply() {
        print(EOL + "headerFormatRoleReply()");
        String h = hdr(mkEvt(mkMsg(ROLE_REPLY, ControllerRole.SLAVE)));
        print(h);
        assertEquals(AM_NEQ, "[V_1_3,ROLE_REPLY/SLAVE,len=24,xid=101]", h);
    }

    @Test
    public void headerFormatGetAsyncRequest() {
        print(EOL + "headerFormatGetAsyncRequest()");
        String h = hdr(mkEvt(mkMsg(GET_ASYNC_REQUEST, null)));
        print(h);
        assertEquals(AM_NEQ, "[V_1_3,GET_ASYNC_REQUEST,len=8,xid=101]", h);
    }

    @Test
    public void headerFormatGetAsyncReply() {
        print(EOL + "headerFormatGetAsyncReply()");
        String h = hdr(mkEvt(mkMsg(GET_ASYNC_REPLY, null)));
        print(h);
        assertEquals(AM_NEQ, "[V_1_3,GET_ASYNC_REPLY,len=32,xid=101]", h);
    }

    @Test
    public void headerFormatSetAsync() {
        print(EOL + "headerFormatSetAsync()");
        String h = hdr(mkEvt(mkMsg(SET_ASYNC, null)));
        print(h);
        assertEquals(AM_NEQ, "[V_1_3,SET_ASYNC,len=32,xid=101]", h);
    }

    @Test
    public void headerFormatMeterMod() {
        print(EOL + "headerFormatMeterMod()");
        String h = hdr(mkEvt(mkMsg(METER_MOD, MeterModCommand.MODIFY)));
        print(h);
        assertEquals(AM_NEQ, "[V_1_3,METER_MOD/MODIFY,len=16,xid=101]", h);
    }

}
