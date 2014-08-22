/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.DecodeException;
import org.opendaylight.of.lib.MessageParseException;
import org.opendaylight.of.lib.OfPacketReader;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.instr.ActionFactory;
import org.opendaylight.of.lib.instr.ActionType;
import org.opendaylight.of.lib.mp.MBodyGroupStats.BucketCounter;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.NotYetImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.instr.InstructionFactory.parseInstructionList;
import static org.opendaylight.of.lib.match.MatchFactory.parseMatch;
import static org.opendaylight.of.lib.mp.MBodyMeterStats.MeterBandStats;
import static org.opendaylight.of.lib.mp.MpBodyFactory.*;

/**
 * Provides facilities for parsing {@link MultipartBody} instances.
 * <p>
 * Used by the {@link MpBodyFactory}.
 *
 * @author Simon Hunt
 */
class MpBodyParser {

    // no instantiation
    private MpBodyParser() { }

    /**
     * Parses a multipart request message body from the supplied buffer.
     * Note that this method causes the reader index of the underlying
     * packet reader to be advanced by the length of the body.
     *
     * @param type the type of request body to parse
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a parsed request message body
     * @throws MessageParseException if unable to parse the body
     */
    static MultipartBody parseRequestBody(MultipartType type,
                                          OfPacketReader pkt,
                                          ProtocolVersion pv)
            throws MessageParseException {
        try {
            return createParsedRequestBody(type, pkt, pv);
        } catch (MessageParseException mpe) {
            // rethrow MPE
            throw mpe;
        } catch (Exception e) {
            // wrap any unexpected exception in an MPE
            throw MBF.mpe(pkt, e);
        }
    }

    /**
     * Parses a multipart reply message body from the supplied buffer.
     * Note that this method causes the reader index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the body.
     *
     * @param type the type of reply body to parse
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a parsed reply message body
     * @throws MessageParseException if unable to parse the body
     */
    static MultipartBody parseReplyBody(MultipartType type, OfPacketReader pkt,
                                        ProtocolVersion pv)
            throws MessageParseException {
        try {
            return createParsedReplyBody(type, pkt, pv);
        } catch (MessageParseException mpe) {
            // rethrow MPE
            throw mpe;
        } catch (Exception e) {
            // wrap any unexpected exception in an MPE
            throw MBF.mpe(pkt, e);
        }
    }

    /**
     * Creates the concrete instance of multipart request body and
     * populates it by parsing the buffer.
     *
     * @param type the type of request body to create
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return the fully parsed request body
     * @throws MessageParseException if there is an issue parsing the body
     */
    private static MultipartBody createParsedRequestBody(MultipartType type,
                                                         OfPacketReader pkt,
                                                         ProtocolVersion pv)
            throws MessageParseException {
        String typeLabel = type.name();
        switch (type) {
            case FLOW:
                return readFlowReq(new MBodyFlowStatsRequest(pv), pkt);

            case AGGREGATE:
                throw new NotYetImplementedException();

            case PORT_STATS:
                return readPortStatsReq(new MBodyPortStatsRequest(pv), pkt);

            case QUEUE:
                return readQueueReq(new MBodyQueueStatsRequest(pv), pkt);

            case GROUP:
                verMin11(pv, typeLabel);
                return readGroupReq(new MBodyGroupStatsRequest(pv), pkt);

            case METER:
                verMin13(pv, typeLabel);
                return readMeterReq(new MBodyMeterStatsRequest(pv), pkt);

            case METER_CONFIG:
                verMin13(pv, typeLabel);
                return readMeterReq(new MBodyMeterConfigRequest(pv), pkt);

            case TABLE_FEATURES:
                verMin13(pv, typeLabel);
                return readTableFeatures(new MBodyTableFeatures.Array(pv), pkt);

            case EXPERIMENTER:
                return readExperimenter(new MBodyExperimenter(pv), pkt);

            default:
            // no body for request types of ...
            // DESC, TABLE, GROUP_DESC, GROUP_FEATURES, METER_FEATURES,PORT_DESC
                return null;
        }
    }

