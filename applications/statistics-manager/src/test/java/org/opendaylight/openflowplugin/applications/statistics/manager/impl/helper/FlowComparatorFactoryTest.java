/*
 * Copyright (c) 2013 IBM Corporation.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.statistics.manager.impl.helper;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.TunnelBuilder;

public final class FlowComparatorFactoryTest {

    private static Flow statsFlow;
    private static Flow storedFlow;
    private static Flow nullFlow;
    private static Flow copyStatsFlow;

    @BeforeClass
    public static void initialization() {
        statsFlow = prepareFlow("statsFlow", (short) 1, 1, 1);
        copyStatsFlow = prepareFlow("statsFlow", (short) 1, 1, 1);
        storedFlow = prepareFlow("storedFlow", (short) 2, 2, 2);
        nullFlow = new FlowBuilder().build();
    }

    private static Flow prepareFlow(String containerName, short tableId, int priority, int tunnelId) {
        final FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.setContainerName(containerName);
        flowBuilder.setTableId(tableId);
        flowBuilder.setPriority(priority);
        flowBuilder.setMatch(new MatchBuilder().setTunnel(
                new TunnelBuilder().setTunnelId(BigInteger.valueOf(tunnelId)).build()).build());
        return flowBuilder.build();
    }

    @Test
    public void containerNameComparationTest() {
        final SimpleComparator<Flow> simpleComparator = FlowComparatorFactory.createContainerName();
        compareViaComparator(simpleComparator);
        assertEquals(false, simpleComparator.areObjectsEqual(nullFlow, storedFlow));
    }


    @Test
    public void tableIdComparationTest() {
        final SimpleComparator<Flow> simpleComparator = FlowComparatorFactory.createTableId();
        compareViaComparator(simpleComparator);
        assertEquals(false, simpleComparator.areObjectsEqual(nullFlow, storedFlow));
    }

    @Test
    public void priorityComparationTest() {
        final SimpleComparator<Flow> simpleComparator = FlowComparatorFactory.createPriority();
        compareViaComparator(simpleComparator);
        assertEquals(false, simpleComparator.areObjectsEqual(storedFlow, nullFlow));
    }

    @Test
    public void matchComparationTest() {
        final SimpleComparator<Flow> simpleComparator = FlowComparatorFactory.createMatch();
        compareViaComparator(simpleComparator);
        assertEquals(false, simpleComparator.areObjectsEqual(nullFlow, storedFlow));
        assertEquals(true, simpleComparator.areObjectsEqual(statsFlow, copyStatsFlow));
        assertEquals(false,simpleComparator.areObjectsEqual(statsFlow,nullFlow));
    }

    private void compareViaComparator(SimpleComparator<Flow> simpleComparator) {
        assertEquals(true, simpleComparator.areObjectsEqual(nullFlow, nullFlow));
        assertEquals(false, simpleComparator.areObjectsEqual(statsFlow, storedFlow));
        assertEquals(true, simpleComparator.areObjectsEqual(statsFlow, statsFlow));
    }
}
