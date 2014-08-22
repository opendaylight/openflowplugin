/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.instr.*;
import org.opendaylight.of.lib.match.MatchField;
import org.opendaylight.of.lib.match.MatchLookup;
import org.opendaylight.of.lib.match.OxmBasicFieldType;
import org.opendaylight.util.SafeMap;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.MacAddress;

import java.util.Set;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.msg.MessageType.*;
import static org.opendaylight.util.ByteUtils.toHexArrayString;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Base class for testing openflow message classes.
 *
 * @author Simon Hunt
 */
public abstract class OfmTest extends AbstractTest {

    protected static final String MSG_DIR = "msg/";

    // mapping of message type to concrete class
    private static final SafeMap<MessageType,
            Class<? extends OpenflowMessage>> OFM_CLS =
            new SafeMap.Builder<MessageType,
                    Class<? extends OpenflowMessage>>(OpenflowMessage.class)
                    .add(HELLO, OfmHello.class)
                    .add(ERROR, OfmError.class)
                    .add(ECHO_REQUEST, OfmEchoRequest.class)
                    .add(ECHO_REPLY, OfmEchoReply.class)
                    .add(EXPERIMENTER, OfmExperimenter.class)
                    .add(FEATURES_REQUEST, OfmFeaturesRequest.class)
                    .add(FEATURES_REPLY, OfmFeaturesReply.class)
                    .add(GET_CONFIG_REQUEST, OfmGetConfigRequest.class)
                    .add(GET_CONFIG_REPLY, OfmGetConfigReply.class)
                    .add(SET_CONFIG, OfmSetConfig.class)
                    .add(PACKET_IN, OfmPacketIn.class)
                    .add(FLOW_REMOVED, OfmFlowRemoved.class)
                    .add(PORT_STATUS, OfmPortStatus.class)
                    .add(PACKET_OUT, OfmPacketOut.class)
                    .add(FLOW_MOD, OfmFlowMod.class)
                    .add(GROUP_MOD, OfmGroupMod.class)
                    .add(PORT_MOD, OfmPortMod.class)
                    .add(TABLE_MOD, OfmTableMod.class)
                    .add(MULTIPART_REQUEST, OfmMultipartRequest.class)
                    .add(MULTIPART_REPLY, OfmMultipartReply.class)
                    .add(BARRIER_REQUEST, OfmBarrierRequest.class)
                    .add(BARRIER_REPLY, OfmBarrierReply.class)
                    .add(QUEUE_GET_CONFIG_REQUEST, OfmQueueGetConfigRequest.class)
                    .add(QUEUE_GET_CONFIG_REPLY, OfmQueueGetConfigReply.class)
                    .add(ROLE_REQUEST, OfmRoleRequest.class)
                    .add(ROLE_REPLY, OfmRoleReply.class)
                    .add(GET_ASYNC_REQUEST, OfmGetAsyncRequest.class)
                    .add(GET_ASYNC_REPLY, OfmGetAsyncReply.class)
                    .add(SET_ASYNC, OfmSetAsync.class)
                    .add(METER_MOD, OfmMeterMod.class)
                    .build();

    // mapping of message type to concrete mutable class
    private static final SafeMap<MessageType,
            Class<? extends MutableMessage>> OFM_MUT_CLS =
            new SafeMap.Builder<MessageType,
                    Class<? extends MutableMessage>>(MutableMessage.class)
                    .add(HELLO, OfmMutableHello.class)
                    .add(ERROR, OfmMutableError.class)
                    .add(ECHO_REQUEST, OfmMutableEchoRequest.class)
                    .add(ECHO_REPLY, OfmMutableEchoReply.class)
                    .add(EXPERIMENTER, OfmMutableExperimenter.class)
                    .add(FEATURES_REQUEST, OfmMutableFeaturesRequest.class)
                    .add(FEATURES_REPLY, OfmMutableFeaturesReply.class)
                    .add(GET_CONFIG_REQUEST, OfmMutableGetConfigRequest.class)
                    .add(GET_CONFIG_REPLY, OfmMutableGetConfigReply.class)
                    .add(SET_CONFIG, OfmMutableSetConfig.class)
                    .add(PACKET_IN, OfmMutablePacketIn.class)
                    .add(FLOW_REMOVED, OfmMutableFlowRemoved.class)
                    .add(PORT_STATUS, OfmMutablePortStatus.class)
                    .add(PACKET_OUT, OfmMutablePacketOut.class)
                    .add(FLOW_MOD, OfmMutableFlowMod.class)
                    .add(GROUP_MOD, OfmMutableGroupMod.class)
                    .add(PORT_MOD, OfmMutablePortMod.class)
                    .add(TABLE_MOD, OfmMutableTableMod.class)
                    .add(MULTIPART_REQUEST, OfmMutableMultipartRequest.class)
                    .add(MULTIPART_REPLY, OfmMutableMultipartReply.class)
                    .add(BARRIER_REQUEST, OfmMutableBarrierRequest.class)
                    .add(BARRIER_REPLY, OfmMutableBarrierReply.class)
                    .add(QUEUE_GET_CONFIG_REQUEST, OfmMutableQueueGetConfigRequest.class)
                    .add(QUEUE_GET_CONFIG_REPLY, OfmMutableQueueGetConfigReply.class)
                    .add(ROLE_REQUEST, OfmMutableRoleRequest.class)
                    .add(ROLE_REPLY, OfmMutableRoleReply.class)
                    .add(GET_ASYNC_REQUEST, OfmMutableGetAsyncRequest.class)
                    .add(GET_ASYNC_REPLY, OfmMutableGetAsyncReply.class)
                    .add(SET_ASYNC, OfmMutableSetAsync.class)
                    .add(METER_MOD, OfmMutableMeterMod.class)
                    .build();

