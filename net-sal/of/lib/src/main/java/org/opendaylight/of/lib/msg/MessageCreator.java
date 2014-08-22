/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.ExperimenterId;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.err.ErrorType;
import org.opendaylight.of.lib.mp.MpBodyFactory;
import org.opendaylight.of.lib.mp.MultipartType;

import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;

/**
 * Creates empty OpenFlow messages, ready to be filled in by the consumer.
 *
 * @author Simon Hunt
 * @author Radhika Hegde
 */
class MessageCreator {

    /** Creates the appropriately initialized mutable message.
     *
     * @param pv the protocol version
     * @param type the message type
     * @param subtype a subtype appropriate for the message type
     * @param xid the transaction ID
     * @return the mutable message
     */
    static MutableMessage create(ProtocolVersion pv, MessageType type,
                                 Enum<?> subtype, long xid) {
        MessageSubtypeLookup.validate(pv, type, subtype);
        OpenflowMessage.Header hdr = new OpenflowMessage.Header(pv, type, xid);
        MutableMessage msg = null;
        switch (type) {
            case HELLO:
                msg = new OfmMutableHello(hdr);
                break;

            case ERROR:
                msg = createError(hdr, (ErrorType) subtype);
                break;

            case ECHO_REQUEST:
                msg = new OfmMutableEchoRequest(hdr);
                break;

            case ECHO_REPLY:
                msg = new OfmMutableEchoReply(hdr);
                break;

            case EXPERIMENTER:
                msg = createExperimenter(hdr, (ExperimenterId) subtype);
                break;

            case FEATURES_REQUEST:
                msg = new OfmMutableFeaturesRequest(hdr);
                break;

            case FEATURES_REPLY:
                msg = new OfmMutableFeaturesReply(hdr);
                hdr.length = MessageFactory.LIB_FEATURES_REPLY;
                break;

            case GET_CONFIG_REQUEST:
                msg = new OfmMutableGetConfigRequest(hdr);
                break;

            case GET_CONFIG_REPLY:
                msg = new OfmMutableGetConfigReply(hdr);
                hdr.length = MessageFactory.LIB_SWITCH_CONFIG;
                break;

            case SET_CONFIG:
                msg = new OfmMutableSetConfig(hdr);
                hdr.length = MessageFactory.LIB_SWITCH_CONFIG;
                break;

            case PACKET_IN:
                msg = createPacketIn(hdr, (PacketInReason) subtype);
                break;

            case FLOW_REMOVED:
                msg = createFlowRemoved(hdr, (FlowRemovedReason) subtype);
                break;

            case PORT_STATUS:
                msg = createPortStatus(hdr, (PortReason) subtype);
                break;

            case PACKET_OUT:
                msg = new OfmMutablePacketOut(hdr);
                hdr.length = pv == V_1_0 ? MessageFactory.LIB_PACKET_OUT_10
                                         : MessageFactory.LIB_PACKET_OUT;
                break;

            case FLOW_MOD:
                msg = createFlowMod(hdr, (FlowModCommand) subtype);
                break;

            case GROUP_MOD:
                verMin11(pv);
                msg = createGroupMod(hdr, (GroupModCommand) subtype);
                break;

            case PORT_MOD:
                msg = new OfmMutablePortMod(hdr);
                hdr.length = pv == V_1_0 ? MessageFactory.LIB_PORT_MOD_10
                                         : MessageFactory.LIB_PORT_MOD;
                break;

            case TABLE_MOD:
                msg = new OfmMutableTableMod(hdr);
                hdr.length = MessageFactory.LIB_TABLE_MOD;
                break;

            case MULTIPART_REQUEST:
                msg = createMpRequest(hdr, (MultipartType) subtype);
                break;

            case MULTIPART_REPLY:
                msg = createMpReply(hdr, (MultipartType) subtype);
                break;

            case BARRIER_REQUEST:
                msg = new OfmMutableBarrierRequest(hdr);
                break;

            case BARRIER_REPLY:
                msg = new OfmMutableBarrierReply(hdr);
                break;

            case QUEUE_GET_CONFIG_REQUEST:
                msg = new OfmMutableQueueGetConfigRequest(hdr);
                hdr.length = pv == V_1_0 ? MessageFactory.LIB_Q_GET_CFG_10
                                         : MessageFactory.LIB_Q_GET_CFG;
                break;

            case QUEUE_GET_CONFIG_REPLY:
                msg = new OfmMutableQueueGetConfigReply(hdr);
                hdr.length = MessageFactory.LIB_Q_GET_CFG;
                break;

            case ROLE_REQUEST:
                msg = createRoleRequest(hdr, (ControllerRole) subtype);
                break;

            case ROLE_REPLY:
                msg = createRoleReply(hdr, (ControllerRole) subtype);
                break;

            case GET_ASYNC_REQUEST:
                verMin13(pv);
                msg = new OfmMutableGetAsyncRequest(hdr);
                break;

            case GET_ASYNC_REPLY:
                verMin13(pv);
                msg = new OfmMutableGetAsyncReply(hdr);
                hdr.length = MessageFactory.LIB_ASYNC_CONFIG;
                break;

            case SET_ASYNC:
                verMin13(pv);
                msg = new OfmMutableSetAsync(hdr);
                hdr.length = MessageFactory.LIB_ASYNC_CONFIG;
                break;

            case METER_MOD:
                verMin13(pv);
                msg = createMeterMod(hdr, (MeterModCommand) subtype);
                break;
        }
        return msg;
    }

