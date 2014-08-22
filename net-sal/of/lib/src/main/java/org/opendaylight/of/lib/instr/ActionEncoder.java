/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.instr;

import org.opendaylight.of.lib.OfPacketWriter;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.match.*;
import org.opendaylight.of.lib.msg.Port;
import org.opendaylight.of.lib.msg.PortFactory;
import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.net.PortNumber;

import java.util.ResourceBundle;

import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_2;
import static org.opendaylight.of.lib.instr.ActionFactory.ACT_HEADER_LEN;

/**
 * Provides facilities for encoding {@link Action} instances.
 * <p>
 * Used by the {@link ActionFactory}.
 *
 * @author Simon Hunt
 */
class ActionEncoder {

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            ActionEncoder.class, "actionEncoder");

    private static final String E_BAD_MAP_ACTION_TO_LEGACY = RES
            .getString("e_bad_map_action_to_legacy");
    private static final String E_BAD_MAP_FIELD_TO_LEGACY = RES
            .getString("e_bad_map_field_to_legacy");

    // No instantiation
    private ActionEncoder() { }

    /** Encodes an action, writing it into the supplied buffer.
     * Note that this method causes the writer index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the action.
     *
     * @param act the action
     * @param pkt the buffer into which the action is to be written
     */
    static void encodeAction(Action act, OfPacketWriter pkt) {
        // first, write out the header..
        ProtocolVersion pv = act.getVersion();
        ActionType at = act.header.type;
        OldActionType oat = getLegacyActionType(act.header, pv);
        int typeCode = oat != null ? oat.getCode(pv) : at.getCode(pv);
        pkt.writeU16(typeCode);
        pkt.writeU16(act.header.length);

        // if the action is header-only, we are done.
        if (ActHeader.class.isInstance(act))
            return;

        // now deal with the payload, based on type
        switch (act.header.type) {

            case OUTPUT:
                encodeOutputAction((ActOutput) act, pkt);
                break;
            case COPY_TTL_OUT:
                encodeHeaderOnly(pkt);
                break;
            case COPY_TTL_IN:
                encodeHeaderOnly(pkt);
                break;
            case SET_MPLS_TTL:
                encodeTtlAction((ActionTtl) act, pkt);
                break;
            case DEC_MPLS_TTL:
                encodeHeaderOnly(pkt);
                break;
            case PUSH_VLAN:
                encodeEthTypeAction((ActionEther) act, pkt);
                break;
            case POP_VLAN:
                encodeHeaderOnly(pkt);
                break;
            case PUSH_MPLS:
                encodeEthTypeAction((ActionEther) act, pkt);
                break;
            case POP_MPLS:
                encodeEthTypeAction((ActionEther) act, pkt);
                break;
            case SET_QUEUE:
                encodeSetQueueAction((ActSetQueue) act, pkt);
                break;
            case GROUP:
                encodeU32Action((ActionU32) act, pkt);
                break;
            case SET_NW_TTL:
                encodeTtlAction((ActionTtl) act, pkt);
                break;
            case DEC_NW_TTL:
                encodeHeaderOnly(pkt);
                break;
            case SET_FIELD:
                encodeSetFieldAction((ActSetField) act, pkt);
                break;
            case PUSH_PBB:
                encodeEthTypeAction((ActionEther) act, pkt);
                break;
            case POP_PBB:
                encodeHeaderOnly(pkt);
                break;
            case EXPERIMENTER:
                encodeExperAction((ActExperimenter) act, pkt);
                break;
        }
    }

    /** Returns the legacy type for a pre-1.2 action.
     *
     * @param header the header
     * @param pv protocol version
     * @return the legacy action type
     */
    private static OldActionType getLegacyActionType(Action.Header header,
                                                     ProtocolVersion pv) {
        if (pv.ge(V_1_2))
            return null;

        OldActionType oat;
        switch (header.type) {
            case OUTPUT:
                oat = OldActionType.OUTPUT;
                break;
            case COPY_TTL_OUT:
                oat = OldActionType.COPY_TTL_OUT;
                break;
            case COPY_TTL_IN:
                oat = OldActionType.COPY_TTL_IN;
                break;
            case SET_MPLS_TTL:
                oat = OldActionType.SET_MPLS_TTL;
                break;
            case DEC_MPLS_TTL:
                oat = OldActionType.DEC_MPLS_TTL;
                break;
            case PUSH_VLAN:
                oat = OldActionType.PUSH_VLAN;
                break;
            case POP_VLAN:
                oat = pv == V_1_0 ? OldActionType.STRIP_VLAN
                                  : OldActionType.POP_VLAN;
                break;
            case PUSH_MPLS:
                oat = OldActionType.PUSH_MPLS;
                break;
            case POP_MPLS:
                oat = OldActionType.POP_MPLS;
                break;
            case SET_QUEUE:
                oat = OldActionType.SET_QUEUE;
                break;
            case GROUP:
                oat = OldActionType.GROUP;
                break;
            case SET_NW_TTL:
                oat = OldActionType.SET_NW_TTL;
                break;
            case DEC_NW_TTL:
                oat = OldActionType.DEC_NW_TTL;
                break;
            case EXPERIMENTER:
                oat = OldActionType.EXPERIMENTER;
                break;

            case SET_FIELD:
                oat = lookupOldActionType(header.fieldType);
                break;

            default:
                throw new IllegalStateException(E_BAD_MAP_ACTION_TO_LEGACY +
                                                header.type);
        }
        return oat;
    }

    /** Returns the appropriate legacy action type, based on the given
     * field type (from a SET_FIELD action).
     *
     * @param ft the field type
     * @return the appropriate legacy action type
     */
    private static OldActionType lookupOldActionType(OxmBasicFieldType ft) {
        OldActionType oat;
        switch (ft) {
            case VLAN_VID:
                oat = OldActionType.SET_VLAN_VID;
                break;
            case VLAN_PCP:
                oat = OldActionType.SET_VLAN_PCP;
                break;
            case ETH_SRC:
                oat = OldActionType.SET_DL_SRC;
                break;
            case ETH_DST:
                oat = OldActionType.SET_DL_DST;
                break;
            case IPV4_SRC:
                oat = OldActionType.SET_NW_SRC;
                break;
            case IPV4_DST:
                oat = OldActionType.SET_NW_DST;
                break;
            case IP_DSCP:
                oat = OldActionType.SET_NW_TOS;
                break;
            case IP_ECN:
                oat = OldActionType.SET_NW_ECN;
                break;
            case TCP_SRC:
            case UDP_SRC:
            case SCTP_SRC:
                oat = OldActionType.SET_TP_SRC;
                break;
            case TCP_DST:
            case UDP_DST:
            case SCTP_DST:
                oat = OldActionType.SET_TP_DST;
                break;
            default:
                throw new IllegalStateException(E_BAD_MAP_FIELD_TO_LEGACY + ft);
        }
        return oat;
    }

    //=====================================================================
    // == encode specific instruction types

    // encodes an action that consists of just the header
    private static void encodeHeaderOnly(OfPacketWriter pkt) {
        pkt.writeZeros(ActionFactory.PAD_HEADER);
    }

    // encodes an output action
    private static void encodeOutputAction(ActOutput act, OfPacketWriter pkt) {
        ProtocolVersion pv = act.getVersion();
        PortFactory.encodePortNumber(act.port, pkt, pv);
        pkt.writeU16(act.maxLen);
        if (pv.gt(V_1_0))
            pkt.writeZeros(ActionParser.PAD_OUTPUT_13);
    }

    // encodes a set queue action
    private static void encodeSetQueueAction(ActSetQueue act,
                                             OfPacketWriter pkt) {
        if (act.getVersion() == V_1_0) {
            PortNumber special = Port.equivalentSpecial(act.port);
            int pnum = special != null ? special.toInt()
                                       : (int) act.port.toLong();
            pkt.writeU16(pnum);
            pkt.writeZeros(ActionParser.PAD_SET_QUEUE_10);
        }
        pkt.write(act.queueId);
    }

    // encodes a TTL-based action
    private static void encodeTtlAction(ActionTtl act, OfPacketWriter pkt) {
        pkt.writeU8(act.ttl);
        pkt.writeZeros(ActionFactory.PAD_TTL);
    }

    // encodes an EtherType-based action
    private static void encodeEthTypeAction(ActionEther act,
                                            OfPacketWriter pkt) {
        pkt.write(act.ethType);
        pkt.writeZeros(ActionFactory.PAD_ETH_TYPE);
    }

    // encodes a U32-based action
    private static void encodeU32Action(ActionU32 act, OfPacketWriter pkt) {
        pkt.writeU32(act.value);
    }

    // encodes a set-field action
    private static void encodeSetFieldAction(ActSetField act,
                                             OfPacketWriter pkt) {
        if (act.getVersion().lt(V_1_2)) {
            encodeLegacySetFieldAction(act, pkt);
        } else {
            int wi = pkt.wi();
            FieldFactory.encodeField(act.field, pkt);
            int flen = pkt.wi() - wi;
            // assumption is that header length is correct
            // TODO: Review - is this a practical assumption? or should we
            //   calculate the 0-7 fill here (not when the field is attached
            //   to the ActSetField instance) ?
            int zeroFill = act.header.length - ACT_HEADER_LEN - flen;
            pkt.writeZeros(zeroFill);
        }
    }

    /** Where we need to map the SET_FIELD value to a legacy set* action.
     *
     * @param act the action to encode
     * @param pkt the data buffer into which we are writing
     */
    private static void encodeLegacySetFieldAction(ActSetField act,
                                                   OfPacketWriter pkt) {
        switch (act.header.fieldType) {
            case VLAN_VID:
                MfbVlanVid mvv = (MfbVlanVid) act.field;
                pkt.write(mvv.getVlanId());
                pkt.writeZeros(ActionParser.PAD_SET_VLAN_VID_10);
                break;

            case VLAN_PCP:
            case IP_DSCP:
                MFieldBasicInt mfInt = (MFieldBasicInt) act.field;
                pkt.writeU8(mfInt.getValue());
                pkt.writeZeros(ActionParser.PAD_SET_U8_VAL_10);
                break;

            case ETH_SRC:
            case ETH_DST:
                MFieldBasicMac mfMac = (MFieldBasicMac) act.field;
                pkt.write(mfMac.getMacAddress());
                pkt.writeZeros(ActionParser.PAD_SET_DL_ADDR_10);
                break;

            case IPV4_SRC:
            case IPV4_DST:
                MFieldBasicIp mfIp = (MFieldBasicIp) act.field;
                pkt.write(mfIp.getIpAddress());
                break;

            /*
            * IMPLEMENTATION NOTE:
            *  TCP, UDP, SCTP all map down to TP_*, since the
            *  fields are overloaded.
            */
            case TCP_SRC:
            case TCP_DST:
            case UDP_SRC:
            case UDP_DST:
            case SCTP_SRC:
            case SCTP_DST:
                MFieldBasicPort mfPort = (MFieldBasicPort) act.field;
                pkt.write(mfPort.getPort());
                pkt.writeZeros(ActionParser.PAD_SET_TP_PORT_10);
                break;

            default:
                throw new IllegalStateException("encodeLegacySetFieldAction(): " +
                        "Can't handle: " + act.header.fieldType);
        }
    }

    // encodes an experimenter action
    private static void encodeExperAction(ActExperimenter act,
                                          OfPacketWriter pkt) {
        pkt.writeInt(act.id);
        // assumption is that data is a valid length (multiple of 8)
        pkt.writeBytes(act.data);
    }
}