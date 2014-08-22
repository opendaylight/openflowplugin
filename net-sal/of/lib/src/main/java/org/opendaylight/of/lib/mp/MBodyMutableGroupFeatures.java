/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.instr.ActionType;
import org.opendaylight.of.lib.msg.GroupCapability;
import org.opendaylight.of.lib.msg.GroupType;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Collections.emptySet;
import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.util.PrimitiveUtils.verifyU32;

/**
 * Mutable subclass of {@link MBodyGroupFeatures}.
 *
 * @author Scott Simes
 * @author Simon Hunt
 */
public class MBodyMutableGroupFeatures extends MBodyGroupFeatures
        implements MutableStructure {

    private static final Set<ActionType> EMPTY_ACTION_SET = emptySet();
    private static final int GROUP_TYPE_COUNT = GroupType.values().length;

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable multipart body <em>GroupFeatures</em> type.
     *
     * @param pv the protocol version
     */
    public MBodyMutableGroupFeatures(ProtocolVersion pv) {
        super(pv);
        types = new TreeSet<GroupType>();
        capabilities = new TreeSet<GroupCapability>();
        maxGroups = new HashMap<GroupType, Long>(GROUP_TYPE_COUNT);
        actions = new HashMap<GroupType, Set<ActionType>>(GROUP_TYPE_COUNT);
        for (GroupType type: GroupType.values()) {
            maxGroups.put(type, 0L);
            actions.put(type, EMPTY_ACTION_SET);
        }
    }

    @Override
    public OpenflowStructure toImmutable() {
        // Can only do this once
        mutt.invalidate(this);
        MBodyGroupFeatures gf = new MBodyGroupFeatures(version);
        gf.types = this.types;
        gf.capabilities = this.capabilities;
        gf.maxGroups = this.maxGroups;
        gf.actions = this.actions;
        return gf;
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
     * Sets the supported group types; since 1.2.
     *
     * @param types the set of supported group types
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if types is null
     */
    public MBodyMutableGroupFeatures groupTypes(Set<GroupType> types) {
        mutt.checkWritable(this);
        notNull(types);
        this.types.clear();
        this.types.addAll(types);
        return this;
    }

    /**
     * Sets the supported group capabilities; since 1.2
     *
     * @param caps the set of supported group capabilities
     * @return this, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if caps is null
     */
    public MBodyMutableGroupFeatures capabilities(Set<GroupCapability> caps) {
        mutt.checkWritable(this);
        notNull(caps);
        capabilities.clear();
        capabilities.addAll(caps);
        return this;
    }

    /**
     * Sets the maximum number of groups supported for the given group type;
     * since 1.2.
     * Note that the maximum must be an unsigned 32-bit value.
     *
     * @param type the group type
     * @param max the maximum number of groups supported
     * @return this, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if type is null
     * @throws IllegalArgumentException if max is not u32
     */
    public MBodyMutableGroupFeatures maxGroupsForType(GroupType type,
                                                      long max) {
        mutt.checkWritable(this);
        notNull(type);
        verifyU32(max);
        maxGroups.put(type, max);
        return this;
    }

    /**
     * Sets the actions supported for the given group type; since 1.2.
     *
     * @param type the group type
     * @param actions the actions supported
     * @return this, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if either parameter is null
     */
    public MBodyMutableGroupFeatures actionsForType(GroupType type,
                                                    Set<ActionType> actions) {
        mutt.checkWritable(this);
        notNull(type, actions);
        this.actions.put(type, new TreeSet<ActionType>(actions));
        return this;
    }
}
