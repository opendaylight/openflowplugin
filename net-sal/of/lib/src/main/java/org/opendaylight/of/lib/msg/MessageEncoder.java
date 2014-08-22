/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.IncompleteMessageException;
import org.opendaylight.of.lib.IncompleteStructureException;
import org.opendaylight.of.lib.OfPacketWriter;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.err.ErrorType;
import org.opendaylight.of.lib.instr.ActionFactory;
import org.opendaylight.of.lib.instr.InstructionFactory;
import org.opendaylight.of.lib.match.MatchFactory;
import org.opendaylight.of.lib.mp.MpBodyFactory;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.*;

/**
 * Encodes OpenFlow messages as byte arrays.
 *
 * @author Simon Hunt
 * @author Scott Simes
 * @author Radhika Hegde
 * @author Thomas Vachuska
 */
class MessageEncoder {

    /** Encodes the specified OpenFlow message, returning the result in
     * a newly allocated byte array.
     *
     * @param msg the message to encode
     * @return the encoded message in a buffer
     * @throws IllegalArgumentException if the message is mutable
     * @throws IncompleteMessageException if the message was
     *          insufficiently initialized
     */
    static byte[] encode(OpenflowMessage msg)
            throws IncompleteMessageException {
        OfPacketWriter pkt = new OfPacketWriter(msg.header.length);
        encodeMsg(msg, pkt);
        return pkt.array();
    }

    /**
     * Encodes the specified OpenFlow message into the supplied byte buffer.
     * 
     * @param msg the message to encode
     * @param buffer byte buffer to receive the encoded message bytes
     * @throws IllegalArgumentException if the message is mutable
     * @throws IncompleteMessageException if the message was insufficiently
     *         initialized
     * @throws BufferOverflowException if the given buffer does not have
     *         sufficient number of remaining bytes
     */
    static void encode(OpenflowMessage msg, ByteBuffer buffer)
            throws IncompleteMessageException {
        if (buffer.remaining() < msg.length())
            throw new BufferOverflowException();

        OfPacketWriter pkt = new OfPacketWriter(buffer);
        encodeMsg(msg, pkt);
    }

    /** Encodes the specified message, writing it into the specified buffer.
     * It is assumed that the writer index is in the correct place for
     * the start of the message. At the end of this method call, the
     * writer index will have advanced by the length of the encoded message.
     *
     * @param msg the message to write
     * @param pkt the buffer to write into
     * @throws IllegalArgumentException if the message is mutable
     * @throws IncompleteMessageException if the message was
     *          insufficiently initialized
     */
    private static void encodeMsg(OpenflowMessage msg, OfPacketWriter pkt)
            throws IncompleteMessageException {
        notMutable(msg);
        // only allow the message to be encoded if we support the version
        MessageParser.checkSupported(msg.header);
        // first, write out the message header
        OpenflowMessage.writeHeader(msg.header, pkt);
        // now write out the rest of the payload (if any)
        final ProtocolVersion pv = msg.getVersion();
        switch (msg.header.type) {
            //=== The following are header-only, thus no further encoding.
//            case FEATURES_REQUEST:
//            case GET_CONFIG_REQUEST:
//            case BARRIER_REQUEST:
//            case BARRIER_REPLY:
//            case GET_ASYNC_REQUEST:

            //=== The following have been dealt with
            case HELLO:
                encodeHello((OfmHello) msg, pkt);
                break;
            case ERROR:
                encodeError((OfmError) msg, pkt);
                break;
            case FEATURES_REPLY:
                encodeFeaturesReply((OfmFeaturesReply) msg, pkt);
                break;
            case ECHO_REQUEST:
            case ECHO_REPLY:
                encodeEcho((Echo) msg, pkt);
                break;
            case EXPERIMENTER:
                encodeExperimenter((OfmExperimenter) msg, pkt);
                break;
            case SET_CONFIG:
            case GET_CONFIG_REPLY:
                encodeSwitchConfig((SwitchConfig) msg, pkt);
                break;
            case PACKET_IN:
                encodePacketIn((OfmPacketIn) msg, pkt);
                break;
            case PACKET_OUT:
                encodePacketOut((OfmPacketOut) msg, pkt);
                break;
            case FLOW_REMOVED:
                encodeFlowRemoved((OfmFlowRemoved)msg, pkt);
                break;
            case TABLE_MOD:
                verMin11(pv);
                encodeTableMod((OfmTableMod) msg, pkt);
                break;
            case FLOW_MOD:
                encodeFlowMod((OfmFlowMod) msg, pkt);
                break;
            case GROUP_MOD:
                verMin11(pv);
                encodeGroupMod((OfmGroupMod) msg, pkt);
                break;
            case PORT_MOD:
                encodePortMod((OfmPortMod) msg, pkt);
                break;
            case METER_MOD:
                verMin13(pv);
                encodeMeterMod((OfmMeterMod) msg, pkt);
                break;
            case MULTIPART_REQUEST:
                // do NOT check for version. See OfmMultipartRequest javadocs
                encodeMultipartRequest((OfmMultipartRequest) msg, pkt);
                break;
            case MULTIPART_REPLY:
                // do NOT check for version. See OfmMultipartReply javadocs
                encodeMultipartReply((OfmMultipartReply) msg, pkt);
                break;
            case PORT_STATUS:
                encodePortStatus((OfmPortStatus)msg, pkt);
                break;
            case QUEUE_GET_CONFIG_REQUEST:
                encodeQueueGetCfgRequest((OfmQueueGetConfigRequest) msg, pkt);
                break;
            case QUEUE_GET_CONFIG_REPLY:
                encodeQueueGetCfgReply((OfmQueueGetConfigReply) msg, pkt);
                break;
            case SET_ASYNC:
            case GET_ASYNC_REPLY:
                encodeAsyncConfig((AsyncConfig) msg, pkt);
                break;
            case ROLE_REQUEST:
            case ROLE_REPLY:
                verMin12(pv);
                encodeRole((Role) msg, pkt);
                break;

            default:
                // default is to do nothing further
                break;
        }
    }

