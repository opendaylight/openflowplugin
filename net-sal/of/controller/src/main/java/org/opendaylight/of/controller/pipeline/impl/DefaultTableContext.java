/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pipeline.impl;

import org.opendaylight.of.controller.pipeline.TableContext;
import org.opendaylight.of.lib.OfpCodeBasedEnum;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.match.MatchField;
import org.opendaylight.of.lib.match.OxmFieldType;
import org.opendaylight.of.lib.mp.MBodyDesc;
import org.opendaylight.of.lib.msg.TableFeaturePropType;

import java.util.*;
import java.util.Map.Entry;

import static org.opendaylight.of.lib.CommonUtils.EOLI;
import static org.opendaylight.of.lib.CommonUtils.INDENT;
import static org.opendaylight.of.lib.msg.TableFeaturePropType.WILDCARDS;

/**
 * Default implementation for {@link TableContext} based on
 * <em>TableFeatures</em> OpenFlow message.
 * <p>
 * The
 * <em>openFlow</em> based capabilities
 * are as categorized as follows: <p>
 * <em>1. capabilities</em> : All the supported <em>Instructions</em> and
 * <em>Actions</em> capabilities. ex: INSTRUCTIONS, APPLY_ACTIONS.<br>
 * <em>2. match capabilities</em> : All the supported <em>Action</em>
 * capabilities having <em>MatchFields</em>. ex: WRITE_SETFIELD, APPLY_SETFIELD.
 * <br>
 * <em>3. match fields</em> : All the supported match fields along with mask.
 * ex : ETH_SRC, ETH_DST.
 * @author Pramod Shanbhag
 */
public class DefaultTableContext implements TableContext {

    protected TableId tableId;
    protected MBodyDesc dpDesc;
    long maxEntries;

    // table feature capabilities
    protected Map<TableFeaturePropType, Set<? extends OfpCodeBasedEnum>> caps =
            new HashMap<TableFeaturePropType, Set<? extends OfpCodeBasedEnum>>();
    // match field capabilities
    protected Map<TableFeaturePropType, Set<? extends OxmFieldType>> mfcaps =
            new HashMap<TableFeaturePropType, Set<? extends OxmFieldType>>();
    // match fields
    protected Map<OxmFieldType, Boolean> match =
            new HashMap<OxmFieldType, Boolean>();

    protected Set<TableId> nextTables = new TreeSet<TableId>();
    protected Set<TableId> nextTablesMiss = new TreeSet<TableId>();

    @Override
    public TableId tableId() {
        return tableId;
    }

    /**
     * Returns the device description for current table context 
     * @return device description
     */
    public MBodyDesc dpDesc() {
        return dpDesc;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("tableId = ").append(tableId)
          .append(", maxEntries :").append(maxEntries).append(EOLI);
        sb.append("capabilities :");
        for (Entry<TableFeaturePropType, Set<? extends OfpCodeBasedEnum>> cap:
                caps.entrySet())
            sb.append(EOLI).append(INDENT).append(cap.getKey().name())
              .append(": ").append(cap.getValue()).append(", ");

        sb.append(EOLI).append("match capabilities :");
        for (Entry<TableFeaturePropType, Set<? extends OxmFieldType>> cap:
                mfcaps.entrySet())
            sb.append(EOLI).append(INDENT).append(cap.getKey().name())
              .append(": ").append(cap.getValue()).append(", ");

        sb.append(EOLI).append("match fields:");
        for (Entry<OxmFieldType,Boolean> mfCap: match.entrySet())
            sb.append(EOLI).append(INDENT).append(mfCap.getKey()).append(":")
              .append(mfCap.getValue()).append(",");

        sb.append(EOLI).append("nextTables =").append(nextTables)
          .append(EOLI).append("nextTablesMiss =").append(nextTablesMiss);

        return sb.toString();
    }

    @Override
    public TableId getNextTableMiss() {
        TableId id = null;
        if (hasNextTablesMiss())
            id = nextTablesMiss.iterator().next();
        return id;
    }

    @Override
    public boolean hasNextTablesMiss() {
        return (nextTablesMiss.size() > 0);
    }

    @Override
    public long maxEntries() {
        return maxEntries;
    }

    @Override
    public boolean containsNextTable(TableId id) {
        return nextTables.contains(id);
    }

    @Override
    public boolean containsNextTableMiss(TableId id) {
        return nextTablesMiss.contains(id);
    }

    @Override
    public boolean supportsCapability(TableFeaturePropType prop,
                                 OfpCodeBasedEnum code) {
        Set<? extends OfpCodeBasedEnum> capCodes = caps.get(prop);
        if (capCodes != null)
            return capCodes.contains(code);

        return false;
    }

    @Override
    public boolean supportsMatchFieldCapability(TableFeaturePropType prop,
                                           OxmFieldType code) {
        Set<? extends OxmFieldType> capCodes = mfcaps.get(prop);
        if (capCodes != null)
            return capCodes.contains(code);

        return false;
    }

    @Override
    public boolean supportsMatchField(MatchField mf) {
        if (!match.containsKey(mf.getFieldType()))
            return false;

        if (mf.hasMask())
            return match.get(mf.getFieldType());

        return true;
    }

    @Override
    public boolean supportsWildCards(Set<OxmFieldType> fmmf) {
        // Any match field that is supported by the table
        // and is omitted in the flow mod match fields, such a match field
        // is wild carded. It should be present in supported wild card set.

        // match fields supported by the table
        Set<OxmFieldType> tmf = new HashSet<OxmFieldType>(match.keySet());

        // if any fields are omitted
        if (tmf.removeAll(fmmf)) {
            // such fields should be present as a supported wild card
            for (OxmFieldType ft: tmf)
                if (!supportsMatchFieldCapability(WILDCARDS, ft))
                    return false;
        }
        return true;
    }
}
