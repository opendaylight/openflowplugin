/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action;

import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.nx.codec.match.NiciraMatchCodecs;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionRegLoad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.reg.load.grouping.NxActionRegLoad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxTunIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegLoadNotifFlowsStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegLoadNotifFlowsStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegLoadNotifDirectStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegLoadNotifDirectStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionRegLoadNotifGroupDescStatsUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.group.buckets.bucket.action.action.NxActionRegLoadNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegLoadNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.NxRegLoad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.nx.reg.load.Dst;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link RegLoadConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class RegLoadConvertorTest {

    private static final Logger LOG = LoggerFactory.getLogger(RegLoadConvertorTest.class);

    @Mock
    private NxActionRegLoadNodesNodeGroupBucketsBucketActionsCase actionsCase;

    @Mock
    private Action action;

    private RegLoadConvertor regLoadConvertor;

    @Before
    public void setUp() {
        final NxRegLoad nxRegLoad = Mockito.mock(NxRegLoad.class);
        final Dst dst = Mockito.mock(Dst.class);

        when(dst.getStart()).thenReturn(Uint16.valueOf(1));
        when(dst.getEnd()).thenReturn(Uint16.valueOf(2));
        when(nxRegLoad.getDst()).thenReturn(dst);
        when(nxRegLoad.getValue()).thenReturn(Uint64.valueOf(3));
        when(nxRegLoad.getDst().getDstChoice()).thenReturn(new DstNxTunIdCaseBuilder().build());
        when(actionsCase.getNxRegLoad()).thenReturn(nxRegLoad);

        final ActionRegLoad actionRegLoad = Mockito.mock(ActionRegLoad.class);
        final NxActionRegLoad nxActionRegLoad = Mockito.mock(NxActionRegLoad.class);
        when(nxActionRegLoad.getDst()).thenReturn(
            Uint32.valueOf(NiciraMatchCodecs.ICMP_TYPE_CODEC.getHeaderWithoutHasMask().toLong()));
        when(nxActionRegLoad.getOfsNbits()).thenReturn(Uint16.valueOf(4));
        when(nxActionRegLoad.getValue()).thenReturn(Uint64.ONE);
        when(actionRegLoad.getNxActionRegLoad()).thenReturn(nxActionRegLoad);
        when(action.getActionChoice()).thenReturn(actionRegLoad);

        regLoadConvertor = new RegLoadConvertor();
    }

    @Test
    public void testConvert() {
        final ActionRegLoad actionRegLoad = (ActionRegLoad)regLoadConvertor.convert(actionsCase).getActionChoice();
        Assert.assertEquals(Uint16.valueOf(65), actionRegLoad.getNxActionRegLoad().getOfsNbits());
        Assert.assertEquals(Uint64.valueOf(3), actionRegLoad.getNxActionRegLoad().getValue());
    }

    @Test
    public void testConvert1() {
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult
                = regLoadConvertor.convert(action, ActionPath.INVENTORY_FLOWNODE_TABLE_WRITE_ACTIONS);
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult1
                = regLoadConvertor.convert(action, ActionPath.FLOWS_STATISTICS_UPDATE_APPLY_ACTIONS);
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult2
                = regLoadConvertor.convert(action, ActionPath.FLOWS_STATISTICS_UPDATE_WRITE_ACTIONS);
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult3
                = regLoadConvertor.convert(action, ActionPath.GROUP_DESC_STATS_UPDATED_BUCKET_ACTION);
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult4
                = regLoadConvertor.convert(action, ActionPath.FLOWS_STATISTICS_RPC_APPLY_ACTIONS);
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult5
                = regLoadConvertor.convert(action, ActionPath.FLOWS_STATISTICS_RPC_WRITE_ACTIONS);

        Assert.assertEquals(Uint16.ZERO,
                ((NxActionRegLoadNodesNodeTableFlowWriteActionsCase) actionResult).getNxRegLoad().getDst().getStart());
        Assert.assertEquals(Uint16.valueOf(4),
                ((NxActionRegLoadNodesNodeTableFlowWriteActionsCase) actionResult).getNxRegLoad().getDst().getEnd());
        Assert.assertEquals(Uint64.ONE,
                ((NxActionRegLoadNodesNodeTableFlowWriteActionsCase) actionResult).getNxRegLoad().getValue());

        Assert.assertEquals(Uint16.ZERO,
                ((NxActionRegLoadNotifFlowsStatisticsUpdateApplyActionsCase) actionResult1).getNxRegLoad().getDst()
                        .getStart());
        Assert.assertEquals(Uint16.valueOf(4),
                ((NxActionRegLoadNotifFlowsStatisticsUpdateApplyActionsCase) actionResult1).getNxRegLoad().getDst()
                        .getEnd());
        Assert.assertEquals(Uint64.ONE,
                ((NxActionRegLoadNotifFlowsStatisticsUpdateApplyActionsCase) actionResult1).getNxRegLoad().getValue());

        Assert.assertEquals(Uint16.ZERO,
                ((NxActionRegLoadNotifFlowsStatisticsUpdateWriteActionsCase) actionResult2).getNxRegLoad().getDst()
                        .getStart());
        Assert.assertEquals(Uint16.valueOf(4),
                ((NxActionRegLoadNotifFlowsStatisticsUpdateWriteActionsCase) actionResult2).getNxRegLoad().getDst()
                        .getEnd());
        Assert.assertEquals(Uint64.ONE,
                ((NxActionRegLoadNotifFlowsStatisticsUpdateWriteActionsCase) actionResult2).getNxRegLoad().getValue());

        Assert.assertEquals(Uint16.ZERO,
                ((NxActionRegLoadNotifGroupDescStatsUpdatedCase) actionResult3).getNxRegLoad().getDst().getStart());
        Assert.assertEquals(Uint16.valueOf(4),
                ((NxActionRegLoadNotifGroupDescStatsUpdatedCase) actionResult3).getNxRegLoad().getDst().getEnd());
        Assert.assertEquals(Uint64.ONE,
                ((NxActionRegLoadNotifGroupDescStatsUpdatedCase) actionResult3).getNxRegLoad().getValue());

        Assert.assertEquals(Uint16.ZERO,
                ((NxActionRegLoadNotifDirectStatisticsUpdateApplyActionsCase) actionResult4).getNxRegLoad().getDst()
                        .getStart());
        Assert.assertEquals(Uint16.valueOf(4),
                ((NxActionRegLoadNotifDirectStatisticsUpdateApplyActionsCase) actionResult4).getNxRegLoad().getDst()
                        .getEnd());
        Assert.assertEquals(Uint64.ONE,
                ((NxActionRegLoadNotifDirectStatisticsUpdateApplyActionsCase) actionResult4).getNxRegLoad().getValue());

        Assert.assertEquals(Uint16.ZERO,
                ((NxActionRegLoadNotifDirectStatisticsUpdateWriteActionsCase) actionResult5).getNxRegLoad().getDst()
                        .getStart());
        Assert.assertEquals(Uint16.valueOf(4),
                ((NxActionRegLoadNotifDirectStatisticsUpdateWriteActionsCase) actionResult5).getNxRegLoad().getDst()
                        .getEnd());
        Assert.assertEquals(Uint64.ONE,
                ((NxActionRegLoadNotifDirectStatisticsUpdateWriteActionsCase) actionResult5).getNxRegLoad().getValue());
    }
}
