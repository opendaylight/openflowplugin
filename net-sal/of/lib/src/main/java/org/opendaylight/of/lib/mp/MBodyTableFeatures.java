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
import org.opendaylight.of.lib.msg.TableConfig;
import org.opendaylight.of.lib.msg.TableFeatureFactory;
import org.opendaylight.of.lib.msg.TableFeatureProp;
import org.opendaylight.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.opendaylight.of.lib.CommonUtils.*;

/**
 * Represents a table features element; part of a reply to a
 * table-features request multipart message; Since 1.3.
 *
 * @author Simon Hunt
 */
public class MBodyTableFeatures extends OpenflowStructure
        implements MultipartBody {

    /** The 64-bit value where all bits are set. */
    public static final long ALL_META_BITS = 0xffffffffffffffffL;

    static final int FIXED_LEN = 64;
    private static final Set<TableConfig> EMPTY_CONFIG = Collections.emptySet();

    int length;
    TableId tableId;
    String name;
    long metadataMatch;
    long metadataWrite;

    // IMPLEMENTATION NOTE:
    //   no config flags are defined in 1.3, so all instances are going to
    //   simply share a common empty set. In some future version of the
    //   protocol, we will declare:
    // Set<TableConfig> config;

    long maxEntries;
    List<TableFeatureProp> props;

    /**
     * Constructs an OpenFlow structure.
     *
     * @param pv the protocol version
     */
    public MBodyTableFeatures(ProtocolVersion pv) {
        super(pv);
    }

    @Override
    public String toString() {
        return "{tfeats:tid=" + tableId + ",name=" + StringUtils.quoted(name) +
                ",maxEnt=" + maxEntries + ",...}";
    }

    @Override
    public String toDebugString() {
        return toDebugString(0);
    }

    /** Returns a multi-line representation of this Table Features object.
     *
     * @param indent the additional indent (number of spaces)
     * @return a multi-line representation
     */
    public String toDebugString(int indent) {
        final String in = EOLI + StringUtils.spaces(indent);
        StringBuilder sb = new StringBuilder();
        sb.append(in).append("Table ID : ").append(tableId)
                .append(in).append("Name : ").append(StringUtils.quoted(name))
                .append(in).append("Metadata Match : ").append(hex(metadataMatch))
                .append(in).append("Metadata Write : ").append(hex(metadataWrite))
                .append(in).append("Table Config : ").append(getConfig())
                .append(in).append("# Max Entries : ").append(maxEntries)
                .append(in).append("Table Feature Properties : ")
                    .append(TableFeatureFactory.toDebugString(indent, props));
        return sb.toString();
    }


    @Override
    public int getTotalLength() {
        return length;
    }

    @Override
    public void validate() throws IncompleteStructureException {
        notNullIncompleteStruct(tableId);
    }

    /** Returns the table ID; Since 1.3.
     *
     * @return the table ID
     */
    public TableId getTableId() {
        return tableId;
    }

    /** Returns the table name; Since 1.3.
     * <p>
     * Note that the table name is stored in a 32 character field.
     *
     * @return the table name
     */
    public String getName() {
        return name;
    }

    /** Returns the metadata match value; Since 1.3.
     * <p>
     * The value returned indicates the bits of the metadata field that the
     * table can match on, when using the
     * {@link org.opendaylight.of.lib.match.OxmBasicFieldType#METADATA metadata}
     * match field. A value of {@link #ALL_META_BITS} indicates that the
     * table can match the full metadata field.
     *
     * @return the metadata match value
     */
    public long getMetadataMatch() {
        return metadataMatch;
    }

    /** Returns the metadata write value; Since 1.3.
     * <p>
     * The value returned indicates the bits of the metadata field that the
     * table can write using the
     * {@link org.opendaylight.of.lib.instr.InstructionType#WRITE_METADATA write-metadata}
     * instruction. A value of {@link #ALL_META_BITS} indicates that the
     * table can write the full metadata field.
     *
     * @return the metadata write value
     */
    public long getMetadataWrite() {
        return metadataWrite;
    }

    /** Returns the set of config flags representing the configuration set on
     * the table; Since 1.3.
     * <p>
     * Note that, as of version 1.3, there are no flags defined so this
     * method will always return an empty set.
     *
     * @return the config flags set on the table
     */
    public Set<TableConfig> getConfig() {
        return EMPTY_CONFIG;
    }

    /** Returns the maximum number of flow entries that can be inserted into
     * this table; Since 1.3.
     * <p>
     * Due to limitations imposed by modern hardware, this value should be
     * considered advisory and a best effort approximation of the capacity
     * of the table. Despite the high-level abstraction of a table, in practice
     * the resource consumed by a single flow table entry is not constant.
     * <p>
     * For example, a flow table entry might consume more than one entry,
     * depending on its match parameters (e.g. IPv4 vs. IPv6). Also, tables
     * that appear distinct at an <em>OpenFlow-level</em> might in fact share
     * the same underlying physical resources. Further, on <em>OpenFlow</em>
     * hybrid switches, those tables may be shared with <em>non-OpenFlow</em>
     * functions. The result is that switch implementations should report an
     * approximation of the total flow entries supported and controller writers
     * should not treat this value as a fixed, physical constant.
     *
     * @return the maximum number of flow entries
     */
    public long getMaxEntries() {
        return maxEntries;
    }

    /** Returns the list of table feature properties describing various
     * capabilities of the table; Since 1.3.
     *
     * @return the list of table feature properties
     */
    public List<TableFeatureProp> getProps() {
        return Collections.unmodifiableList(props);
    }

    //=======================================================================
    // Exo-Skeletal Arrays

    private static final String LINE_PREFIX = EOLI + "-- TABLE FEATURE ";
    private static final String LINE = " ----------------";

    /** Represents an array of table features elements. */
    public static class Array extends MBodyList<MBodyTableFeatures> {
        Array(ProtocolVersion pv) {
            super(pv);
        }

        @Override
        public Class<MBodyTableFeatures> getElementClass() {
            return MBodyTableFeatures.class;
        }

        @Override
        public String toString() {
            return "{TableFeats: count=" + cSize(list) + "}";
        }

        @Override
        public String toDebugString() {
            StringBuilder sb = new StringBuilder();
            int index = 0;
            for (MBodyTableFeatures tf: list)
                sb.append(LINE_PREFIX).append(index++).append(LINE)
                        .append(tf.toDebugString());
            return sb.toString();
        }
    }

    /** A mutable array of table features elements. */
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
            MBodyTableFeatures.Array array = new Array(version);
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

        /** Adds a table features object to this mutable array.
         *
         * @param features the features object to add
         * @return self, for chaining
         * @throws InvalidMutableException if this array is no longer writable
         * @throws NullPointerException if feats is null
         * @throws IncompleteStructureException if the table features
         *          is incomplete
         */
        public MutableArray addTableFeatures(MBodyTableFeatures features)
                throws IncompleteStructureException {
            mutt.checkWritable(this);
            notNull(features);
            notMutable((OpenflowStructure) features);
            features.validate();
            list.add(features);
            return this;
        }
    }
}
