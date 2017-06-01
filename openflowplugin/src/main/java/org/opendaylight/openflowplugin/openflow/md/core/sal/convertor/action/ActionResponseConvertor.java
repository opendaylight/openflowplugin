/*
 * Copyright (c) 2014 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.core.extension.ActionExtensionHelper;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalCopyTtlInCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalCopyTtlOutCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalDecMplsTtlCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalDecNwTtlCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalGroupCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalOutputActionCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalPopMplsCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalPopPbbCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalPopVlanCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalPushMplsCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalPushPbbCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalPushVlanCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalSetDlDstCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalSetDlSrcCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalSetFieldCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalSetMplsTtlCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalSetNwDstCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalSetNwSrcCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalSetNwTosCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalSetNwTtlCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalSetQueueCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalSetTpDstCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalSetTpSrcCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalSetVlanIdCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalSetVlanPcpCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalStripVlanCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionResponseConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorProcessor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.ActionChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * Converts OF actions associated with bucket to SAL Actions.
 *
 * Example usage:
 * <pre>
 * {@code
 * ActionResponseConvertorData data = new ActionResponseConvertorData(version);
 * data.setActionPath(actionPath);
 * Optional<List<Action>> salActions = convertorManager.convert(ofActions, data);
 * }
 * </pre>
 */
public final class ActionResponseConvertor extends Convertor<
        List<Action>,
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action>,
        ActionResponseConvertorData> {

    private static final ConvertorProcessor<ActionChoice, org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, ActionResponseConvertorData> PROCESSOR = new ConvertorProcessor<
            ActionChoice,
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action,
            ActionResponseConvertorData>()
            // Add rules for each action type
            .addCase(new OfToSalCopyTtlInCase())
            .addCase(new OfToSalCopyTtlOutCase())
            .addCase(new OfToSalDecMplsTtlCase())
            .addCase(new OfToSalDecNwTtlCase())
            .addCase(new OfToSalGroupCase())
            .addCase(new OfToSalOutputActionCase())
            .addCase(new OfToSalPopMplsCase())
            .addCase(new OfToSalPopVlanCase())
            .addCase(new OfToSalPushMplsCase())
            .addCase(new OfToSalPushPbbCase())
            .addCase(new OfToSalPushVlanCase())
            .addCase(new OfToSalSetMplsTtlCase())
            .addCase(new OfToSalSetNwTtlCase())
            .addCase(new OfToSalSetQueueCase())
            // OpenFlow 1.3 specific actions
            .addCase(new OfToSalPopPbbCase())
            .addCase(new OfToSalSetFieldCase())
            // OpenFlow 1.0 specific actions
            .addCase(new OfToSalSetNwSrcCase())
            .addCase(new OfToSalSetNwDstCase())
            .addCase(new OfToSalSetNwTosCase())
            .addCase(new OfToSalSetDlSrcCase())
            .addCase(new OfToSalSetDlDstCase())
            .addCase(new OfToSalSetTpSrcCase())
            .addCase(new OfToSalSetTpDstCase())
            .addCase(new OfToSalSetVlanPcpCase())
            .addCase(new OfToSalSetVlanIdCase())
            .addCase(new OfToSalStripVlanCase());
    private static final Set<Class<? extends DataContainer>> TYPES = Collections.singleton(Action.class);

    @Override
    public Collection<Class<? extends DataContainer>> getTypes() {
        return TYPES;
    }

    @Override
    public List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> convert(List<Action> source, ActionResponseConvertorData data) {
        final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> result = new ArrayList<>();
        final OpenflowVersion ofVersion = OpenflowVersion.get(data.getVersion());

        // Iterate over Openflow actions, run them through tokenizer and then add them to list of converted actions
        if (source != null) {
            for (final Action action : source) {
                final Optional<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> convertedAction = PROCESSOR.process(action.getActionChoice(), data, getConvertorExecutor());

                if (convertedAction.isPresent()) {
                    result.add(convertedAction.get());
                } else {
                    /**
                     * TODO: EXTENSION PROPOSAL (action, OFJava to MD-SAL)
                     * - we might also need a way on how to identify exact type of augmentation to be
                     *   used as match can be bound to multiple models
                     */
                    org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action processedAction =
                            ActionExtensionHelper.processAlienAction(action, ofVersion, data.getActionPath());

                    if (processedAction != null) {
                        result.add(processedAction);
                    }
                }
            }
        }

        return result;
    }
}
