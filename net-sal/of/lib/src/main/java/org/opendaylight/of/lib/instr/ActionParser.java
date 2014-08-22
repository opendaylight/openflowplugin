/*
 * (c) Copyright 2012-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.instr;

import org.opendaylight.of.lib.MessageParseException;
import org.opendaylight.of.lib.OfPacketReader;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.VersionNotSupportedException;
import org.opendaylight.of.lib.dt.VId;
import org.opendaylight.of.lib.match.FieldFactory;
import org.opendaylight.of.lib.match.MatchFactory;
import org.opendaylight.of.lib.match.OxmBasicFieldType;
import org.opendaylight.of.lib.match.OxmFieldType;
import org.opendaylight.of.lib.msg.MessageFactory;
import org.opendaylight.of.lib.msg.PortFactory;
import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.net.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_2;
import static org.opendaylight.of.lib.instr.ActionFactory.*;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;

/**
 * Provides facilities for parsing {@link Action} instances.
 * <p>
 * Used by the {@link ActionFactory}.
 *
 * @author Simon Hunt
 */
class ActionParser {

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            ActionParser.class, "actionParser");

    private static final String E_OFF_BY = RES.getString("e_off_by");

    // No instantiation
    private ActionParser() {}

    private static void verifyTargetRi(int targetRi, OfPacketReader pkt)
            throws MessageParseException {
        if (pkt.ri() != targetRi) {
            int offby = pkt.ri() - targetRi;
            throw AF.mpe(pkt, E_OFF_BY + offby);
        }
    }

    /** Parses a list of action structures from the supplied buffer.
     * The caller must calculate and specify the target reader index of
     * the buffer that marks the end of the list, so we know when to stop.
     * <p>
     * Note that this method causes the reader index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the list,
     * which should leave the reader index at {@code targetRi}.
     * <p>
     * This method delegates to {@link #parseAction} for each individual
     * action.
     *
     * @param targetRi the target reader index
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return list of actions
     * @throws MessageParseException if unable to parse the structure
     */
    public static List<Action> parseActionList(int targetRi,
                                               OfPacketReader pkt,
                                               ProtocolVersion pv)
            throws MessageParseException {
        List<Action> actList = new ArrayList<Action>();
        while (pkt.ri() < targetRi) {
            Action act = parseAction(pkt, pv);
            actList.add(act);
        }
        verifyTargetRi(targetRi, pkt);
        return actList;
    }

    /** Parses a single action from the supplied buffer.
     * Note that this method causes the reader index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the action.
     *
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a parsed action
     * @throws MessageParseException if unable to parse the action
     */
    static Action parseAction(OfPacketReader pkt, ProtocolVersion pv)
            throws MessageParseException {
        try {
            Action.Header header = Action.parseHeader(pkt, pv);
            return createActionInstance(header, pkt, pv);
        } catch (MessageParseException mpe) {
            // rethrow MPE
            throw mpe;
        } catch (Exception e) {
            // wrap any other exception in an MPE
            throw AF.mpe(pkt, e);
        }
    }

    /** Parses a list of action header structures from the supplied buffer.
     * This method is provided to support the parsing of an "action" table
     * feature property. The list returned contains either
     * {@link ActHeader} instances or {@link ActExperimenter} instances.
     *
     * @see org.opendaylight.of.lib.msg.TableFeaturePropAction
     *
     * @param targetRi the target reader index
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a list of parsed actions (header info only)
     * @throws MessageParseException if unable to parse the headers
     */
    public static List<Action> parseActionHeaders(int targetRi,
                                                  OfPacketReader pkt,
                                                  ProtocolVersion pv)
            throws MessageParseException {
        List<Action> actList = new ArrayList<Action>();
        while (pkt.ri() < targetRi) {
            Action act = parseActionHeader(pkt, pv);
            actList.add(act);
        }
        verifyTargetRi(targetRi, pkt);
        return actList;
    }

    /** Parses an Action structure (header only) from the supplied buffer.
     * Note that this method causes the reader index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the structure.
     *
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a parsed action structure (header info only)
     * @throws MessageParseException if unable to parse the structure
     */
    private static Action parseActionHeader(OfPacketReader pkt,
                                            ProtocolVersion pv)
            throws MessageParseException {
        try {
            Action.Header header = Action.parseHeader(pkt, pv);
            Action result;
            switch (header.type) {
                case EXPERIMENTER:
                    result = experAction(new ActExperimenter(pv, header), pkt);
                    // all experimenter actions should have a length div by 8
                    if (header.length % 8 != 0)
                        throw AF.mpe(pkt,
                              E_UNEXPECTED_EXPER_HEADER_LENGTH + header.length);
                    break;
                default:
                    // all non-experimenter actions should consist of
                    // a u16 type and a u16 length field (set to 4??)
                    // TODO: clean this up once the spec is clarified...
//                    if (header.length != ACT_HEADER_LEN)
//                        throw AF.mpe(pkt,
//                                E_UNEXPECTED_HEADER_LENGTH + header.length);
                    result = new ActHeader(pv, header);
                    break;
            }
            return result;
        } catch (Exception e) {
            // wrap any unexpected exception in an MPE
            throw AF.mpe(pkt, e);
        }
    }


    /** Creates the action instance, based on the header information.
     *
     * @param header the already-parsed header
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return the instantiated action
     * @throws VersionNotSupportedException if the version is not supported
     * @throws MessageParseException if there is an issue parsing the action
     */
    private static Action createActionInstance(Action.Header header,
                                               OfPacketReader pkt,
                                               ProtocolVersion pv)
            throws MessageParseException {
        // toss an exception if we are not supporting the requested version
        MessageFactory.checkVersionSupported(pv);

        Action action = null;
        switch (header.type) {
            case OUTPUT:
                action = outputAction(new ActOutput(pv, header), pkt);
                break;
            case COPY_TTL_OUT:
                action = headerOnly(new ActCopyTtlOut(pv, header), pkt);
                break;
            case COPY_TTL_IN:
                action = headerOnly(new ActCopyTtlIn(pv, header), pkt);
                break;
            case SET_MPLS_TTL:
                action = ttlAction(new ActSetMplsTtl(pv, header), pkt);
                break;
            case DEC_MPLS_TTL:
                action = headerOnly(new ActDecMplsTtl(pv, header), pkt);
                break;
            case PUSH_VLAN:
                action = ethTypeAction(new ActPushVlan(pv, header), pkt);
                break;
            case POP_VLAN:
                action = headerOnly(new ActPopVlan(pv, header), pkt);
                break;
            case PUSH_MPLS:
                action = ethTypeAction(new ActPushMpls(pv, header), pkt);
                break;
            case POP_MPLS:
                action = ethTypeAction(new ActPopMpls(pv, header), pkt);
                break;
            case SET_QUEUE:
                action = setQueueAction(new ActSetQueue(pv, header), pkt);
                break;
            case GROUP:
                action = u32Action(new ActGroup(pv, header), pkt);
                break;
            case SET_NW_TTL:
                action = ttlAction(new ActSetNwTtl(pv, header), pkt);
                break;
            case DEC_NW_TTL:
                action = headerOnly(new ActDecNwTtl(pv, header), pkt);
                break;
            case SET_FIELD:
                action = setFieldAction(new ActSetField(pv, header), pkt);
                break;
            case PUSH_PBB:
                action = ethTypeAction(new ActPushPbb(pv, header), pkt);
                break;
            case POP_PBB:
                action = headerOnly(new ActPopPbb(pv, header), pkt);
                break;
            case EXPERIMENTER:
                action = experAction(new ActExperimenter(pv, header), pkt);
                break;
        }
        return action;
    }

    // =======================================================================

    // Completes parsing a header-only action
    private static Action headerOnly(Action action, OfPacketReader pkt) {
        pkt.skip(PAD_HEADER);
        return action;
    }

    // Completes parsing an action with a TTL (u8) payload
    private static Action ttlAction(ActionTtl action, OfPacketReader pkt) {
        action.ttl = pkt.readU8();
        pkt.skip(PAD_TTL);
        return action;
    }

    // Completes parsing an action with a U32 payload
    private static Action u32Action(ActionU32 action, OfPacketReader pkt) {
        action.value = pkt.readU32();
        return action;
    }

    // Completes parsing an action with an EthernetType (u16) payload
    private static Action ethTypeAction(ActionEther action, OfPacketReader pkt) {
        action.ethType = pkt.readEthernetType();
        pkt.skip(PAD_ETH_TYPE);
        return action;
    }

    // =======================================================================

    static final int PAD_OUTPUT_13 = 6;

    // Completes parsing an OUTPUT action.
    private static Action outputAction(ActOutput act, OfPacketReader pkt) {
        ProtocolVersion pv = act.getVersion();
        act.port = PortFactory.parsePortNumber(pkt, pv);
        act.maxLen = pkt.readU16();
        if (pv.gt(V_1_0))
            pkt.skip(PAD_OUTPUT_13);
        return act;
    }

    static final int PAD_SET_QUEUE_10 = 6;

    // Completes parsing a SET_QUEUE action.
    private static Action setQueueAction(ActSetQueue act, OfPacketReader pkt) {
        ProtocolVersion pv = act.getVersion();
        if (pv == V_1_0) {
            act.port = BigPortNumber.valueOf(pkt.readU16());
            pkt.skip(PAD_SET_QUEUE_10);
        }
        act.queueId = pkt.readQueueId();
        return act;
    }

    // Completes parsing a SET_FIELD action.
    private static Action setFieldAction(ActSetField act, OfPacketReader pkt)
            throws MessageParseException {
        if (act.getVersion().lt(V_1_2))
            return parseLegacySetFieldAction(act, pkt);

        act.field = FieldFactory.parseField(pkt, act.getVersion());
        // store the field type in the header, if its basic
        OxmFieldType oft = act.field.getFieldType();
        if (OxmBasicFieldType.class.isInstance(oft))
            act.header.fieldType = (OxmBasicFieldType) oft;
        // need to suck up any padding
        int bytesLeft = act.header.length - ACT_HEADER_LEN -
                act.field.getTotalLength();
        pkt.skip(bytesLeft);
        return act;
    }

    static final VId NO_VLAN_ID = VId.valueOf(VId.MAX_VALUE);
    static final int PAD_SET_VLAN_VID_10 = 2;
    static final int PAD_SET_U8_VAL_10 = 3;
    static final int PAD_SET_DL_ADDR_10 = 6;
    static final int PAD_SET_NW_TOS_10 = 3; // NOTE: maps to IP_DSCP
    static final int PAD_SET_TP_PORT_10 = 2;

    /** Where we need to map the old set* actions onto a SET_FIELD action.
     *
     * @param act the partially completed action instance
     * @param pkt the data buffer we are parsing
     * @return the completely parsed action
     */
    private static Action parseLegacySetFieldAction(ActSetField act,
                                                    OfPacketReader pkt) {
        final ProtocolVersion pv = act.getVersion();
        switch (act.header.fieldType) {
            case VLAN_VID:
                VId vid = pkt.readVId();
                pkt.skip(PAD_SET_VLAN_VID_10);
                if (vid.equals(NO_VLAN_ID))
                    vid = VId.NONE;
                VlanId vlanId = MatchFactory.equivVlanId(vid);
                act.field = createBasicField(pv, OxmBasicFieldType.VLAN_VID, vlanId);
                break;

            case VLAN_PCP:
                int pri = pkt.readU8();
                pkt.skip(PAD_SET_U8_VAL_10);
                act.field = createBasicField(pv, OxmBasicFieldType.VLAN_PCP, pri);
                break;

            case ETH_SRC:
                MacAddress sMac = pkt.readMacAddress();
                pkt.skip(PAD_SET_DL_ADDR_10);
                act.field = createBasicField(pv, OxmBasicFieldType.ETH_SRC, sMac);
                break;

            case ETH_DST:
                MacAddress dMac = pkt.readMacAddress();
                pkt.skip(PAD_SET_DL_ADDR_10);
                act.field = createBasicField(pv, OxmBasicFieldType.ETH_DST, dMac);
                break;

            case IPV4_SRC:
                IpAddress sIp = pkt.readIPv4Address();
                act.field = createBasicField(pv, OxmBasicFieldType.IPV4_SRC, sIp);
                break;

            case IPV4_DST:
                IpAddress dIp = pkt.readIPv4Address();
                act.field = createBasicField(pv, OxmBasicFieldType.IPV4_DST, dIp);
                break;

            case IP_DSCP:
                int dscp = pkt.readU8();
                pkt.skip(PAD_SET_NW_TOS_10);
                act.field = createBasicField(pv, OxmBasicFieldType.IP_DSCP, dscp);
                break;

            /*
             * IMPLEMENTATION NOTE:
             *  TCP_SRC(DST) stands in for TCP/UDP/SCTP, since in 1.0 these
             *  fields are overloaded.
             */
            case TCP_SRC:
                PortNumber spn = pkt.readPortNumber();
                pkt.skip(PAD_SET_TP_PORT_10);
                act.field = createBasicField(pv, OxmBasicFieldType.TCP_SRC, spn);
                break;

            case TCP_DST:
                PortNumber dpn = pkt.readPortNumber();
                pkt.skip(PAD_SET_TP_PORT_10);
                act.field = createBasicField(pv, OxmBasicFieldType.TCP_DST, dpn);
                break;

            default:
                throw new IllegalStateException("parseLegacySetFieldAction(): " +
                        "Can't handle: " + act.header.fieldType);
        }
        return act;
    }

    // Completes parsing an EXPERIMENTER action.
    private static Action experAction(ActExperimenter act, OfPacketReader pkt) {
        act.id = pkt.readInt();
        int dataBytes = act.header.length - ACT_HEADER_LEN - EXP_ID_LEN;
        act.data = pkt.readBytes(dataBytes);
        return act;
    }
}