/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.controller.MessageEvent;
import org.opendaylight.of.controller.OpenflowEventType;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.StringUtils;

/**
 * A message event.
 *
 * @author Simon Hunt
 */
class MessageEvt extends OpenflowEvt implements MessageEvent {

    static final String PV_LABEL = ",pv=";
    static final String DPID_LABEL = ",dpid=";
    static final String AUX_LABEL = ",aux=";
    static final String MSG_LABEL = ",msg=";
    static final String LEN_LABEL = ",len=";
    static final String XID_LABEL = ",xid=";
    static final String SLASH = "/";


    private final OpenflowMessage msg;
    private final DataPathId dpid;
    private final int auxId;
    private final ProtocolVersion pv;

    /** Constructs the message event.
     *
     * @param type the event type
     * @param msg the openflow message
     * @param dpid the ID of the associated datapath
     * @param auxId the auxiliary connection ID
     * @param pv the protocol version negotiated with the datapath
     */
    MessageEvt(OpenflowEventType type, OpenflowMessage msg, DataPathId dpid,
               int auxId, ProtocolVersion pv) {
        super(type);
        this.msg = msg;
        this.dpid = dpid;
        this.auxId = auxId;
        this.pv = pv;
    }

    private String msgHeader(OpenflowMessage msg) {
        if (msg == null) {
            return "null";
        }
        MessageType type = msg.getType();
        StringBuilder sb = new StringBuilder("[").append(msg.getVersion());
        String subtype = "";
        switch (type) {
            case ERROR:
                OfmError err = (OfmError) msg;
                subtype = SLASH + err.getErrorType();
                break;
            case EXPERIMENTER:
                OfmExperimenter exp = (OfmExperimenter) msg;
                subtype = SLASH + exp.getExpId();
                break;
            case PACKET_IN:
                OfmPacketIn pi = (OfmPacketIn) msg;
                subtype = SLASH + pi.getReason();
                break;
            case FLOW_REMOVED:
                OfmFlowRemoved fr = (OfmFlowRemoved) msg;
                subtype = SLASH + fr.getReason();
                break;
            case PORT_STATUS:
                OfmPortStatus ps = (OfmPortStatus) msg;
                subtype = SLASH + ps.getReason();
                break;
            case FLOW_MOD:
                OfmFlowMod fm = (OfmFlowMod) msg;
                subtype = SLASH + fm.getCommand();
                break;
            case GROUP_MOD:
                OfmGroupMod gm = (OfmGroupMod) msg;
                subtype = SLASH + gm.getCommand();
                break;
            case MULTIPART_REQUEST:
                OfmMultipartRequest mrq = (OfmMultipartRequest) msg;
                subtype = SLASH + mrq.getMultipartType();
                break;
            case MULTIPART_REPLY:
                OfmMultipartReply mrp = (OfmMultipartReply) msg;
                subtype = SLASH + mrp.getMultipartType();
                break;
            case ROLE_REQUEST:
                OfmRoleRequest rrq = (OfmRoleRequest) msg;
                subtype = SLASH + rrq.getRole();
                break;
            case ROLE_REPLY:
                OfmRoleReply rrp = (OfmRoleReply) msg;
                subtype = SLASH + rrp.getRole();
                break;
            case METER_MOD:
                OfmMeterMod mm = (OfmMeterMod) msg;
                subtype = SLASH + mm.getCommand();
                break;
            default:
                break;
        }
        sb.append(",").append(type).append(subtype)
                .append(LEN_LABEL).append(msg.length())
                .append(XID_LABEL).append(msg.getXid())
                .append("]");
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int n = sb.length();
        // IMPLEMENTATION NOTE: *MUST* use the negotiated(), dpid() and auxId()
        //      method calls to allow HandshakeMessageEvt to override and
        //      provide values from the dpid-future.
        sb.replace(n-1, n, PV_LABEL).append(negotiated())
                .append(DPID_LABEL).append(dpid())
                .append(AUX_LABEL).append(auxId())
                .append(MSG_LABEL).append(msgHeader(msg)).append("}");
        return sb.toString();
    }

    @Override
    public OpenflowMessage msg() {
        return msg;
    }

    @Override
    public DataPathId dpid() {
        return dpid;
    }

    @Override
    public int auxId() {
        return auxId;
    }

    @Override
    public ProtocolVersion negotiated() {
        return pv;
    }

    @Override
    public String remoteId() {
        return dpid != null ? dpid.toString() : StringUtils.EMPTY;
    }
}
