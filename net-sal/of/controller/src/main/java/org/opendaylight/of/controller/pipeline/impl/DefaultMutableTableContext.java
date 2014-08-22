/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pipeline.impl;

import org.opendaylight.of.controller.pipeline.MutableTableContext;
import org.opendaylight.of.controller.pipeline.TableContext;
import org.opendaylight.of.lib.Mutable;
import org.opendaylight.of.lib.OfpCodeBasedEnum;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.match.OxmFieldType;
import org.opendaylight.of.lib.mp.MBodyDesc;
import org.opendaylight.of.lib.msg.TableFeaturePropType;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/** Mutable variant of {@link DefaultTableContext}. Provides modification
 * capabilities to the context.
 *
 * @author Pramod Shanbhag
 */
public class DefaultMutableTableContext extends DefaultTableContext
        implements MutableTableContext {

    private final Mutable mutt = new Mutable();

    @Override
    public TableContext toImmutable() {
        // Can only do this once
        mutt.invalidate(this);
        // Transfer the payload to an immutable instance
        DefaultTableContext tc = new DefaultTableContext();
        tc.tableId = this.tableId;
        tc.maxEntries = this.maxEntries;
        tc.dpDesc = this.dpDesc;
        tc.nextTables = new TreeSet<TableId>(this.nextTables);
        tc.nextTablesMiss = new TreeSet<TableId>(this.nextTablesMiss);

        tc.caps = new HashMap<TableFeaturePropType,
                              Set<? extends OfpCodeBasedEnum>>(this.caps);
        tc.mfcaps = new HashMap<TableFeaturePropType,
                                Set<? extends OxmFieldType>>(this.mfcaps);
        tc.match = new HashMap<OxmFieldType, Boolean>(this.match);

        return tc;
    }

    @Override
    public boolean writable() {
        return mutt.writable();
    }

    @Override
    public MutableTableContext tableId(TableId id) {
        tableId = id;
        return this;
    }

    /**
     * TODO: Remove this when device driver framework is in place.
     * Hopefully we will not need to store the desc in table context then.
     * 
     * Store the reference to device description in table context so that
     * it can be accessed later in the PipelineMediator.
     * @param desc Device description
     * @return current table context.
     */
    public MutableTableContext dpDesc(MBodyDesc desc) {
        dpDesc = desc;
        return this;
    }

    @Override
    public MutableTableContext maxEntries(long max) {
        maxEntries = max;
        return this;
    }

    @Override
    public MutableTableContext addNextTable(TableId id) {
        nextTables.add(id);
        return this;
    }

    @Override
    public MutableTableContext addNextTableMiss(TableId id) {
        nextTablesMiss.add(id);
        return this;
    }

    @Override
    public MutableTableContext addCapability(TableFeaturePropType prop,
                                             Set<? extends OfpCodeBasedEnum> capCodes) {
        caps.put(prop, capCodes);
        return this;
    }

    @Override
    public MutableTableContext addMatchFieldCapability(TableFeaturePropType prop,
                                       Set<? extends OxmFieldType> mfCapCodes) {
        mfcaps.put(prop, mfCapCodes);
        return this;
    }

    @Override
    public MutableTableContext addMatchField(OxmFieldType ft,
                                             boolean hasMask) {
        match.put(ft, hasMask);
        return this;
    }

    // ========================================================================
    // Patch code to allow for 1.3 optional _MISS table feature prop types.
    // Pipeline Manager may need to patch the optional values in the definition
    // based on the reported regular prop type.  Returning internal refs of
    // regular prop types as a temporary adjustment until module can be
    // reconsidered.

    /**
     * Get the set of capabilities for the given property type.
     *
     * @param prop the property type
     * @return the set of capabilities for the given property
     */
    Set<? extends OfpCodeBasedEnum> getCapabilities(TableFeaturePropType prop) {
        Set<? extends OfpCodeBasedEnum> propCaps = caps.get(prop);
        return propCaps == null ? null : propCaps;
    }

    /**
     * Get the set of match field capabilities for the given property type.
     *
     * @param prop the property type
     * @return the set of match field capabilities
     */
    Set<? extends OxmFieldType> getMatchFieldCapabilities(TableFeaturePropType prop) {
        Set<? extends OxmFieldType> propMatchCaps = mfcaps.get(prop);
        return propMatchCaps == null ? null : propMatchCaps;
    }

    /**
     * Get the set of next  table ids for this table context.
     *
     * @return the set of next table ids
     */
    Set<TableId> getNextTableIdSet() {
        return nextTables;
    }
}