    /**
     * Creates the concrete instance of multipart reply body and
     * populates it by parsing the buffer.
     *
     * @param type the type of reply body to create
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return the fully parsed reply body
     * @throws MessageParseException if there is an issue parsing the body
     * @throws DecodeException if there is an issue decoding the group type
     */
    private static MultipartBody createParsedReplyBody(MultipartType type,
                                                       OfPacketReader pkt,
                                                       ProtocolVersion pv)
            throws MessageParseException, DecodeException {
        String typeLabel = type.name();
        switch (type) {
            case DESC:
                return readDesc(new MBodyDesc(pv), pkt);

            case FLOW:
                return readFlow(new MBodyFlowStats.Array(pv), pkt);

            case AGGREGATE:
                throw new NotYetImplementedException();

            case TABLE:
                return readTable(new MBodyTableStats.Array(pv), pkt);

            case PORT_STATS:
                return readPortStats(new MBodyPortStats.Array(pv), pkt);

            case QUEUE:
                return readQueue(new MBodyQueueStats.Array(pv), pkt);

            case GROUP:
                verMin11(pv, typeLabel);
                return readGroup(new MBodyGroupStats.Array(pv), pkt);

            case GROUP_DESC:
                verMin11(pv, typeLabel);
                return readGroupDesc(new MBodyGroupDescStats.Array(pv), pkt);

            case GROUP_FEATURES:
                verMin12(pv, typeLabel);
                return readGroupFeatures(new MBodyGroupFeatures(pv), pkt);

            case METER:
                verMin13(pv, typeLabel);
                return readMeter(new MBodyMeterStats.Array(pv), pkt);

            case METER_CONFIG:
                verMin13(pv, typeLabel);
                return readMeterConfig(new MBodyMeterConfig.Array(pv), pkt);

            case METER_FEATURES:
                verMin13(pv, typeLabel);
                return readMeterFeatures(new MBodyMeterFeatures(pv), pkt);

            case TABLE_FEATURES:
                verMin13(pv, typeLabel);
                return readTableFeatures(new MBodyTableFeatures.Array(pv), pkt);

            case PORT_DESC:
                verMin13(pv, typeLabel);
                return readPortDesc(new MBodyPortDesc.Array(pv), pkt);

            case EXPERIMENTER:
                return readExperimenter(new MBodyExperimenter(pv), pkt);

            default:
                return null;
        }
    }

    // === PARSE REQUESTS ==================================================

    // parse a flow stats request body
    private static MultipartBody readFlowReq(MBodyFlowStatsRequest body,
                                             OfPacketReader pkt)
            throws MessageParseException {
        final ProtocolVersion pv = body.getVersion();
        if (pv == V_1_0)
            body.match = parseMatch(pkt, pv);
        body.tableId = pkt.readTableId();
        pkt.skip(pv == V_1_0 ? PAD_FLOW_STATS_REQ_10 : PAD_FLOW_STATS_REQ);
        body.outPort = PortFactory.parsePortNumber(pkt, pv);
        if (pv.gt(V_1_0)) {
            body.outGroup = pkt.readGroupId();
            pkt.skip(PAD_FLOW_STATS_REQ_2);
            body.cookie = pkt.readLong();
            body.cookieMask = pkt.readLong();
            body.match = parseMatch(pkt, pv);
        }
        return body;
    }

    // TODO : readAggrStatsReq(...) goes here

    // parse a port stats request body
    private static MultipartBody readPortStatsReq(MBodyPortStatsRequest body,
                                                  OfPacketReader pkt) {
        final ProtocolVersion pv = body.getVersion();
        body.port = PortFactory.parsePortNumber(pkt, pv);
        pkt.skip(pv == V_1_0 ? PAD_PORT_STATS_REQ_10 : PAD_PORT_STATS_REQ);
        return body;
    }

    // parse a queue stats request body
    private static MultipartBody readQueueReq(MBodyQueueStatsRequest body,
                                              OfPacketReader pkt) {
        final ProtocolVersion pv = body.getVersion();
        body.port = PortFactory.parsePortNumber(pkt, pv);
        if (pv == V_1_0)
            pkt.skip(PAD_QUEUE_STATS);
        body.queueId = pkt.readQueueId();
        return body;
    }

    // parse a group stats request body
    private static MultipartBody readGroupReq(MBodyGroupStatsRequest body,
                                              OfPacketReader pkt) {
        body.groupId = pkt.readGroupId();
        pkt.skip(PAD_GROUP_STATS_REQ);
        return body;
    }

