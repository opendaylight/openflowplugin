/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.dt.GroupId;
import org.opendaylight.of.lib.msg.Bucket;
import org.opendaylight.of.lib.msg.BucketFactory;
import org.opendaylight.of.lib.msg.GroupType;
import org.opendaylight.util.StringUtils;

import java.util.Collections;
import java.util.List;

import static org.opendaylight.of.lib.CommonUtils.*;


/**
 * Represents a group description; part of a reply to a group-description
 * request multipart message; since 1.1.
 *
 * @author Prashant Nayak
 */
public class MBodyGroupDescStats extends OpenflowStructure
        implements MultipartBody {
    int length;
    GroupType type;
    GroupId groupId;
    List<Bucket> buckets;

    /**
     * Constructs a multipart body GROUP_DESC type.
     *
     * @param pv the protocol version
     */
    public MBodyGroupDescStats(ProtocolVersion pv) {
        super(pv);
    }

    @Override
    public int getTotalLength() {
        return length;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{gdesc:gpId=").append(groupId).append(",gtype=")
                .append(type).append(",#bkts=").append(cSize(buckets))
                .append("}");
        return sb.toString();
    }

    @Override
    public String toDebugString() {
        return toDebugString(0);
    }
    
    

    @Override
    public void validate() throws IncompleteStructureException {
        notNullIncompleteStruct(groupId, type);
    }

    /** Returns a multi-line representation of this group desc stats object.
     *
     * @param indent the additional indent (number of spaces)
     * @return a multi-line representation
     */
    public String toDebugString(int indent) {
        final String in = EOLI + StringUtils.spaces(indent);
        StringBuilder sb = new StringBuilder();
        sb.append(in).append("Group ID   : ").append(groupId)
          .append(in).append("Group Type : ").append(type)
          .append(in).append("Buckets    : ")
          .append(BucketFactory.toDebugString(1, buckets));
        return sb.toString();
    }

    /** Returns the group type; Since 1.1.
     *
     * @return the group type
     */
    public GroupType getType() {
        return type;
    }

    /** Returns the group ID; Since 1.1.
     *
     * @return the group ID
     */
    public GroupId getGroupId() {
        return groupId;
    }

    /** Returns the list of buckets for this group; Since 1.1.
     *
     * @return the list of bucket
     */
    public List<Bucket> getBuckets() {
        return buckets == null ? null : Collections.unmodifiableList(buckets);
    }

    //=======================================================================
    // Exo-Skeletal Arrays

    private static final String LINE_PREFIX = EOLI + "-- GROUP DESC STATS ";
    private static final String LINE = " ----------------";

    /** Represents an array of group description elements. */
    public static class Array extends MBodyList<MBodyGroupDescStats> {
        Array(ProtocolVersion pv) {
            super(pv);
        }

        @Override
        public Class<MBodyGroupDescStats> getElementClass() {
            return MBodyGroupDescStats.class;
        }

        @Override
        public String toString() {
            return "{GroupDesc: count=" + cSize(list) + "}";
        }

        @Override
        public String toDebugString() {
            StringBuilder sb = new StringBuilder();
            int index = 0;
            for (MBodyGroupDescStats gs: list)
                sb.append(LINE_PREFIX).append(index++).append(LINE)
                  .append(gs.toDebugString());
            return sb.toString();
        }
    }

    /** A mutable array of group description elements. */
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
        public String toString() {
            return mutt.tagString(super.toString());
        }

        @Override
        public boolean writable() {
            return mutt.writable();
        }

        @Override
        public OpenflowStructure toImmutable() {
            // Can only do this once
            mutt.invalidate(this);
            // Transfer the payload to an immutable instance
            MBodyGroupDescStats.Array array = new Array(version);
            // copy elements across
            array.addAll(this.list);
            return array;
        }

        // ===================================================================
        // ==== ADDERS

        /** Adds a group description object to this mutable array.
         *
         * @param groupDesc the group description to add
         * 
         * @return self, for chaining
         * @throws InvalidMutableException if this array is no longer writable
         * @throws NullPointerException if groupDesc is null
         * @throws IllegalArgumentException if groupDesc is mutable
         * @throws IncompleteStructureException if the group description is
         *          incomplete
         */
        public MutableArray addGroupDesc(MBodyGroupDescStats groupDesc)
                throws IncompleteStructureException {
            mutt.checkWritable(this);
            notNull(groupDesc);
            notMutable((OpenflowStructure) groupDesc);
            groupDesc.validate();
            list.add(groupDesc);
            return this;
        }
    }
}
