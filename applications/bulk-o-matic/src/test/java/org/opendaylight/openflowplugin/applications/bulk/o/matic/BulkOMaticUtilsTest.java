/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.bulk.o.matic;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link BulkOMaticUtils}.
 */
public class BulkOMaticUtilsTest {

    private static final Logger LOG = LoggerFactory.getLogger(BulkOMaticUtilsTest.class);
    private static final String FLOW_ID = "1";

    @Test
    public void testIpIntToStr() throws Exception {
        Assert.assertEquals("255.255.255.255/32", BulkOMaticUtils.ipIntToStr(0xffffffff));
        Assert.assertEquals("255.255.255.255/32", BulkOMaticUtils.ipIntToStr(-1));
        Assert.assertEquals("0.0.0.0/32", BulkOMaticUtils.ipIntToStr(0));
        Assert.assertEquals("1.2.3.4/32", BulkOMaticUtils.ipIntToStr(0x01020304));
    }

    @Test
    public void testGetMatch() throws Exception {
        final Match match = BulkOMaticUtils.getMatch(0xffffffff);
        Assert.assertNotNull(match);
    }

    @Test
    public void testBuildFlow() throws Exception {
        final Match match = BulkOMaticUtils.getMatch(0xffffffff);
        final Flow flow = BulkOMaticUtils.buildFlow((short)1, FLOW_ID, match);
        Assert.assertEquals(FLOW_ID,flow.getId().getValue());
        Assert.assertEquals((short) 1 ,flow.getTableId().shortValue());
    }

    @Test
    public void testGetFlowInstanceIdentifier() throws Exception {
        Assert.assertNotNull(BulkOMaticUtils.getFlowInstanceIdentifier((short)1, "1", "1"));
    }

    @Test
    public void testGetFlowCapableNodeId() throws Exception {
        Assert.assertNotNull(BulkOMaticUtils.getFlowCapableNodeId("1"));
    }

    @Test
    public void testGetTableId() throws Exception {
        Assert.assertNotNull(BulkOMaticUtils.getTableId((short)1, "1"));
    }

    @Test
    public void testGetFlowId() throws Exception {
        Assert.assertNotNull(BulkOMaticUtils.getFlowId(BulkOMaticUtils.getTableId((short)1, "1"), "1"));
    }

}