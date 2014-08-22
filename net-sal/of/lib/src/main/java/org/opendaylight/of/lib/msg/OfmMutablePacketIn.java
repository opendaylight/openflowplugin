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
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.match.OxmBasicFieldType;
import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.net.BigPortNumber;

import java.util.ResourceBundle;

import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_1;

/**
 * Mutable subclass of {@link OfmPacketIn}.
 *
 * @author Simon Hunt
 */
public class OfmMutablePacketIn extends OfmPacketIn implements MutableMessage {

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable OpenFlow PACKET_IN message.
     * Buffer-ID field starts as {@link BufferId#NO_BUFFER}.
     *
     * @param header the message header
     */
    OfmMutablePacketIn(Header header) {
        super(header);
        bufferId = BufferId.NO_BUFFER;
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
        OfmPacketIn msg = new OfmPacketIn(header);
        msg.bufferId = this.bufferId;
        msg.inPort = this.inPort;
        msg.inPhyPort = this.inPhyPort;
        msg.totalLen = this.totalLen;
        msg.reason = this.reason;
        msg.tableId = this.tableId;
        msg.cookie = this.cookie;
        msg.match = this.match;
        msg.data = this.data == null ? null : this.data.clone();
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

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            OfmMutablePacketIn.class, "ofmMutablePacketIn");

    private static final String E_MATCH_FIELD_IN_PORT = RES
            .getString("e_match_field_in_port");

//    private static final String E_MATCH_FIELD_IN_PHY_PORT =
//            "Set an IN_PHY_PORT match field instead";


    /** Sets the buffer id; Since 1.0.
     *
     * @param bufferId the buffer id
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if bufferId is null
     */
    public OfmMutablePacketIn bufferId(BufferId bufferId) {
        mutt.checkWritable(this);
        notNull(bufferId);
        this.bufferId = bufferId;
        return this;
    }

    /** Sets the ingress port; Since 1.0.
     * <p>
     * Note that in 1.0, port numbers are u16.
     *
     * @param inPort the ingress port
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is &gt; 1.1
     * @throws NullPointerException if inPort is null
     * @throws IllegalArgumentException if the port number is invalid
     */
    public OfmMutablePacketIn inPort(BigPortNumber inPort) {
        mutt.checkWritable(this);
        if (header.version.gt(V_1_1))
            throw new VersionMismatchException(E_MATCH_FIELD_IN_PORT);
        notNull(inPort);
        Port.validatePortValue(inPort, header.version);
        this.inPort = inPort;
        return this;
    }

    // +++++ CURRENTLY, 1.1 is not supported +++++
    /** Sets the ingress physical port; Since 1.1.
     *
     * @param inPhyPort the ingress physical port
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is not 1.1
     * @throws NullPointerException if inPhyPort is null
     */
/*
    public OfmMutablePacketIn inPhyPort(BigPortNumber inPhyPort) {
        mutt.checkWritable(this);
        if (header.version != V_1_1)
            throw new VersionMismatchException(E_MATCH_FIELD_IN_PHY_PORT);
        notNull(inPort);
        this.inPhyPort = inPhyPort;
        return this;
    }
*/

    /** Sets the frame total length value; Since 1.0.
     *
     * @param totalLen the total length value
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    // TODO: Review - consider dropping method: set total len from data[].length
    public OfmMutablePacketIn totalLen(int totalLen) {
        mutt.checkWritable(this);
        this.totalLen = totalLen;
        return this;
    }

    /** Sets the reason for the packet-in message; Since 1.0.
     *
     * @param reason the reason
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if reason is null
     */
    public OfmMutablePacketIn reason(PacketInReason reason) {
        mutt.checkWritable(this);
        notNull(reason);
        this.reason = reason;
        return this;
    }

    /** Sets the table ID; Since 1.1.
     * <p>
     * This should be set to the ID of the table that was looked up.
     *
     * @param tableId the table id
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is &lt; 1.1
     * @throws NullPointerException if reason is null
     */
    public OfmMutablePacketIn tableId(TableId tableId) {
        mutt.checkWritable(this);
        verMin11(header.version);
        notNull(tableId);
        this.tableId = tableId;
        return this;
    }

    /** Sets the cookie value; Since 1.3.
     * <p>
     * This should be set to the cookie of the flow entry that was looked up.
     *
     * @param cookie the cookie
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is &lt; 1.3
     */
    public OfmMutablePacketIn cookie(long cookie) {
        mutt.checkWritable(this);
        verMin13(header.version);
        this.cookie = cookie;
        return this;
    }

    /** Sets the match (frame meta data); Since 1.2.
     * <p>
     * The match field reflects the packet's headers and context when the event
     * that triggers the packet-in message occurred and contains a set
     * of OXM TLVs. This context includes any changes applied to the packet
     * in previous processing, including actions already executed, if any,
     * but not any changes in the action set.
     * <p>
     * The OXM TLVs must include context fields, that is, fields whose values
     * cannot be determined from the packet data. The standard context fields
     * are {@link OxmBasicFieldType#IN_PORT IN_PORT},
     * {@link OxmBasicFieldType#IN_PHY_PORT IN_PHY_PORT},
     * {@link OxmBasicFieldType#METADATA METADATA} and
     * {@link OxmBasicFieldType#TUNNEL_ID TUNNEL_ID}. Fields whose values are
     * all-bits-zero should be omitted.
     * <p>
     * Optionally, the OXM TLVs may also include packet header fields that
     * were previously extracted from the packet, including any modifications
     * of those in the course of processing.
     * <p>
     * When a packet is received directly on a physical port and not
     * processed by a logical port, {@link OxmBasicFieldType#IN_PORT IN_PORT}
     * and {@link OxmBasicFieldType#IN_PHY_PORT IN_PHY_PORT} have the same
     * value - the OpenFlow port number of this physical port - in this case
     * {@link OxmBasicFieldType#IN_PHY_PORT IN_PHY_PORT} should be omitted.
     *
     * @param match the match
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is &lt; 1.2
     * @throws NullPointerException if match is null
     * @throws IllegalArgumentException if match is mutable
     */
    public OfmMutablePacketIn match(Match match) {
        mutt.checkWritable(this);
        verMin12(header.version);
        notNull(match);
        notMutable(match);
        this.match = match;
        this.header.length += match.getTotalLength();
        return this;
    }

    /** Sets the packet frame data; Since 1.0.
     *
     * @param data the frame data
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if data is null
     */
    public OfmMutablePacketIn data(byte[] data) {
        mutt.checkWritable(this);
        // Note: can't use notNull(...) because of ambiguous var-args call
        if (data == null)
            throw new NullPointerException(E_NULL_PARAMS);
        this.data = data.clone();
        this.header.length += data.length;
        return this;
    }
}