    // =====================================================================

    // creates the appropriate ERROR message (etype might be null)
    private static MutableMessage createError(OpenflowMessage.Header hdr,
                                              ErrorType etype) {
        MutableMessage mm;
        if (etype == ErrorType.EXPERIMENTER) {
            verMin12(hdr.version, etype.name());
            mm = new OfmMutableErrorExper(hdr);
            hdr.length = MessageFactory.LIB_ERROR_EXPER;
        } else {
            mm = new OfmMutableError(hdr);
            hdr.length = MessageFactory.LIB_ERROR;
            if (etype != null)
                ((OfmMutableError) mm).errorType(etype);
        }
        return mm;
    }

    // creates an EXPERIMENTER message, and sets experimenter ID if not null
    private static MutableMessage createExperimenter(OpenflowMessage.Header hdr,
                                                     ExperimenterId eid) {
        OfmMutableExperimenter msg = new OfmMutableExperimenter(hdr);
        hdr.length = hdr.version == V_1_0 ? MessageFactory.LIB_VENDOR
                                          : MessageFactory.LIB_EXPERIMENTER;
        if (eid != null)
            msg.expId(eid);
        return msg;
    }

    // creates a PACKET_IN message, and sets the reason if not null
    private static MutableMessage createPacketIn(OpenflowMessage.Header hdr,
                                                 PacketInReason reason) {
        OfmMutablePacketIn msg = new OfmMutablePacketIn(hdr);
        hdr.length = hdr.version == V_1_0 ? MessageFactory.LIB_PACKET_IN_10
                                          : MessageFactory.LIB_PACKET_IN;
        if (reason != null)
            msg.reason(reason);
        return msg;
    }

    // creates a FLOW_REMOVED message, and sets the reason if not null
    private static MutableMessage createFlowRemoved(OpenflowMessage.Header hdr,
                                                    FlowRemovedReason reason) {
        OfmMutableFlowRemoved msg = new OfmMutableFlowRemoved(hdr);
        hdr.length = hdr.version == V_1_0 ? MessageFactory.LIB_FLOW_REMOVED_10
                                          : MessageFactory.LIB_FLOW_REMOVED;
        if (reason != null)
            msg.reason(reason);
        return msg;
    }

