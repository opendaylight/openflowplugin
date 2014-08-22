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

import java.util.Set;

import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;

/**
 * Represents a table stats element; part of a reply to a
 * table-stats request multipart message; Since 1.0.
 *
 * @author Simon Hunt
 */
public class MBodyTableStats extends OpenflowStructure
        implements MultipartBody {

    private static final int LIB_TABLE_10 = 64;
    private static final int LIB_TABLE_13 = 24;

    TableId tableId;
    String name;
    // FIXME: figure out how to represent the wildcard bits (for 1.0)
    Set<Object> wildcards;
    long maxEntries;
    long activeCount;
    long lookupCount;
    long matchedCount;

    /**
     * Constructs an OpenFlow structure.
     *
     * @param pv the protocol version
     */
    public MBodyTableStats(ProtocolVersion pv) {
        super(pv);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{tstats:tid=").append(tableId);
        if (version.lt(V_1_3))
            sb.append(",name=").append(StringUtils.quoted(name))
                    .append(",maxEnt=").append(maxEntries);
        sb.append(",#activeEnt=").append(activeCount)
                .append(",#lookup=").append(lookupCount)
                .append(",#matched=").append(matchedCount)
                .append("}");
        return sb.toString();
    }

    @Override
    public String toDebugString() {
        return toDebugString(0);
    }

    /** Returns a multi-line representation of this Table Stats object.
     *
     * @param indent the additional indent (number of spaces)
     * @return a multi-line representation
     */
    public String toDebugString(int indent) {
        final String in = EOLI + StringUtils.spaces(indent);
        StringBuilder sb = new StringBuilder();
        sb.append(in).append("Table ID : ").append(tableId);
        if (version.lt(V_1_3))
            sb.append(in).append("Name : ").append(StringUtils.quoted(name))
                // TODO: replace "[[TBD]]" with wildcarding info
            .append(in).append("Wildcarding allowed for : ").append("[[TBD]]")
            .append(in).append("# Max Entries : ").append(maxEntries);
        sb.append(in).append("# Active Entries : ").append(activeCount)
            .append(in).append("# Packets looked up : ").append(lookupCount)
            .append(in).append("# Packets matched : ").append(matchedCount)
            .append(EOLI);
        return sb.toString();
    }

        @Override
    public int getTotalLength() {
        return version == V_1_0 ? LIB_TABLE_10 : LIB_TABLE_13;
    }

    @Override
    public void validate() throws IncompleteStructureException {
        notNullIncompleteStruct(tableId);
    }

    /** Returns the table ID; Since 1.0.
     *
     * @return the table ID
     */
    public TableId getTableId() {
        return tableId;
    }

    /** Returns the table name; Since 1.0; Removed at 1.3.
     * <p>
     * Note that the table name is stored in a 32 character field.
     * <p>
     * As of 1.3, the table name is provided in the <em>TableFeatures</em>
     * multipart-reply, and not here. This method will return null for
     * version 1.3.
     *
     * @see MBodyTableFeatures
     *
     * @return the table name
     */
    public String getName() {
        return name;
    }

    // FIXME : need getter for "supported wildcarding information"

    /** Returns the maximum number of entries supported in this table;
     * Since 1.0; Removed at 1.3.
     * This value is u32.
     * <p>
     * As of 1.3, the maximum number of entries supported is provided in the
     * <em>TableFeatures</em> multipart-reply, and not here. This method will
     * return 0 for version 1.3.
     *
     * @see MBodyTableFeatures
     *
     * @return the maximum number of supported entries
     */
    public long getMaxEntries() {
        return maxEntries;
    }

    /** Returns the number of active entries; Since 1.0.
     * This value is u32.
     *
     * @return the number of active entries
     */
    public long getActiveCount() {
        return activeCount;
    }

    /** Returns the number of packets looked up in the table; Since 1.0.
     * This value is u64.
     *
     * @return the number of packet lookups
     */
    public long getLookupCount() {
        return lookupCount;
    }

    /** Returns the number of packets that hit the table; Since 1.0.
     * This value is u64.
     *
     * @return the number of table hits
     */
    public long getMatchedCount() {
        return matchedCount;
    }

    //=======================================================================
    // Exo-Skeletal Arrays

    private static final String LINE_PREFIX = EOLI + "-- TABLE ";
    private static final String LINE = " ----------------";


    /** Represents an array of table stats elements. */
    public static class Array extends MBodyList<MBodyTableStats> {
        Array(ProtocolVersion pv) {
            super(pv);
        }

        @Override
        public Class<MBodyTableStats> getElementClass() {
            return MBodyTableStats.class;
        }

        @Override
        public String toString() {
            return "{TableStats: count=" + cSize(list) + "}";
        }

        @Override
        public String toDebugString() {
            StringBuilder sb = new StringBuilder();
            int index = 0;
            for (MBodyTableStats ts: list)
                sb.append(LINE_PREFIX).append(index++).append(LINE)
                        .append(ts.toDebugString());
            return sb.toString();
        }
    }

    /** A mutable array of table stats elements. */
    public static class MutableArray extends Array implements MutableStructure {

        private final Mutable mutt = new Mutable();

        /** Constructor, initializing the internal list.
         *
         * @param pv the protocol version
         */
        MutableArray(ProtocolVersion pv) {
            super(pv);
        }

        @Override
        public OpenflowStructure toImmutable() {
            // Can do this only once
            mutt.invalidate(this);
            // Transfer the payload to an immutable instance
            MBodyTableStats.Array array = new Array(version);
            // copy elements across
            array.addAll(this.list);
            return array;
        }

        @Override
        public boolean writable() {
            return mutt.writable();
        }

        @Override
        public String toString() {
            return mutt.tagString(super.toString());
        }

        // =================================================================
        // ==== ADDERS

        /** Adds a table stats object to this mutable array.
         *
         * @param stats the stats object to add
         * @return self, for chaining
         * @throws InvalidMutableException if this array is no longer writable
         * @throws NullPointerException if stats is null
         * @throws IncompleteStructureException if the table stats is incomplete
         */
        public MutableArray addTableStats(MBodyTableStats stats)
                throws IncompleteStructureException {
            mutt.checkWritable(this);
            notNull(stats);
            notMutable((OpenflowStructure) stats);
            stats.validate();
            list.add(stats);
            return this;
        }
    }
}