    // encodes a hello
    private static void encodeHello(OfmHello msg, OfPacketWriter pkt)
            throws IncompleteMessageException {
        msg.validate();
        HelloElementFactory.encodeElementList(msg.elements, pkt);
    }

    // encodes an error
    private static void encodeError(OfmError msg, OfPacketWriter pkt)
            throws IncompleteMessageException {
        msg.validate();
        final ProtocolVersion pv = msg.getVersion();
        pkt.writeU16(msg.type.getCode(pv));
        if (msg.type == ErrorType.EXPERIMENTER) {
            OfmErrorExper msgErrExp = (OfmErrorExper) msg;
            pkt.writeU16(msgErrExp.expType);
            pkt.writeInt(msgErrExp.id);
        } else {
            pkt.writeU16(msg.code.getCode(pv));
        }
        if (msg.data != null)
            pkt.writeBytes(msg.data);
    }

    // encodes an echo request/reply message
    private static void encodeEcho(Echo msg, OfPacketWriter pkt)
            throws IncompleteMessageException {
        msg.validate();
        if (msg.data != null)
            pkt.writeBytes(msg.data);
    }

    private static final int EXP_11_RESERVED = 0;

    // encode an experimenter message
    private static void encodeExperimenter(OfmExperimenter msg,
                                           OfPacketWriter pkt)
            throws IncompleteMessageException {
        msg.validate();
        final ProtocolVersion pv = msg.getVersion();
        pkt.writeInt(msg.id);
        if(pv.gt(V_1_0))
            pkt.writeInt(pv.gt(V_1_1) ? msg.type : EXP_11_RESERVED);
        if(msg.data != null)
            pkt.writeBytes(msg.data);
    }

