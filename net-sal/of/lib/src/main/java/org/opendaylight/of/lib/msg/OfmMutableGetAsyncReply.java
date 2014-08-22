/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.InvalidMutableException;
import org.opendaylight.of.lib.Mutable;

import java.util.Set;
import java.util.TreeSet;

import static org.opendaylight.of.lib.CommonUtils.notNull;

/**
 * Mutable subclass of {@link OfmGetAsyncReply}.
 *
 * @author Scott Simes
 */
public class OfmMutableGetAsyncReply extends OfmGetAsyncReply
        implements MutableMessage {

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable OpenFlow GET_ASYNC_REPLY message.
     *
     * @param header the message header
     */
    OfmMutableGetAsyncReply(Header header) {
        super(header);
        pktInMask = new TreeSet<PacketInReason>();
        pktInMaskSlave = new TreeSet<PacketInReason>();
        portStatusMask = new TreeSet<PortReason>();
        portStatusMaskSlave = new TreeSet<PortReason>();
        flowRemovedMask = new TreeSet<FlowRemovedReason>();
        flowRemovedMaskSlave = new TreeSet<FlowRemovedReason>();
    }

    @Override
    public void clearXid() {
        mutt.checkWritable(this);
        header.xid = 0;
    }

    @Override
    public OpenflowMessage toImmutable() {
        // Can only do this once
        mutt.invalidate(this);
        // Copy over to read-only instance
        OfmGetAsyncReply msg = new OfmGetAsyncReply(header);
        msg.pktInMask = this.pktInMask;
        msg.pktInMaskSlave = this.pktInMaskSlave;
        msg.portStatusMask = this.portStatusMask;
        msg.portStatusMaskSlave = this.portStatusMaskSlave;
        msg.flowRemovedMask = this.flowRemovedMask;
        msg.flowRemovedMaskSlave = this.flowRemovedMaskSlave;
        return msg;
    }

    @Override
    public boolean writable() {
        return mutt.writable();
    }

    @Override
    public String toString() {
        return mutt.tagString(super.toString());
    }

    // =====================================================================
    // ==== SETTERS

    /**
     * Sets the reasons why packet in messages may be sent to the controller
     * when it is in the master or equal role; since 1.3.
     *
     * @param flags the reasons for sending packet in messages
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if flags is null
     */
    public OfmMutableGetAsyncReply pktInMask(Set<PacketInReason> flags) {
        mutt.checkWritable(this);
        notNull(flags);
        this.pktInMask.clear();
        this.pktInMask.addAll(flags);
        return this;
    }

    /**
     * Sets the reasons why packet in messages may be sent to the controller
     * when it is in the slave role; since 1.3.
     *
     * @param flags the reasons for sending packet in messages
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if flags is null
     */
    public OfmMutableGetAsyncReply pktInMaskSlave(Set<PacketInReason> flags) {
        mutt.checkWritable(this);
        notNull(flags);
        this.pktInMaskSlave.clear();
        this.pktInMaskSlave.addAll(flags);
        return this;
    }

    /**
     * Sets the reasons why port status messages may be sent to the controller
     * when it is in the master or equal role; since 1.3.
     *
     * @param flags the reasons for sending port status messages
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if flags is null
     */
    public OfmMutableGetAsyncReply portStatusMask(Set<PortReason> flags) {
        mutt.checkWritable(this);
        notNull(flags);
        this.portStatusMask.clear();
        this.portStatusMask.addAll(flags);
        return this;
    }

    /**
     * Sets the reasons why port status messages may be sent to the controller
     * when it is in the slave role; since 1.3.
     *
     * @param flags the reasons for sending port status messages
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if flags is null
     */
    public OfmMutableGetAsyncReply portStatusMaskSlave(Set<PortReason> flags) {
        mutt.checkWritable(this);
        notNull(flags);
        this.portStatusMaskSlave.clear();
        this.portStatusMaskSlave.addAll(flags);
        return this;
    }

    /**
     * Sets the reasons why flow removed messages may be sent to the controller
     * when it is in the master or equal role; since 1.3.
     *
     * @param flags the reasons for sending flow removed messages
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if flags is null
     */
    public OfmMutableGetAsyncReply
    flowRemovedMask(Set<FlowRemovedReason> flags) {
        mutt.checkWritable(this);
        notNull(flags);
        this.flowRemovedMask.clear();
        this.flowRemovedMask.addAll(flags);
        return this;
    }

    /**
     * Sets the reasons why flow removed messages may be sent to the controller
     * when it is in the slave role; since 1.3.
     *
     * @param flags the reasons for sending flow removed messages
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if flags is null
     */
    public OfmMutableGetAsyncReply
    flowRemovedMaskSlave(Set<FlowRemovedReason> flags) {
        mutt.checkWritable(this);
        notNull(flags);
        this.flowRemovedMaskSlave.clear();
        this.flowRemovedMaskSlave.addAll(flags);
        return this;
    }
}
