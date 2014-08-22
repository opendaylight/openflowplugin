/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.IncompleteStructureException;
import org.opendaylight.of.lib.OfPacketWriter;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.instr.ActionFactory;
import org.opendaylight.of.lib.instr.ActionType;
import org.opendaylight.of.lib.instr.InstructionFactory;
import org.opendaylight.of.lib.match.MatchFactory;
import org.opendaylight.of.lib.mp.MBodyGroupStats.BucketCounter;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.NotYetImplementedException;

import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.mp.MBodyMeterStats.MeterBandStats;
import static org.opendaylight.of.lib.mp.MpBodyFactory.*;

/**
 * Provides facilities for encoding {@link MultipartBody} instances.
 * <p>
 * Used by the {@link MpBodyFactory}.
 *
 * @author Simon Hunt
 */
class MpBodyEncoder {

    // No instantiation
    private MpBodyEncoder() { }

    /** Encodes a multipart request body, writing it into the supplied buffer.
     * Note that this method causes the writer index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the body.
     *
     * @param type the multipart message type
     * @param body the request body
     * @param pkt the buffer into which the body is to be written
     * @throws IncompleteStructureException if the body is incomplete
     */
    static void encodeRequestBody(MultipartType type, MultipartBody body,
                                  OfPacketWriter pkt)
            throws IncompleteStructureException {

        switch (type) {
            // === no body for the following
            case DESC:
            case TABLE:
            case GROUP_DESC:
            case GROUP_FEATURES:
            case METER_FEATURES:
            case PORT_DESC:
                throw new IllegalStateException(type + E_NO_BODY);

            case FLOW:
                encodeRequestFlowStats((MBodyFlowStatsRequest) body, pkt);
                break;

            case AGGREGATE:
                throw new NotYetImplementedException();

            case PORT_STATS:
                encodeRequestPortStats((MBodyPortStatsRequest) body, pkt);
                break;

            case QUEUE:
                encodeRequestQueueStats((MBodyQueueStatsRequest) body, pkt);
                break;

            case GROUP:
                encodeRequestGroupStats((MBodyGroupStatsRequest) body, pkt);
                break;

            case METER:
                encodeRequestMeter((MBodyMeterRequest) body, pkt);
                break;

            case METER_CONFIG:
                encodeRequestMeter((MBodyMeterRequest) body, pkt);
                break;

            case TABLE_FEATURES:
                encodeTableFeatures((MBodyTableFeatures.Array) body, pkt);
                break;

            case EXPERIMENTER:
                encodeExperimenter((MBodyExperimenter) body, pkt);
                break;
        }
    }

    /** Encodes a multipart reply body, writing it into the supplied buffer.
     * Note that this method causes the writer index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the body.
     *
     * @param type the multipart message type
     * @param body the reply body
     * @param pkt the buffer into which the body is to be written
     * @throws IncompleteStructureException if encoding encounters incomplete
     *         or malformed structure
     */
    static void encodeReplyBody(MultipartType type, MultipartBody body,
                                OfPacketWriter pkt)
            throws IncompleteStructureException {
        switch (type) {
            // === implemented
            case DESC:
                encodeReplyDesc((MBodyDesc) body, pkt);
                break;

            case FLOW:
                encodeReplyFlowStats((MBodyFlowStats.Array) body, pkt);
                break;

            case AGGREGATE:
                throw new NotYetImplementedException();

            case TABLE:
                encodeReplyTableStats((MBodyTableStats.Array) body, pkt);
                break;

            case PORT_STATS:
                encodeReplyPortStats((MBodyPortStats.Array) body, pkt);
                break;

            case QUEUE:
                encodeReplyQueueStats((MBodyQueueStats.Array) body, pkt);
                break;

            case GROUP:
                encodeReplyGroupStats((MBodyGroupStats.Array) body, pkt);
                break;

            case GROUP_DESC:
                encodeReplyGroupDesc((MBodyGroupDescStats.Array) body, pkt);
                break;

            case GROUP_FEATURES:
                encodeGroupFeatures((MBodyGroupFeatures) body, pkt);
                break;

            case METER:
                encodeMeterStats((MBodyMeterStats.Array) body, pkt);
                break;

            case METER_CONFIG:
                encodeMeterConfig((MBodyMeterConfig.Array) body, pkt);
                break;

            case METER_FEATURES:
                encodeMeterFeatures((MBodyMeterFeatures) body, pkt);
                break;

            case TABLE_FEATURES:
                encodeTableFeatures((MBodyTableFeatures.Array) body, pkt);
                break;

            case PORT_DESC:
                encodeReplyPortDesc((MBodyPortDesc.Array) body, pkt);
                break;

            case EXPERIMENTER:
                encodeExperimenter((MBodyExperimenter) body, pkt);
                break;
        }
    }

