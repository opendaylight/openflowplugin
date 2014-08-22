/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.InvalidMutableException;
import org.opendaylight.of.lib.Mutable;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.VersionMismatchException;
import org.opendaylight.of.lib.dt.DataPathId;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.util.PrimitiveUtils.verifyU32;
import static org.opendaylight.util.PrimitiveUtils.verifyU8;

/**
 * Mutable subclass of {@link OfmFeaturesReply}.
 *
 * @author Simon Hunt
 */
public class OfmMutableFeaturesReply extends OfmFeaturesReply
        implements MutableMessage {

    private final Mutable mutt = new Mutable();

    /** Constructs a mutable OpenFlow FEATURES REPLY message.
     *
     * @param header the message header
     */
    OfmMutableFeaturesReply(Header header) {
        super(header);
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
        OfmFeaturesReply msg = new OfmFeaturesReply(header);
        msg.dpid = this.dpid;
        msg.numBuffers = this.numBuffers;
        msg.numTables = this.numTables;
        msg.auxId = this.auxId;
        msg.capabilities = this.capabilities;
        msg.suppActions = this.suppActions;
        msg.ports = this.ports;
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

    /** Sets the datapath id; Since 1.0.
     *
     * @param dpid the datapath id
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if dpid is null
     */
    public OfmMutableFeaturesReply dpid(DataPathId dpid) {
        mutt.checkWritable(this);
        notNull(dpid);
        this.dpid = dpid;
        return this;
    }

    /** Sets the number of buffers; Since 1.0.
     * That is, the maximum number of packets
     * the switch can buffer when sending packets to the controller using
     * <em>packet-in</em> messages.
     *
     * @param numBuffers the number of buffers
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalArgumentException if argument is not u32
     */
    public OfmMutableFeaturesReply numBuffers(long numBuffers) {
        mutt.checkWritable(this);
        verifyU32(numBuffers);
        this.numBuffers = numBuffers;
        return this;
    }

    /** Sets the number of tables supported by the switch; Since 1.0.
     *
     * @param numTables the number of tables
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalArgumentException if argument is not u8
     */
    public OfmMutableFeaturesReply numTables(int numTables) {
        mutt.checkWritable(this);
        verifyU8(numTables);
        this.numTables = numTables;
        return this;
    }

    /** Sets the auxiliary id; Since 1.3.
     * That is, the connection type from the
     * switch to the controller. Zero indicates the main connection;
     * non-zero values indicate auxiliary connections.
     *
     * @param auxId the auxiliary id
     * @return self, for chaining
     * @throws VersionMismatchException if version &lt; 1.3
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalArgumentException if argument is not u8
     */
    public OfmMutableFeaturesReply auxId(int auxId) {
        verMin13(header.version);
        mutt.checkWritable(this);
        verifyU8(auxId);
        this.auxId = auxId;
        return this;
    }

    /** Sets the set of capabilities (may be null).
     *
     * @param caps the capabilities
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public OfmMutableFeaturesReply capabilities(Set<Capability> caps) {
        mutt.checkWritable(this);
        if (caps == null) {
            this.capabilities = null;
        } else {
            this.capabilities = new TreeSet<Capability>();
            this.capabilities.addAll(caps);
        }
        return this;
    }

    /** Sets the set of supported actions (may be null); Since 1.0;
     * Removed at 1.1.
     *
     * @param suppActs the supported actions
     * @return self, for chaining
     * @throws VersionMismatchException if version &gt; 1.0
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public OfmMutableFeaturesReply
    supportedActions(Set<SupportedAction> suppActs) {
        if (header.version != V_1_0)
            throw new VersionMismatchException(E_DEPRECATED + V_1_1);
        mutt.checkWritable(this);
        if (suppActs == null) {
            this.suppActions = null;
        } else {
            this.suppActions = new TreeSet<SupportedAction>();
            this.suppActions.addAll(suppActs);
        }
        return this;
    }

    /** Adds a port to the list of ports; Since 1.0; Removed at 1.3.
     *
     * @param port the port to add
     * @return self, for chaining
     * @throws VersionMismatchException if version &gt; 1.2
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public OfmMutableFeaturesReply addPort(Port port) {
        ProtocolVersion pv = getVersion();
        if (pv.gt(V_1_2))
            throw new VersionMismatchException(E_DEPRECATED + V_1_3);
        mutt.checkWritable(this);
        notNull(port);
        notMutable(port);
        notContains(ports, port);
        if (ports == null)
            ports = new ArrayList<Port>();
        ports.add(port);
        header.length += PortFactory.getPortLength(pv);
        return this;
    }
}
