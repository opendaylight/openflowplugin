/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.vendor.cisco.convertor.action;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.action.container.action.choice.OfjCofActionFcid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.action.container.action.choice.OfjCofActionFcidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.fcid.grouping.ActionFcid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.fcid.grouping.ActionFcidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.CofActionFcidNotifGroupDescStatsUpdatedCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.CofActionFcidGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.FcidId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.cof.action.fcid.grouping.ActionFcidHi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.cof.action.fcid.grouping.ActionFcidHiBuilder;

public class FcidConvertorTest extends TestCase {

    /**
     * default action path suitable for tests
     */
    private static final ActionPath DEFAULT_ACTION_PATH =
            ActionPath.GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION;
    /**
     * singleton converter
     */
    private static final FcidConvertor FCID_CONVERTOR = new FcidConvertor();

    /**
     * Test method for {@link org.opendaylight.openflowplugin.extension.vendor.cisco.convertor.action.FcidConvertor#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action)}
     */
    @Test
    public void testConvertLowToHigh() {
        ActionFcidBuilder actionFcidBuilder = new ActionFcidBuilder();
        actionFcidBuilder.setFcid((short) 42);
        OfjCofActionFcidBuilder cofActionBld = new OfjCofActionFcidBuilder().setActionFcid(actionFcidBuilder.build());

        ActionBuilder inputBld = new ActionBuilder().setActionChoice(cofActionBld.build());
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionHi =
                FCID_CONVERTOR.convert(inputBld.build(), DEFAULT_ACTION_PATH);

        Assert.assertTrue("converted action is of incorrect type: " + actionHi.getClass(),
                actionHi instanceof CofActionFcidGrouping);
        ActionFcidHi actionFcidHi = ((CofActionFcidGrouping) actionHi).getActionFcidHi();
        Assert.assertNotNull(actionFcidHi);
        Assert.assertEquals((short) 42, actionFcidHi.getFcid().getValue().shortValue());
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.extension.vendor.cisco.convertor.action.FcidConvertor#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action, org.opendaylight.openflowplugin.extension.api.path.ActionPath)}
     */
    @Test
    public void testCoverterHighToLow() {
        ActionFcidHiBuilder actionFcidHiBuilder = new ActionFcidHiBuilder();
        FcidId fcidId = new FcidId(new Integer(42));
        actionFcidHiBuilder.setFcid(fcidId);

        CofActionFcidNotifGroupDescStatsUpdatedCaseBuilder cofActionBld = new CofActionFcidNotifGroupDescStatsUpdatedCaseBuilder();
        cofActionBld.setActionFcidHi(actionFcidHiBuilder.build());

        Action action = FCID_CONVERTOR.convert(cofActionBld.build());

        ActionFcid actionFcid = ((OfjCofActionFcid) action.getActionChoice()).getActionFcid();
        Assert.assertNotNull(actionFcid);
        Assert.assertEquals((short) 42, actionFcid.getFcid().shortValue());
    }
}