    // parse a meter stats request body
    private static MultipartBody readMeterReq(MBodyMeterRequest body,
                                              OfPacketReader pkt) {
        body.meterId = pkt.readMeterId();
        pkt.skip(PAD_METER_REQ);
        return body;
    }


    // === PARSE REPLIES ===================================================

    // parse a DESC body
    private static MultipartBody readDesc(MBodyDesc body,
                                          OfPacketReader pkt) {
        body.mfrDesc = pkt.readString(DESC_STR_LEN);
        body.hwDesc = pkt.readString(DESC_STR_LEN);
        body.swDesc = pkt.readString(DESC_STR_LEN);
        body.serialNum = pkt.readString(SERIAL_NUM_LEN);
        body.dpDesc = pkt.readString(DESC_STR_LEN);
        return body;
    }

    // parse a FLOW body -- array of flow stats elements
    private static MultipartBody readFlow(MBodyFlowStats.Array array,
                                          OfPacketReader pkt)
            throws MessageParseException {
        final ProtocolVersion pv = array.getVersion();
        // need to read flow stats until there are no more
        final int targetRi = pkt.targetIndex();
        while (pkt.ri() < targetRi) {
            try {
                array.list.add(parseFlowStats(pkt, pv));
            } catch (MessageParseException mpe) {
                if (MessageFactory.isStrictMessageParsing())
                    throw mpe;

                // Uh-oh! we encountered a bad element.
                // Call it good and make a quick exit...
                array.markIncomplete(mpe);
                return array;
            }
        }
        return array;
    }

    // parse a flow stats element
    private static MBodyFlowStats parseFlowStats(OfPacketReader pkt,
                                                 ProtocolVersion pv)
            throws MessageParseException {
        MBodyFlowStats fs = new MBodyFlowStats(pv);
        int currentRi = pkt.ri();
        fs.length = pkt.readU16();
        final int targetRi = currentRi + fs.length;
        fs.tableId = pkt.readTableId();
        pkt.skip(PAD_FLOW_STATS);

        if (pv == V_1_0)
            fs.match = parseMatch(pkt, pv);

        fs.durationSec = pkt.readU32();
        fs.durationNsec = pkt.readU32();
        fs.priority = pkt.readU16();
        fs.idleTimeout = pkt.readU16();
        fs.hardTimeout = pkt.readU16();

        if (pv.ge(V_1_3)) {
            fs.flags = FlowModFlag.decodeBitmap(pkt.readU16(), pv);
            pkt.skip(PAD_FLOW_STATS_2);
        } else {
            pkt.skip(PAD_FLOW_STATS_2_101112);
        }

        fs.cookie = pkt.readLong();
        fs.packetCount = pkt.readLong();
        fs.byteCount = pkt.readLong();

        if (pv == V_1_0) {
            fs.actions = ActionFactory.parseActionList(targetRi, pkt, pv);
        } else {
            fs.match = parseMatch(pkt, pv);
            fs.instructions = parseInstructionList(targetRi, pkt, pv);
        }
        return fs;
    }

    // TODO - parse an AGGREGATE body -- aggregate stats reply

    // parse a TABLE body -- array of table stats elements
    private static MultipartBody readTable(MBodyTableStats.Array array,
                                           OfPacketReader pkt) {
        final ProtocolVersion pv = array.getVersion();
        // need to read table stats until there are no more
        final int targetRi = pkt.targetIndex();
        while (pkt.ri() < targetRi)
            array.list.add(parseTableStats(pkt, pv));
        return array;
    }

    // parse a table stats element
    private static MBodyTableStats parseTableStats(OfPacketReader pkt,
                                                   ProtocolVersion pv) {
        MBodyTableStats ts = new MBodyTableStats(pv);
        ts.tableId = pkt.readTableId();
        pkt.skip(PAD_TABLE_STATS);
        if (pv == V_1_0) {
            ts.name = pkt.readString(TABLE_NAME_LEN);
            /* int wild = */ pkt.readInt();
            // FIXME : need to translate wild bits into result
            ts.maxEntries = pkt.readU32();
        }
        ts.activeCount = pkt.readU32();
        ts.lookupCount = pkt.readLong();
        ts.matchedCount = pkt.readLong();
        return ts;
    }

    // parse a PORT_STATS body -- array of port stats elements
    private static MultipartBody readPortStats(MBodyPortStats.Array array,
                                               OfPacketReader pkt) {
        final ProtocolVersion pv = array.getVersion();
        // need to read port stats until there are no more
        final int targetRi = pkt.targetIndex();
        while (pkt.ri() < targetRi)
            array.list.add(parsePortStats(pkt, pv));
        return array;
    }

