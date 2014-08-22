/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;


import java.util.Collections;
import java.util.Set;

import static org.opendaylight.of.lib.CommonUtils.EOLI;

/**
 * Base class for OfmGetAsyncReply and OfmSetAsync messages; since 1.3.
 *
 * @author Scott Simes
 */
abstract class AsyncConfig extends OpenflowMessage {

    /** Packet in messages of interest when controller has OFPCR_ROLE_EQUAL or
     * OFPCR_ROLE_MASTER.
     */
    Set<PacketInReason> pktInMask;

    /** Packet in messages of interest when controller has
     * OFPCR_ROLE_SLAVE.
     */
    Set<PacketInReason> pktInMaskSlave;

    /** Port status messages of interest when controller has OFPCR_ROLE_EQUAL
     * or OFPCR_ROLE_MASTER.
     */
    Set<PortReason> portStatusMask;

    /** Port status messages of interest when controller has
     * OFPCR_ROLE_SLAVE.
     */
    Set<PortReason> portStatusMaskSlave;

    /** Flow removed messages of interest when controller has OFPCR_ROLE_EQUAL
     * or OFPCR_ROLE_MASTER.
     */
    Set<FlowRemovedReason> flowRemovedMask;

    /** Flow removed messages of interest when controller has
     * OFPCR_ROLE_SLAVE.
     */
    Set<FlowRemovedReason> flowRemovedMaskSlave;

    /**
     * Base constructor that stores the header.
     *
     * @param header the message header
     */
    AsyncConfig(Header header) {
        super(header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",mPktIn=").append(pktInMask)
                .append(",sPktIn=").append(pktInMaskSlave)
                .append(",mPortRea=").append(portStatusMask)
                .append(",sPortRea=").append(portStatusMaskSlave)
                .append(",mFlowRem=").append(flowRemovedMask)
                .append(",sFlowRem=").append(flowRemovedMaskSlave).append("}");
        return sb.toString();
    }

    @Override
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, EOLI).append("{mPktIn=").append(pktInMask)
                .append(",sPktIn=").append(pktInMaskSlave).append("}")
                .append(EOLI).append("{mPortRea=").append(portStatusMask)
                .append(",sPortRea=").append(portStatusMaskSlave).append("}")
                .append(EOLI).append("{mFlowRem=").append(flowRemovedMask)
                .append(",sFlowRem=").append(flowRemovedMaskSlave).append("}")
                .append("}");
        return sb.toString();
    }

    /**
     * Return the set of reasons why asynchronous packet in messages are sent to
     * the controller when it functions in the master or equal role; since 1.3.
     *
     * @return the set of reasons for packet in messages
     */
    public Set<PacketInReason> getPacketInMask() {
        return Collections.unmodifiableSet(pktInMask);
    }

    /**
     * Return the set of reasons why asynchronous packet in messages are sent to
     * the controller when it functions in the slave role; since 1.3.
     *
     * @return the set of reasons for packet in messages
     */
    public Set<PacketInReason> getSlavePacketInMask() {
        return Collections.unmodifiableSet(pktInMaskSlave);
    }

    /**
     * Return the set of reasons why asynchronous port messages are sent to
     * the controller when it functions in the master or equal role; since 1.3.
     *
     * @return the set of reasons for port status messages
     */
    public Set<PortReason> getPortStatusMask() {
        return Collections.unmodifiableSet(portStatusMask);
    }

    /**
     * Return the set of reasons why asynchronous port messages are sent to
     * the controller when it functions in the slave role; since 1.3.
     *
     * @return the set of reasons for port status messages
     */
    public Set<PortReason> getSlavePortStatusMask() {
        return Collections.unmodifiableSet(portStatusMaskSlave);
    }

    /**
     * Return the set of reasons why asynchronous flow removed messages are
     * sent to the controller when it functions in the master or equals role;
     * since 1.3.
     *
     * @return the set of reasons for flow removed messages
     */
    public Set<FlowRemovedReason> getFlowRemovedMask() {
        return Collections.unmodifiableSet(flowRemovedMask);
    }

    /**
     * Return the set of reasons why asynchronous flow removed messages are
     * sent to the controller when it functions in the slave role; since 1.3.
     *
     * @return the set of reasons for flow removed messages
     */
    public Set<FlowRemovedReason> getSlaveFlowRemovedMask() {
        return Collections.unmodifiableSet(flowRemovedMaskSlave);
    }
}
