/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionDecNshTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.dec.nsh.ttl.grouping.NxActionDecNshTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionDecNshTtlNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.dec.nsh.ttl.grouping.NxDecNshTtl;

@RunWith(MockitoJUnitRunner.class)
public class DecNshTtlConvertorTest {

    @Mock
    private NxActionDecNshTtlNodesNodeTableFlowWriteActionsCase actionsCase;

    @Mock
    private Action action;

    private DecNshTtlConvertor decNshTtlConvertor;

    @Before
    public void setUp() {
        NxDecNshTtl nxDecNshTtl = Mockito.mock(NxDecNshTtl.class);

        NxActionDecNshTtl nxActionDecNshTtl = Mockito.mock(NxActionDecNshTtl.class);
        ActionDecNshTtl actionDecNshTtl = Mockito.mock(ActionDecNshTtl.class);

        decNshTtlConvertor = new DecNshTtlConvertor();
    }

    @Test
    public void testConvertSalToOf() {
        ActionDecNshTtl actionDecNshTtl = (ActionDecNshTtl) decNshTtlConvertor.convert(actionsCase).getActionChoice();
        Assert.assertNotNull(actionDecNshTtl);
    }

    @Test
    public void testConvertOfToSal() {
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult
                = decNshTtlConvertor.convert(action, ActionPath.FLOWS_STATISTICS_UPDATE_APPLY_ACTIONS);
        Assert.assertNotNull(actionResult);
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult1
                = decNshTtlConvertor.convert(action, ActionPath.FLOWS_STATISTICS_UPDATE_WRITE_ACTIONS);
        Assert.assertNotNull(actionResult1);
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult2
                = decNshTtlConvertor.convert(action, ActionPath.GROUP_DESC_STATS_UPDATED_BUCKET_ACTION);
        Assert.assertNotNull(actionResult2);
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult3
                = decNshTtlConvertor.convert(action, ActionPath.INVENTORY_FLOWNODE_TABLE_WRITE_ACTIONS);
        Assert.assertNotNull(actionResult3);
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult4
                = decNshTtlConvertor.convert(action, ActionPath.FLOWS_STATISTICS_RPC_APPLY_ACTIONS);
        Assert.assertNotNull(actionResult4);
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult5
                = decNshTtlConvertor.convert(action, ActionPath.FLOWS_STATISTICS_RPC_WRITE_ACTIONS);
        Assert.assertNotNull(actionResult5);
    }
}