    // encodes a features-reply
    private static void encodeFeaturesReply(OfmFeaturesReply msg,
                                            OfPacketWriter pkt)
            throws IncompleteMessageException {
        msg.validate();
        final ProtocolVersion pv = msg.getVersion();
        pkt.write(msg.dpid);
        pkt.writeU32(msg.numBuffers);
        pkt.writeU8(msg.numTables);
        if (pv.le(V_1_2)) {
            pkt.writeZeros(MessageParser.PAD_FEAT_REP_1_012);
        } else {
            pkt.writeU8(msg.auxId);
            pkt.writeZeros(MessageParser.PAD_FEAT_REP_1);
        }
        pkt.writeU32(Capability.encodeBitmap(msg.capabilities, pv));
        if (pv == V_1_0)
            pkt.writeU32(SupportedAction.encodeBitmap(msg.suppActions, pv));
        else
            pkt.writeZeros(MessageParser.PAD_FEAT_REP_2);
        if (pv.le(V_1_2))
            try {
                PortFactory.encodePortList(msg.ports, pkt);
            } catch (IncompleteStructureException e) {
                throw new IncompleteMessageException(e);
            }
    }

    // encodes a switch-config based message
    private static void encodeSwitchConfig(SwitchConfig msg,
                                           OfPacketWriter pkt)
            throws IncompleteMessageException {
        msg.validate();
        pkt.writeU16(ConfigFlag.encodeBitmap(msg.flags, msg.getVersion()));
        pkt.writeU16(msg.missSendLength);
    }

    // encodes a packet-in message
    private static void encodePacketIn(OfmPacketIn msg, OfPacketWriter pkt)
            throws IncompleteMessageException {
        msg.validate();
        final ProtocolVersion pv = msg.getVersion();
        pkt.write(msg.bufferId);
        pkt.writeU16(msg.totalLen);
        if (pv == V_1_0)
            PortFactory.encodePortNumber(msg.inPort, pkt, pv);
        pkt.writeU8(msg.reason.getCode(pv));
        if (pv.ge(V_1_1))
            pkt.write(msg.tableId);
        if (pv.ge(V_1_3))
            pkt.writeLong(msg.cookie);
        if (pv.ge(V_1_2))
            MatchFactory.encodeMatch(msg.match, pkt);
        pkt.writeZeros(pv == V_1_0 ? MessageParser.PAD_PKT_IN_10
                : MessageParser.PAD_PKT_IN_13);
        if (msg.data != null)
            pkt.writeBytes(msg.data);
    }

    // encodes a flow-removed message
    private static void encodeFlowRemoved(OfmFlowRemoved msg,
                                          OfPacketWriter pkt)
            throws IncompleteMessageException {
        msg.validate();
        final ProtocolVersion pv = msg.getVersion();
        if (pv == V_1_0)
            MatchFactory.encodeMatch(msg.match, pkt);
        pkt.writeLong(msg.cookie);
        pkt.writeU16(msg.priority);
        pkt.writeU8(msg.reason.getCode(pv));
        if (pv == V_1_0)
            pkt.writeZeros(MessageParser.PAD_FLOW_REMOVED_1);
        if (pv == V_1_3)
            pkt.write(msg.tableId);
        pkt.writeU32(msg.durationSec);
        pkt.writeU32(msg.durationNsec);
        pkt.writeU16(msg.idleTimeout);
        if (pv == V_1_3)
            pkt.writeU16(msg.hardTimeout);
        if (pv == V_1_0)
            pkt.writeZeros(MessageParser.PAD_FLOW_REMOVED_2);
        pkt.writeLong(msg.packetCount);
        pkt.writeLong(msg.byteCount);
        if (pv == V_1_3) {
            MatchFactory.encodeMatch(msg.match, pkt);
            msg.header.length += msg.match.getTotalLength();
        }
    }

    // encodes a port status message
    private static void encodePortStatus(OfmPortStatus msg, OfPacketWriter pkt)
            throws IncompleteMessageException {
        msg.validate();
        final ProtocolVersion pv = msg.getVersion();
        pkt.writeU8(msg.reason.getCode(pv));
        pkt.writeZeros(MessageParser.PAD_PORT_STATUS);
        try {
            PortFactory.encodePort(msg.desc, pkt);
        } catch (IncompleteStructureException e) {
            throw new IncompleteMessageException(e);
        }
    }

