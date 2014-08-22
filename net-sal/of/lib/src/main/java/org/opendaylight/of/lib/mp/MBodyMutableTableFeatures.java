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
import org.opendaylight.of.lib.msg.TableFeatureProp;

import java.util.ArrayList;

import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.of.lib.CommonUtils.stringField;
import static org.opendaylight.util.PrimitiveUtils.verifyU32;

/**
 * Mutable subclass of {@link MBodyTableFeatures}.
 *
 * @author Simon Hunt
 */
public class MBodyMutableTableFeatures extends MBodyTableFeatures
        implements MutableStructure {

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable multipart body TABLE FEATURES element.
     * <p>
     * The metadata match and write fields default to {@link #ALL_META_BITS}.
     * <p>
     * A valid {@link TableId} must be present for
     * this element to be valid.
     *
     * @param pv the protocol version
     */
    public MBodyMutableTableFeatures(ProtocolVersion pv) {
        super(pv);
        metadataMatch = ALL_META_BITS;
        metadataWrite = ALL_META_BITS;
        props = new ArrayList<TableFeatureProp>();
        length = FIXED_LEN;
    }

    @Override
    public OpenflowStructure toImmutable() {
        // Can only do this once
        mutt.invalidate(this);
        // Transfer the payload to an immutable instance
        MBodyTableFeatures tf = new MBodyTableFeatures(version);
        tf.length = length;
        tf.tableId = tableId;
        tf.name = name;
        tf.metadataMatch = metadataMatch;
        tf.metadataWrite = metadataWrite;
        // IMPLEMENTATION NOTE: config not defined - see superclass for details
        tf.maxEntries = maxEntries;
        tf.props = props;
        return tf;
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

    /** Sets the ID of the table; Since 1.3.
     *
     * @param tableId the table ID
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if tableId is null
     */
    public MBodyMutableTableFeatures tableId(TableId tableId) {
        mutt.checkWritable(this);
        notNull(tableId);
        this.tableId = tableId;
        return this;
    }

    /** Sets the name of the table; Since 1.3.
     * <p>
     * Note that the maximum allowed length of the string is
     * {@link MpBodyFactory#TABLE_NAME_LEN} - 1.
     *
     * @param name the table name
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if name is null
     * @throws IllegalArgumentException if string length is &gt;31
     */
    public MBodyMutableTableFeatures name(String name) {
        mutt.checkWritable(this);
        notNull(name);
        stringField(name, MpBodyFactory.TABLE_NAME_LEN);
        this.name = name;
        return this;
    }

    /** Sets the metadata match value; Since 1.3.
     * <p>
     * The value set indicates the bits of the metadata field that the table
     * can match on, when using the
     * {@link org.opendaylight.of.lib.match.OxmBasicFieldType#METADATA metadata}
     * match field. A value of {@link #ALL_META_BITS} indicates that the
     * table can match the full metadata field.
     *
     * @param meta the metadata match value
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutableTableFeatures metadataMatch(long meta) {
        mutt.checkWritable(this);
        this.metadataMatch = meta;
        return this;
    }

    /** Sets the metadata write value; Since 1.3.
     * <p>
     * The value set indicates the bits of the metadata field that the table
     * can write using the
     * {@link org.opendaylight.of.lib.instr.InstructionType#WRITE_METADATA write-metadata}
     * instruction. A value of {@link #ALL_META_BITS} indicates that the
     * table can write the full metadata field.
     *
     * @param meta the metadata write value
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutableTableFeatures metadataWrite(long meta) {
        mutt.checkWritable(this);
        this.metadataWrite = meta;
        return this;
    }

    //=== IMPLEMENTATION NOTE:
    // == As of 1.3, there are no configuration flags that can be set, so
    //    no direct support is given in the API for now.
    // public MBodyMutableTableFeatures config(Set<TableConfig> config) { ... }

    /** Sets the maximum number of flow entries that can be inserted into the
     * table; Since 1.3.
     *
     * @see org.opendaylight.of.lib.mp.MBodyTableFeatures#getMaxEntries
     *
     * @param max the maximum number of flow entries
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalArgumentException if max is not u32
     */
    public MBodyMutableTableFeatures maxEntries(long max) {
        mutt.checkWritable(this);
        verifyU32(max);
        this.maxEntries = max;
        return this;
    }

    /** Adds a table features property to this entry, describing a capability
     * of the table.
     *
     * @param prop the property to add
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if prop is null
     */
    public MBodyMutableTableFeatures addProp(TableFeatureProp prop) {
        mutt.checkWritable(this);
        notNull(prop);
        props.add(prop);
        int plen = prop.getTotalLength();
        length += plen + calcPadding(plen);
        return this;
    }

    /** Calculates how many zero-filled bytes of padding are required at the
     * end of the property structure, so that the end of the structure lands
     * on an 8-byte boundary.
     *
     * @param len unpadded length
     * @return the number of padding bytes required
     */
    private int calcPadding(int len) {
        // See section A.2.3.1 (p.39) of the 1.3 spec for details...
        return ((len + 7) / 8 * 8 - len);
    }
}
