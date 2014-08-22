/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.err.ErrorCodeLookup;
import org.opendaylight.of.lib.err.ErrorType;
import org.opendaylight.of.lib.instr.ActionFactory;
import org.opendaylight.of.lib.instr.InstructionFactory;
import org.opendaylight.of.lib.match.*;
import org.opendaylight.of.lib.mp.MpBodyFactory;
import org.opendaylight.of.lib.mp.MultipartType;
import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.net.BigPortNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ResourceBundle;

import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.MessageFactory.*;
import static org.opendaylight.of.lib.msg.MessageType.ERROR;
import static org.opendaylight.of.lib.msg.MessageType.HELLO;
import static org.opendaylight.of.lib.msg.OpenflowMessage.Header;
import static org.opendaylight.of.lib.msg.OpenflowMessage.OFM_HEADER_LEN;
import static org.opendaylight.util.ByteUtils.hex;

/**
 * Parses OpenFlow messages from a Byte Buffer.
 *
 * @author Simon Hunt
 * @author Thomas Vachuska
 * @author Frank Wood
 * @author Scott Simes
 * @author Radhika Hegde
 */
class MessageParser {

    static Logger log = LoggerFactory.getLogger(MessageParser.class);

    private static final int HEADER_LENGTH = 8;
    private static final int MIN_LENGTH = 4;
    private static final int LENGTH_MASK = 0xffff;

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            MessageParser.class, "messageParser");

    private static final String E_PARSE = RES.getString("e_parse");
    private static final String E_UNSUPPORTED_VER = RES
            .getString("e_unsupported_ver");
    private static final String E_PARSE_TERMINATED = RES
            .getString("e_parse_terminated");

    /**
     * Parses a single OpenFlow message from the supplied byte buffer. Note
     * that this method affects the byte buffer, by advancing the position
     * by the length of the message being parsed.
     * <p>
     * If it is determined that there are insufficient bytes in the buffer for
     * a complete message to be parsed, the method returns null without
     * disturbing the buffer.
     *
     * @param buffer the byte buffer from which message is to be parsed
     * @return a parsed OpenFlow message or null if the buffer does not have
     *         sufficient bytes remaining to read the complete message
     * @throws NullPointerException if buffer is null
     * @throws MessageParseException if unable to parse the message
     * @throws VersionNotSupportedException if the given version is not
     *         supported
     */
    static OpenflowMessage parse(ByteBuffer buffer)
            throws MessageParseException {
        // Ensure we have a complete message in the buffer first.
        //  ----+-------+-------+-------+-------+----//----+-------+-------
        //  ... | P.Ver | Type  |     Length    | ...      | P.Ver | Type
        //  ----+-------+-------+-------+-------+----//----+-------+-------
        //      ^                                          ^
        //      buffer.position()                          next message
        //
        if (buffer.remaining() < MIN_LENGTH)
            // The length field was not received yet; return null.
            return null;

        // We have enough bytes in the buffer to get at the length field which
        // is in the second two bytes of the four-byte integer value located
        // at the current buffer position. Peek, without moving position...
        int length = buffer.getInt(buffer.position()) & LENGTH_MASK;

        if (buffer.remaining() < length)
            // Still don't have a complete message; return null.
            return null;

        // We have enough bytes in the buffer to parse a complete message.
        return parse(new OfPacketReader(buffer), length);
    }

    /**
     * Parses a single OpenFlow message from the supplied packet reader.
     * Note that this method causes the reader index of the reader to
     * be advanced by the length of the message being parsed.
     *
     * @param pkt the packet reader
     * @param msgLen the expected message length
     * @return a parsed OpenFlow message
     * @throws MessageParseException if unable to parse the message
     * @throws VersionNotSupportedException if the given version is
     *          not supported
     */
    static OpenflowMessage parse(OfPacketReader pkt, int msgLen)
            throws MessageParseException {
        Header header = null;
        int startPosition = pkt.ri();
        int targetPosition = startPosition + msgLen;
        // annotate the reader with start and target
        pkt.startIndex(startPosition);
        pkt.targetIndex(targetPosition);
        try {
            header = OpenflowMessage.parseHeader(pkt);
            checkSupported(header);
            return createMessageInstance(header, pkt);

        } catch (VersionNotSupportedException vnse) {
            // Need to catch and rethrow, because we don't want to wrap in MPE
            logUnsupportedVersion(header);
            throw vnse; // rethrow

        } catch (Exception e) {
            logFailedParse(header, pkt);
            log.warn("Exception was: {}", e.getMessage());
            // wrap in a MessageParseException
            throw MSGF.mpe(pkt, "OFM:" + header, e);

        } finally {
            // ensure the read head is at the beginning of the next message
            // if we aborted the parsing of this message midway...
            final int ri = pkt.ri();
            if (ri < targetPosition) {
                log.warn(E_PARSE_TERMINATED, startPosition, targetPosition,
                        ri - startPosition, targetPosition - ri);
                // protect against fewer bytes in buffer than expected length
                pkt.ri(Math.min(targetPosition, pkt.limit()));
            }
        }
    }

    private static void logUnsupportedVersion(Header header) {
        log.warn(E_UNSUPPORTED_VER, header);
    }

    private static void logFailedParse(Header header, OfPacketReader pkt) {
        // Make a copy of relevant bytes for formatting
        int headerLength = header != null ? header.length : HEADER_LENGTH;
        int bytesRead = pkt.ri() - pkt.startIndex();
        int arraySize = Math.min(headerLength, pkt.readableBytes() + bytesRead);
        byte bytes[] = new byte[arraySize];

        int offset = -bytesRead;
        for (int i = 0; i < arraySize; i++)
            bytes[i] = pkt.peek(offset++);

        log.warn(E_PARSE, header, hex(bytes));
    }

    /**
     * Checks that the message we are attempting to parse is supported. If
     * it is not, we will throw an exception.
     *
     * @param header the parsed header
     * @throws VersionNotSupportedException if we aren't going to decode
     */
    static void checkSupported(Header header) {
        // HELLO and ERROR are supported in all versions
        if (header.type == HELLO || header.type == ERROR)
            return;

        if (!MessageFactory.isVersionSupported(header.version))
            throw new VersionNotSupportedException(header + E_SUPP_VERSIONS);
    }

    /**
     * Uses message type to decide which concrete instance of OpenflowMessage
     * to instantiate, then continues to parse the body of the message.
     *
     * @param header the parsed header
     * @param pkt the data packet
     * @return a completely parsed message
     * @throws MessageParseException if message cannot be parsed
     * @throws DecodeException if header field cannot be decoded
     */
    private static OpenflowMessage createMessageInstance(Header header,
                                                         OfPacketReader pkt)
            throws MessageParseException, DecodeException {
        ProtocolVersion pv = header.version;
        String typeLabel = header.type.name();
        switch (header.type) {
            case HELLO:
                return readHello(new OfmHello(header), pkt);

            case ERROR:
                return readError(new OfmError(header), pkt);

            case ECHO_REQUEST:
                return readEcho(new OfmEchoRequest(header), pkt);

            case ECHO_REPLY:
                return readEcho(new OfmEchoReply(header), pkt);

            case EXPERIMENTER:
                return readExperimenter(new OfmExperimenter(header), pkt);

            case FEATURES_REQUEST:
                return new OfmFeaturesRequest(header);

            case FEATURES_REPLY:
                return readFeaturesReply(new OfmFeaturesReply(header), pkt);

            case GET_CONFIG_REQUEST:
                return new OfmGetConfigRequest(header);

            case GET_CONFIG_REPLY:
                return readSwitchConfig(new OfmGetConfigReply(header), pkt);

            case SET_CONFIG:
                return readSwitchConfig(new OfmSetConfig(header), pkt);

            case PACKET_IN:
                return readPacketIn(new OfmPacketIn(header), pkt);

            case FLOW_REMOVED:
                return readFlowRemoved(new OfmFlowRemoved(header), pkt);

            case PORT_STATUS:
                return readPortStatus(new OfmPortStatus(header), pkt);

            case PACKET_OUT:
                return readPacketOut(new OfmPacketOut(header), pkt);

            case FLOW_MOD:
                return readFlowMod(new OfmFlowMod(header), pkt);

            case GROUP_MOD:
                verMin11(pv, typeLabel);
                return readGroupMod(new OfmGroupMod(header), pkt);

            case PORT_MOD:
                return readPortMod(new OfmPortMod(header), pkt);

            case TABLE_MOD:
                verMin11(pv, typeLabel);
                return readTableMod(new OfmTableMod(header), pkt);

            case MULTIPART_REQUEST:
                // do NOT check for version. See OfmMultipartRequest class desc
                return readMpRequest(new OfmMultipartRequest(header), pkt);

            case MULTIPART_REPLY:
                // do NOT check for version. See OfmMultipartReply class desc
                return readMpReply(new OfmMultipartReply(header), pkt);

            case BARRIER_REQUEST:
                return new OfmBarrierRequest(header);

            case BARRIER_REPLY:
                return new OfmBarrierReply(header);

            case QUEUE_GET_CONFIG_REQUEST:
                return readQueGetCfgReq(new OfmQueueGetConfigRequest(header), pkt);

            case QUEUE_GET_CONFIG_REPLY:
                return readQueGetCfgReply(new OfmQueueGetConfigReply(header), pkt);

            case ROLE_REQUEST:
                verMin12(pv, typeLabel);
                return readRole(new OfmRoleRequest(header), pkt);

            case ROLE_REPLY:
                verMin12(pv, typeLabel);
                return readRole(new OfmRoleReply(header), pkt);

            case GET_ASYNC_REQUEST:
                verMin13(pv, typeLabel);
                return new OfmGetAsyncRequest(header);

            case GET_ASYNC_REPLY:
                verMin13(pv, typeLabel);
                return readAsyncConfig(new OfmGetAsyncReply(header), pkt);

            case SET_ASYNC:
                verMin13(pv, typeLabel);
                return readAsyncConfig(new OfmSetAsync(header), pkt);

            case METER_MOD:
                verMin13(pv, typeLabel);
                return readMeterMod(new OfmMeterMod(header), pkt);

            default:
                return null;
        }
    }

    //=======================================================================

    // Read the payload of a HELLO message
    private static OpenflowMessage readHello(OfmHello msg, OfPacketReader pkt)
            throws MessageParseException {
        ProtocolVersion pv = msg.getVersion();
        /*  IMPLEMENTATION NOTE:
         *    The protocol version defined in the header of a HELLO message is
         *    the highest version that the sender supports; not the "version"
         *    of the internal structure. Therefore we cannot disregard possible
         *    additional payload in the form of hello elements if this version
         *    is reported as less than 1.3 (when elements were introduced into
         *    the protocol).
         */
        msg.elements = HelloElementFactory.parseElementList(pkt, pv);
        return msg;
    }

    // Read the payload of an ERROR message
    private static OpenflowMessage readError(OfmError msg, OfPacketReader pkt)
            throws DecodeException {
        ProtocolVersion pv = msg.getVersion();
        final int targetRi = pkt.targetIndex();
        msg.type = ErrorType.decode(pkt.readU16(), pv);
        if (msg.type == ErrorType.EXPERIMENTER) {
            // Parsing for errorType experimenter
            OfmErrorExper errExp = new OfmErrorExper(msg.header);
            errExp.type = msg.type;
            errExp.expType = pkt.readU16();
            errExp.id = pkt.readInt();
            final int dataSize = targetRi - pkt.ri();
            if (dataSize > 0)
                errExp.data = pkt.readBytes(dataSize);
            return errExp;
        }
        // Parsing all other errorTypes.
        msg.code = ErrorCodeLookup.lookup(msg.type, pkt.readU16(), pv);
        final int dataSize = targetRi - pkt.ri();
        if (dataSize > 0)
            msg.data = pkt.readBytes(dataSize);
        if (msg.type == ErrorType.HELLO_FAILED)
            msg.errMsg = ByteUtils.getNullTerminatedAscii(msg.data);
        return msg;
    }

    // Read the payload of an echo request/reply message
    private static OpenflowMessage readEcho(Echo msg,  OfPacketReader pkt) {
        final int dataSize = msg.header.length - OFM_HEADER_LEN;
        if (dataSize > 0)
            msg.data = pkt.readBytes(dataSize);
        return msg;
    }

    // Read the payload of an experimenter message
    private static OpenflowMessage readExperimenter(OfmExperimenter msg,
                                                    OfPacketReader pkt) {
        ProtocolVersion pv = msg.getVersion();
        msg.id = pkt.readInt();

        if(pv.gt(V_1_0))
            msg.type = pkt.readInt();

        final int dataSize = pkt.targetIndex() - pkt.ri();
        if(dataSize > 0)
            msg.data = pkt.readBytes(dataSize);
        return msg;
    }

    static final int PAD_FEAT_REP_1 = 2;
    static final int PAD_FEAT_REP_1_012 = 3;
    static final int PAD_FEAT_REP_2 = 4; // Reserved

    // Read the payload of a features reply message
    private static OpenflowMessage readFeaturesReply(OfmFeaturesReply msg,
                                                     OfPacketReader pkt)
            throws MessageParseException {
        ProtocolVersion pv = msg.getVersion();
        msg.dpid = pkt.readDataPathId();
        msg.numBuffers = pkt.readU32();
        msg.numTables = pkt.readU8();
        if (pv.le(V_1_2)) {
            pkt.skip(PAD_FEAT_REP_1_012);
        } else {
            msg.auxId = pkt.readU8();
            pkt.skip(PAD_FEAT_REP_1);
        }
        int capFlags = pkt.readInt();
        msg.capabilities = Capability.decodeBitmap(capFlags, pv);
        if (pv == V_1_0) {
            int saBitmap = pkt.readInt();
            msg.suppActions = SupportedAction.decodeBitmap(saBitmap, pv);
        } else {
            pkt.skip(PAD_FEAT_REP_2); // reserved u32
        }
        if (pv.le(V_1_2)) {
            msg.ports = PortFactory.parsePortList(pkt, pv);
        }
        return msg;
    }

    // read the payload of a switch config message
    private static OpenflowMessage readSwitchConfig(SwitchConfig msg,
                                                    OfPacketReader pkt) {
        ProtocolVersion pv = msg.getVersion();
        msg.flags = ConfigFlag.decodeBitmap(pkt.readU16(), pv);
        msg.missSendLength = pkt.readU16();
        return msg;
    }

    static final int PAD_TAB_MOD = 3;

    // read the payload of a table mod message
    private static OpenflowMessage readTableMod(OfmTableMod msg,
                                                OfPacketReader pkt) {
        ProtocolVersion pv = msg.getVersion();
        msg.tableId = pkt.readTableId();
        pkt.skip(PAD_TAB_MOD);
        msg.config = TableConfig.decodeBitmap(pkt.readInt(), pv);
        return msg;
    }

    static final int PAD_FLOW_MOD = 2;

    // read the payload of a flow mod message
    private static OpenflowMessage readFlowMod(OfmFlowMod msg,
                                               OfPacketReader pkt)
            throws MessageParseException, DecodeException {
        ProtocolVersion pv = msg.getVersion();
        final int targetRi = pkt.targetIndex();

        if (pv == V_1_0)
            msg.match = MatchFactory.parseMatch(pkt, pv);

        msg.cookie = pkt.readLong();
        if (pv == V_1_0) {
            msg.command = FlowModCommand.decode(pkt.readU16(), pv);
        } else {
            msg.cookieMask = pkt.readLong();
            msg.tableId = pkt.readTableId();
            msg.command = FlowModCommand.decode(pkt.readU8(), pv);
        }

        msg.idleTimeout = pkt.readU16();
        msg.hardTimeout = pkt.readU16();
        msg.priority = pkt.readU16();
        msg.bufferId = pkt.readBufferId();

        msg.outPort = PortFactory.parsePortNumber(pkt, pv);

        if (pv == V_1_0) {
            msg.flags = FlowModFlag.decodeBitmap(pkt.readU16(), pv);
            msg.actions = ActionFactory.parseActionList(targetRi, pkt, pv);
        } else {
            msg.outGroup = pkt.readGroupId();
            msg.flags = FlowModFlag.decodeBitmap(pkt.readU16(), pv);
            pkt.skip(PAD_FLOW_MOD);
            msg.match = MatchFactory.parseMatch(pkt, pv);
            // now read instructions till we reach the end of the message
            msg.instructions =
                    InstructionFactory.parseInstructionList(targetRi, pkt, pv);
        }

        return msg;
    }

    static final int PAD_FLOW_REMOVED_1 = 1;
    static final int PAD_FLOW_REMOVED_2 = 2;

    private static OpenflowMessage readFlowRemoved(OfmFlowRemoved msg,
                                                   OfPacketReader pkt)
            throws MessageParseException, DecodeException {
        ProtocolVersion pv = msg.getVersion();

        if (pv == V_1_0)
            msg.match = MatchFactory.parseMatch(pkt, pv);

        msg.cookie = pkt.readLong();
        msg.priority = pkt.readU16();
        msg.reason = FlowRemovedReason.decode(pkt.readU8(), pv);

        if (pv == V_1_0)
            pkt.skip(PAD_FLOW_REMOVED_1);

        if (pv == V_1_3)
            msg.tableId = pkt.readTableId();

        msg.durationSec = pkt.readU32();
        msg.durationNsec = pkt.readU32();
        msg.idleTimeout = pkt.readU16();

        if (pv == V_1_3)
            msg.hardTimeout = pkt.readU16();

        if (pv == V_1_0)
            pkt.skip(PAD_FLOW_REMOVED_2);

        msg.packetCount = pkt.readLong();
        msg.byteCount = pkt.readLong();

        if (pv == V_1_3)
            msg.match = MatchFactory.parseMatch(pkt, pv);

        return msg;
    }

    static final int PAD_PORT_STATUS = 7;

    // read the payload of a port status message (v1.0+)
    private static OpenflowMessage readPortStatus(OfmPortStatus msg,
                                                  OfPacketReader pkt)
            throws MessageParseException, DecodeException {
        ProtocolVersion pv = msg.getVersion();
        msg.reason = PortReason.decode(pkt.readU8(), pv);
        pkt.skip(PAD_PORT_STATUS);
        msg.desc = PortFactory.parsePort(pkt, pv);
        return msg;
    }

    static final int PAD_GROUP_MOD = 1;

    // read the payload of a group mod message (v1.1+)
    private static OpenflowMessage readGroupMod(OfmGroupMod msg,
                                                OfPacketReader pkt)
            throws MessageParseException, DecodeException {
        ProtocolVersion pv = msg.getVersion();

        msg.command = GroupModCommand.decode(pkt.readU16(), pv);
        if (msg.command == null)
            throw MSGF.mpe(pkt, "unknown GroupModCmd: " + msg.command);
        msg.groupType = GroupType.decode(pkt.readU8(), pv);
        pkt.skip(PAD_GROUP_MOD);
        msg.groupId = pkt.readGroupId();

        msg.buckets = BucketFactory.parseBucketList(pkt.targetIndex(), pkt, pv);
        return msg;
    }

    static final int PAD_PORT_MOD_1 = 4;
    static final int PAD_PORT_MOD_2 = 2;
    static final int PAD_PORT_MOD_3 = 4;

    // read the payload of a port mod message
    private static OpenflowMessage readPortMod(OfmPortMod msg,
                                               OfPacketReader pkt) {
        ProtocolVersion pv = msg.getVersion();
        if (pv == V_1_0) {
            msg.port = BigPortNumber.valueOf(pkt.readU16());
            msg.hwAddress = pkt.readMacAddress();
        } else {
            msg.port = pkt.readBigPortNumber();
            pkt.skip(PAD_PORT_MOD_1);
            msg.hwAddress = pkt.readMacAddress();
            pkt.skip(PAD_PORT_MOD_2);
        }
        msg.config = PortConfig.decodeBitmap(pkt.readInt(), pv);
        msg.mask = PortConfig.decodeBitmap(pkt.readInt(), pv);
        msg.advertise = PortFeature.decodeBitmap(pkt.readInt(), pv);
        pkt.skip(PAD_PORT_MOD_3);
        return msg;
    }

    // read the payload of a meter mod message
    private static OpenflowMessage readMeterMod(OfmMeterMod msg,
                                                OfPacketReader pkt)
            throws MessageParseException, DecodeException {
        ProtocolVersion pv = msg.getVersion();
        final int targetRi = pkt.ri() + msg.header.length - OFM_HEADER_LEN;

        msg.command = MeterModCommand.decode(pkt.readU16(), pv);
        msg.flags = MeterFlag.decodeBitmap(pkt.readU16(), pv);
        msg.meterId = pkt.readMeterId();
        msg.bands = MeterBandFactory.parseMeterBandList(targetRi, pkt, pv);
        return msg;
    }

    static final int PAD_MULTIPART_HEADER_13 = 4;

    // read the payload of a multipart request message
    private static OpenflowMessage readMpRequest(OfmMultipartRequest msg,
                                                 OfPacketReader pkt)
            throws MessageParseException, DecodeException {
        ProtocolVersion pv = msg.getVersion();
        msg.multipartType = MultipartType.decode(pkt.readU16(), pv);
        msg.flags = MultipartRequestFlag.decodeBitmap(pkt.readU16(), pv);
        if (pv == V_1_3)
            pkt.skip(PAD_MULTIPART_HEADER_13);
        msg.body = MpBodyFactory.parseRequestBody(msg.multipartType, pkt, pv);
        return msg;
    }

    // read the payload of a multipart reply message
    private static OpenflowMessage readMpReply(OfmMultipartReply msg,
                                               OfPacketReader pkt)
            throws MessageParseException, DecodeException {
        ProtocolVersion pv = msg.getVersion();
        msg.multipartType = MultipartType.decode(pkt.readU16(), pv);
        msg.flags = MultipartReplyFlag.decodeBitmap(pkt.readU16(), pv);
        if (pv == V_1_3)
            pkt.skip(PAD_MULTIPART_HEADER_13);
        msg.body = MpBodyFactory.parseReplyBody(msg.multipartType, pkt, pv);
        return msg;
    }

    static final int PAD_QUE_GET_CFG_REQ_10 = 2;
    static final int PAD_QUE_GET_CFG_REQ_13 = 4;

    // read QueueGetConfig request message
    private static OpenflowMessage readQueGetCfgReq(OfmQueueGetConfigRequest msg,
                                                    OfPacketReader pkt) {
        ProtocolVersion pv = msg.getVersion();
        msg.port = PortFactory.parsePortNumber(pkt, pv);

        pkt.skip(pv == V_1_0 ? PAD_QUE_GET_CFG_REQ_10
                             : PAD_QUE_GET_CFG_REQ_13);
        return msg;
    }

    static final int PAD_QUE_GET_CFG_REPLY_10 = 6;
    static final int PAD_QUE_GET_CFG_REPLY_13 = 4;

    // read QueueGetConfig reply message
    private static OpenflowMessage readQueGetCfgReply(OfmQueueGetConfigReply msg,
                                                      OfPacketReader pkt)
            throws MessageParseException {
        ProtocolVersion pv = msg.getVersion();
        msg.port = PortFactory.parsePortNumber(pkt, pv);

        pkt.skip(pv == V_1_0 ? PAD_QUE_GET_CFG_REPLY_10
                             : PAD_QUE_GET_CFG_REPLY_13);

        final int targetRi = pkt.ri() + msg.header.length - LIB_Q_GET_CFG;

        msg.queues = QueueFactory.parseQueueList(targetRi, pkt, pv);
        return msg;
    }


    static final int PAD_PKT_IN_10 = 1;
    static final int PAD_PKT_IN_13 = 2;

    // read the payload of a packet-in message
    private static OpenflowMessage readPacketIn(OfmPacketIn msg,
                                                OfPacketReader pkt)
            throws MessageParseException, DecodeException {
        ProtocolVersion pv = msg.getVersion();
        final int targetRi = pkt.targetIndex();

        msg.bufferId = pkt.readBufferId();
        msg.totalLen = pkt.readU16();

        if (pv == V_1_0)
            msg.inPort = BigPortNumber.valueOf(pkt.readU16());
        msg.reason = PacketInReason.decode(pkt.readU8(), pv);
        if (pv == V_1_3) {
            msg.tableId = pkt.readTableId();
            msg.cookie = pkt.readLong();
            msg.match = MatchFactory.parseMatch(pkt, pv);
            // look through match for port data
            for (MatchField mf: msg.match.getMatchFields()) {
                OxmFieldType oft = mf.getFieldType();
                if (OxmBasicFieldType.class.isInstance(oft)) {
                    switch ((OxmBasicFieldType) oft) {
                        case IN_PORT:
                            MfbInPort ip = (MfbInPort) mf;
                            msg.inPort = ip.getPort();
                            break;
                        case IN_PHY_PORT:
                            MfbInPhyPort ipp = (MfbInPhyPort) mf;
                            msg.inPhyPort = ipp.getPort();
                            break;
                        default:
                            break;
                    }
                }
            }
            // if in_phy_port is omitted, it has the same value as in_port
            // See section A.4.1 of 1.3.1 spec (pg. 93) for details
            if (msg.inPhyPort == null)
                msg.inPhyPort = msg.inPort;
        }
        pkt.skip(pv == V_1_0 ? PAD_PKT_IN_10 : PAD_PKT_IN_13);

        // Assume that remaining bytes are the frame data
        msg.data = pkt.readBytes(targetRi - pkt.ri());
        return msg;
    }

    static final int PAD_PKT_OUT = 6;

    // read the payload of a packet-out message
    private static OpenflowMessage readPacketOut(OfmPacketOut msg,
                                                 OfPacketReader pkt)
            throws MessageParseException {
        ProtocolVersion pv = msg.getVersion();
        final int targetRi = pkt.targetIndex();
        msg.bufferId = pkt.readBufferId();
        msg.inPort = PortFactory.parsePortNumber(pkt, pv);
        msg.actionsLen = pkt.readU16();
        if (pv.gt(V_1_0))
            pkt.skip(PAD_PKT_OUT);
        int actTargRi = pkt.ri() + msg.actionsLen;
        msg.actions = ActionFactory.parseActionList(actTargRi, pkt, pv);
        // Assume that remaining bytes are the frame data
        msg.data = pkt.readBytes(targetRi - pkt.ri());
        return msg;
    }

    // read the payload of an asynchronous config message
    private static OpenflowMessage readAsyncConfig(AsyncConfig msg,
                                                   OfPacketReader pkt) {
        final ProtocolVersion pv = msg.getVersion();
        msg.pktInMask = PacketInReason.decodeFlags(pkt.readInt(), pv);
        msg.pktInMaskSlave = PacketInReason.decodeFlags(pkt.readInt(), pv);

        msg.portStatusMask = PortReason.decodeFlags(pkt.readInt(), pv);
        msg.portStatusMaskSlave = PortReason.decodeFlags(pkt.readInt(), pv);

        msg.flowRemovedMask = FlowRemovedReason.decodeFlags(pkt.readInt(), pv);
        msg.flowRemovedMaskSlave =
                FlowRemovedReason.decodeFlags(pkt.readInt(), pv);

        return msg;
    }

    static final int PAD_ROLE = 4;

    // read the payload of a role request or a role reply message
    private static OpenflowMessage readRole(Role msg, OfPacketReader pkt)
            throws DecodeException {
        ProtocolVersion pv = msg.getVersion();
        msg.role = ControllerRole.decode(pkt.readInt(), pv);
        pkt.skip(PAD_ROLE);
        msg.generationId = pkt.readLong();
        return msg;
    }
}