    // encodes a packet-out message
    private static void encodePacketOut(OfmPacketOut msg, OfPacketWriter pkt)
            throws IncompleteMessageException {
        msg.validate();
        final ProtocolVersion pv = msg.getVersion();
        pkt.write(msg.bufferId);
        PortFactory.encodePortNumber(msg.inPort, pkt, pv);
        pkt.writeU16(msg.actionsLen);
        if (pv.gt(V_1_0))
            pkt.writeZeros(MessageParser.PAD_PKT_OUT);
        ActionFactory.encodeActionList(msg.actions, pkt);
        if (msg.data != null)
            pkt.writeBytes(msg.data);
    }

    // encodes a flow-mod message
    private static void encodeFlowMod(OfmFlowMod msg, OfPacketWriter pkt)
            throws IncompleteMessageException {
        msg.validate();
        final ProtocolVersion pv = msg.getVersion();
        if (pv == V_1_0)
            MatchFactory.encodeMatch(msg.match, pkt);
        pkt.writeLong(msg.cookie);
        if (pv == V_1_0) {
            pkt.writeU16(msg.command.getCode(pv));
        } else {
            pkt.writeLong(msg.cookieMask);
            pkt.write(msg.tableId);
            pkt.writeU8(msg.command.getCode(pv));
        }
        pkt.writeU16(msg.idleTimeout);
        pkt.writeU16(msg.hardTimeout);
        pkt.writeU16(msg.priority);
        pkt.write(msg.bufferId);
        PortFactory.encodePortNumber(msg.outPort, pkt, pv);
        if (pv == V_1_0) {
            pkt.writeU16(FlowModFlag.encodeBitmap(msg.flags, pv));
            ActionFactory.encodeActionList(msg.actions, pkt);
        } else {
            pkt.write(msg.outGroup);
            pkt.writeU16(FlowModFlag.encodeBitmap(msg.flags, pv));
            pkt.writeZeros(MessageParser.PAD_FLOW_MOD);
            MatchFactory.encodeMatch(msg.match, pkt);
            InstructionFactory.encodeInstructionList(msg.instructions, pkt);
        }
    }

    // encodes a group-mod message
    private static void encodeGroupMod(OfmGroupMod msg, OfPacketWriter pkt)
            throws IncompleteMessageException {
        msg.validate();
        final ProtocolVersion pv = msg.getVersion();
        pkt.writeU16(msg.command.getCode(pv));
        pkt.writeU8(msg.groupType.getCode(pv));
        pkt.writeZeros(MessageParser.PAD_GROUP_MOD);
        pkt.write(msg.groupId);
        BucketFactory.encodeBucketList(msg.buckets, pkt);
    }

    // encodes a port-mod message
    private static void encodePortMod(OfmPortMod msg, OfPacketWriter pkt)
            throws IncompleteMessageException {
        msg.validate();
        final ProtocolVersion pv = msg.getVersion();
        PortFactory.encodePortNumber(msg.port, pkt, pv);
        if (pv.gt(V_1_0))
            pkt.writeZeros(MessageParser.PAD_PORT_MOD_1);
        pkt.write(msg.hwAddress);
        if (pv.gt(V_1_0))
            pkt.writeZeros(MessageParser.PAD_PORT_MOD_2);
        pkt.writeU32(PortConfig.encodeBitmap(msg.config, pv));
        pkt.writeU32(PortConfig.encodeBitmap(msg.mask, pv));
        pkt.writeU32(PortFeature.encodeBitmap(msg.advertise, pv));
        pkt.writeZeros(MessageParser.PAD_PORT_MOD_3);
    }

    // encodes a table-mod message
    private static void encodeTableMod(OfmTableMod msg, OfPacketWriter pkt)
            throws IncompleteMessageException {
        msg.validate();
        pkt.write(msg.tableId);
        pkt.writeZeros(MessageParser.PAD_TAB_MOD);
        pkt.writeU32(TableConfig.encodeBitmap(msg.config, msg.getVersion()));
    }

