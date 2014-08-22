/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pipeline.impl;


import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.instr.ActionType;
import org.opendaylight.of.lib.instr.InstructionType;
import org.opendaylight.of.lib.match.OxmBasicFieldType;

import java.util.EnumSet;
import java.util.Set;

import static org.opendaylight.of.lib.msg.TableFeaturePropType.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Set of test cases for the {@link DefaultMutableTableContext}.
 *
 * @author Scott Simes
 */
public class DefaultMutableTableContextTest {

    @Test @Ignore
    public void muttability() {
        print(EOL + "muttability()");
        // If object can be made immutable, we should validate that
        DefaultMutableTableContext tc = new DefaultMutableTableContext();
        tc.toImmutable();
        try {
            Assert.assertFalse(tc.writable());
            tc.addNextTableMiss(TableId.ALL);
            Assert.fail(AM_NOEX);
        } catch (Exception e) {
            print("EX> " + e);
        }
    }

    @Test
    public void getCapabilities() {
        print(EOL + "getCapabilities()");
        DefaultMutableTableContext tc = new DefaultMutableTableContext();
        Assert.assertNull(AM_HUH,
                tc.getCapabilities(APPLY_ACTIONS));
        tc = populateTableContext();
        Assert.assertEquals(AM_NEQ, EnumSet.allOf(InstructionType.class),
                tc.getCapabilities(INSTRUCTIONS));
        Assert.assertEquals(AM_NEQ, EnumSet.allOf(ActionType.class),
                tc.getCapabilities(APPLY_ACTIONS));
    }

    @Test
    public void getMatchFieldCapabilities() {
        print(EOL + "getMatchFieldCapabilities()");
        DefaultMutableTableContext tc = new DefaultMutableTableContext();
        Assert.assertNull(AM_HUH,
                tc.getMatchFieldCapabilities(WRITE_SETFIELD));
        tc = populateTableContext();
        Assert.assertEquals(AM_NEQ, EnumSet.allOf(OxmBasicFieldType.class),
                tc.getMatchFieldCapabilities(APPLY_SETFIELD));
    }

    @Test
    public void getNextTableId() {
        print(EOL + "getNextTableId()");
        DefaultMutableTableContext tc = new DefaultMutableTableContext();
        Assert.assertTrue(AM_HUH, tc.getNextTableIdSet().isEmpty());
        tc = populateTableContext();
        Set<TableId> idSet = tc.getNextTableIdSet();
        Assert.assertEquals(AM_NEQ, 1, idSet.size());
        Assert.assertEquals(AM_NEQ, TableId.valueOf(2), idSet.iterator().next());
    }

    private DefaultMutableTableContext populateTableContext() {
        Set<InstructionType> instSet = EnumSet.allOf(InstructionType.class);
        Set<ActionType> actionSet = EnumSet.allOf(ActionType.class);
        Set<OxmBasicFieldType> matchFieldTypeSet = EnumSet.allOf(OxmBasicFieldType.class);
        return (DefaultMutableTableContext)
                new DefaultMutableTableContext().tableId(TableId.valueOf(1))
                .maxEntries(100).addNextTable(TableId.valueOf(2))
                .addCapability(INSTRUCTIONS, instSet)
                .addCapability(WRITE_ACTIONS, actionSet)
                .addCapability(APPLY_ACTIONS, actionSet)
                .addMatchFieldCapability(WRITE_SETFIELD, matchFieldTypeSet)
                .addMatchFieldCapability(APPLY_SETFIELD, matchFieldTypeSet)
                .addMatchFieldCapability(WILDCARDS, matchFieldTypeSet)
                .addMatchField(OxmBasicFieldType.ETH_DST, true);
    }
}
