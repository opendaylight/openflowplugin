/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.NshFlagsConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.experimenter.id.match.entry.ExperimenterIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.aug.nx.action.ActionRegLoad2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.reg.load2.grouping.NxActionRegLoad2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.OfjAugNxExpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.exp.match.nx.exp.match.entry.value.NshFlagsCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsh.flags.grouping.NshFlagsValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNshFlagsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegLoadNotifFlowsStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegLoadNotifFlowsStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegLoadNotifDirectStatisticsUpdateApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.get.flow.statistics.output.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegLoadNotifDirectStatisticsUpdateWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionRegLoadNotifGroupDescStatsUpdatedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegLoadNodesNodeTableFlowApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionRegLoadNodesNodeTableFlowWriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.NxRegLoad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.nx.reg.load.Dst;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

@RunWith(JUnitParamsRunner.class)
public class RegLoad2ConvertorTest {

    public static Iterable<Object[]> commonData() {
        return Arrays.asList(new Object[][] {
                {Uint16.valueOf(0), Uint16.valueOf(0), 0x01, 0x01, 0x01, null},
                {Uint16.valueOf(0), Uint16.valueOf(7), 0xFF, 0xFF, 0xFF, null},
                {Uint16.valueOf(0), Uint16.valueOf(3), 0x0F, 0x0F, 0x0F, null},
                {Uint16.valueOf(4), Uint16.valueOf(7), 0x0F, 0xF0, 0xF0, null},
                {Uint16.valueOf(3), Uint16.valueOf(5), 0x05, 0x28, 0x38, null}
        });
    }

    public static Iterable<Object[]> salToOpenflowData() {
        return Arrays.asList(new Object[][] {
                // specified values do not fit in the bit range
                {Uint16.valueOf(1), Uint16.valueOf(5), 0xFF, null, null, IllegalArgumentException.class},
                {Uint16.valueOf(3), Uint16.valueOf(5), 0x08, null, null, IllegalArgumentException.class},
                // out of range value and mask
                {Uint16.valueOf(0), Uint16.valueOf(16), 0x1FFFF, null, null, IllegalArgumentException.class},
                // out of range mask
                {Uint16.valueOf(0), Uint16.valueOf(16), 0x1, null, null, IllegalArgumentException.class}
        });
    }

    public static Iterable<Object[]> openflowToSalData() {
        return Arrays.asList(new Object[][] {
                // multiple 1-bit segment in mask
                {null, null, null, 0x05, 0x05, IllegalArgumentException.class},
                {null, null, null, 0x28, 0x28, IllegalArgumentException.class},
                // no maskInteger
                {Uint16.valueOf(0), Uint16.valueOf(7), 0x01, 0x01, null, null}
        });
    }

    @Test
    @Parameters(method = "commonData, salToOpenflowData")
    public void testConvertSalToOf(final Uint16 rangeStart,
                                   final Uint16 rangeEnd,
                                   final Integer rangeValue,
                                   final Integer value,
                                   final Integer mask,
                                   final Class<? extends Exception> expectedException) {

        Dst dst = mock(Dst.class);
        when(dst.getStart()).thenReturn(rangeStart);
        when(dst.getEnd()).thenReturn(rangeEnd);
        NxRegLoad nxRegLoad = mock(NxRegLoad.class);
        when(nxRegLoad.getValue()).thenReturn(Uint64.valueOf(rangeValue));
        when(nxRegLoad.getDst()).thenReturn(dst);
        when(nxRegLoad.getDst().getDstChoice()).thenReturn(mock(DstNxNshFlagsCase.class));
        NxActionRegLoadNodesNodeTableFlowApplyActionsCase actionsCase =
                mock(NxActionRegLoadNodesNodeTableFlowApplyActionsCase.class);
        when(actionsCase.getNxRegLoad()).thenReturn(nxRegLoad);

        RegLoad2Convertor regLoad2Convertor = new RegLoad2Convertor();

        if (expectedException != null) {
            assertThrows(expectedException, () -> regLoad2Convertor.convert(actionsCase).getActionChoice());
            return;
        }

        ActionRegLoad2 actionRegLoad = (ActionRegLoad2) regLoad2Convertor.convert(actionsCase).getActionChoice();

        MatchEntry matchEntry = actionRegLoad.getNxActionRegLoad2().getMatchEntry().get(0);
        ExperimenterIdCase experimenterIdCase = (ExperimenterIdCase) matchEntry.getMatchEntryValue();
        OfjAugNxExpMatch ofjAugNxExpMatch = experimenterIdCase.augmentation(OfjAugNxExpMatch.class);
        NshFlagsCaseValue nshFlagsCaseValue = (NshFlagsCaseValue) ofjAugNxExpMatch.getNxExpMatchEntryValue();
        NshFlagsValues nshFlagsValues = nshFlagsCaseValue.getNshFlagsValues();

        assertEquals(value.intValue(), nshFlagsValues.getNshFlags().intValue());
        assertEquals(mask.intValue(), nshFlagsValues.getMask().intValue());
    }