    // encodes a multipart-request message
    private static void encodeMultipartRequest(OfmMultipartRequest msg,
                                               OfPacketWriter pkt)
            throws IncompleteMessageException {
        msg.validate();
        final ProtocolVersion pv = msg.getVersion();
        pkt.writeU16(msg.multipartType.getCode(pv));
        pkt.writeU16(MultipartRequestFlag.encodeBitmap(msg.flags, pv));
        if (pv == V_1_3)
            pkt.writeZeros(MessageParser.PAD_MULTIPART_HEADER_13);
        try {
            MpBodyFactory.encodeRequestBody(msg.body, pkt);
        } catch (IncompleteStructureException e) {
            throw new IncompleteMessageException(e);
        }
    }

    // encodes a multipart-reply message
    private static void encodeMultipartReply(OfmMultipartReply msg,
                                             OfPacketWriter pkt)
            throws IncompleteMessageException {
        msg.validate();
        final ProtocolVersion pv = msg.getVersion();
        pkt.writeU16(msg.multipartType.getCode(pv));
        pkt.writeU16(MultipartReplyFlag.encodeBitmap(msg.flags, pv));
        if (pv == V_1_3)
            pkt.writeZeros(MessageParser.PAD_MULTIPART_HEADER_13);
        try {
            MpBodyFactory.encodeReplyBody(msg.body, pkt);
        } catch (IncompleteStructureException e) {
            throw new IncompleteMessageException(e);
        }
    }

    // encodes a queue-get-config-request message
    private static void encodeQueueGetCfgRequest(OfmQueueGetConfigRequest msg,
                                                 OfPacketWriter pkt)
            throws IncompleteMessageException {
        msg.validate();
        final ProtocolVersion pv = msg.getVersion();
        PortFactory.encodePortNumber(msg.port, pkt, pv);
        pkt.writeZeros(pv == V_1_0 ? MessageParser.PAD_QUE_GET_CFG_REQ_10
                : MessageParser.PAD_QUE_GET_CFG_REQ_13);
    }

    // encodes a queue-get-config-reply message
    private static void encodeQueueGetCfgReply(OfmQueueGetConfigReply msg,
                                               OfPacketWriter pkt)
            throws IncompleteMessageException {
        msg.validate();
        final  ProtocolVersion pv = msg.getVersion();
        PortFactory.encodePortNumber(msg.port, pkt, pv);
        pkt.writeZeros(pv == V_1_0 ? MessageParser.PAD_QUE_GET_CFG_REPLY_10
                : MessageParser.PAD_QUE_GET_CFG_REPLY_13);
        QueueFactory.encodeQueueList(msg.getQueues(), pkt);
    }

    // ===== TODO : encodeRole(...) goes HERE

    // encodes an get-async-config-reply or set-async-config message
    private static void encodeAsyncConfig(AsyncConfig msg, OfPacketWriter pkt)
            throws IncompleteMessageException {
        msg.validate();
        final ProtocolVersion pv = msg.getVersion();
        pkt.writeInt(PacketInReason.encodeFlags(msg.pktInMask, pv));
        pkt.writeInt(PacketInReason.encodeFlags(msg.pktInMaskSlave, pv));

        pkt.writeInt(PortReason.encodeFlags(msg.portStatusMask, pv));
        pkt.writeInt(PortReason.encodeFlags(msg.portStatusMaskSlave, pv));

        pkt.writeInt(FlowRemovedReason.encodeFlags(msg.flowRemovedMask, pv));
        pkt.writeInt(FlowRemovedReason.encodeFlags(msg.flowRemovedMaskSlave,
                                                    pv));
    }

    // encodes a meter-mod message
    private static void encodeMeterMod(OfmMeterMod msg, OfPacketWriter pkt)
            throws IncompleteMessageException {
        msg.validate();
        final ProtocolVersion pv = msg.getVersion();
        pkt.writeU16(msg.command.getCode(pv));
        pkt.writeU16(MeterFlag.encodeBitmap(msg.flags, pv));
        pkt.write(msg.meterId);
        MeterBandFactory.encodeBandList(msg.bands, pkt);
    }

    //encodes a role request or role reply message
    private static void encodeRole(Role msg, OfPacketWriter pkt)
            throws IncompleteMessageException {
        msg.validate();
        pkt.writeU32(msg.role.getCode(msg.getVersion()));
        pkt.writeZeros(MessageParser.PAD_ROLE);
        pkt.writeLong(msg.generationId);
    }
}