    // creates a PORT_STATUS message, and sets the reason if not null
    private static MutableMessage createPortStatus(OpenflowMessage.Header hdr,
                                                   PortReason reason) {
        OfmMutablePortStatus msg = new OfmMutablePortStatus(hdr);
        hdr.length = hdr.version == V_1_0 ? MessageFactory.LIB_PORT_STATUS_10
                                          : MessageFactory.LIB_PORT_STATUS;
        if (reason != null)
            msg.reason(reason);
        return msg;
    }

    // creates a FLOW_MOD message, and sets the command if not null
    private static MutableMessage createFlowMod(OpenflowMessage.Header hdr,
                                                FlowModCommand cmd) {
        OfmMutableFlowMod msg = new OfmMutableFlowMod(hdr);
        hdr.length = hdr.version == V_1_0 ? MessageFactory.LIB_FLOW_MOD_10
                                          : MessageFactory.LIB_FLOW_MOD;
        if (cmd != null)
            msg.command(cmd);
        return msg;
    }

    // creates a GROUP_MOD message, and sets the command if not null
    private static MutableMessage createGroupMod(OpenflowMessage.Header hdr,
                                                 GroupModCommand cmd) {
        OfmMutableGroupMod msg = new OfmMutableGroupMod(hdr);
        hdr.length = MessageFactory.LIB_GROUP_MOD;
        if (cmd != null)
            msg.command(cmd);
        return msg;
    }

    // creates a MULTIPART_REQUEST message, and sets the type
    // (and possibly body) if type is not null
    private static MutableMessage createMpRequest(OpenflowMessage.Header hdr,
                                                  MultipartType type) {
        OfmMutableMultipartRequest msg = new OfmMutableMultipartRequest(hdr);
        hdr.length = getMpHeaderLength(hdr.version);
        MpBodyFactory.setRequestType(msg, type);
        return msg;
    }

    // creates a MULTIPART_REPLY message, and sets the type and body
    // if type is not null
    private static MutableMessage createMpReply(OpenflowMessage.Header hdr,
                                                MultipartType type) {
        OfmMutableMultipartReply msg = new OfmMutableMultipartReply(hdr);
        hdr.length = getMpHeaderLength(hdr.version);
        MpBodyFactory.setReplyType(msg, type);
        return msg;
    }

    // creates a ROLE_REQUEST message, and sets the role if not null
    private static MutableMessage createRoleRequest(OpenflowMessage.Header hdr,
                                                    ControllerRole role) {
        OfmMutableRoleRequest msg = new OfmMutableRoleRequest(hdr);
        hdr.length = MessageFactory.LIB_ROLE;
        if (role != null)
            msg.role(role);
        return msg;
    }

    // creates a ROLE_REPLY message, and sets the role if not null
    private static MutableMessage createRoleReply(OpenflowMessage.Header hdr,
                                                  ControllerRole role) {
        OfmMutableRoleReply msg = new OfmMutableRoleReply(hdr);
        hdr.length = MessageFactory.LIB_ROLE;
        if (role != null)
            msg.role(role);
        return msg;
    }

    // creates a METER_MOD message, and sets the command if not null
    private static MutableMessage createMeterMod(OpenflowMessage.Header hdr,
                                                 MeterModCommand cmd) {
        OfmMutableMeterMod msg = new OfmMutableMeterMod(hdr);
        hdr.length = MessageFactory.LIB_METER_MOD;
        if (cmd != null)
            msg.command(cmd);
        return msg;
    }

    // === UTILITY METHODS =================================================

    /** Returns the fixed header length of a Multipart Request or Reply,
     * for the given version.
     *
     * @param pv the protocol version
     * @return the fixed header length in bytes
     */
    static int getMpHeaderLength(ProtocolVersion pv) {
        return pv == V_1_0 ? MessageFactory.LIB_MP_HEADER_10
                           : MessageFactory.LIB_MP_HEADER;
    }
}

