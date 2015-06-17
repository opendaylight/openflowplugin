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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.action.container.action.choice.OfjCofActionMplsLsp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.action.container.action.choice.OfjCofActionMplsLspBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.mpls.lsp.grouping.ActionMplsLsp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.mpls.lsp.grouping.ActionMplsLspBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.CofActionMplsLspNotifGroupDescStatsUpdatedCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.CofActionMplsLspGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.MplsLspName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.cof.action.mpls.lsp.grouping.ActionMplsLspHi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.cof.action.mpls.lsp.grouping.ActionMplsLspHiBuilder;

public class MplsLspConvertorTest extends TestCase {
    /**
     * default action path suitable for tests
     */
    private static final ActionPath DEFAULT_ACTION_PATH =
            ActionPath.GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION;
    /**
     * singleton converter
     */
    private static final MplsLspConvertor MPLS_LSP_CONVERTOR = new MplsLspConvertor();

    private static final String MPLS_NAME = "MPLS";

    /**
     * Test method for {@link org.opendaylight.openflowplugin.extension.vendor.cisco.convertor.action.MplsLspConvertor#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action)}
     */
    @Test
    public void testConvertLowToHigh() {
        ActionMplsLspBuilder actionMplsLspBuilder = new ActionMplsLspBuilder();
        actionMplsLspBuilder.setName(MPLS_NAME.getBytes());
        OfjCofActionMplsLspBuilder cofActionBld = new OfjCofActionMplsLspBuilder().setActionMplsLsp(actionMplsLspBuilder.build());

        ActionBuilder inputBld = new ActionBuilder().setActionChoice(cofActionBld.build());
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionHi =
                MPLS_LSP_CONVERTOR.convert(inputBld.build(), DEFAULT_ACTION_PATH);

        Assert.assertTrue("converted action is of incorrect type: " + actionHi.getClass(),
                actionHi instanceof CofActionMplsLspGrouping);
        ActionMplsLspHi actionFcidHi = ((CofActionMplsLspGrouping) actionHi).getActionMplsLspHi();
        Assert.assertNotNull(actionFcidHi);
        Assert.assertEquals(MPLS_NAME, actionFcidHi.getMplsLspName().getValue());
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.extension.vendor.cisco.convertor.action.FcidConvertor#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action)}
     */
    @Test
    public void testCoverterHighToLow() {
        ActionMplsLspHiBuilder actionMplsLspHiBuilder = new ActionMplsLspHiBuilder();
        MplsLspName mplsLspName = new MplsLspName(MPLS_NAME);
        actionMplsLspHiBuilder.setMplsLspName(mplsLspName);

        CofActionMplsLspNotifGroupDescStatsUpdatedCaseBuilder cofActionBld = new CofActionMplsLspNotifGroupDescStatsUpdatedCaseBuilder();
        cofActionBld.setActionMplsLspHi(actionMplsLspHiBuilder.build());

        Action action = MPLS_LSP_CONVERTOR.convert(cofActionBld.build());

        ActionMplsLsp actionMplsLsp = ((OfjCofActionMplsLsp) action.getActionChoice()).getActionMplsLsp();
        Assert.assertNotNull(actionMplsLsp);
        Assert.assertEquals(MPLS_NAME, new String(actionMplsLsp.getName()));
    }
}