    // === ENCODE REQUESTS =================================================

    // encodes a multipart-request FLOW body
    private static void encodeRequestFlowStats(MBodyFlowStatsRequest fsr,
                                               OfPacketWriter pkt)
            throws IncompleteStructureException {
        fsr.validate();
        final ProtocolVersion pv = fsr.getVersion();
        if (pv == V_1_0)
            MatchFactory.encodeMatch(fsr.match, pkt);
        pkt.write(fsr.tableId);
        pkt.writeZeros(pv == V_1_0 ? PAD_FLOW_STATS_REQ_10 : PAD_FLOW_STATS_REQ);
        PortFactory.encodePortNumber(fsr.outPort, pkt, pv);
        if (pv.gt(V_1_0)) {
            pkt.write(fsr.outGroup);
            pkt.writeZeros(PAD_FLOW_STATS_REQ_2);
            pkt.writeLong(fsr.cookie);
            pkt.writeLong(fsr.cookieMask);
            MatchFactory.encodeMatch(fsr.match, pkt);
        }
    }

    // TODO: encodeRequestAggrStats(...) goes here

    // encodes a multipart-request PORT_STATS body
    private static void encodeRequestPortStats(MBodyPortStatsRequest psr,
                                               OfPacketWriter pkt)
            throws IncompleteStructureException {
        psr.validate();
        final ProtocolVersion pv = psr.getVersion();
        PortFactory.encodePortNumber(psr.port, pkt, pv);
        pkt.writeZeros(pv == V_1_0 ?
            PAD_PORT_STATS_REQ_10 : PAD_PORT_STATS_REQ);
    }

    //encodes a multipart-request QUEUE body
    private static void encodeRequestQueueStats(MBodyQueueStatsRequest qsr,
                                                OfPacketWriter pkt)
            throws IncompleteStructureException {
        qsr.validate();
        final ProtocolVersion pv = qsr.getVersion();
        PortFactory.encodePortNumber(qsr.port, pkt, pv);
        if (pv == V_1_0)
            pkt.writeZeros(PAD_QUEUE_STATS);
        pkt.write(qsr.queueId);
    }

    // encodes a multipart-request GROUP body
    private static void encodeRequestGroupStats(MBodyGroupStatsRequest gsr,
                                                OfPacketWriter pkt)
            throws IncompleteStructureException {
        gsr.validate();
        pkt.write(gsr.groupId);
        pkt.writeZeros(PAD_GROUP_STATS_REQ);
    }

    // encodes a multipart-request METER body
    private static void encodeRequestMeter(MBodyMeterRequest msr,
                                                OfPacketWriter pkt)
            throws IncompleteStructureException {
        msr.validate();
        pkt.write(msr.meterId);
        pkt.writeZeros(PAD_METER_REQ);
    }


    // === ENCODE REPLIES =================================================

