/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.util.StringUtils;

import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.util.PrimitiveUtils.verifyU32;

/**
 * Mutable subclass of {@link MBodyTableStats}.
 *
 * @author Simon Hunt
 */
public class MBodyMutableTableStats extends MBodyTableStats
        implements MutableStructure {

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable multipart body TABLE stats element.
     * <p>
     * A valid {@link TableId} must be present
     * for this element to be valid.
     *
     * @param pv the protocol version
     */
    public MBodyMutableTableStats(ProtocolVersion pv) {
        super(pv);
        if (pv.lt(V_1_3)) {
            // FIXME: initialize wildcards representation
            name = StringUtils.EMPTY;
        }
    }

    @Override
    public OpenflowStructure toImmutable() {
        // Can only do this once
        mutt.invalidate(this);
        // Transfer the payload to an immutable instance
        MBodyTableStats ts = new MBodyTableStats(version);
        ts.tableId = this.tableId;
        ts.name = this.name;
        ts.wildcards = this.wildcards;
        ts.maxEntries = this.maxEntries;
        ts.activeCount = this.activeCount;
        ts.lookupCount = this.lookupCount;
        ts.matchedCount = this.matchedCount;
        return ts;
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

    /** Sets the ID of the table; Since 1.0.
     *
     * @param tableId the table ID
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if tableId is null
     */
    public MBodyMutableTableStats tableId(TableId tableId) {
        mutt.checkWritable(this);
        notNull(tableId);
        this.tableId = tableId;
        return this;
    }

    /** Sets the name of the table; Since 1.0; Removed at 1.3.
     * <p>
     * Note that the maximum allowed length of the string is
     * {@link MpBodyFactory#TABLE_NAME_LEN} - 1.
     *
     * @param name the table name
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is 1.3 or higher
     * @throws NullPointerException if name is null
     * @throws IllegalArgumentException if string length is &gt;31
     */
    public MBodyMutableTableStats name(String name) {
        mutt.checkWritable(this);
        notDeprecated(version, V_1_3, "name");
        notNull(name);
        stringField(name, MpBodyFactory.TABLE_NAME_LEN);
        this.name = name;
        return this;
    }

    // FIXME: need setter (adders?) for supported wildcarding

    /** Sets the maximum number of entries supported by this table.
     *
     * @param max the maximum number of entries
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is 1.3 or higher
     * @throws IllegalArgumentException if max is not u32
     */
    public MBodyMutableTableStats maxEntries(long max) {
        mutt.checkWritable(this);
        notDeprecated(version, V_1_3, "maxEntries");
        verifyU32(max);
        this.maxEntries = max;
        return this;
    }

    /** Sets the number of active entries.
     *
     * @param active the number of active entries
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalArgumentException if active is not u32
     */
    public MBodyMutableTableStats activeCount(long active) {
        mutt.checkWritable(this);
        verifyU32(active);
        this.activeCount = active;
        return this;
    }

    /** Sets the number of packets looked up in the table.
     *
     * @param lookup the number packets looked up
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutableTableStats lookupCount(long lookup) {
        mutt.checkWritable(this);
        this.lookupCount = lookup;
        return this;
    }

    /** Sets the number of packets that hit the table.
     *
     * @param matched the number of table hits
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutableTableStats matchedCount(long matched) {
        mutt.checkWritable(this);
        this.matchedCount = matched;
        return this;
    }
}