    // parse a port stats element
    private static MBodyPortStats parsePortStats(OfPacketReader pkt,
                                                 ProtocolVersion pv) {
        MBodyPortStats ps = new MBodyPortStats(pv);
        ps.port = PortFactory.parsePortNumber(pkt, pv);
        pkt.skip(pv == V_1_0 ? PAD_PORT_STATS_REQ_10 : PAD_PORT_STATS_REQ);
        ps.rxPackets = pkt.readLong();
        ps.txPackets = pkt.readLong();
        ps.rxBytes = pkt.readLong();
        ps.txBytes = pkt.readLong();
        ps.rxDropped = pkt.readLong();
        ps.txDropped = pkt.readLong();
        ps.rxErrors = pkt.readLong();
        ps.txErrors = pkt.readLong();
        ps.rxFrameErr = pkt.readLong();
        ps.rxOverErr = pkt.readLong();
        ps.rxCrcErr = pkt.readLong();
        ps.collisions = pkt.readLong();

        if (pv == V_1_3) {
            ps.durationSec = pkt.readU32();
            ps.durationNsec = pkt.readU32();
        }
        return ps;
    }

    // parse a QUEUE body -- an array of queue stats elements
    private static MultipartBody readQueue(MBodyQueueStats.Array array,
                                           OfPacketReader pkt) {
        final ProtocolVersion pv = array.getVersion();
        // need to read queue stats until there are no more
        final int targetRi = pkt.targetIndex();
        while (pkt.ri() < targetRi)
            array.list.add(parseQueueStats(pkt, pv));
        return array;
    }

    // parse a queue stats element
    private static MBodyQueueStats parseQueueStats(OfPacketReader pkt,
                                                   ProtocolVersion pv) {
        MBodyQueueStats qs = new MBodyQueueStats(pv);
        qs.port = PortFactory.parsePortNumber(pkt, pv);
        if (pv == V_1_0)
            pkt.skip(PAD_QUEUE_STATS);
        qs.queueId = pkt.readQueueId();
        qs.txBytes = pkt.readLong();
        qs.txPackets = pkt.readLong();
        qs.txErrors = pkt.readLong();
        if (pv == V_1_3) {
            qs.durationSec = pkt.readU32();
            qs.durationNsec = pkt.readU32();
        }
        return qs;
    }

    // parse a GROUP body -- array of group stats elements
    private static MultipartBody readGroup(MBodyGroupStats.Array array,
                                           OfPacketReader pkt) {
        final ProtocolVersion pv = array.getVersion();
        // need to read group stats until there are no more
        final int targetRi = pkt.targetIndex();
        while (pkt.ri() < targetRi)
            array.list.add(parseGroupStats(pkt, pv));
        return array;
    }

    // parse a group stats element
    private static MBodyGroupStats parseGroupStats(OfPacketReader pkt,
                                                   ProtocolVersion pv) {
        MBodyGroupStats gs = new MBodyGroupStats(pv);
        int currentRi = pkt.ri();
        gs.length = pkt.readU16();
        pkt.skip(PAD_GROUP_STATS);
        final int targetRi = currentRi + gs.length;
        gs.groupId = pkt.readGroupId();
        gs.refCount = pkt.readU32();
        pkt.skip(PAD_GROUP_STATS_2);
        gs.packetCount = pkt.readLong();
        gs.byteCount = pkt.readLong();

        if (pv == V_1_3) {
            gs.durationSec = pkt.readU32();
            gs.durationNsec = pkt.readU32();
        }
        gs.bucketStats = parseBucketCounters(targetRi, pkt);
        return gs;
    }

    private static List<BucketCounter> parseBucketCounters(int targetRi,
                                       OfPacketReader pkt) {
        List<BucketCounter> bucketStats = new ArrayList<BucketCounter>();
        while (pkt.ri() < targetRi)
            bucketStats.add(new BucketCounter(pkt.readLong(), pkt.readLong()));
        return bucketStats;
    }