    // encodes a multipart-reply DESC body
    private static void encodeReplyDesc(MBodyDesc body, OfPacketWriter pkt) {
        pkt.writeString(body.mfrDesc, MpBodyFactory.DESC_STR_LEN);
        pkt.writeString(body.hwDesc, MpBodyFactory.DESC_STR_LEN);
        pkt.writeString(body.swDesc, MpBodyFactory.DESC_STR_LEN);
        pkt.writeString(body.serialNum, MpBodyFactory.SERIAL_NUM_LEN);
        pkt.writeString(body.dpDesc, MpBodyFactory.DESC_STR_LEN);
    }

    // encodes a multipart-reply FLOW_STATS body (Array-based)
    private static void encodeReplyFlowStats(MBodyFlowStats.Array array,
                                             OfPacketWriter pkt) {
        final ProtocolVersion pv = array.getVersion();
        for (MBodyFlowStats fs: array.getList()) {
            pkt.writeU16(fs.length);
            pkt.write(fs.tableId);
            pkt.writeZeros(PAD_FLOW_STATS);
            if (pv == V_1_0)
                MatchFactory.encodeMatch(fs.match, pkt);
            pkt.writeU32(fs.durationSec);
            pkt.writeU32(fs.durationNsec);
            pkt.writeU16(fs.priority);
            pkt.writeU16(fs.idleTimeout);
            pkt.writeU16(fs.hardTimeout);

            if (pv.ge(V_1_3)) {
                pkt.writeU16(FlowModFlag.encodeBitmap(fs.flags, pv));
                pkt.writeZeros(PAD_FLOW_STATS_2);
            } else {
                pkt.writeZeros(PAD_FLOW_STATS_2_101112);
            }

            pkt.writeLong(fs.cookie);
            pkt.writeLong(fs.packetCount);
            pkt.writeLong(fs.byteCount);

            if (pv == V_1_0) {
                ActionFactory.encodeActionList(fs.actions, pkt);
            } else {
                MatchFactory.encodeMatch(fs.match, pkt);
                InstructionFactory.encodeInstructionList(fs.instructions, pkt);
            }
        }
    }

    // TODO: encodeReplyAggrStats(...) goes here

    // encodes a multipart-reply TABLE_STATS body (Array-based)
    private static void encodeReplyTableStats(MBodyTableStats.Array array,
                                              OfPacketWriter pkt) {
        final ProtocolVersion pv = array.getVersion();
        for (MBodyTableStats ts: array.getList()) {
            pkt.write(ts.tableId);
            pkt.writeZeros(PAD_TABLE_STATS);
            if (pv.lt(V_1_3)) {
                pkt.writeString(ts.name, TABLE_NAME_LEN);
                // FIXME: encode allowable wildcarded flags here.....
                pkt.writeZeros(4);
                pkt.writeU32(ts.maxEntries);
            }
            pkt.writeU32(ts.activeCount);
            pkt.writeLong(ts.lookupCount);
            pkt.writeLong(ts.matchedCount);
        }
    }


    // encodes a multipart-reply PORT_STATS body (Array-based)
    private static void encodeReplyPortStats(MBodyPortStats.Array array,
                                             OfPacketWriter pkt) {
        final ProtocolVersion pv = array.getVersion();
        for (MBodyPortStats ps: array.getList()) {
            PortFactory.encodePortNumber(ps.port, pkt, pv);
            pkt.writeZeros(pv == V_1_0 ?
                    PAD_PORT_STATS_REQ_10 : PAD_PORT_STATS_REQ);
            pkt.writeLong(ps.rxPackets);
            pkt.writeLong(ps.txPackets);
            pkt.writeLong(ps.rxBytes);
            pkt.writeLong(ps.txBytes);
            pkt.writeLong(ps.rxDropped);
            pkt.writeLong(ps.txDropped);
            pkt.writeLong(ps.rxErrors);
            pkt.writeLong(ps.txErrors);
            pkt.writeLong(ps.rxFrameErr);
            pkt.writeLong(ps.rxOverErr);
            pkt.writeLong(ps.rxCrcErr);
            pkt.writeLong(ps.collisions);

            if (pv == V_1_3) {
                pkt.writeU32(ps.durationSec);
                pkt.writeU32(ps.durationNsec);
            }
        }
    }