    /** Parses the given openflow message to the point where
     * we throw a VersionNotSupportedException.
     *
     * @param datafile the basename of the .hex data file
     */
    protected void verifyNotSupported(String datafile) {
        try {
            OfPacketReader pkt = getPacketReader(MSG_DIR + datafile + HEX);
            MessageFactory.parseMessage(pkt);
            fail(AM_NOEX);

        } catch (VersionNotSupportedException e) {
            print(FMT_EX, e);
            print(FMT_EX_CAUSE, e.getCause());
        } catch (Exception e) {
            print(e);
            fail(AM_WREX + " " + e);
        }
    }

    /**
     * Creates and returns a packet reader containing the contents of the
     * specified test file.
     *
     * @param datafile the test file name
     * @return the initialized packet reader
     */
    protected OfPacketReader getOfmTestReader(String datafile) {
        return getPacketReader(MSG_DIR + datafile + HEX);
    }

    /** Parses an openflow message and verifies attributes of its header.
     *
     * @param datafile the basename of the .hex data file
     * @param expPv expected protocol version
     * @param expType expected message type
     * @param expLen expected message length
     * @param expXid expected transaction id
     * @return the instantiated message
     */
    protected OpenflowMessage verifyMsgHeader(String datafile,
                                              ProtocolVersion expPv,
                                              MessageType expType, int expLen,
                                              long expXid) {
        OpenflowMessage msg = null;
        OfPacketReader pkt = null;
        Class<? extends OpenflowMessage> expClass = OFM_CLS.get(expType);
        if (expClass == OpenflowMessage.class)
            fail("OFM Type to Class Map needs updating!");

        try {
            pkt = getOfmTestReader(datafile);
            msg = MessageFactory.parseMessage(pkt);
            print(msg.toDebugString());
            assertEquals(AM_NEQ, expPv, msg.getVersion());
            assertEquals(AM_NEQ, expType, msg.getType());
            assertEquals(AM_NEQ, expLen, msg.header.length); // no getter!
            assertEquals(AM_NEQ, expLen, pkt.array().length); // verify test file
            assertEquals(AM_NEQ, expXid, msg.getXid());
            assertTrue(AM_WRCL, expClass.isInstance(msg));

        } catch (MessageParseException e) {
            print(e);
            fail("Unexpected MPE " + e);
        } catch (Exception e) {
            print(e);
            fail(AM_UNEX + " " + e);
        }
        checkEOBuffer(pkt);
        return msg;
    }

    /** Utility method to parse an openflow message and verify attributes
     * of its header; where the transaction id (xid) is expected to be 0.
     *
     * @param datafile the basename of the .hex data file
     * @param expPv expected protocol version
     * @param expType expected message type
     * @param expLen expected message length
     * @return the instantiated message
     */
    protected OpenflowMessage verifyMsgHeader(String datafile,
                                              ProtocolVersion expPv,
                                              MessageType expType, int expLen) {
        return verifyMsgHeader(datafile, expPv, expType, expLen, 0);
    }

