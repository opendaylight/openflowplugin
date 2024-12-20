/*
 * Copyright (c) 2013, 2015 IBM Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.extension.api.AugmentTuple;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.openflow.md.core.extension.MatchExtensionHelper;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionResponseConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.FlowStatsResponseConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.duration.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.multipart.reply.flow.FlowStats;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Converts flow related statistics messages coming from openflow switch to MD-SAL messages.
 *
 * <p>Example usage:
 * <pre>
 * {@code
 * VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData(version);
 * data.setDatapathId(datapathId);
 * Optional<List<FlowAndStatisticsMapList>> salFlowStats = convertorManager.convert(ofFlowStats, data);
 * }
 * </pre>
 */
public class FlowStatsResponseConvertor extends Convertor<List<FlowStats>, List<FlowAndStatisticsMapList>,
        FlowStatsResponseConvertorData> {

    private static final Set<Class<?>> TYPES = Collections.singleton(FlowStats.class);

    /**
     * Method wraps openflow 1.0 actions list to Apply Action Instructions.
     *
     * @param actionsList list of action
     * @param ipProtocol ip protocol
     * @return OF10 actions as an instructions
     */
    private Instructions wrapOF10ActionsToInstruction(
            final List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions
                .grouping.Action> actionsList,
            final Uint8 version, final Uint8 ipProtocol) {
        ActionResponseConvertorData actionResponseConvertorData = new ActionResponseConvertorData(version);
        actionResponseConvertorData.setActionPath(ActionPath.FLOWS_STATISTICS_UPDATE_WRITE_ACTIONS);
        actionResponseConvertorData.setIpProtocol(ipProtocol);

        ApplyActionsCaseBuilder applyActionsCaseBuilder = new ApplyActionsCaseBuilder();
        ApplyActionsBuilder applyActionsBuilder = new ApplyActionsBuilder();

        final Optional<List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action>>
            actions = getConvertorExecutor().convert(actionsList, actionResponseConvertorData);

        applyActionsBuilder.setAction(FlowConvertorUtil.wrapActionList(actions.orElse(Collections.emptyList())));
        applyActionsCaseBuilder.setApplyActions(applyActionsBuilder.build());

        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setInstruction(applyActionsCaseBuilder.build())
                .setOrder(0)
                .build()))
            .build();
    }

    @Override
    public Collection<Class<?>> getTypes() {
        return TYPES;
    }

    @Override
    public List<FlowAndStatisticsMapList> convert(final List<FlowStats> source,
            final FlowStatsResponseConvertorData data) {
        final List<FlowAndStatisticsMapList> result = new ArrayList<>();

        for (FlowStats flowStats : source) {
            // Convert Openflow switch specific flow statistics to the MD-SAL format flow statistics
            FlowAndStatisticsMapListBuilder salFlowStatsBuilder = new FlowAndStatisticsMapListBuilder();
            salFlowStatsBuilder.setByteCount(new Counter64(flowStats.getByteCount()));

            if (flowStats.getCookie() != null) {
                salFlowStatsBuilder.setCookie(new FlowCookie(flowStats.getCookie()));
            }

            DurationBuilder time = new DurationBuilder();
            time.setSecond(new Counter32(flowStats.getDurationSec()));
            time.setNanosecond(new Counter32(flowStats.getDurationNsec()));
            salFlowStatsBuilder.setDuration(time.build());

            salFlowStatsBuilder.setHardTimeout(flowStats.getHardTimeout());
            salFlowStatsBuilder.setIdleTimeout(flowStats.getIdleTimeout());
            salFlowStatsBuilder.setPacketCount(new Counter64(flowStats.getPacketCount()));
            salFlowStatsBuilder.setPriority(flowStats.getPriority());
            salFlowStatsBuilder.setTableId(flowStats.getTableId());

            if (flowStats.getMatchV10() != null) {
                final Optional<MatchBuilder> matchBuilderOptional = getConvertorExecutor().convert(
                        flowStats.getMatchV10(), data);
                Uint8 ipProtocol = null;

                if (matchBuilderOptional.isPresent()) {
                    final MatchBuilder matchBuilder = matchBuilderOptional.orElseThrow();
                    final IpMatch ipMatch = matchBuilder.getIpMatch();
                    if (ipMatch != null) {
                        ipProtocol = ipMatch.getIpProtocol();
                    }

                    salFlowStatsBuilder.setMatch(matchBuilder.build());
                }

                if (flowStats.getAction() != null && flowStats.getAction().size() != 0) {
                    salFlowStatsBuilder.setInstructions(wrapOF10ActionsToInstruction(flowStats.getAction(),
                            data.getVersion(), ipProtocol));
                }
            }

            if (flowStats.getMatch() != null) {
                final Optional<MatchBuilder> matchBuilderOptional = getConvertorExecutor().convert(
                        flowStats.getMatch(), data);

                if (matchBuilderOptional.isPresent()) {
                    final MatchBuilder matchBuilder = matchBuilderOptional.orElseThrow();

                    final AugmentTuple<Match> matchExtensionWrap =
                            MatchExtensionHelper.processAllExtensions(
                                    flowStats.getMatch().nonnullMatchEntry(),
                                    OpenflowVersion.get(data.getVersion()),
                                    data.getMatchPath());

                    if (matchExtensionWrap != null) {
                        matchBuilder.addAugmentation(matchExtensionWrap.getAugmentationObject());
                    }

                    salFlowStatsBuilder.setMatch(matchBuilder.build());
                }


                salFlowStatsBuilder.setFlags(
                        new FlowModFlags(flowStats.getFlags().getOFPFFCHECKOVERLAP(),
                                flowStats.getFlags().getOFPFFRESETCOUNTS(),
                                flowStats.getFlags().getOFPFFNOPKTCOUNTS(),
                                flowStats.getFlags().getOFPFFNOBYTCOUNTS(),
                                flowStats.getFlags().getOFPFFSENDFLOWREM()));
            }

            if (flowStats.getInstruction() != null) {
                final VersionConvertorData simpleConvertorData = new VersionConvertorData(data.getVersion());
                final Optional<Instructions> instructions = getConvertorExecutor().convert(
                        flowStats.getInstruction(), simpleConvertorData);

                salFlowStatsBuilder.setInstructions(instructions.orElse(new InstructionsBuilder()
                        .setInstruction(Collections.emptyMap()).build()));
            }

            result.add(salFlowStatsBuilder.build());
        }

        return result;
    }
}