    // encodes a multipart-reply QUEUE_STATS body (Array-based)
    private static void encodeReplyQueueStats(MBodyQueueStats.Array array,
                                              OfPacketWriter pkt) {
        final ProtocolVersion pv = array.getVersion();
        for (MBodyQueueStats qs: array.getList()) {
            PortFactory.encodePortNumber(qs.port, pkt, pv);
            if (pv == V_1_0)
                pkt.writeZeros(PAD_QUEUE_STATS);
            pkt.write(qs.queueId);
            pkt.writeLong(qs.txBytes);
            pkt.writeLong(qs.txPackets);
            pkt.writeLong(qs.txErrors);

            if (pv == V_1_3) {
                pkt.writeU32(qs.durationSec);
                pkt.writeU32(qs.durationNsec);
            }
        }
    }

    // encodes a multipart-reply GROUP_STATS body (Array-based)
    private static void encodeReplyGroupStats(MBodyGroupStats.Array array,
                                              OfPacketWriter pkt) {
        final ProtocolVersion pv = array.getVersion();
        for (MBodyGroupStats gs: array.getList()) {
            pkt.writeU16(gs.length);
            pkt.writeZeros(PAD_GROUP_STATS);
            pkt.write(gs.groupId);
            pkt.writeU32(gs.refCount);
            pkt.writeZeros(PAD_GROUP_STATS_2);
            pkt.writeLong(gs.packetCount);
            pkt.writeLong(gs.byteCount);

            if (pv == V_1_3) {
                pkt.writeU32(gs.durationSec);
                pkt.writeU32(gs.durationNsec);
            }

            for (BucketCounter bc: gs.bucketStats) {
                pkt.writeLong(bc.packetCount);
                pkt.writeLong(bc.byteCount);
            }
        }
    }

    // encodes a multipart-reply GROUP_DESC body (Array-based)
    private static void encodeReplyGroupDesc(MBodyGroupDescStats.Array array,
                                             OfPacketWriter pkt) {
        final ProtocolVersion pv = array.getVersion();
        for (MBodyGroupDescStats gd: array.getList()) {
            pkt.writeU16(gd.length);
            pkt.writeU8(gd.type.getCode(pv));
            pkt.writeZeros(PAD_GROUP_DESC);
            pkt.write(gd.groupId);
            BucketFactory.encodeBucketList(gd.buckets, pkt);
        }
    }

    // encodes a multipart-reply GROUP_FEATURES body
    private static void encodeGroupFeatures(MBodyGroupFeatures body,
                                            OfPacketWriter pkt) {
        final ProtocolVersion pv = body.getVersion();
        pkt.writeInt(GroupType.encodeFlags(body.types, pv));
        pkt.writeInt(GroupCapability.encodeBitmap(body.capabilities, pv));

        pkt.writeU32(body.maxGroups.get(GroupType.ALL));
        pkt.writeU32(body.maxGroups.get(GroupType.SELECT));
        pkt.writeU32(body.maxGroups.get(GroupType.INDIRECT));
        pkt.writeU32(body.maxGroups.get(GroupType.FF));

        pkt.writeInt(ActionType.encodeFlags(
                body.actions.get(GroupType.ALL), pv));
        pkt.writeInt(ActionType.encodeFlags(
                body.actions.get(GroupType.SELECT), pv));
        pkt.writeInt(ActionType.encodeFlags(
                body.actions.get(GroupType.INDIRECT), pv));
        pkt.writeInt(ActionType.encodeFlags(
                body.actions.get(GroupType.FF), pv));
    }