    /** Verifies that the specified match field is of the correct type,
     * and contains the specified value and mask (if any).
     *
     * @param mf the match field
     * @param expType the expected type
     * @param expValueMask value and mask
     */
    protected void verifyMatchField(MatchField mf, OxmBasicFieldType expType,
                                    Object... expValueMask) {
        MatchLookup.verifyField(mf, expType, expValueMask);
    }

    /** Verifies that the specified action is of the correct type.
     *
     * @param act the action
     * @param expType the expected type
     */
    protected void verifyAction(Action act, ActionType expType) {
        ActionLookup.verifyAction(act, expType);
    }

    /** Verifies that the specified action is of the correct type, and has
     * the expected value.
     *
     * @param act the action
     * @param expType the expected type
     * @param expValue the expected value
     */
    protected void verifyAction(Action act, ActionType expType,
                                Object expValue) {
        ActionLookup.verifyAction(act, expType, expValue);
    }

    /** Verifies that the set field action contains the correct field
     * information.
     *
     * @param a the set field action
     * @param expFt the expected match field type
     * @param expValue the expected match field value
     */
    protected void verifyActionSetField(Action a, OxmBasicFieldType expFt,
                              Object expValue) {
        ActionLookup.verifyActionSetField(a, expFt, expValue);
    }

    /** Verify a GOTO_TABLE instruction.
     *
     * @param ins the instruction
     * @param expTableId the expected table id
     */
    protected void verifyInstrGoTab(Instruction ins, int expTableId) {
        InstrLookup.verifyInstrGoTab(ins, expTableId);
    }

    /** Verify a WRITE_METADATA instruction.
     *
     * @param ins the instruction
     * @param expMeta the expected metadata
     * @param expMask the expected metadata mask
     */
    protected void verifyInstrWrMeta(Instruction ins, long expMeta,
                                     long expMask) {
        InstrLookup.verifyInstrWrMeta(ins, expMeta, expMask);
    }

    /** Verify one of the "Action" Instructions: WriteActions, ApplyActions
     * or ClearActions. Specify the number of actions expected in the
     * instruction. This must be 0 for ClearActions.
     * Note that this method does not verify the action contents; you must
     * do that elsewhere.
     *
     * @param ins the instruction
     * @param expType the expected type
     * @param expActionCount the expected action count
     */
    protected void verifyInstrActions(Instruction ins, InstructionType expType,
                                      int expActionCount) {
        InstrLookup.verifyInstrActions(ins, expType, expActionCount);
    }

    /** Verifies a METER instruction.
     *
     * @param ins the instruction
     * @param expMeterId the expected meter id
     */
    protected void verifyInstrMeter(Instruction ins, long expMeterId) {
        InstrLookup.verifyInstrMeter(ins, expMeterId);
    }

    // Stuff for Mutable Messages

    /** Verifies the header information of a given message.
     *
     * @param msg the message
     * @param expPv the expected protocol version
     * @param expType the expected message type
     * @param expXid the expected XID
     */
    protected void verifyHeader(Message msg, ProtocolVersion expPv,
                                MessageType expType, long expXid) {
        assertEquals(AM_NEQ, expPv, msg.getVersion());
        assertEquals(AM_NEQ, expType, msg.getType());
        assertEquals(AM_NEQ, expXid, msg.getXid());
    }

    /** Verifies the header information of the given mutable message, and
     * also verifies that the concrete instance is of the correct type
     * for the given message type.
     *
     * @param msg the mutable message
     * @param expPv the expected protocol version
     * @param expType the expected message type
     * @param expXid the expected XID
     * @return the message
     */
    protected MutableMessage verifyMutableHeader(MutableMessage msg,
                                                 ProtocolVersion expPv,
                                                 MessageType expType,
                                                 long expXid) {
        print(msg.toDebugString() + EOL);
        verifyHeader(msg, expPv, expType, expXid);

        Class<? extends MutableMessage> expClass = OFM_MUT_CLS.get(expType);
        if (expClass == MutableMessage.class)
            fail("OFM Type to Mutable Class Map needs updating!");
        assertTrue(AM_WRCL, expClass.isInstance(msg));
        return msg;
    }

    /** Returns a byte array slurped from a .hex file in the message dir.
     *
     * @param datafile the data file
     * @return the slurped byte array
     */
    protected byte[] getExpByteArray(String datafile) {
        return slurpedBytes(MSG_DIR + datafile + HEX);
    }

