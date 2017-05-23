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
import java.util.Objects;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.duration.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.multipart.reply.flow.FlowStats;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * Converts flow related statistics messages coming from openflow switch to MD-SAL messages.
 *
 * Example usage:
 * <pre>
 * {@code
 * VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData(version);
 * data.setDatapathId(datapathId);
 * Optional<List<FlowAndStatisticsMapList>> salFlowStats = convertorManager.convert(ofFlowStats, data);
 * }
 * </pre>
 */
public class FlowStatsResponseConvertor extends Convertor<List<FlowStats>, List<FlowAndStatisticsMapList>, FlowStatsResponseConvertorData> {

    private static final Set<Class<? extends DataContainer>> TYPES = Collections.singleton(FlowStats.class);

    /**
     * Method wraps openflow 1.0 actions list to Apply Action Instructions
     *
     * @param actionsList list of action
     * @param ipProtocol ip protocol
     * @return OF10 actions as an instructions
     */
    private Instructions wrapOF10ActionsToInstruction(List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action> actionsList,
                                                      final short version,
                                                      final Short ipProtocol) {
        ActionResponseConvertorData actionResponseConvertorData = new ActionResponseConvertorData(version);
        actionResponseConvertorData.setActionPath(ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION);
        actionResponseConvertorData.setIpProtocol(ipProtocol);

        InstructionsBuilder instructionsBuilder = new InstructionsBuilder();
        List<Instruction> salInstructionList = new ArrayList<>();

        ApplyActionsCaseBuilder applyActionsCaseBuilder = new ApplyActionsCaseBuilder();
        ApplyActionsBuilder applyActionsBuilder = new ApplyActionsBuilder();

        final Optional<List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action>> actions = getConvertorExecutor().convert(
                actionsList, actionResponseConvertorData);

        applyActionsBuilder.setAction(FlowConvertorUtil.wrapActionList(actions.orElse(Collections.emptyList())));
        applyActionsCaseBuilder.setApplyActions(applyActionsBuilder.build());

        InstructionBuilder instBuilder = new InstructionBuilder();
        instBuilder.setInstruction(applyActionsCaseBuilder.build());
        instBuilder.setKey(new InstructionKey(0));
        instBuilder.setOrder(0);
        salInstructionList.add(instBuilder.build());

        instructionsBuilder.setInstruction(salInstructionList);
        return instructionsBuilder.build();
    }

    @Override
    public Collection<Class<? extends DataContainer>> getTypes() {
        return TYPES;
    }

    @Override
    public List<FlowAndStatisticsMapList> convert(List<FlowStats> source, FlowStatsResponseConvertorData data) {
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

            Short ipProtocol = null;

            if (flowStats.getMatchV10() != null) {
                final Optional<MatchBuilder> matchBuilderOptional = getConvertorExecutor().convert(flowStats.getMatchV10(), data);

                if (matchBuilderOptional.isPresent()) {
                    if (Objects.nonNull(matchBuilderOptional.get().getIpMatch())) {
                        ipProtocol = matchBuilderOptional.get().getIpMatch().getIpProtocol();
                    }

                    salFlowStatsBuilder.setMatch(matchBuilderOptional.get().build());
                }

                if (flowStats.getAction() != null && flowStats.getAction().size() != 0) {
                    salFlowStatsBuilder.setInstructions(wrapOF10ActionsToInstruction(flowStats.getAction(), data.getVersion(), ipProtocol));
                }
            }

            if (flowStats.getMatch() != null) {
                final Optional<MatchBuilder> matchBuilderOptional = getConvertorExecutor().convert(flowStats.getMatch(), data);

                if (matchBuilderOptional.isPresent()) {
                    final MatchBuilder matchBuilder = matchBuilderOptional.get();

                    final AugmentTuple<Match> matchExtensionWrap =
                            MatchExtensionHelper.processAllExtensions(
                                    flowStats.getMatch().getMatchEntry(),
                                    OpenflowVersion.get(data.getVersion()),
                                    data.getMatchPath());

                    if (matchExtensionWrap != null) {
                        matchBuilder.addAugmentation(matchExtensionWrap.getAugmentationClass(), matchExtensionWrap.getAugmentationObject());
                    }

                    salFlowStatsBuilder.setMatch(matchBuilder.build());
                }


                salFlowStatsBuilder.setFlags(
                        new FlowModFlags(flowStats.getFlags().isOFPFFCHECKOVERLAP(),
                                flowStats.getFlags().isOFPFFRESETCOUNTS(),
                                flowStats.getFlags().isOFPFFNOPKTCOUNTS(),
                                flowStats.getFlags().isOFPFFNOBYTCOUNTS(),
                                flowStats.getFlags().isOFPFFSENDFLOWREM()));
            }

            if (flowStats.getInstruction() != null) {
                final VersionConvertorData simpleConvertorData = new VersionConvertorData(data.getVersion());
                final Optional<Instructions> instructions = getConvertorExecutor().convert(
                        flowStats.getInstruction(), simpleConvertorData);

                salFlowStatsBuilder.setInstructions(instructions.orElse(new InstructionsBuilder()
                        .setInstruction(Collections.emptyList()).build()));
            }

            result.add(salFlowStatsBuilder.build());
        }

        return result;
    }
}