    // encodes a multipart-part METER_STATS body (Array-based)
    private static void encodeMeterStats(MBodyMeterStats.Array array,
                                         OfPacketWriter pkt) {
        for (MBodyMeterStats meterStats: array.getList()) {
            pkt.write(meterStats.meterId);
            pkt.writeU16(meterStats.length);
            pkt.writeZeros(PAD_METER_STAT_REPLY);
            pkt.writeU32(meterStats.flowCount);
            pkt.writeLong(meterStats.pktInCount);
            pkt.writeLong(meterStats.byteInCount);
            pkt.writeU32(meterStats.durationSec);
            pkt.writeU32(meterStats.durationNSec);

            for (MeterBandStats mbs: meterStats.bandStats) {
                pkt.writeLong(mbs.pktBandCnt);
                pkt.writeLong(mbs.byteBandCnt);
            }
        }
    }

    // encodes a multipart-reply METER_CONFIG body (Array-based)
    private static void encodeMeterConfig(MBodyMeterConfig.Array array,
                                          OfPacketWriter pkt) {
        final ProtocolVersion pv = array.getVersion();
        for (MBodyMeterConfig meterConfig: array.getList()) {
            pkt.writeU16(meterConfig.length);
            pkt.writeU16(MeterFlag.encodeBitmap(meterConfig.flags, pv));
            pkt.write(meterConfig.meterId);
            MeterBandFactory.encodeBandList(meterConfig.bands, pkt);
        }
    }

    // encodes a multipart-reply METER_FEATURES body
    private static void encodeMeterFeatures(MBodyMeterFeatures body,
                                            OfPacketWriter pkt) {
        final ProtocolVersion pv = body.getVersion();
        pkt.writeU32(body.maxMeters);
        pkt.writeInt(MeterBandType.encodeFlags(body.bandTypes, pv));
        pkt.writeInt(MeterFlag.encodeBitmap(body.capabilities, pv));
        pkt.writeU8(body.maxBands);
        pkt.writeU8(body.maxColor);
        pkt.writeZeros(PAD_METER_FEATURE_REPLY);
    }

    // encodes a multipart-reply PORT_DESC body (Array-based)
    private static void encodeReplyPortDesc(MBodyPortDesc.Array array,
                                            OfPacketWriter pkt)
            throws IncompleteStructureException {
        for (MBodyPortDesc pd: array.getList())
            PortFactory.encodePort(pd.port, pkt);
    }

    // === ENCODE BOTH =====================================================

    // encodes a MP-request / MP-reply TABLE_FEATURES body (Array based)
    private static void encodeTableFeatures(MBodyTableFeatures.Array array,
                                            OfPacketWriter pkt) {
        for (MBodyTableFeatures tf: array.getList())
            encodeTableFeaturesElement(tf, pkt);
    }

    // encodes a TABLE_FEATURES element
    private static void encodeTableFeaturesElement(MBodyTableFeatures tf,
                                                   OfPacketWriter pkt) {
        ProtocolVersion pv = tf.getVersion();
        pkt.writeU16(tf.length);
        pkt.write(tf.tableId);
        pkt.writeZeros(PAD_TABLE_FEATURES);
        pkt.writeString(tf.name, TABLE_NAME_LEN);
        pkt.writeLong(tf.metadataMatch);
        pkt.writeLong(tf.metadataWrite);

        // IMPLEMENTATION NOTE:
        //  for now, we are going to write the next 4 bytes as zero.
        //   as the config field is currently reserved, but unused in 1.3.
        //  This might change in future versions.
        pkt.writeInt(0);

        pkt.writeU32(tf.maxEntries);
        TableFeatureFactory.encodePropList(pv, tf.props, pkt);
    }


    // encodes a multipart-request / multipart-reply EXPERIMENTER body
    private static void encodeExperimenter(MBodyExperimenter body,
                                           OfPacketWriter pkt) {
        ProtocolVersion pv = body.getVersion();
        pkt.writeInt(body.id);
        if(pv.gt(V_1_0))
            pkt.writeInt(body.type);

        if(body.data != null && body.data.length > 0)
            pkt.writeBytes(body.data);
    }

    // =====================================================================
}
