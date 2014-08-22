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
import org.opendaylight.of.lib.VersionMismatchException;
import org.opendaylight.of.lib.dt.BufferId;
import org.opendaylight.of.lib.instr.Action;
import org.opendaylight.of.lib.instr.ActionFactory;
import org.opendaylight.util.net.BigPortNumber;

import java.util.ArrayList;
import java.util.ResourceBundle;

import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.of.lib.CommonUtils.sameVersion;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.util.ResourceUtils.getBundledResource;

/**
 * Mutable subclass of {@link OfmPacketOut}.
 *
 * @author Simon Hunt
 */
public class OfmMutablePacketOut extends OfmPacketOut 
        implements MutableMessage {

    private static final ResourceBundle RES = 
          getBundledResource(OfmMutablePacketOut.class, "ofmMutablePacketOut");

    private static final String E_NOT_STANDARD_OR_CTRL = 
            RES.getString("e_not_standard_or_controller");
    private static final String E_DATA_SET_ALREADY = 
            RES.getString("e_data_set_already");

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable OpenFlow PACKET_OUT message.
     * Buffer-ID field starts as {@link BufferId#NO_BUFFER}.
     *
     * @param header the message header
     */
    OfmMutablePacketOut(Header header) {
        super(header);
        bufferId = BufferId.NO_BUFFER;
        actions = new ArrayList<Action>();
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
        OfmPacketOut msg = new OfmPacketOut(header);
        msg.bufferId = this.bufferId;
        msg.inPort = this.inPort;
        msg.actionsLen = this.actionsLen;
        msg.actions = this.actions;
        msg.data = this.data;
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
     * Sets the buffer id; Since 1.0.
     *
     * @param bufferId the buffer id
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if bufferId is null
     */
    public OfmMutablePacketOut bufferId(BufferId bufferId) {
        mutt.checkWritable(this);
        notNull(bufferId);
        this.bufferId = bufferId;
        return this;
    }

    /** 
     * Sets the ingress port that must be associated with the packet;
     * Since 1.0. This must be either a valid standard switch port
     * (from 1 to <em>{@link Port#MAX MAX}</em>) or 
     * <em>{@link Port#CONTROLLER CONTROLLER}</em>. 
     * (For 1.0, <em>{@link Port#NONE NONE}</em> can also be used.) 
     * <p>
     * Note that in 1.0, port numbers are u16.  
     *
     * @see Port
     * @param inPort the ingress port
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if inPort is null
     * @throws IllegalArgumentException if the port number is invalid
     */
    public OfmMutablePacketOut inPort(BigPortNumber inPort) {
        mutt.checkWritable(this);
        notNull(inPort);
        // port must be either a standard port number, or CONTROLLER
        // (though in 1.0 we'll also allow NONE)
        if (!Port.isStandardPort(inPort, header.version) &&
                !inPort.equals(Port.CONTROLLER) && 
                !(inPort.equals(Port.NONE) && header.version == V_1_0))
            throw new IllegalArgumentException(E_NOT_STANDARD_OR_CTRL + inPort);
        this.inPort = inPort;
        return this;
    }


    // FIXME: Added as a temporary bypass
    // FIXME: We need to look into why we're getting exception because the pkt-in port is 0xfffe (LOCAL-like, not LOCAL)


    /** Sets the ingress port that must be associated with the packet;
     * Since 1.0. This must be either a valid standard switch port
     * (from 1 to <em>MAX</em>) or <em>CONTROLLER</em>.
     * <p>
     * Note that in 1.0, port numbers are u16.
     *
     * @see Port
     * @param pktIn the ingress port
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if inPort is null
     * @throws IllegalArgumentException if the port number is invalid
     */
    @Deprecated
    public OfmMutablePacketOut inPort(OfmPacketIn pktIn) {
        mutt.checkWritable(this);
        notNull(pktIn);
        this.inPort = pktIn.getInPort();
        return this;
    }


    /** Adds an action to the action list; Since 1.0; Removed at 1.1.
     *
     * @param act the action to add
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if action version does not match
     * @throws NullPointerException if act is null
     */
    public OfmMutablePacketOut addAction(Action act) {
        mutt.checkWritable(this);
        notNull(act);
        sameVersion("PacketOut / Action", header.version, act.getVersion());
        ActionFactory.validateAction(header.version, act, "PacketOut");
        // if we are still going, the action validated okay
        actions.add(act);
        int actLen = act.getTotalLength();
        actionsLen += actLen;
        header.length += actLen;
        return this;
    }

    /** Clears any previously added actions.
     *
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public OfmMutablePacketOut clearActions() {
        mutt.checkWritable(this);
        actions.clear();
        header.length -= actionsLen;
        actionsLen = 0;
        return this;
    }

    /** Sets the packet frame data; Since 1.0.
     *
     * @param data the frame data
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if data is null
     */
    public OfmMutablePacketOut data(byte[] data) {
        mutt.checkWritable(this);
        if (this.data != null)
            throw new IllegalStateException(E_DATA_SET_ALREADY);

        if (data != null) {
            this.data = data.clone();
            this.header.length += data.length;
        }
        return this;
    }

    /** Privileged method to set the data without defensive copy.
     *
     * @param data the data to set
     * @return self, for chaining
     */
    OfmMutablePacketOut dataNoClone(byte[] data) {
        mutt.checkWritable(this);
        if (this.data != null)
            throw new IllegalStateException(E_DATA_SET_ALREADY);

        if (data != null) {
            this.data = data;
            this.header.length += data.length;
        }
        return this;
    }

    /**
     * Privileged method that returns a reference to the backing packet bytes.
     * Provided for efficiency to avoid data copy operation. Callers are
     * expected to refrain from modifying any bytes.
     *
     * @return reference to the packet data
     */
    byte[] getPacketBytes() {
        return data;
    }
}