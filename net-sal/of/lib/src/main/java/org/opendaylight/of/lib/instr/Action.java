/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.instr;

import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.match.OxmBasicFieldType;
import org.opendaylight.util.NotYetImplementedException;
import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.StringUtils;

import java.util.ResourceBundle;

import static org.opendaylight.of.lib.ProtocolVersion.V_1_1;
import static org.opendaylight.of.lib.instr.ActionType.POP_VLAN;
import static org.opendaylight.of.lib.instr.ActionType.SET_FIELD;

/**
 * Represents a Flow action. This abstract class serves as the base for
 * all actions.
 *
 * @author Simon Hunt
 */
public abstract class Action extends OpenflowStructure
        implements Comparable<Action> {

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            Action.class, "action");

    private static final String E_UNKNOWN_TYPE = RES
            .getString("e_unknown_type");

    /** The action header. */
    final Header header;

    /** Constructs an action.
     *
     * @param pv the protocol version
     * @param header the action header
     */
    Action(ProtocolVersion pv, Header header) {
        super(pv);
        this.header = header;
    }

    @Override
    public String toString() {
        return "{Act:" + header + "}";
    }

    /** Returns a string representation useful for debugging.
     * This default implementation delegates to {@link #toString()}, but
     * subclasses are free to override this behavior.
     *
     * @param indent the number of spaces with which to prefix each line
     * @return a (possibly multi-line) string representation of this action
     */
    public String toDebugString(int indent) {
        return StringUtils.spaces(indent) + toString();
    }

    /** Returns a string representation useful for debugging.
     * This default implementation delegates to {@link #toString()}, but
     * subclasses are free to override this behavior.
     *
     * @return a (possibly multi-line) string representation of this instruction
     */
    @Override
    public String toDebugString() {
        return toDebugString(0);
    }

    /** Returns the action type.
     *
     * @return the action type
     */
    public ActionType getActionType() {
        return header.type;
    }

    /* Implementation note:
    *   we don't expose the length field, since that is an
    *   implementation detail that the consumer should not care about.
    */

    /** Returns a short label to be used in {@link InstrAction#actionList}.
     * This default implementation returns the action type constant as a
     * string.
     *
     * @return a short label for this action
     */
    String getActionLabel() {
        return header.type.toString();
    }

    /** Comparable implemented to sort actions according to their
     * decoded action type.
     *
     * @param o the other action
     * @return a result conforming to the Comparable contract
     */
    @Override
    public int compareTo(Action o) {
        return (this.header.type == SET_FIELD && o.header.type == SET_FIELD)
                ? this.header.fieldType.compareTo(o.header.fieldType)
                : this.header.type.compareTo(o.header.type);
    }

    // ======================================================================

    /** Parses the header structure from the given data buffer.
     * Note that this method will advance the reader index of the buffer
     * by the length of an action header (4 bytes).
     *
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a parsed header
     * @throws HeaderParseException if there is an issue parsing the header
     * @throws DecodeException if the header cannot be decoded
     */
    static Header parseHeader(OfPacketReader pkt, ProtocolVersion pv)
            throws HeaderParseException, DecodeException {
        Header hdr = new Header();
        int typeCode = pkt.readU16();
        if (pv.le(V_1_1)) {
            OldActionType oat = OldActionType.decode(typeCode, pv);
            completeHeaderTypeForLegacy(hdr, oat);
        } else {
            hdr.type = ActionType.decode(typeCode, pv);
        }
        if (hdr.type == null)
            throw new HeaderParseException(pv + E_UNKNOWN_TYPE + typeCode);
        hdr.length = pkt.readU16();
        return hdr;
    }

    /** Fills out the header given the legacy action type.
     *
     * @param hdr the header
     * @param oat the legacy action type
     */
    private static void completeHeaderTypeForLegacy(Header hdr,
                                                    OldActionType oat) {
        switch (oat) {
             case OUTPUT:
                hdr.type = ActionType.OUTPUT;
                break;
            case SET_VLAN_VID:
                hdr.type = ActionType.SET_FIELD;
                hdr.fieldType = OxmBasicFieldType.VLAN_VID;
                break;
            case SET_VLAN_PCP:
                hdr.type = ActionType.SET_FIELD;
                hdr.fieldType = OxmBasicFieldType.VLAN_PCP;
                break;
            case STRIP_VLAN:
                hdr.type = POP_VLAN;
                break;
            case SET_DL_SRC:
                hdr.type = ActionType.SET_FIELD;
                hdr.fieldType = OxmBasicFieldType.ETH_SRC;
                break;
            case SET_DL_DST:
                hdr.type = ActionType.SET_FIELD;
                hdr.fieldType = OxmBasicFieldType.ETH_DST;
                break;
            case SET_NW_SRC:
                hdr.type = ActionType.SET_FIELD;
                hdr.fieldType = OxmBasicFieldType.IPV4_SRC;
                break;
            case SET_NW_DST:
                hdr.type = ActionType.SET_FIELD;
                hdr.fieldType = OxmBasicFieldType.IPV4_DST;
                break;
            case SET_NW_TOS:
                hdr.type = ActionType.SET_FIELD;
                // TODO: Review - Validate that NW_TOS maps to IP_DSCP
                hdr.fieldType = OxmBasicFieldType.IP_DSCP;
                break;
            case SET_NW_ECN:
                hdr.type = ActionType.SET_FIELD;
                hdr.fieldType = OxmBasicFieldType.IP_ECN;
                break;
            case SET_TP_SRC:
                hdr.type = ActionType.SET_FIELD;
                hdr.fieldType = OxmBasicFieldType.TCP_SRC;
                // TODO: Review - but it could be UDP_SRC or SCTP_SRC
                // In 1.0 they overloaded the type!!
                break;
            case SET_TP_DST:
                hdr.type = ActionType.SET_FIELD;
                hdr.fieldType = OxmBasicFieldType.TCP_DST;
                // TODO: Review - but it could be UDP_DST or SCTP_DST
                // In 1.0 they overloaded the type!!
                break;
            case COPY_TTL_OUT:
                hdr.type = ActionType.COPY_TTL_OUT;
                break;
            case COPY_TTL_IN:
                hdr.type = ActionType.COPY_TTL_IN;
                break;
            case SET_MPLS_LABEL:
                // TODO: Equivalent in 1.3?
                throw new NotYetImplementedException();
            case SET_MPLS_TC:
                // TODO: Equivalent in 1.3?
                throw new NotYetImplementedException();
            case SET_MPLS_TTL:
                hdr.type = ActionType.SET_MPLS_TTL;
                break;
            case DEC_MPLS_TTL:
                hdr.type = ActionType.DEC_MPLS_TTL;
                break;
            case PUSH_VLAN:
                hdr.type = ActionType.PUSH_VLAN;
                break;
            case POP_VLAN:
                hdr.type = ActionType.POP_VLAN;
                break;
            case PUSH_MPLS:
                hdr.type = ActionType.PUSH_MPLS;
                break;
            case POP_MPLS:
                hdr.type = ActionType.POP_MPLS;
                break;
            case SET_QUEUE:
                hdr.type = ActionType.SET_QUEUE;
                break;
            case GROUP:
                hdr.type = ActionType.GROUP;
                break;
            case SET_NW_TTL:
                hdr.type = ActionType.SET_NW_TTL;
                break;
            case DEC_NW_TTL:
                hdr.type = ActionType.DEC_NW_TTL;
                break;
            case EXPERIMENTER:
                hdr.type = ActionType.EXPERIMENTER;
                break;
        }
    }

    /** Returns the total length of this action, in bytes.
     *
     * @return the total length, in bytes
     */
    public int getTotalLength() {
        return header.length;
    }


    //======================================================================
    /** Represents the action header. */
    static class Header {
        /** Action type. */
        ActionType type;
        /** Action length, including this header (should be 64-bit aligned). */
        int length;
        /** Field type, needed when action type is SET_FIELD. */
        OxmBasicFieldType fieldType;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("[").append(type);
            if (type == SET_FIELD)
                sb.append("/").append(fieldType);
            sb.append(",len=").append(length).append("]");
            return sb.toString();
        }
    }
}