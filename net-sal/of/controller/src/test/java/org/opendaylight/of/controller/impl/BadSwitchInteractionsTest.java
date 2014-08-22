/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.of.lib.dt.BufferId;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.match.MatchFactory;
import org.opendaylight.of.lib.match.MutableMatch;
import org.opendaylight.of.lib.match.OxmBasicFieldType;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.of.mockswitch.OfmEncoder;
import org.opendaylight.of.mockswitch.PipelineFactory;
import org.opendaylight.util.api.NotFoundException;
import org.opendaylight.util.junit.TestLogger;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.VlanId;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.fail;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.msg.MessageFactory.create;
import static org.opendaylight.of.lib.msg.MessageType.ECHO_REQUEST;
import static org.opendaylight.of.lib.msg.MessageType.PACKET_IN;
import static org.opendaylight.util.ByteUtils.toHexString;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Testing specific issues that came up from undesired switch behavior.
 *
 * @author Simon Hunt
 */
public class BadSwitchInteractionsTest extends AbstractControllerTest {
    private static final String MSG_BEFORE_CORRUPT = "Corrupting> [{}]";
    private static final String MSG_AFTER_CORRUPT =  "Corrupted>> [{}]";

    private static final String DEF = SW13P8;
    private static final DataPathId DPID = SW13P8_DPID;

    private static final BigPortNumber INPORT = bpn(13);
    private static final BufferId BUFFER = BufferId.NO_BUFFER;
    private static final TableId TABLE = tid(2);
    private static final VlanId VLAN_ID = VlanId.valueOf("42");

    private static final int PI_TOTAL_LEN = 16;
    private static final byte[] FAKE_PACKET = {
            0x77, 0x66, 0x77, 0x66, 0x77, 0x66, 0x77, 0x66,
            0x77, 0x66, 0x77, 0x66, 0x77, 0x66, 0x77, 0x66,
    };

    // a simple DTO that defines where and how to corrupt the byte encoding
    private static class Corruption {
        private int offset;
        private byte value;

        Corruption(int offset, byte value) {
            this.offset = offset;
            this.value = value;
        }
    }

    // A test switch that will send badly formatted messages
    private class BadSwitch extends BasicSwitch {
        public BadSwitch(DataPathId dpid, String defPath) throws IOException {
            super(dpid, defPath);
        }

        @Override
        protected PipelineFactory createPipelineFactory() {
            return new CorruptiblePipelineFactory();
        }

        // send a bad packet in message to the controller
        public void sendBadPi() {
            OfmMutablePacketIn pi = (OfmMutablePacketIn)
                    create(V_1_3, PACKET_IN, PacketInReason.NO_MATCH);
            pi.totalLen(PI_TOTAL_LEN).data(FAKE_PACKET);
            pi.bufferId(BUFFER).tableId(TABLE).match(createMatch());

            OpenflowMessage msg = pi.toImmutable();
            print(msg.toDebugString());

            // mark the message for corruption...
            corruption = new Corruption(40, (byte) 0x00); // wipe out Present bit
            send(msg);
        }

        private Match createMatch() {
            MutableMatch m = MatchFactory.createMatch(V_1_3);
            m.addField(createBasicField(V_1_3, OxmBasicFieldType.IN_PORT, INPORT));
            m.addField(createBasicField(V_1_3, OxmBasicFieldType.VLAN_VID, VLAN_ID));
            return (Match) m.toImmutable();
        }

        public void sendEchoRequest() {
            OpenflowMessage echo = create(V_1_3, ECHO_REQUEST).toImmutable();
            // need to cache the request, so we can reconcile the reply
            cache(ECHO_REQUEST, echo);
            send(echo);
        }
    }

    // A pipeline factory that uses a corruptible message encoder
    private class CorruptiblePipelineFactory extends PipelineFactory {
        @Override
        protected OfmEncoder createOfmEncoder() {
            return new OfmCorruptibleEncoder();
        }
    }

