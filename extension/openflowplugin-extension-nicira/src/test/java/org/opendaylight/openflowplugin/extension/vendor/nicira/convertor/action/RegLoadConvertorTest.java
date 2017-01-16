/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action;

import static org.mockito.Mockito.when;

import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.nx.NiciraMatchCodecs;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionRegLoad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.reg.load.grouping.NxActionRegLoad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxTunIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegLoadNotifFlowsStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegLoadNotifFlowsStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegLoadNotifDirectStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegLoadNotifDirectStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionRegLoadNotifGroupDescStatsUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.group.buckets.bucket.action.action.NxActionRegLoadNodesNodeGroupBucketsBucketActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegLoadNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.NxRegLoad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.nx.reg.load.Dst;
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
    public void setUp() throws Exception {
        final NxRegLoad nxRegLoad = Mockito.mock(NxRegLoad.class);
        final Dst dst = Mockito.mock(Dst.class);

        when(dst.getStart()).thenReturn(1);
        when(dst.getEnd()).thenReturn(2);
        when(nxRegLoad.getDst()).thenReturn(dst);
        when(nxRegLoad.getValue()).thenReturn(BigInteger.valueOf(3L));
        when(nxRegLoad.getDst().getDstChoice()).thenReturn(Mockito.mock(DstNxTunIdCase.class));
        when(actionsCase.getNxRegLoad()).thenReturn(nxRegLoad);

        final ActionRegLoad actionRegLoad = Mockito.mock(ActionRegLoad.class);
        final NxActionRegLoad nxActionRegLoad = Mockito.mock(NxActionRegLoad.class);
        when(nxActionRegLoad.getDst()).thenReturn(NiciraMatchCodecs.ICMP_TYPE_CODEC.getHeaderWithoutHasMask().toLong());
        when(nxActionRegLoad.getOfsNbits()).thenReturn(4);
        when(nxActionRegLoad.getValue()).thenReturn(BigInteger.ONE);
        when(actionRegLoad.getNxActionRegLoad()).thenReturn(nxActionRegLoad);
        when(action.getActionChoice()).thenReturn(actionRegLoad);

        regLoadConvertor = new RegLoadConvertor();
    }

    @Test
    public void testConvert() throws Exception {
        final ActionRegLoad actionRegLoad = (ActionRegLoad)regLoadConvertor.convert(actionsCase).getActionChoice();
        Assert.assertEquals(Integer.valueOf(65), actionRegLoad.getNxActionRegLoad().getOfsNbits());
        Assert.assertEquals(BigInteger.valueOf(3), actionRegLoad.getNxActionRegLoad().getValue());
    }

    @Test
    public void testConvert1() throws Exception {
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult
                = regLoadConvertor.convert(action, ActionPath.NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION);
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult1
                = regLoadConvertor.convert(action, ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION);
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult2
                = regLoadConvertor.convert(action, ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION);
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult3
                = regLoadConvertor.convert(action, ActionPath.GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION);
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult4
                = regLoadConvertor.convert(action, ActionPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION);
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult5
                = regLoadConvertor.convert(action, ActionPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION);

        Assert.assertEquals(Integer.valueOf(0), ((NxActionRegLoadNodesNodeTableFlowWriteActionsCase) actionResult).getNxRegLoad().getDst().getStart());
        Assert.assertEquals(Integer.valueOf(4), ((NxActionRegLoadNodesNodeTableFlowWriteActionsCase) actionResult).getNxRegLoad().getDst().getEnd());
        Assert.assertEquals(BigInteger.ONE, ((NxActionRegLoadNodesNodeTableFlowWriteActionsCase) actionResult).getNxRegLoad().getValue());

        Assert.assertEquals(Integer.valueOf(0), ((NxActionRegLoadNotifFlowsStatisticsUpdateApplyActionsCase) actionResult1).getNxRegLoad().getDst().getStart());
        Assert.assertEquals(Integer.valueOf(4), ((NxActionRegLoadNotifFlowsStatisticsUpdateApplyActionsCase) actionResult1).getNxRegLoad().getDst().getEnd());
        Assert.assertEquals(BigInteger.ONE, ((NxActionRegLoadNotifFlowsStatisticsUpdateApplyActionsCase) actionResult1).getNxRegLoad().getValue());

        Assert.assertEquals(Integer.valueOf(0), ((NxActionRegLoadNotifFlowsStatisticsUpdateWriteActionsCase) actionResult2).getNxRegLoad().getDst().getStart());
        Assert.assertEquals(Integer.valueOf(4), ((NxActionRegLoadNotifFlowsStatisticsUpdateWriteActionsCase) actionResult2).getNxRegLoad().getDst().getEnd());
        Assert.assertEquals(BigInteger.ONE, ((NxActionRegLoadNotifFlowsStatisticsUpdateWriteActionsCase) actionResult2).getNxRegLoad().getValue());

        Assert.assertEquals(Integer.valueOf(0), ((NxActionRegLoadNotifGroupDescStatsUpdatedCase) actionResult3).getNxRegLoad().getDst().getStart());
        Assert.assertEquals(Integer.valueOf(4), ((NxActionRegLoadNotifGroupDescStatsUpdatedCase) actionResult3).getNxRegLoad().getDst().getEnd());
        Assert.assertEquals(BigInteger.ONE, ((NxActionRegLoadNotifGroupDescStatsUpdatedCase) actionResult3).getNxRegLoad().getValue());

        Assert.assertEquals(Integer.valueOf(0), ((NxActionRegLoadNotifDirectStatisticsUpdateApplyActionsCase) actionResult4).getNxRegLoad().getDst().getStart());
        Assert.assertEquals(Integer.valueOf(4), ((NxActionRegLoadNotifDirectStatisticsUpdateApplyActionsCase) actionResult4).getNxRegLoad().getDst().getEnd());
        Assert.assertEquals(BigInteger.ONE, ((NxActionRegLoadNotifDirectStatisticsUpdateApplyActionsCase) actionResult4).getNxRegLoad().getValue());

        Assert.assertEquals(Integer.valueOf(0), ((NxActionRegLoadNotifDirectStatisticsUpdateWriteActionsCase) actionResult5).getNxRegLoad().getDst().getStart());
        Assert.assertEquals(Integer.valueOf(4), ((NxActionRegLoadNotifDirectStatisticsUpdateWriteActionsCase) actionResult5).getNxRegLoad().getDst().getEnd());
        Assert.assertEquals(BigInteger.ONE, ((NxActionRegLoadNotifDirectStatisticsUpdateWriteActionsCase) actionResult5).getNxRegLoad().getValue());

    }

}