    /** Prints the hex array output of the given byte array.
     *
     * @param array the array
     */
    protected void printHexArray(byte[] array) {
        StringBuilder sb = new StringBuilder(toHexArrayString(array));
        int len = sb.length();
        if (len > 102)
            sb.replace(100, len, "... ]");
        print("Encoded: [{}] {}", array.length, sb);
    }

    /** Encodes the given message to a byte array, and verifies that the
     * result matches the test data in the specified file.
     *
     * @param msg the OpenFlow message to encode
     * @param data the name of the test data file
     */
    protected void encodeAndVerifyMessage(OpenflowMessage msg, String data) {
        print(msg.toDebugString());
        byte[] expData = getExpByteArray(data);
        byte[] encoded = new byte[0];
        try {
            encoded = MessageFactory.encodeMessage(msg);
        } catch (Exception e) {
            print(e);
            fail(AM_UNEX);
        }
        printHexArray(encoded);
        debugPrint(msg.getClass().getSimpleName(), expData, encoded);
        assertArrayEquals(AM_NEQ, expData, encoded);
    }

    /** Returns a test file wrapped in a channel buffer, relative
     * to the "msg/" directory.
     *
     * @param testfile the test file name
     * @return the test data wrapped in an array-backed channel buffer
     */
    protected OfPacketReader getMsgPkt(String testfile) {
        return getPacketReader("msg/" + testfile + HEX);
    }

    /** Verifies that the data written into an OfPacketWriter buffer matches
     * the expected data.
     *
     * @param label a test identifying label
     * @param expData the expected data
     * @param pkt the just-written packet buffer
     */
    protected void verifyEncodement(String label, byte[] expData,
                                    OfPacketWriter pkt) {
        assertEquals("buffer too big", 0, pkt.writableBytes());
        byte[] encoded = pkt.array();
        printHexArray(encoded);
        debugPrint(label, expData, encoded);
        assertArrayEquals(AM_NEQ, expData, encoded);
    }

    /** Verifies that the port structure is as expected.
     *
     * @param p the port
     * @param expPNum expected port number
     * @param expMac expected hardware address
     * @param expName expected name
     * @param expCfg expected config flags
     * @param expState expected state flags
     * @param expCurr expected current feature flags
     * @param expAdv expected advertised feature flags
     * @param expSupp expected supported feature flags
     * @param expPeer expected peer feature flags
     */
    protected void verifyPort(Port p, BigPortNumber expPNum,
                              MacAddress expMac, String expName,
                              Set<PortConfig> expCfg, Set<PortState> expState,
                              Set<PortFeature> expCurr, Set<PortFeature> expAdv,
                              Set<PortFeature> expSupp, Set<PortFeature> expPeer) {
        verifyPort(p, expPNum, expMac, expName, expCfg, expState,
                expCurr, expAdv, expSupp, expPeer, 0, 0);
    }

    /** Verifies that the port structure is as expected.
     *
     * @param p the port
     * @param expPNum expected port number
     * @param expMac expected hardware address
     * @param expName expected name
     * @param expCfg expected config flags
     * @param expState expected state flags
     * @param expCurr expected current feature flags
     * @param expAdv expected advertised feature flags
     * @param expSupp expected supported feature flags
     * @param expPeer expected peer feature flags
     * @param expCurrSpeed expected current speed
     * @param expMaxSpeed expected max speed
     */
    protected void verifyPort(Port p, BigPortNumber expPNum,
                              MacAddress expMac, String expName,
                              Set<PortConfig> expCfg, Set<PortState> expState,
                              Set<PortFeature> expCurr, Set<PortFeature> expAdv,
                              Set<PortFeature> expSupp, Set<PortFeature> expPeer,
                              long expCurrSpeed, long expMaxSpeed) {
        assertEquals(AM_NEQ, expPNum, p.getPortNumber());
        assertEquals(AM_NEQ, expMac, p.getHwAddress());
        assertEquals(AM_NEQ, expName, p.getName());
        assertEquals(AM_NEQ, expCfg, p.getConfig());
        assertEquals(AM_NEQ, expState, p.getState());
        assertEquals(AM_NEQ, expCurr, p.getCurrent());
        assertEquals(AM_NEQ, expAdv, p.getAdvertised());
        assertEquals(AM_NEQ, expSupp, p.getSupported());
        assertEquals(AM_NEQ, expPeer, p.getPeer());
        assertEquals(AM_NEQ, expCurrSpeed, p.getCurrentSpeed());
        assertEquals(AM_NEQ, expMaxSpeed, p.getMaxSpeed());
    }

}
