/*
 * Copyright (c) 2018 Redhat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionCtClear;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.ct.clear.grouping.NxActionCtClear;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionCtClearNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.ct.clear.grouping.NxCtClear;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link CtClearConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CtClearConvertorTest {

    private static final Logger LOG = LoggerFactory.getLogger(CtClearConvertorTest.class);

    @Mock
    private NxActionCtClearNodesNodeTableFlowWriteActionsCase actionsCase;

    @Mock
    private Action action;

    private CtClearConvertor ctClearConvertor;

    @Before
    public void setUp() {
        final NxCtClear nxCtClear = Mockito.mock(NxCtClear.class);

        final ActionCtClear actionCtClear = Mockito.mock(ActionCtClear.class);
        final NxActionCtClear nxActionCtClear = Mockito.mock(NxActionCtClear.class);

        ctClearConvertor = new CtClearConvertor();
    }

    @Test
    public void testConvert() {
        final ActionCtClear actionCtClear = (ActionCtClear) ctClearConvertor.convert(actionsCase).getActionChoice();
        Assert.assertNotNull(actionCtClear);
    }

    @Test
    public void testConvert1() {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult
                = ctClearConvertor.convert(action, ActionPath.FLOWS_STATISTICS_UPDATE_APPLY_ACTIONS);
        Assert.assertNotNull(actionResult);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult1
                = ctClearConvertor.convert(action, ActionPath.FLOWS_STATISTICS_UPDATE_WRITE_ACTIONS);
        Assert.assertNotNull(actionResult1);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult2
                = ctClearConvertor.convert(action, ActionPath.GROUP_DESC_STATS_UPDATED_BUCKET_ACTION);
        Assert.assertNotNull(actionResult2);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult3
                = ctClearConvertor.convert(action, ActionPath.INVENTORY_FLOWNODE_TABLE_WRITE_ACTIONS);
        Assert.assertNotNull(actionResult3);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult4
                = ctClearConvertor.convert(action, ActionPath.FLOWS_STATISTICS_RPC_APPLY_ACTIONS);
        Assert.assertNotNull(actionResult4);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult5
                = ctClearConvertor.convert(action, ActionPath.FLOWS_STATISTICS_RPC_WRITE_ACTIONS);
        Assert.assertNotNull(actionResult5);
    }
}