    // An encoder where we can selectively corrupt the bytes...
    private class OfmCorruptibleEncoder extends OfmEncoder {
        @Override
        protected Object encode(ChannelHandlerContext ctx,
                                Channel channel, Object ofm) throws Exception {
            // We have to assume that the input is OpenflowMessage.
            OpenflowMessage msg = (OpenflowMessage) ofm;
            byte[] encoded = MessageFactory.encodeMessage(msg);
            tweakEncodedBytes(encoded);
            return ChannelBuffers.wrappedBuffer(encoded);
        }

        private void tweakEncodedBytes(byte[] encoded) {
            if (corruption != null) {
                print(MSG_BEFORE_CORRUPT, toHexString(encoded));
                encoded[corruption.offset] = corruption.value;
                print(MSG_AFTER_CORRUPT, toHexString(encoded));
                corruption = null;
            }
        }
    }

    private static final TestLogger tlog = new TestLogger();

    private Corruption corruption = null;

    // ======================================================================
    // utility class to give us access to the message parser logger
    private static class MsgLibTestUtils extends MessageLibTestUtils {
        public void setTestLogger() {
            setMessageParserLogger(tlog);
        }
        public void restoreLogger() {
            restoreMessageParserLogger();
        }
    }

    // ======================================================================

    private static MsgLibTestUtils msgLibTestUtils;

    @BeforeClass
    public static void classSetUp() {
        setUpLogger();
        msgLibTestUtils = new MsgLibTestUtils();
        msgLibTestUtils.setTestLogger();
    }

    @AfterClass
    public static void classTearDown() {
        msgLibTestUtils.restoreLogger();
    }

    @After
    public void tearDown() {
        cmgr.shutdown();
    }

    // ======================================================================
    // === HELPER methods

    private void initController() {
        alertSink = new AlertLogger();
        roleAdvisor = new MockRoleAdvisor(DPID);
        eds = new MockEventDispatcher();

        cmgr = new TestControllerManager(DEFAULT_CTRL_CFG, alertSink,
                roleAdvisor, eds);
        cs = cmgr;
        cmgr.startIOProcessing();
        lmgr = cmgr.getListenerManager();
        lmgr.resetStats();
        initTxRxControl(lmgr);
        print("... controller activated ...");
    }

    private BadSwitch connectBadSwitch() {
        switchesReady = new CountDownLatch(1);
        lmgr.setDataPathAddedLatch(switchesReady);
        BadSwitch sw = null;
        try {
            sw = new BadSwitch(DPID, DEF);
            sw.activate();
            print("... bad-switch activated : {} ...", sw.getDpid());
            waitForHandshake();
        } catch (IOException e) {
            print(e);
            fail(AM_UNEX);
        }
        return sw;
    }

    private void disconnectSwitch(BasicSwitch sw) {
        try {
            lmgr.getDataPathInfo(DPID);
            switchesGone = new CountDownLatch(1);
            lmgr.setDataPathRemovedLatch(switchesGone);
            sw.deactivate();
            waitForDisconnect();
        } catch (NotFoundException e) {
            // already gone
        }
    }


    private void pause() {
        delay(200);
    }

    @Test
    public void malformedPacketIn() {
        beginTest("malformedPacketIn");
        initController();
        startRecording(10);

        BadSwitch sw = connectBadSwitch();
        print(cs);
        pause(); // wait for port stats message to be processed
        print(cs.getDataPathInfo(DPID).ports());

        sw.sendEchoRequest();
        pause();

        sw.sendBadPi();
        pause();
        // NOTE: intentionally truncating the message before the XID, as we
        //  don't know what that will be since it is issued by the factory
//        tlog.assertWarningContains("Parse FAILED: hdr=[V_1_3,PACKET_IN,66,");
        // TODO: augment test logger to handle multiple messages
        tlog.assertWarning("Parse terminated before end. Start=0, Target=66, Read=42, Remaining=24");

        sw.sendEchoRequest();
        pause();

        disconnectSwitch(sw);
        stopRecordingAndPrintDebugTrace();
        endTest();
    }
}
