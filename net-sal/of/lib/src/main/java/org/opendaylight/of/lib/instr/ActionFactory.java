/*
 * (c) Copyright 2012-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.instr;

import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.dt.GroupId;
import org.opendaylight.of.lib.dt.QueueId;
import org.opendaylight.of.lib.match.*;
import org.opendaylight.of.lib.msg.MessageFactory;
import org.opendaylight.of.lib.msg.Port;
import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.StringUtils;
import org.opendaylight.util.net.*;

import java.util.*;

import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_1;
import static org.opendaylight.of.lib.instr.ActionType.*;
import static org.opendaylight.util.PrimitiveUtils.verifyU8;

/**
 * Provides facilities for parsing, creating and encoding {@link Action}
 * instances.
 * <p>
 * Used by the {@link InstructionFactory} in its loftier goal of creating
 * action-list-based instructions.
 *
 * @author Simon Hunt
 */
public class ActionFactory extends AbstractFactory {

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            ActionFactory.class, "actionFactory");

    static final String E_UNEXPECTED_HEADER_LENGTH = RES
            .getString("e_unexpected_header_length");

    static final String E_UNEXPECTED_EXPER_HEADER_LENGTH = RES
            .getString("e_unexpected_exper_header_length");

    static final int ACT_HEADER_LEN = 4;
    static final int EXP_ID_LEN = 4;

    static final int PAD_HEADER = 4;
    static final int PAD_TTL = 3;
    static final int PAD_ETH_TYPE = 2;

    // Length-in-Bytes...
    private static final int LIB_OUTPUT_13 = 16;
    private static final int LIB_OUTPUT_10 = 8;
    private static final int LIB_SET_QUEUE_10 = 16;
    private static final int LIB_SET_QUEUE_13 = 8;
    private static final int LIB_ACTION_U32 = 8;
    private static final int LIB_ACTION_TTL = 8;
    private static final int LIB_ACTION_ETHER = 8;
    private static final int LIB_SET_10_BASE = 8;
    private static final int LIB_SET_10_DL = 16;

    static final ActionFactory AF = new ActionFactory();

    // No instantiation but here
    private ActionFactory() {}

    /** Returns an identifying tag for the action factory.
     *
     * @return an identifying tag
     */
    @Override
    protected String tag() {
        return "AF";
    }

    // =======================================================================
    // === Delegate to the ActionParser to parse actions.

    /** Parses a single action from the supplied buffer.
     * Note that this method causes the reader index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the action.
     *
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a parsed action
     * @throws MessageParseException if unable to parse the action
     */
    public static Action parseAction(OfPacketReader pkt, ProtocolVersion pv)
            throws MessageParseException {
        return ActionParser.parseAction(pkt, pv);
    }

    /** Parses a list of action structures from the supplied buffer.
     * The caller must calculate and specify the target reader index of
     * the buffer that marks the end of the list, so we know when to stop.
     * <p>
     * Note that this method causes the reader index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the list,
     * which should leave the reader index at {@code targetRi}.
     *
     * @param targetRi the target reader index
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a list of parsed actions
     * @throws MessageParseException if unable to parse the structure
     */
    public static List<Action> parseActionList(int targetRi,
                                               OfPacketReader pkt,
                                               ProtocolVersion pv)
            throws MessageParseException {
        return ActionParser.parseActionList(targetRi, pkt, pv);
    }

    /** Parses a list of action header structures from the supplied buffer.
     * This method is provided to support the parsing of an "action" table
     * feature property.
     * The list returned contains either
     * {@link ActHeader} instances or {@link ActExperimenter} instances.
     *
     * @see org.opendaylight.of.lib.msg.TableFeatureFactory
     * @see org.opendaylight.of.lib.msg.TableFeaturePropAction
     *
     * @param targetRi the target reader index
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a list of parsed actions (header info only)
     * @throws MessageParseException if there is an issue parsing the structure
     */
    public static List<Action> parseActionHeaders(int targetRi,
                                                  OfPacketReader pkt,
                                                  ProtocolVersion pv)
            throws MessageParseException {
        return ActionParser.parseActionHeaders(targetRi, pkt, pv);
    }

    // =======================================================================
    // === Create Actions

    private static final String E_UNEX_TYPE = RES.getString("e_unex_type");
    private static final String E_MF_MASK = RES.getString("e_mf_mask");
    private static final String E_UNSUP_FIELD = RES.getString("e_unsup_field");
    private static final String E_MAXLEN_NON_CON = RES
            .getString("e_maxlen_non_con");
    private static final String E_MAXLEN_OOB = RES.getString("e_maxlen_oob");
    private static final String E_NOT_DIV_BY_8 = RES
            .getString("e_not_div_by_8");

    /** Create an action header, setting the length to the default value.
     *
     * @param pv the protocol version
     * @param type the action type
     * @return the header
     */
    private static Action.Header createHeader(ProtocolVersion pv,
                                              ActionType type) {
        Action.Header header = new Action.Header();
        header.type = type;
        header.length = ACT_HEADER_LEN;
        return header;
    }

    /** Creates an action (header only, no payload),
     * using the specified protocol version.
     *
     * @param pv the protocol version
     * @param type the type of action
     * @return the action
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if type is inappropriate
     */
    public static Action createAction(ProtocolVersion pv, ActionType type) {
        MessageFactory.checkVersionSupported(pv);

        notNull(pv, type);
        Action.Header hdr = createHeader(pv, type);
        Action act;
        switch (type) {
            case COPY_TTL_OUT:
                verMin11(pv);
                act = new ActCopyTtlOut(pv, hdr);
                break;
            case COPY_TTL_IN:
                verMin11(pv);
                act = new ActCopyTtlIn(pv, hdr);
                break;
            case DEC_MPLS_TTL:
                verMin11(pv);
                act = new ActDecMplsTtl(pv, hdr);
                break;
            case POP_VLAN:
                act = new ActPopVlan(pv, hdr);
                break;
            case DEC_NW_TTL:
                verMin11(pv);
                act = new ActDecNwTtl(pv, hdr);
                break;
            case POP_PBB:
                verMin13(pv);
                act = new ActPopPbb(pv, hdr);
                break;
            default:
                throw new IllegalArgumentException(E_UNEX_TYPE + type);
        }
        hdr.length += PAD_HEADER; // account for padding
        return act;
    }

    /** Creates a SET_FIELD action.
     * <p>
     * The supplied match field should be a basic match field with its type
     * identifying the field to set, and its value denoting the value to
     * set. There must be no mask included.
     * <p>
     * Additional note: The match of the flow entry must contain the
     * OXM prerequisite corresponding to the field to be set, otherwise
     * an error will be generated.
     * <p>
     * The type of the set-field action can be any valid
     * {@link OxmBasicFieldType OXM header type}.
     * Set-field actions for types
     * {@link OxmBasicFieldType#IN_PORT IN_PORT},
     * {@link OxmBasicFieldType#IN_PHY_PORT IN_PHY_PORT}, and
     * {@link OxmBasicFieldType#METADATA METADATA} are not supported,
     * because those are not header fields.
     * <p>
     * The set-field actions overwrite the header field specified by the
     * OXM type, and perform the necessary CRC recalculation based on the
     * header field. The OXM fields refer to the outermost-possible occurrence
     * in the header, unless the field type explicitly specifies otherwise,
     * and therefore in general the set-field actions apply to the
     * outermost-possible header (e.g. a "Set VLAN ID" set-field action always
     * sets the ID of the outermost VLAN tag).
     *
     * @see FieldFactory
     *
     * @param pv the protocol version
     * @param type the action type (SET_FIELD)
     * @param mf the match field
     * @return the action
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if type is not SET_FIELD, or if
     *          mf is inappropriate
     */
    public static Action createAction(ProtocolVersion pv, ActionType type,
                                      MFieldBasic mf) {
        MessageFactory.checkVersionSupported(pv);
        notNull(pv, type, mf);
        if (type != ActionType.SET_FIELD)
            throw new IllegalArgumentException(E_UNEX_TYPE + type);
        if (mf.hasMask())
            throw new IllegalArgumentException(E_MF_MASK + mf);
        OxmBasicFieldType mft = (OxmBasicFieldType) mf.getFieldType();
        if (mft == OxmBasicFieldType.IN_PORT ||
                mft == OxmBasicFieldType.IN_PHY_PORT ||
                mft == OxmBasicFieldType.METADATA)
            throw new IllegalArgumentException(E_UNSUP_FIELD + mft);

        Action.Header hdr = createHeader(pv, type);
        hdr.fieldType = mft;
        ActSetField act = new ActSetField(pv, hdr);
        act.field = mf;
        if (pv.le(V_1_1)) {
            hdr.length = (mft == OxmBasicFieldType.ETH_SRC ||
                          mft == OxmBasicFieldType.ETH_DST) ? LIB_SET_10_DL
                                                            : LIB_SET_10_BASE;
        } else {
            // calculate the required action length, to hit the 64-bit boundary
            int alen = ACT_HEADER_LEN + mf.getTotalLength();
            int pad = (alen + 7)/8*8 - alen;
            hdr.length = alen + pad;
        }
        return act;
    }

    /** Creates an OUTPUT action.
     * <p>
     * When port is {@link Port#CONTROLLER} (i.e. the action is to output
     * the packet to the controller), {@code maxLen} indicates the
     * maximum number of bytes of the frame <em>(from 0 to CONTROLLER_MAX)</em>
     * to send to the controller:
     * <ul>
     *     <li>
     *         <em>0</em> : no bytes should be sent
     *     </li>
     *     <li>
     *         <em>1 .. {@link ActOutput#CONTROLLER_MAX}</em> : at most, send
     *         this number of bytes
     *     </li>
     *     <li>
     *        {@link ActOutput#CONTROLLER_NO_BUFFER} : the complete packet
     *        is to be sent
     *     </li>
     * </ul>
     * <p>
     * When port is <strong>not</strong> {@code Port.CONTROLLER}, the
     * {@code maxLen} value should be ignored (since the only reasonable action
     * is to send <em>all</em> the bytes of the packet), but this method will
     * allow a value of either <em>0</em> or
     * {@code ActOutput.CONTROLLER_NO_BUFFER} to be set.
     * <p>
     * The {@link #createAction(ProtocolVersion, ActionType, BigPortNumber) degenerate form}
     * of this method invokes this method with {@code maxLen} equal to 0.
     * <p>
     * Note that in 1.0, port numbers are u16.
     *
     * @param pv the protocol version
     * @param type the action type (OUTPUT)
     * @param port output port
     * @param maxLen max length to send to the controller
     * @return the action
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if type is not OUTPUT, or if
     *          maxLen is out of bounds, or if the port number is invalid
     */
    public static Action createAction(ProtocolVersion pv, ActionType type,
                                      BigPortNumber port, int maxLen) {
        MessageFactory.checkVersionSupported(pv);
        notNull(pv, type, port);
        if (type != ActionType.OUTPUT)
            throw new IllegalArgumentException(E_UNEX_TYPE + type);
        if (!port.equals(Port.CONTROLLER) &&
                maxLen != 0 && maxLen != ActOutput.CONTROLLER_NO_BUFFER)
            throw new IllegalArgumentException(E_MAXLEN_NON_CON + maxLen +
                    " port=" + Port.portNumberToString(port));
        if (maxLen != ActOutput.CONTROLLER_NO_BUFFER &&
                (maxLen < 0 || maxLen > ActOutput.CONTROLLER_MAX))
            throw new IllegalArgumentException(E_MAXLEN_OOB + maxLen);
        Port.validatePortValue(port, pv);

        Action.Header hdr = createHeader(pv, type);
        ActOutput act = new ActOutput(pv, hdr);
        act.port = port;
        act.maxLen = maxLen;
        hdr.length = pv == V_1_0 ? LIB_OUTPUT_10 : LIB_OUTPUT_13;
        return act;
    }

    /** Creates an OUTPUT action.
     * <p>
     * If the required output port is {@link Port#CONTROLLER}, use
     * {@link #createAction(ProtocolVersion, ActionType, BigPortNumber, int)}
     * instead.
     *
     * @param pv the protocol version
     * @param type the action type (OUTPUT)
     * @param port output port
     * @return the action
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if type is not OUTPUT, or if
     *          port is CONTROLLER
     */
    public static Action createAction(ProtocolVersion pv, ActionType type,
                                      BigPortNumber port) {
        // TODO Add check for port=Port.CONTROLLER?
        return createAction(pv, type, port, 0);
    }

    /** Creates a GROUP action.
     *
     * @param pv the protocol version
     * @param type the action type (GROUP)
     * @param id the group id
     * @return the action
     * @throws VersionNotSupportedException if the version is not supported
     * @throws VersionMismatchException if the version is &lt; 1.1
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if type is not GROUP
     */
    public static Action createAction(ProtocolVersion pv, ActionType type,
                                      GroupId id) {
        MessageFactory.checkVersionSupported(pv);
        notNull(pv, type, id);
        if (type != ActionType.GROUP)
            throw new IllegalArgumentException(E_UNEX_TYPE + type);
        verMin11(pv);

        Action.Header hdr = createHeader(pv, type);
        ActGroup act = new ActGroup(pv, hdr);
        act.value = id.toLong();
        hdr.length = LIB_ACTION_U32;
        return act;
    }

    /** Creates a SET_QUEUE action for version 1.1 onwards.
     *
     * @param pv the protocol version
     * @param type the action type (SET_QUEUE)
     * @param id the queue id
     * @return the action
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if type is not SET_QUEUE
     */
    public static Action createAction(ProtocolVersion pv, ActionType type,
                                      QueueId id) {
        MessageFactory.checkVersionSupported(pv);
        notNull(pv, type, id);
        if (type != ActionType.SET_QUEUE)
            throw new IllegalArgumentException(E_UNEX_TYPE + type);
        if (pv == V_1_0)
            throw new IllegalArgumentException("v1.0 SET_QUEUE should use" +
                    " createAction(pv,SET_QUEUE,queueId,port)");

        Action.Header hdr = createHeader(pv, type);
        ActSetQueue act = new ActSetQueue(pv, hdr);
        act.queueId = id;
        hdr.length = LIB_SET_QUEUE_13;
        return act;
    }

    /** Creates a SET_QUEUE action (used to be named ENQUEUE) for version 1.0.
     * <p>
     * The specified port is the port to which the queue belongs. It should
     * refer to a valid physical port; that is less than {@code MAX}
     * (defined as {@code 0xff00} in 1.0) or equal to {@link Port#IN_PORT}.
     * <p>
     * Note that in 1.0, port numbers are u16.
     *
     * @param pv the protocol version
     * @param type the action type (SET_QUEUE)
     * @param id the queue id
     * @param port the port to which the queue belongs
     * @return the action
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if type is not SET_QUEUE, or if the
     *      port number is invalid
     */
    public static Action createAction(ProtocolVersion pv, ActionType type,
                                      QueueId id, BigPortNumber port) {
        MessageFactory.checkVersionSupported(pv);
        notNull(pv, type, id);
        if (type != ActionType.SET_QUEUE)
            throw new IllegalArgumentException(E_UNEX_TYPE + type);
        if (pv != V_1_0)
            throw new IllegalArgumentException("v1.1+ SET_QUEUE should use" +
                    " createAction(pv,SET_QUEUE,queueId)");
        Port.validatePortValue(port, pv);

        Action.Header hdr = createHeader(pv, type);
        ActSetQueue act = new ActSetQueue(pv, hdr);
        act.port = port;
        act.queueId = id;
        hdr.length = LIB_SET_QUEUE_10;
        return act;
    }


    /** Creates a TTL-based action.
     *
     * @param pv the protocol version
     * @param type the action type (SET_MPLS_TTL or SET_NW_TTL)
     * @param ttl the TTL value (u8)
     * @return the action
     * @throws VersionNotSupportedException if the version is not supported
     * @throws VersionMismatchException if the version is &lt; 1.1
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if type is inappropriate or
     *      ttl is out of bounds
     */
    public static Action createAction(ProtocolVersion pv, ActionType type,
                                      int ttl) {
        MessageFactory.checkVersionSupported(pv);
        notNull(pv, type);
        verMin11(pv);
        verifyU8(ttl);
        Action.Header hdr = createHeader(pv, type);
        ActionTtl act;
        switch (type) {
            case SET_MPLS_TTL:
                act = new ActSetMplsTtl(pv, hdr);
                break;
            case SET_NW_TTL:
                act = new ActSetNwTtl(pv, hdr);
                break;
            default:
                throw new IllegalArgumentException(E_UNEX_TYPE + type);
        }
        act.ttl = ttl;
        hdr.length = LIB_ACTION_TTL;
        return act;
    }

    /** Creates an EthernetType-based action. This corresponds to the
     * <em>PUSH_VLAN</em>, <em>PUSH_MPLS</em>, <em>PUSH_PBB</em>,
     * and <em>POP_MPLS</em> actions. Note that the acceptable values for
     * <em>EthernetType</em> are:
     * <ul>
     *     <li> <em>PUSH_VLAN</em> can be VLAN(0x8100) or PRV_BRDG(0x88a8)</li>
     *     <li> <em>PUSH_MPLS</em> can be MPLS_U(0x8847) or MPLS_M(0x8848)</li>
     *     <li> <em>PUSH_PBB</em> can be PBB(0x88e7)</li>
     * </ul>
     *
     * @param pv the protocol version
     * @param type the action type
     * @param ethType the EthernetType of the tag to be pushed/popped
     * @return the action
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if the ethernet type is not appropriate
     */
    public static Action createAction(ProtocolVersion pv, ActionType type,
                                      EthernetType ethType) {
        MessageFactory.checkVersionSupported(pv);
        notNull(pv, type, ethType);
        Action.Header hdr = createHeader(pv, type);
        ActionEther act;
        switch (type) {
            case PUSH_VLAN:
                verMin11(pv);
                // as per the OF 1.3.1 spec (pg. 23)...
                if (!ethType.equals(EthernetType.VLAN) &&
                        !ethType.equals(EthernetType.PRV_BRDG))
                    throw new IllegalArgumentException(E_BAD_ETH_VLAN + ethType);
                act = new ActPushVlan(pv, hdr);
                break;
            case PUSH_MPLS:
                verMin11(pv);
                // as per the OF 1.3.1 spec (pg. 23)...
                if (!ethType.equals(EthernetType.MPLS_U) &&
                        !ethType.equals(EthernetType.MPLS_M))
                    throw new IllegalArgumentException(E_BAD_ETH_MPLS + ethType);
                act = new ActPushMpls(pv, hdr);
                break;
            case POP_MPLS:
                verMin11(pv);
                act = new ActPopMpls(pv, hdr);
                break;
            case PUSH_PBB:
                verMin13(pv);
                // as per the OF 1.3.1 spec (pg. 23)...
                if (!ethType.equals(EthernetType.PBB))
                    throw new IllegalArgumentException(E_BAD_ETH_PBB + ethType);
                act = new ActPushPbb(pv, hdr);
                break;
            default:
                throw new IllegalArgumentException(E_UNEX_TYPE + type);
        }
        act.ethType = ethType;
        hdr.length = LIB_ACTION_ETHER;
        return act;
    }

    private static final String E_BAD_ETH_VLAN = RES
            .getString("e_bad_eth_vlan");
    private static final String E_BAD_ETH_MPLS = RES
            .getString("e_bad_eth_mpls");
    private static final String E_BAD_ETH_PBB = RES.getString("e_bad_eth_pbb");

    /** Creates an EXPERIMENTER action. Note that the experimenter-defined
     * data must be an array whose length pads the whole action out to a
     * 64-bit boundary. Given the 4-byte header and 4-byte experimenter id,
     * this means that the array must be exactly 0, 8, 16, 24, ... bytes in
     * length; that is, <em>n*8</em> bytes, where <em>n</em> is 0, 1, 2, 3, ...
     *
     * @param pv the protocol version
     * @param type the action type (EXPERIMENTER)
     * @param id the experimenter encoded ID
     * @param data experimenter-defined data
     * @return the action
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if data is of an unsupported length
     */
    public static Action createAction(ProtocolVersion pv, ActionType type,
                                      int id, byte[] data) {
        MessageFactory.checkVersionSupported(pv);
        notNull(pv, type, data);
        if (type != ActionType.EXPERIMENTER)
            throw new IllegalArgumentException(E_UNEX_TYPE + type);
        if (data.length % 8 != 0)
            throw new IllegalArgumentException(E_NOT_DIV_BY_8 + data.length);

        Action.Header hdr = createHeader(pv, type);
        ActExperimenter act = new ActExperimenter(pv, hdr);
        act.id = id;
        act.data = data.clone();
        hdr.length = ACT_HEADER_LEN + EXP_ID_LEN + data.length;
        return act;
    }

    /** Creates an EXPERIMENTER action. Note that the experimenter-defined
     * data must be an array whose length pads the whole action out to a
     * 64-bit boundary. Given the 4-byte header and 4-byte experimenter ID,
     * this means that the array length must be a multiple of 8.
     *
     * @param pv the protocol version
     * @param type the action type (EXPERIMENTER)
     * @param eid the experimenter ID
     * @param data experimenter-defined data
     * @return the action
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if data is of an unsupported length
     */
    public static Action createAction(ProtocolVersion pv, ActionType type,
                                      ExperimenterId eid, byte[] data) {
        return createAction(pv, type, eid.encodedId(), data);
    }


    // =======================================================================
    // === Convenience methods to create SET_FIELD actions

    /** Creates a set-field action for a mac-address-based match field,
     * using the given protocol version.
     *
     * @param pv the protocol version
     * @param ft the field type
     * @param mac the MAC address
     * @return the set-field action
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if field type is not appropriate
     */
    public static Action createActionSetField(ProtocolVersion pv,
                                              OxmBasicFieldType ft,
                                              MacAddress mac) {
        return createAction(pv, ActionType.SET_FIELD,
                FieldFactory.createBasicField(pv, ft, mac));
    }

    /** Creates a set-field action for an ip-address-based match field,
     * using the given protocol version.
     *
     * @param pv the protocol version
     * @param ft the field type
     * @param ip the IP address
     * @return the set-field action
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if field type is not appropriate
     *          or IP address family is not appropriate
     */
    public static Action createActionSetField(ProtocolVersion pv,
                                              OxmBasicFieldType ft,
                                              IpAddress ip) {
        return createAction(pv, ActionType.SET_FIELD,
                FieldFactory.createBasicField(pv, ft, ip));
    }

    /** Creates a set-field action for a port-number-based match field,
     * using the given protocol version.
     *
     * @param pv the protocol version
     * @param ft the field type
     * @param port the port number
     * @return the set-field action
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if field type is not appropriate
     */
    public static Action createActionSetField(ProtocolVersion pv,
                                              OxmBasicFieldType ft,
                                              PortNumber port) {
        return createAction(pv, ActionType.SET_FIELD,
                FieldFactory.createBasicField(pv, ft, port));
    }

    /** Creates a set-field action for an ICMPv4 Type match field,
     * using the given protocol version.
     *
     * @param pv the protocol version
     * @param ft the field type (ICMPV4_TYPE)
     * @param icmpv4Type the ICMPv4 Type
     * @return the set-field action
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if field type is not appropriate
     */
    public static Action createActionSetField(ProtocolVersion pv,
                                              OxmBasicFieldType ft,
                                              ICMPv4Type icmpv4Type) {
        return createAction(pv, ActionType.SET_FIELD,
                FieldFactory.createBasicField(pv, ft, icmpv4Type));
    }

    /** Creates a set-field action for an ICMPv6 Type match field,
     * using the given protocol version.
     *
     * @param pv the protocol version
     * @param ft the field type (ICMPV6_TYPE)
     * @param icmpv6Type the ICMPv6 Type
     * @return the set-field action
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if field type is not appropriate
     */
    public static Action createActionSetField(ProtocolVersion pv,
                                              OxmBasicFieldType ft,
                                              ICMPv6Type icmpv6Type) {
        return createAction(pv, ActionType.SET_FIELD,
                FieldFactory.createBasicField(pv, ft, icmpv6Type));
    }

    /** Creates a set-field action for an int-payload-based match field,
     * using the given protocol version.
     *
     * @param pv the protocol version
     * @param ft the field type
     * @param value the value
     * @return the set-field action
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if field type is not appropriate or
     *          the value is not appropriate
     */
    public static Action createActionSetField(ProtocolVersion pv,
                                              OxmBasicFieldType ft,
                                              int value) {
        return createAction(pv, ActionType.SET_FIELD,
                FieldFactory.createBasicField(pv, ft, value));
    }

    /** Creates a set-field action for an ETH_TYPE match field,
     * using the given protocol version.
     *
     * @param pv the protocol version
     * @param ft the field type
     * @param ethType the Ethernet Type
     * @return the set-field action
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if field type is not appropriate
     */
    public static Action createActionSetField(ProtocolVersion pv,
                                              OxmBasicFieldType ft,
                                              EthernetType ethType) {
        return createAction(pv, ActionType.SET_FIELD,
                FieldFactory.createBasicField(pv, ft, ethType));
    }

    /**
     * Creates a set-field action for a VLAN_VID,
     * using the given protocol version.
     *
     * @param pv the protocol version
     * @param ft the field type (VLAN_VID)
     * @param vlanId the VLAN vid to match
     * @return the set-field action
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if field type is not appropriate
     */
    public static Action createActionSetField(ProtocolVersion pv,
                                              OxmBasicFieldType ft,
                                              VlanId vlanId) {
        return createAction(pv, ActionType.SET_FIELD,
                FieldFactory.createBasicField(pv, ft, vlanId));
    }

    /** Creates a set-field action for an IP Protocol match field,
     * using the given protocol version.
     *
     * @param pv the protocol version
     * @param ft the field type (IP_PROTO)
     * @param ipp the IP protocol
     * @return the set-field action
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if field type is not appropriate
     */
    public static Action createActionSetField(ProtocolVersion pv,
                                              OxmBasicFieldType ft,
                                              IpProtocol ipp) {
        return createAction(pv, ActionType.SET_FIELD,
                FieldFactory.createBasicField(pv, ft, ipp));
    }

    /** Creates a set-field action for a long-payload-based match field,
     * using the given protocol version.
     *
     * @param pv the protocol version
     * @param ft the field type
     * @param value the value
     * @return the set-field action
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if field type is not appropriate
     *          or the value is not appropriate
     */
    public static Action createActionSetField(ProtocolVersion pv,
                                              OxmBasicFieldType ft,
                                              long value) {
        return createAction(pv, ActionType.SET_FIELD,
                FieldFactory.createBasicField(pv, ft, value));
    }

    /** Creates a set-field action for an IPv6 Extension Header match field,
     * using the given protocol version.
     *
     * @param pv the protocol version
     * @param ft the field type (IPV6_EXTHDR)
     * @param flags the IPv6 Extension Header flags
     * @return the set-field action
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if field type is not appropriate
     */
    public static Action createActionSetField(ProtocolVersion pv,
                                              OxmBasicFieldType ft,
                                              Map<IPv6ExtHdr, Boolean> flags) {
        return createAction(pv, ActionType.SET_FIELD,
                FieldFactory.createBasicField(pv, ft, flags));
    }

    // ====

    /** Creates action headers to be used in encoding a table features
     * actions property.
     *
     * @param pv the protocol version
     * @param types the types of headers to create
     * @return the list of headers
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     */
    public static List<Action> createActionHeaders(ProtocolVersion pv,
                                                   Set<ActionType> types) {
        notNull(pv, types);
        MessageFactory.checkVersionSupported(pv);
        List<Action> acts = new ArrayList<Action>(types.size());
        for (ActionType t: types)
            acts.add(new ActHeader(pv, createHeader(pv, t)));
        return acts;
    }

    // =======================================================================
    // === Delegate to the ActionEncoder to encode actions.

    /** Encodes an action, writing it into the supplied buffer.
     * Note that this method causes the writer index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the action.
     *
     * @param act the action
     * @param pkt the buffer into which the action is to be written
     */
    public static void encodeAction(Action act, OfPacketWriter pkt) {
        ActionEncoder.encodeAction(act, pkt);
    }

    /** Encodes a list of actions, writing them into the supplied buffer.
     * Note that this method causes the writer index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the written
     * actions.
     *
     * @param acts the list of actions
     * @param pkt the buffer into which the actions are to be written
     */
    public static void encodeActionList(List<Action> acts, OfPacketWriter pkt) {
        for (Action act: acts)
            encodeAction(act, pkt);
    }

    /** Encodes a list of experimenter actions, writing them into the supplied
     * buffer. Note that this method causes the writer index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the written
     * actions.
     *
     * @param acts the list of actions
     * @param pkt the buffer into which the actions are to be written
     */
    public static void encodeActionExperList(List<ActExperimenter> acts,
                                             OfPacketWriter pkt) {
        for (Action a: acts)
            encodeAction(a, pkt);
    }


    //======================================================================
    // === Utilities

    /** Outputs a list of actions in debug string format.
     *
     * @param indent the additional indent (number of spaces)
     * @param acts the list of actions
     * @return a multi-line string representation of the list of actions
     */
    public static String toDebugString(int indent, List<Action> acts) {
        final String indStr = EOLI + StringUtils.spaces(indent);
        StringBuilder sb = new StringBuilder();
        for (Action act: acts)
            sb.append(indStr).append(act.toDebugString(indent));
        return sb.toString();
    }

    /** Ensures that the specified action is appropriate to add to a message of
     * the specified version. If all is well, silently returns. If not,
     * throws an exception.
     *
     * @param pv the protocol version
     * @param act the action to validate
     * @param msgType the message type (label)
     * @throws IllegalArgumentException if the action is invalid
     */
    public static void validateAction(ProtocolVersion pv, Action act,
                                      String msgType) {
        if (pv == V_1_0) {
            if (!V10_ACT_TYPES_SET.contains(act.getActionType()))
                throw new IllegalArgumentException(E_INV_10_ACT_TYPE +
                        msgType + ": " + act);
            if (ActSetField.class.isInstance(act)) {
                MatchField mf = ((ActSetField)act).getField();
                MFieldBasic mfb = (MFieldBasic) mf;
                OxmBasicFieldType ft = (OxmBasicFieldType) mfb.getFieldType();
                if (!V10_SET_FIELD_TYPES_SET.contains(ft))
                    throw new IllegalArgumentException(E_INV_10_SET_FIELD_TYPE +
                            msgType + ": " + ft);
            }
        }
        // Implementation note: assume all actions are okay for 1.1+
    }

    private static final String E_INV_10_ACT_TYPE = RES
            .getString("e_inv_10_act_type");

    private static final String E_INV_10_SET_FIELD_TYPE = RES
            .getString("e_inv_10_set_field_type");

    // define the allowable action types (in 1.3 speak) for 1.0 actions
    private static final ActionType[] V10_ACT_TYPES = {
            OUTPUT,         // OUTPUT
            POP_VLAN,       // STRIP_VLAN
            SET_QUEUE,      // ENQUEUE
            SET_FIELD,      // SET_*
            EXPERIMENTER,   // VENDOR
    };
    private static final Set<ActionType> V10_ACT_TYPES_SET =
            new HashSet<ActionType>(Arrays.asList(V10_ACT_TYPES));

    // define the allowable set field types (in 1.3 speak) for 1.0 actions
    private static final OxmBasicFieldType[] V10_SET_FIELD_TYPES = {
            OxmBasicFieldType.VLAN_VID,     // SET_VLAN_VID
            OxmBasicFieldType.VLAN_PCP,     // SET_VLAN_PCP
            OxmBasicFieldType.ETH_SRC,      // SET_DL_SRC
            OxmBasicFieldType.ETH_DST,      // SET_DL_DST
            OxmBasicFieldType.IPV4_SRC,     // SET_NW_SRC
            OxmBasicFieldType.IPV4_DST,     // SET_NW_DST
            OxmBasicFieldType.IP_DSCP,      // SET_NW_TOS
            OxmBasicFieldType.TCP_SRC,      // SET_TP_SRC
            OxmBasicFieldType.TCP_DST,      // SET_TP_DST
    };
    private static final Set<OxmBasicFieldType> V10_SET_FIELD_TYPES_SET =
            new HashSet<OxmBasicFieldType>(Arrays.asList(V10_SET_FIELD_TYPES));
}