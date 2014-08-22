/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.msg.MessageFactory;
import org.opendaylight.of.lib.msg.OfmMutableMultipartReply;
import org.opendaylight.of.lib.msg.OfmMutableMultipartRequest;
import org.opendaylight.util.NotYetImplementedException;
import org.opendaylight.util.ResourceUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static org.opendaylight.of.lib.CommonUtils.*;

/**
 * Provides facilities for parsing, creating and encoding {@link MultipartBody}
 * instances.
 *
 * @author Simon Hunt
 */
public class MpBodyFactory extends AbstractFactory {

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            MpBodyFactory.class, "mpBodyFactory");

    static final String E_NO_BODY = RES.getString("e_no_body");
    static final String E_NOT_AN_ARRAY_TYPE = RES
            .getString("e_not_an_array_type");

    static final int PAD_FLOW_STATS_REQ = 3;
    static final int PAD_FLOW_STATS_REQ_10 = 1;
    static final int PAD_FLOW_STATS_REQ_2 = 4;
    static final int PAD_FLOW_STATS = 1;
    static final int PAD_FLOW_STATS_2 = 4;
    static final int PAD_FLOW_STATS_2_101112 = 6;

    static final int PAD_QUEUE_STATS = 2;

    static final int PAD_GROUP_STATS_REQ = 4;
    static final int PAD_GROUP_STATS = 2;
    static final int PAD_GROUP_STATS_2 = 4;

    static final int PAD_GROUP_DESC = 1;

    static final int PAD_TABLE_STATS = 3;
    static final int PAD_TABLE_FEATURES = 5;

    // the meter config header data
    static final int METER_CONFIG_FIXED_LEN = 8;
    static final int PAD_METER_REQ = 4;
    static final int PAD_METER_STAT_REPLY = 6;
    static final int PAD_METER_FEATURE_REPLY = 2;

    static final int PAD_PORT_STATS_REQ = 4;
    static final int PAD_PORT_STATS_REQ_10 = 6;

    /** Description string field length. */
    public static final int DESC_STR_LEN = 256;
    /** Serial number string field length. */
    public static final int SERIAL_NUM_LEN = 32;
    /** Table name string field length. */
    public static final int TABLE_NAME_LEN = 32;

    static final MpBodyFactory MBF = new MpBodyFactory();

    // no instantiation except here
    private MpBodyFactory() { }

    /** Returns an identifying tag for the multipart body factory.
     *
     * @return an identifying tag
     */
    @Override
    protected String tag() {
        return "MpBF";
    }

    // =======================================================================
    // === Delegate to the MpBodyParser to parse bodies.

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
    public static MultipartBody parseRequestBody(MultipartType type,
                                                 OfPacketReader pkt,
                                                 ProtocolVersion pv)
            throws MessageParseException {
        return MpBodyParser.parseRequestBody(type, pkt, pv);
    }

    /**
     * Parses a multipart reply message body from the supplied buffer.
     * Note that this method causes the reader index of the underlying
     * packet reader to be advanced by the length of the body.
     *
     * @param type the type of reply body to parse
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a parsed reply message body
     * @throws MessageParseException if unable to parse the body
     */
    public static MultipartBody parseReplyBody(MultipartType type,
                                               OfPacketReader pkt,
                                               ProtocolVersion pv)
            throws MessageParseException {
        return MpBodyParser.parseReplyBody(type, pkt, pv);
    }

    // =======================================================================
    // === Setting Types

    /**
     * Sets the type in the message header, and adds the appropriate body,
     * if any.
     *
     * @param msg the mutable request
     * @param type its required type
     */
    public static void setRequestType(OfmMutableMultipartRequest msg,
                                      MultipartType type) {
        if (type != null) {
            try {
                MultipartBody body = createRequestBody(msg.getVersion(), type);
                msg.body(body);
            } catch (IllegalArgumentException ignored) {
                // request type doesn't have a body
                msg.type(type);
            }
        }
    }

    /**
     * Sets the type in the message header, and adds the appropriate body.
     *
     * @param msg the mutable request
     * @param type its required type
     */
    public static void setReplyType(OfmMutableMultipartReply msg,
                                    MultipartType type) {
        if (type != null) {
            MultipartBody body = createReplyBody(msg.getVersion(), type);
            msg.body(body);
            // reply type (in MP header) is set via the body() method
        }
    }


    // =======================================================================
    // === Create Bodies

    /**
     * Creates the mutable multipart request body for the given type,
     * for the given protocol version.
     *
     * @param pv the protocol version
     * @param type the required multipart request type
     * @return the mutable body
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException either parameter is null
     * @throws IllegalArgumentException if the type does not have a body
     */
    public static MultipartBody createRequestBody(ProtocolVersion pv,
                                                  MultipartType type) {
        notNull(pv, type);
        MessageFactory.checkVersionSupported(pv);
        MultipartBody body = null;
        String typeLabel = type.name();
        switch (type) {
            // the following request types have no body
            case DESC:
            case TABLE:
                throw new IllegalArgumentException(type + E_NO_BODY);

            case GROUP_DESC:
                verMin11(pv, typeLabel);
                throw new IllegalArgumentException(type + E_NO_BODY);

            case GROUP_FEATURES:
                verMin12(pv, typeLabel);
                throw new IllegalArgumentException(type + E_NO_BODY);

            case METER_FEATURES:
            case PORT_DESC:
                verMin13(pv, typeLabel);
                throw new IllegalArgumentException(type + E_NO_BODY);

            // the remaining request types contain a body

            case FLOW:
                body = new MBodyMutableFlowStatsRequest(pv);
                break;

            case AGGREGATE:
                throw new NotYetImplementedException();

            case PORT_STATS:
                body = new MBodyMutablePortStatsRequest(pv);
                break;

            case QUEUE:
                body = new MBodyMutableQueueStatsRequest(pv);
                break;

            case GROUP:
                verMin11(pv, typeLabel);
                body = new MBodyMutableGroupStatsRequest(pv);
                break;

            case METER:
                verMin13(pv, typeLabel);
                body = new MBodyMutableMeterStatsRequest(pv);
                break;

            case METER_CONFIG:
                verMin13(pv, typeLabel);
                body = new MBodyMutableMeterConfigRequest(pv);
                break;

            case TABLE_FEATURES:
                verMin13(pv, typeLabel);
                body = new MBodyTableFeatures.MutableArray(pv);
                break;

            case EXPERIMENTER:
                body = new MBodyMutableExperimenter(pv);
                break;
        }
        return body;
    }

    /**
     * Creates the mutable multipart reply body for the given type and
     * protocol version.
     *
     * @param pv the protocol version
     * @param type the required multipart reply type
     * @return the mutable body
     * @throws NullPointerException if either paramter is null
     * @throws VersionNotSupportedException if the version is not supported
     */
    public static MultipartBody createReplyBody(ProtocolVersion pv,
                                                MultipartType type) {
        notNull(pv, type);
        MessageFactory.checkVersionSupported(pv);
        MultipartBody body = null;
        String typeLabel = type.name();
        switch (type) {

            case DESC:
                body = new MBodyMutableDesc(pv);
                break;

            case FLOW:
                body = new MBodyFlowStats.MutableArray(pv);
                break;

            case AGGREGATE:
                throw new NotYetImplementedException();

            case TABLE:
                body = new MBodyTableStats.MutableArray(pv);
                break;

            case PORT_STATS:
                body = new MBodyPortStats.MutableArray(pv);
                break;

            case QUEUE:
                body = new MBodyQueueStats.MutableArray(pv);
                break;

            case GROUP:
                verMin11(pv, typeLabel);
                body = new MBodyGroupStats.MutableArray(pv);
                break;

            case GROUP_DESC:
                verMin11(pv, typeLabel);
                body = new MBodyGroupDescStats.MutableArray(pv);
                break;

            case GROUP_FEATURES:
                verMin12(pv, typeLabel);
                body = new MBodyMutableGroupFeatures(pv);
                break;

            case METER:
                verMin13(pv, typeLabel);
                body = new MBodyMeterStats.MutableArray(pv);
                break;

            case METER_CONFIG:
                verMin13(pv, typeLabel);
                body = new MBodyMeterConfig.MutableArray(pv);
                break;

            case METER_FEATURES:
                verMin13(pv, typeLabel);
                body = new MBodyMutableMeterFeatures(pv);
                break;

            case TABLE_FEATURES:
                body = new MBodyTableFeatures.MutableArray(pv);
                break;

            case PORT_DESC:
                body = new MBodyPortDesc.MutableArray(pv);
                break;

            case EXPERIMENTER:
                body = new MBodyMutableExperimenter(pv);
                break;
        }
        return body;
    }

    /**
     * Creates the mutable multipart reply body array element for the
     * given type and protocol version.
     *
     * @param pv the protocol version
     * @param type the required multipart reply element type
     * @return the mutable element
     * @throws NullPointerException if either paramter is null
     * @throws VersionNotSupportedException if the version is not supported
     */
    public static MultipartBody createReplyBodyElement(ProtocolVersion pv,
                                                       MultipartType type) {
        notNull(pv, type);
        MessageFactory.checkVersionSupported(pv);
        MultipartBody elem = null;
        String typeLabel = type.name();
        switch (type) {

            // THESE types don't have arrays in their replies
            case DESC:
            case AGGREGATE:
            case GROUP_FEATURES:
            case METER_FEATURES:
            case EXPERIMENTER:
                throw new IllegalArgumentException(E_NOT_AN_ARRAY_TYPE + type);

            case FLOW:
                elem = new MBodyMutableFlowStats(pv);
                break;

            case TABLE:
                elem = new MBodyMutableTableStats(pv);
                break;

            case PORT_STATS:
                elem = new MBodyMutablePortStats(pv);
                break;

            case QUEUE:
                elem = new MBodyMutableQueueStats(pv);
                break;

            case GROUP:
                elem = new MBodyMutableGroupStats(pv);
                break;

            case GROUP_DESC:
                elem = new MBodyMutableGroupDescStats(pv);
                break;

            case METER:
                verMin13(pv, typeLabel);
                elem = new MBodyMutableMeterStats(pv);
                break;

            case METER_CONFIG:
                elem = new MBodyMutableMeterConfig(pv);
                break;

            case TABLE_FEATURES:
                verMin13(pv, typeLabel);
                elem = new MBodyMutableTableFeatures(pv);
                break;

            case PORT_DESC:
                throw new NotYetImplementedException();

        }
        return elem;
    }

    // =======================================================================
    // === Delegate to the MpBodyEncoder to encode bodies.

    /**
     * Encodes a multipart request body (if it is not null), writing it
     * into the supplied buffer. Note that this method causes the writer
     * index of the underlying {@code PacketBuffer} to be advanced by the
     * length of the body.
     *
     * @param body the body (may be null)
     * @param pkt the buffer into which the body is to be written
     * @throws IncompleteStructureException if the body is incomplete
     */
    public static void encodeRequestBody(MultipartBody body, OfPacketWriter pkt)
            throws IncompleteStructureException {
        if (body != null)
            MpBodyEncoder.encodeRequestBody(getType(body), body, pkt);
    }

    /**
     * Encodes a multipart reply body (if it is not null), writing it
     * into the supplied buffer. Note that this method causes the writer
     * index of the underlying {@code PacketBuffer} to be advanced by the
     * length of the body.
     *
     * @param body the body (may be null)
     * @param pkt the buffer into which the body is to be written
     * @throws IncompleteStructureException if encoding encounters incomplete
     *         or malformed structure
     */
    public static void encodeReplyBody(MultipartBody body, OfPacketWriter pkt)
            throws IncompleteStructureException {
        if (body != null)
            MpBodyEncoder.encodeReplyBody(getType(body), body, pkt);
    }

    // =======================================================================
    // === Utility methods

    /**
     * Returns the multipart-type corresponding to the specified
     * multipart body instance. If the body is an {@link MBodyList array},
     * returns the type corresponding to the elements of the array.
     *
     * @param body the body instance
     * @return the corresponding type constant
     */
    public static MultipartType getType(MultipartBody body) {
        if (MBodyList.class.isInstance(body)) {
            // we have to get the parameterized type
            MBodyList<?> array = (MBodyList<?>) body;
            return BODY_MAP.get(array.getElementClass());
        }
        return BODY_MAP.get(body.getClass());
    }


    /**
     * Returns a synthetically created MBodyFlowStats element as a placeholder
     * for unparsable flow stats. The given cause of the parse error attached.
     *
     * @param pv the protocol version
     * @param cause the detected parse exception
     * @return a synthetic MBodyFlowStats
     */
    public static MBodyFlowStats createSyntheticFlowStats(ProtocolVersion pv,
                                                          Throwable cause) {
        return new SynthMBodyFlowStats(pv, cause);
    }

    // =======================================================================
    public static class SynthMBodyFlowStats extends MBodyFlowStats {
        public SynthMBodyFlowStats(ProtocolVersion pv, Throwable cause) {
            super(pv);
            parseErrorCause = cause;
        }
    }

    // =======================================================================
    /** Mapping of concrete classes to multipart types. */
    private static final Map<Class<? extends MultipartBody>, MultipartType>
        BODY_MAP = new HashMap<Class<? extends MultipartBody>, MultipartType>();
    static {
        // === REQUESTS ===
        // no request body for DESC
        BODY_MAP.put(MBodyFlowStatsRequest.class, MultipartType.FLOW);
        BODY_MAP.put(MBodyMutableFlowStatsRequest.class, MultipartType.FLOW);
//        BODY_MAP.put(MBodyAggrStatsRequest.class, MultipartType.AGGREGATE);
//        BODY_MAP.put(MBodyMutableAggrStatsRequest.class, MultipartType.AGGREGATE);
        // no request body for TABLE
        BODY_MAP.put(MBodyPortStatsRequest.class, MultipartType.PORT_STATS);
        BODY_MAP.put(MBodyMutablePortStatsRequest.class, MultipartType.PORT_STATS);
        BODY_MAP.put(MBodyQueueStatsRequest.class, MultipartType.QUEUE);
        BODY_MAP.put(MBodyMutableQueueStatsRequest.class, MultipartType.QUEUE);
        BODY_MAP.put(MBodyGroupStatsRequest.class, MultipartType.GROUP);
        BODY_MAP.put(MBodyMutableGroupStatsRequest.class, MultipartType.GROUP);
        // no request body for GROUP_DESC
        // no request body for GROUP_FEATURES
        BODY_MAP.put(MBodyMeterStatsRequest.class, MultipartType.METER);
        BODY_MAP.put(MBodyMutableMeterStatsRequest.class, MultipartType.METER);
        BODY_MAP.put(MBodyMeterConfigRequest.class, MultipartType.METER_CONFIG);
        BODY_MAP.put(MBodyMutableMeterConfigRequest.class, MultipartType.METER_CONFIG);
        // no request body for METER_FEATURES
        BODY_MAP.put(MBodyTableFeatures.class, MultipartType.TABLE_FEATURES);
        BODY_MAP.put(MBodyMutableTableFeatures.class, MultipartType.TABLE_FEATURES);
        // no request body for PORT_DESC
        BODY_MAP.put(MBodyExperimenter.class, MultipartType.EXPERIMENTER);
        BODY_MAP.put(MBodyMutableExperimenter.class, MultipartType.EXPERIMENTER);

        // === REPLIES ===
        BODY_MAP.put(MBodyDesc.class, MultipartType.DESC);
        BODY_MAP.put(MBodyMutableDesc.class, MultipartType.DESC);
        BODY_MAP.put(MBodyFlowStats.class, MultipartType.FLOW);
        BODY_MAP.put(MBodyMutableFlowStats.class, MultipartType.FLOW);
//        BODY_MAP.put(MBodyAggrStatsReply.class, MultipartType.AGGREGATE);
//        BODY_MAP.put(MBodyMutableAggrStatsReply.class, MultipartType.AGGREGATE);
        BODY_MAP.put(MBodyTableStats.class, MultipartType.TABLE);
        BODY_MAP.put(MBodyMutableTableStats.class, MultipartType.TABLE);
        BODY_MAP.put(MBodyPortStats.class, MultipartType.PORT_STATS);
        BODY_MAP.put(MBodyMutablePortStats.class, MultipartType.PORT_STATS);
        BODY_MAP.put(MBodyQueueStats.class, MultipartType.QUEUE);
        BODY_MAP.put(MBodyMutableQueueStats.class, MultipartType.QUEUE);
        BODY_MAP.put(MBodyGroupStats.class, MultipartType.GROUP);
        BODY_MAP.put(MBodyMutableGroupStats.class, MultipartType.GROUP);
        BODY_MAP.put(MBodyGroupDescStats.class, MultipartType.GROUP_DESC);
        BODY_MAP.put(MBodyMutableGroupDescStats.class, MultipartType.GROUP_DESC);
        BODY_MAP.put(MBodyGroupFeatures.class, MultipartType.GROUP_FEATURES);
        BODY_MAP.put(MBodyMutableGroupFeatures.class, MultipartType.GROUP_FEATURES);
        BODY_MAP.put(MBodyMeterStats.class, MultipartType.METER);
        BODY_MAP.put(MBodyMutableMeterStats.class, MultipartType.METER);
        BODY_MAP.put(MBodyMeterConfig.class, MultipartType.METER_CONFIG);
        BODY_MAP.put(MBodyMutableMeterConfig.class, MultipartType.METER_CONFIG);
        BODY_MAP.put(MBodyMeterFeatures.class, MultipartType.METER_FEATURES);
        BODY_MAP.put(MBodyMutableMeterFeatures.class, MultipartType.METER_FEATURES);
        BODY_MAP.put(MBodyTableFeatures.class, MultipartType.TABLE_FEATURES);
        BODY_MAP.put(MBodyMutableTableFeatures.class, MultipartType.TABLE_FEATURES);
        BODY_MAP.put(MBodyPortDesc.class, MultipartType.PORT_DESC);
//        BODY_MAP.put(MBodyMutablePortDesc.class, MultipartType.PORT_DESC);
        BODY_MAP.put(MBodyExperimenter.class, MultipartType.EXPERIMENTER);
        BODY_MAP.put(MBodyMutableExperimenter.class, MultipartType.EXPERIMENTER);
    }
}