    // parse a GROUP_DESC body -- array of group desc stats elements
    private static MultipartBody readGroupDesc(MBodyGroupDescStats.Array array,
                                               OfPacketReader pkt)
            throws MessageParseException, DecodeException {
        final ProtocolVersion pv = array.getVersion();
        final int targetRi = pkt.targetIndex();
        while(pkt.ri() < targetRi)
            array.list.add(parseGroupDescStats(pkt, pv));
        return array;
    }

    // parse a single group desc stats element
    private static MBodyGroupDescStats parseGroupDescStats(OfPacketReader pkt,
                                                           ProtocolVersion pv)
            throws MessageParseException, DecodeException {
        MBodyGroupDescStats body = new MBodyGroupDescStats(pv);
        int currentRi = pkt.ri();
        body.length = pkt.readU16();
        final int targetRi = currentRi + body.length;
        body.type = GroupType.decode(pkt.readU8(), pv);
        pkt.skip(PAD_GROUP_DESC);
        body.groupId = pkt.readGroupId();
        body.buckets = BucketFactory.parseBucketList(targetRi, pkt, pv);
        return body;
    }

    // parse a GROUP FEATURES body
    private static MultipartBody readGroupFeatures(MBodyGroupFeatures body,
                                                   OfPacketReader pkt) {
        final ProtocolVersion pv = body.getVersion();
        final int nTypes = GroupType.values().length;
        body.types = GroupType.decodeFlags(pkt.readInt(), pv);
        body.capabilities = GroupCapability.decodeBitmap(pkt.readInt(), pv);

        // expect maximum groups to be defined in group type order
        body.maxGroups = new HashMap<GroupType, Long>(nTypes);
        body.maxGroups.put(GroupType.ALL, pkt.readU32());
        body.maxGroups.put(GroupType.SELECT, pkt.readU32());
        body.maxGroups.put(GroupType.INDIRECT, pkt.readU32());
        body.maxGroups.put(GroupType.FF, pkt.readU32());

        // expect actions for group to be defined in group type order
        body.actions = new HashMap<GroupType, Set<ActionType>>(nTypes);
        body.actions.put(GroupType.ALL,
                ActionType.decodeFlags(pkt.readInt(), pv));
        body.actions.put(GroupType.SELECT,
                ActionType.decodeFlags(pkt.readInt(), pv));
        body.actions.put(GroupType.INDIRECT,
                ActionType.decodeFlags(pkt.readInt(), pv));
        body.actions.put(GroupType.FF,
                ActionType.decodeFlags(pkt.readInt(), pv));

        return body;
    }

    // parse a METER_STATS body -- an array of meter stat elements
    private static MultipartBody readMeter(MBodyMeterStats.Array array,
                                           OfPacketReader pkt) {
        final ProtocolVersion pv = array.getVersion();
        // need to read meter configs until there are no more
        final int targetRi = pkt.targetIndex();
        while (pkt.ri() < targetRi)
            array.list.add(parseMeterStats(pkt, pv));
        return array;

    }
    // parse a single meter stats element
    private static MBodyMeterStats parseMeterStats(OfPacketReader pkt,
                                                   ProtocolVersion pv) {
        MBodyMeterStats meterStats = new MBodyMeterStats(pv);
        int startParseRi = pkt.ri();
        meterStats.meterId = pkt.readMeterId();
        meterStats.length = pkt.readU16();
        final int targetRi = startParseRi + meterStats.length;

        pkt.skip(PAD_METER_STAT_REPLY);
        meterStats.flowCount = pkt.readInt();
        meterStats.pktInCount = pkt.readLong();
        meterStats.byteInCount = pkt.readLong();
        meterStats.durationSec = pkt.readU32();
        meterStats.durationNSec = pkt.readU32();

        List<MeterBandStats> mbsList = new ArrayList<MeterBandStats>();
        while (pkt.ri() < targetRi)
            mbsList.add(new MeterBandStats(pkt.readLong(), pkt.readLong()));

        meterStats.bandStats = mbsList;
        return meterStats;
    }

    // parse a METER_CONFIG body -- an array of meter config elements
    private static MultipartBody readMeterConfig(MBodyMeterConfig.Array array,
                                                 OfPacketReader pkt)
            throws MessageParseException {
        final ProtocolVersion pv = array.getVersion();
        // need to read meter configs until there are no more
        final int targetRi = pkt.targetIndex();
        while (pkt.ri() < targetRi)
            array.list.add(parseMeterConfig(pkt, pv));
        return array;
    }

