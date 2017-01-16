/**
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
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.nx.NiciraMatchCodecs;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.OfjNxHashFields;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.OfjNxMpAlgorithm;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionMultipath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.multipath.grouping.NxActionMultipath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNspCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionMultipathNotifFlowsStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionMultipathNotifFlowsStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionMultipathNotifDirectStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionMultipathNotifDirectStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionMultipathNotifGroupDescStatsUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionMultipathNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.multipath.grouping.NxMultipath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.multipath.grouping.nx.multipath.Dst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link MultipathConvertor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class MultipathConvertorTest {

    private static final Logger LOG = LoggerFactory.getLogger(MultipathConvertorTest.class);

    @Mock
    private NxActionMultipathNodesNodeTableFlowWriteActionsCase bucketActionsCase;

    @Mock
    private Action action;

    private MultipathConvertor multipathConvertor;

    @Before
    public void setUp() throws Exception {
        final NxMultipath nxMultipath = Mockito.mock(NxMultipath.class);
        when(bucketActionsCase.getNxMultipath()).thenReturn(nxMultipath);

        final Dst dst = Mockito.mock(Dst.class);
        when(dst.getStart()).thenReturn(1);
        when(dst.getEnd()).thenReturn(2);

        final DstNxNspCase dstNxNspCase = Mockito.mock(DstNxNspCase.class);
        when(dst.getDstChoice()).thenReturn(dstNxNspCase);
        when(nxMultipath.getFields()).thenReturn(OfjNxHashFields.NXHASHFIELDSETHSRC);
        when(nxMultipath.getBasis()).thenReturn(2);
        when(nxMultipath.getAlgorithm()).thenReturn(OfjNxMpAlgorithm.NXMPALGHASHTHRESHOLD);
        when(nxMultipath.getMaxLink()).thenReturn(2);
        when(nxMultipath.getArg()).thenReturn(2L);
        when(nxMultipath.getDst()).thenReturn(dst);

        final ActionMultipath actionMultipath = Mockito.mock(ActionMultipath.class);
        final NxActionMultipath nxActionMultipath = Mockito.mock(NxActionMultipath.class);
        when(nxActionMultipath.getDst()).thenReturn(NiciraMatchCodecs.TUN_ID_CODEC.getHeaderWithoutHasMask().toLong());
        when(nxActionMultipath.getBasis()).thenReturn(1);
        when(nxActionMultipath.getAlgorithm()).thenReturn(OfjNxMpAlgorithm.NXMPALGHRW);
        when(nxActionMultipath.getMaxLink()).thenReturn(2);
        when(nxActionMultipath.getArg()).thenReturn(2L);
        when(actionMultipath.getNxActionMultipath()).thenReturn(nxActionMultipath);
        when(action.getActionChoice()).thenReturn(actionMultipath);

        multipathConvertor = new MultipathConvertor();
    }

    @Test
    public void testConvert() throws Exception {
        final ActionMultipath actionMultipath = (ActionMultipath) multipathConvertor.convert(bucketActionsCase).getActionChoice();
        Assert.assertEquals(OfjNxHashFields.NXHASHFIELDSETHSRC, actionMultipath.getNxActionMultipath().getFields());
        Assert.assertEquals(Integer.valueOf(2), actionMultipath.getNxActionMultipath().getBasis());
        Assert.assertEquals(OfjNxMpAlgorithm.NXMPALGHASHTHRESHOLD, actionMultipath.getNxActionMultipath().getAlgorithm());
        Assert.assertEquals(Integer.valueOf(2), actionMultipath.getNxActionMultipath().getMaxLink());
        Assert.assertEquals(Long.valueOf(2L), actionMultipath.getNxActionMultipath().getArg());
    }

    @Test
    public void testConvert1() throws Exception {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult
                = multipathConvertor.convert(action, ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult1
                = multipathConvertor.convert(action, ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult2
                = multipathConvertor.convert(action, ActionPath.GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult3
                = multipathConvertor.convert(action, ActionPath.NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult4
                = multipathConvertor.convert(action, ActionPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult5
                = multipathConvertor.convert(action, ActionPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION);

        Assert.assertEquals(Integer.valueOf(1), ((NxActionMultipathNotifFlowsStatisticsUpdateApplyActionsCase) actionResult).getNxMultipath().getBasis());
        Assert.assertEquals(OfjNxMpAlgorithm.NXMPALGHRW, ((NxActionMultipathNotifFlowsStatisticsUpdateApplyActionsCase) actionResult).getNxMultipath().getAlgorithm());
        Assert.assertEquals(Long.valueOf(2L), ((NxActionMultipathNotifFlowsStatisticsUpdateApplyActionsCase) actionResult).getNxMultipath().getArg());
        Assert.assertEquals(Integer.valueOf(2), ((NxActionMultipathNotifFlowsStatisticsUpdateApplyActionsCase) actionResult).getNxMultipath().getMaxLink());

        Assert.assertEquals(Integer.valueOf(1), ((NxActionMultipathNotifFlowsStatisticsUpdateWriteActionsCase) actionResult1).getNxMultipath().getBasis());
        Assert.assertEquals(OfjNxMpAlgorithm.NXMPALGHRW, ((NxActionMultipathNotifFlowsStatisticsUpdateWriteActionsCase) actionResult1).getNxMultipath().getAlgorithm());
        Assert.assertEquals(Long.valueOf(2L), ((NxActionMultipathNotifFlowsStatisticsUpdateWriteActionsCase) actionResult1).getNxMultipath().getArg());
        Assert.assertEquals(Integer.valueOf(2), ((NxActionMultipathNotifFlowsStatisticsUpdateWriteActionsCase) actionResult1).getNxMultipath().getMaxLink());

        Assert.assertEquals(Integer.valueOf(1), ((NxActionMultipathNotifGroupDescStatsUpdatedCase) actionResult2).getNxMultipath().getBasis());
        Assert.assertEquals(OfjNxMpAlgorithm.NXMPALGHRW, ((NxActionMultipathNotifGroupDescStatsUpdatedCase) actionResult2).getNxMultipath().getAlgorithm());
        Assert.assertEquals(Long.valueOf(2L), ((NxActionMultipathNotifGroupDescStatsUpdatedCase) actionResult2).getNxMultipath().getArg());
        Assert.assertEquals(Integer.valueOf(2), ((NxActionMultipathNotifGroupDescStatsUpdatedCase) actionResult2).getNxMultipath().getMaxLink());

        Assert.assertEquals(Integer.valueOf(1), ((NxActionMultipathNodesNodeTableFlowWriteActionsCase) actionResult3).getNxMultipath().getBasis());
        Assert.assertEquals(OfjNxMpAlgorithm.NXMPALGHRW, ((NxActionMultipathNodesNodeTableFlowWriteActionsCase) actionResult3).getNxMultipath().getAlgorithm());
        Assert.assertEquals(Long.valueOf(2L), ((NxActionMultipathNodesNodeTableFlowWriteActionsCase) actionResult3).getNxMultipath().getArg());
        Assert.assertEquals(Integer.valueOf(2), ((NxActionMultipathNodesNodeTableFlowWriteActionsCase) actionResult3).getNxMultipath().getMaxLink());

        Assert.assertEquals(Integer.valueOf(1), ((NxActionMultipathNotifDirectStatisticsUpdateApplyActionsCase) actionResult4).getNxMultipath().getBasis());
        Assert.assertEquals(OfjNxMpAlgorithm.NXMPALGHRW, ((NxActionMultipathNotifDirectStatisticsUpdateApplyActionsCase) actionResult4).getNxMultipath().getAlgorithm());
        Assert.assertEquals(Long.valueOf(2L), ((NxActionMultipathNotifDirectStatisticsUpdateApplyActionsCase) actionResult4).getNxMultipath().getArg());
        Assert.assertEquals(Integer.valueOf(2), ((NxActionMultipathNotifDirectStatisticsUpdateApplyActionsCase) actionResult4).getNxMultipath().getMaxLink());

        Assert.assertEquals(Integer.valueOf(1), ((NxActionMultipathNotifDirectStatisticsUpdateWriteActionsCase) actionResult5).getNxMultipath().getBasis());
        Assert.assertEquals(OfjNxMpAlgorithm.NXMPALGHRW, ((NxActionMultipathNotifDirectStatisticsUpdateWriteActionsCase) actionResult5).getNxMultipath().getAlgorithm());
        Assert.assertEquals(Long.valueOf(2L), ((NxActionMultipathNotifDirectStatisticsUpdateWriteActionsCase) actionResult5).getNxMultipath().getArg());
        Assert.assertEquals(Integer.valueOf(2), ((NxActionMultipathNotifDirectStatisticsUpdateWriteActionsCase) actionResult5).getNxMultipath().getMaxLink());
    }
}