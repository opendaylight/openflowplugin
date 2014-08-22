/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;


import org.opendaylight.of.lib.CommonUtils;
import org.opendaylight.of.lib.InvalidMutableException;
import org.opendaylight.of.lib.Mutable;
import org.opendaylight.of.lib.VersionMismatchException;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.match.Match;

import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.util.PrimitiveUtils.verifyU16;
import static org.opendaylight.util.PrimitiveUtils.verifyU32;

/**
 * Mutable subclass of {@link OfmFlowRemoved}.
 *
 * @author Sudheer Duggisetty
 */
public class OfmMutableFlowRemoved extends OfmFlowRemoved
        implements MutableMessage {

    private final Mutable mutt = new Mutable();

    /** Constructs a mutable OpenFlow FLOW REMOVED message.
    *
    * @param header the message header
    */
    OfmMutableFlowRemoved(Header header) {
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
        OfmFlowRemoved msg = new OfmFlowRemoved(header);
        msg.match = this.match;
        msg.cookie = this.cookie;
        msg.priority = this.priority;
        msg.reason = this.reason;
        msg.tableId = this.tableId;
        msg.durationSec = this.durationSec;
        msg.durationNsec = this.durationNsec;
        msg.idleTimeout = this.idleTimeout;
        msg.hardTimeout = this.hardTimeout;
        msg.packetCount = this.packetCount;
        msg.byteCount = this.byteCount;
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

    /** Sets the match; Since 1.0.
     *
     * @param match the match to set
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if match is null
     * @throws IllegalArgumentException if match is mutable
     */
    public OfmMutableFlowRemoved match(Match match) {
        mutt.checkWritable(this);
        notNull(match);
        notMutable(match);
        this.match = match;
        if (header.version.gt(V_1_0))
            this.header.length += match.getTotalLength();
        return this;
    }

    private static final long COOKIE_RESERVED = -1L;

    /** Sets the cookie value; Since 1.0.
     *
     * @param cookie the cookie value to set
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalArgumentException if the reserved value (-1) is specified
     */
    public OfmMutableFlowRemoved cookie(long cookie) {
        mutt.checkWritable(this);
        if (cookie == COOKIE_RESERVED)
            throw new IllegalArgumentException(CommonUtils.E_RESERVED + cookie);
        this.cookie = cookie;
        return this;
    }

    /** Sets the priority level; Since 1.0.
     * <p>
     * The priority indicates priority within the specified flow table.
     * Higher numbers indicate higher priorities.
     *
     * @param priority the priority level to set
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalArgumentException if the priority level is not u16
     */
    public OfmMutableFlowRemoved priority(int priority) {
        mutt.checkWritable(this);
        verifyU16(priority);
        this.priority = priority;
        return this;
    }

    /** Sets the reason a flow was removed; Since 1.0.
     *
     * @param reason the reason to set
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if flow removed reason is null
     */
    public OfmMutableFlowRemoved reason(FlowRemovedReason reason) {
        mutt.checkWritable(this);
        notNull(reason);
        this.reason = reason;
        return this;
    }

    /** Sets the table Id; Since 1.1.
     * <p>
     * The table id field specifies the table into which the flow entry is
     * removed. Table <em>0</em> signifies the first table in the pipeline.
     *
     * @param tableId the table ID to set
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is &lt; 1.1
     * @throws NullPointerException if tableId is null
     */
    public OfmMutableFlowRemoved tableId(TableId tableId) {
        mutt.checkWritable(this);
        verMin11(header.version);
        notNull(tableId);
        this.tableId = tableId;
        return this;
    }

    /** Sets the time in seconds, and fractional nanoseconds,
     * that the flow was installed; Since 1.0.
     *
     * @param seconds the number of seconds
     * @param nano the number of additional nanoseconds
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalArgumentException if either seconds or nano is not u32
     */
    public OfmMutableFlowRemoved duration(long seconds, long nano) {
        mutt.checkWritable(this);
        verifyU32(seconds);
        verifyU32(nano);
        this.durationSec = seconds;
        this.durationNsec = nano;
        return this;
    }

    /** Sets the idle timeout in seconds; Since 1.0.
     *
     * @param idleTimeout the idle timeout in seconds
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalArgumentException if idleTimeout is not u16
     */
    public OfmMutableFlowRemoved idleTimeout(int idleTimeout) {
        mutt.checkWritable(this);
        verifyU16(idleTimeout);
        this.idleTimeout = idleTimeout;
        return this;
    }

    /** Sets the hard timeout in seconds; Since 1.2.
     *
     * @param hardTimeout the hard timeout in seconds
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is &lt; 1.2
     * @throws IllegalArgumentException if hardTimeout is not u16
     */
    public OfmMutableFlowRemoved hardTimeout(int hardTimeout) {
        mutt.checkWritable(this);
        verMin12(header.version);
        verifyU16(hardTimeout);
        this.hardTimeout = hardTimeout;
        return this;
    }

    /** Sets the number of packets associated with this flow; Since 1.0.
     *
     * @param packetCount the number of associated packets
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public OfmMutableFlowRemoved packetCount(long packetCount) {
        mutt.checkWritable(this);
        this.packetCount = packetCount;
        return this;
    }

    /** Sets the number of bytes associated with this flow; Since 1.0;
     *
     * @param byteCount the number of associated bytes
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public OfmMutableFlowRemoved byteCount(long byteCount) {
        mutt.checkWritable(this);
        this.byteCount = byteCount;
        return this;
    }
}