    // parse a single meter config element
    private static MBodyMeterConfig parseMeterConfig(OfPacketReader pkt,
                                               ProtocolVersion pv)
            throws MessageParseException {
        MBodyMeterConfig cfg = new MBodyMeterConfig(pv);
        int currentRi = pkt.ri();
        cfg.length = pkt.readU16();
        final int targetRi = currentRi + cfg.length - METER_CONFIG_FIXED_LEN;
        cfg.flags = MeterFlag.decodeBitmap(pkt.readU16(), pv);
        cfg.meterId = pkt.readMeterId();
        cfg.bands = MeterBandFactory.parseMeterBandList(targetRi, pkt, pv);
        return cfg;
    }

    // parse a meter features element
    private static MultipartBody readMeterFeatures(MBodyMeterFeatures body,
                                                   OfPacketReader pkt) {
        final ProtocolVersion pv = body.getVersion();
        body.maxMeters = pkt.readU32();
        body.bandTypes = MeterBandType.decodeFlags(pkt.readInt(), pv);
        body.capabilities = MeterFlag.decodeBitmap(pkt.readInt(), pv);
        body.maxBands = pkt.readU8();
        body.maxColor = pkt.readU8();
        pkt.skip(PAD_METER_FEATURE_REPLY);
        return body;
    }

    // parse a PORT_DESC body -- an array of port desc elements
    private static MultipartBody readPortDesc(MBodyPortDesc.Array array,
                                              OfPacketReader pkt)
            throws MessageParseException {
        final ProtocolVersion pv = array.getVersion();
        // need to read ports until there are no more
        final int targetRi = pkt.targetIndex();
        while (pkt.ri() < targetRi) {
            MBodyPortDesc pd = new MBodyPortDesc(pv);
            pd.port = PortFactory.parsePort(pkt, pv);
            array.list.add(pd);
        }
        return array;
    }

    // === PARSE BOTH ======================================================

    // parse a table features request body, or a table features reply body
    //  NOTE: the request body MAY be empty, in which case the request is
    //        asking for a list of all tables and their features;
    //        otherwise, it contains an array of table-feature structures
    //        which the switch will use to attempt to change its tables to
    //        match the requested configuration.
    private static MultipartBody
    readTableFeatures(MBodyTableFeatures.Array array, OfPacketReader pkt)
            throws MessageParseException {
        ProtocolVersion pv = array.getVersion();
        final int targetRi = pkt.targetIndex();
        final int bytesLeft = targetRi - pkt.ri();
        if (bytesLeft == 0)
            return array;   // there is no body (array of features) to parse

        while (pkt.ri() < targetRi)
            array.list.add(parseTableFeatures(pkt, pv));
        return array;
    }

    // parse a table features element
    private static MBodyTableFeatures parseTableFeatures(OfPacketReader pkt,
                                                         ProtocolVersion pv)
            throws MessageParseException {
        MBodyTableFeatures tf = new MBodyTableFeatures(pv);
        int currentRi = pkt.ri();
        tf.length = pkt.readU16();
        final int targetRi = currentRi + tf.length;
        tf.tableId = pkt.readTableId();
        pkt.skip(PAD_TABLE_FEATURES);
        tf.name = pkt.readString(TABLE_NAME_LEN);
        tf.metadataMatch = pkt.readLong();
        tf.metadataWrite = pkt.readLong();

        // IMPLEMENTATION NOTE:
        //  The config field (next 4 bytes) had bits defined for 1.1 and 1.2
        //  but 1.3 deprecated those bits. A switch properly conforming to 1.3
        //  would set this field to 0, but to maximise interoperability we
        //  will simply ignore the value. Note that OF-1.4 will use a couple
        //  of bits in this field; watch this space...
        pkt.readInt();

        tf.maxEntries = pkt.readU32();
        tf.props = TableFeatureFactory.parsePropList(targetRi, pkt, pv);
        return tf;
    }

    // parse an Experimenter body
    private static MBodyExperimenter readExperimenter(MBodyExperimenter body,
                                                      OfPacketReader pkt) {
        ProtocolVersion pv = body.getVersion();
        body.id = pkt.readInt();
        if (pv.gt(V_1_0))
            body.type = pkt.readInt();

        final int bytesLeft = pkt.targetIndex() - pkt.ri();
        if (bytesLeft > 0)
            body.data = pkt.readBytes(bytesLeft);

        return body;
    }

}