    @Test
    @Parameters(method = "commonData, openflowToSalData")
    public void testConvertOfToSal(final Uint16 rangeStart,
                                   final Uint16 rangeEnd,
                                   final Integer rangeValue,
                                   final Integer value,
                                   final Integer mask,
                                   final Class<? extends Exception> expectedException) {

        NxActionRegLoad2 nxActionRegLoad2 = mock(NxActionRegLoad2.class);
        when(nxActionRegLoad2.getMatchEntry()).thenReturn(Collections.singletonList(
                NshFlagsConvertor.buildMatchEntry(Uint8.valueOf(value), mask == null ? null : Uint8.valueOf(mask))));
        ActionRegLoad2 actionRegLoad2 = mock(ActionRegLoad2.class);
        when(actionRegLoad2.getNxActionRegLoad2()).thenReturn(nxActionRegLoad2);
        Action action = mock(Action.class);
        when(action.getActionChoice()).thenReturn(actionRegLoad2);

        RegLoad2Convertor regLoad2Convertor = new RegLoad2Convertor();

        if (expectedException != null) {
            assertThrows(expectedException,
                () -> regLoad2Convertor.convert(action, ActionPath.INVENTORY_FLOWNODE_TABLE_WRITE_ACTIONS));
            return;
        }

        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult
                = regLoad2Convertor.convert(action, ActionPath.INVENTORY_FLOWNODE_TABLE_WRITE_ACTIONS);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult1
                = regLoad2Convertor.convert(action, ActionPath.FLOWS_STATISTICS_UPDATE_APPLY_ACTIONS);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult2
                = regLoad2Convertor.convert(action, ActionPath.FLOWS_STATISTICS_UPDATE_WRITE_ACTIONS);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult3
                = regLoad2Convertor.convert(action, ActionPath.GROUP_DESC_STATS_UPDATED_BUCKET_ACTION);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult4
                = regLoad2Convertor.convert(action, ActionPath.FLOWS_STATISTICS_RPC_APPLY_ACTIONS);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionResult5
                = regLoad2Convertor.convert(action, ActionPath.FLOWS_STATISTICS_RPC_WRITE_ACTIONS);

        assertEquals(rangeStart,
                ((NxActionRegLoadNodesNodeTableFlowWriteActionsCase) actionResult).getNxRegLoad().getDst().getStart());
        assertEquals(rangeEnd,
                ((NxActionRegLoadNodesNodeTableFlowWriteActionsCase) actionResult).getNxRegLoad().getDst().getEnd());
        assertEquals(Uint64.valueOf(rangeValue),
                ((NxActionRegLoadNodesNodeTableFlowWriteActionsCase) actionResult).getNxRegLoad().getValue());

        assertEquals(rangeStart,
                ((NxActionRegLoadNotifFlowsStatisticsUpdateApplyActionsCase) actionResult1).getNxRegLoad().getDst()
                        .getStart());
        assertEquals(rangeEnd,
                ((NxActionRegLoadNotifFlowsStatisticsUpdateApplyActionsCase) actionResult1).getNxRegLoad().getDst()
                        .getEnd());
        assertEquals(Uint64.valueOf(rangeValue),
                ((NxActionRegLoadNotifFlowsStatisticsUpdateApplyActionsCase) actionResult1).getNxRegLoad().getValue());

        assertEquals(rangeStart,
                ((NxActionRegLoadNotifFlowsStatisticsUpdateWriteActionsCase) actionResult2).getNxRegLoad().getDst()
                        .getStart());
        assertEquals(rangeEnd,
                ((NxActionRegLoadNotifFlowsStatisticsUpdateWriteActionsCase) actionResult2).getNxRegLoad().getDst()
                        .getEnd());
        assertEquals(Uint64.valueOf(rangeValue),
                ((NxActionRegLoadNotifFlowsStatisticsUpdateWriteActionsCase) actionResult2).getNxRegLoad().getValue());

        assertEquals(rangeStart,
                ((NxActionRegLoadNotifGroupDescStatsUpdatedCase) actionResult3).getNxRegLoad().getDst().getStart());
        assertEquals(rangeEnd,
                ((NxActionRegLoadNotifGroupDescStatsUpdatedCase) actionResult3).getNxRegLoad().getDst().getEnd());
        assertEquals(Uint64.valueOf(rangeValue),
                ((NxActionRegLoadNotifGroupDescStatsUpdatedCase) actionResult3).getNxRegLoad().getValue());

        assertEquals(rangeStart,
                ((NxActionRegLoadNotifDirectStatisticsUpdateApplyActionsCase) actionResult4).getNxRegLoad().getDst()
                        .getStart());
        assertEquals(rangeEnd,
                ((NxActionRegLoadNotifDirectStatisticsUpdateApplyActionsCase) actionResult4).getNxRegLoad().getDst()
                        .getEnd());
        assertEquals(Uint64.valueOf(rangeValue),
                ((NxActionRegLoadNotifDirectStatisticsUpdateApplyActionsCase) actionResult4).getNxRegLoad().getValue());

        assertEquals(rangeStart,
                ((NxActionRegLoadNotifDirectStatisticsUpdateWriteActionsCase) actionResult5).getNxRegLoad().getDst()
                        .getStart());
        assertEquals(rangeEnd,
                ((NxActionRegLoadNotifDirectStatisticsUpdateWriteActionsCase) actionResult5).getNxRegLoad().getDst()
                        .getEnd());
        assertEquals(Uint64.valueOf(rangeValue),
                ((NxActionRegLoadNotifDirectStatisticsUpdateWriteActionsCase) actionResult5).getNxRegLoad().getValue());
    }
}