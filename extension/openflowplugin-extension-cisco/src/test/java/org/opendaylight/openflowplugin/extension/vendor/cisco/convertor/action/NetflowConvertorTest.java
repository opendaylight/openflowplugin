/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.vendor.cisco.convertor.action;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.action.container.action.choice.OfjCofActionNetflow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.action.container.action.choice.OfjCofActionNetflowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.netflow.grouping.ActionNetflow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.netflow.grouping.ActionNetflowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.CofActionNetflowNotifGroupDescStatsUpdatedCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.CofActionNetflowGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.cof.action.netflow.grouping.ActionNetflowHi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.cof.action.netflow.grouping.ActionNetflowHiBuilder;

public class NetflowConvertorTest {

    /**
     * default action path suitable for tests
     */
    private static final ActionPath DEFAULT_ACTION_PATH =
            ActionPath.GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION;
    /**
     * singleton converter
     */
    private static final NetflowConvertor NETFLOW_CONVERTOR = new NetflowConvertor();

    /**
     * Test method for {@link org.opendaylight.openflowplugin.extension.vendor.cisco.convertor.action.MplsLspConvertor#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action)}
     */
    @Test
    public void testConvertLowToHigh() {
        ActionNetflowBuilder actionNetflowBuilder = new ActionNetflowBuilder();
        actionNetflowBuilder.setNetflow(true);
        OfjCofActionNetflowBuilder cofActionBld = new OfjCofActionNetflowBuilder().setActionNetflow(actionNetflowBuilder.build());

        ActionBuilder inputBld = new ActionBuilder().setActionChoice(cofActionBld.build());
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionHi =
                NETFLOW_CONVERTOR.convert(inputBld.build(), DEFAULT_ACTION_PATH);

        Assert.assertTrue("converted action is of incorrect type: " + actionHi.getClass(),
                actionHi instanceof CofActionNetflowGrouping);
        ActionNetflowHi actionNetflowHi = ((CofActionNetflowGrouping) actionHi).getActionNetflowHi();
        Assert.assertNotNull(actionNetflowHi);
        Assert.assertTrue(actionNetflowHi.isNetflow());
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.extension.vendor.cisco.convertor.action.FcidConvertor#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action)}
     */
    @Test
    public void testCoverterHighToLow() {
        ActionNetflowHiBuilder actionNetflowHiBuilder = new ActionNetflowHiBuilder();
        actionNetflowHiBuilder.setNetflow(true);

        CofActionNetflowNotifGroupDescStatsUpdatedCaseBuilder cofActionBld = new CofActionNetflowNotifGroupDescStatsUpdatedCaseBuilder();
        cofActionBld.setActionNetflowHi(actionNetflowHiBuilder.build());

        Action action = NETFLOW_CONVERTOR.convert(cofActionBld.build());

        ActionNetflow actionNetflow = ((OfjCofActionNetflow) action.getActionChoice()).getActionNetflow();
        Assert.assertNotNull(actionNetflow);
        Assert.assertTrue(actionNetflow.isNetflow());
    }

}