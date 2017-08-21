/*
 * Copyright (c) 2014 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.protocol.converter.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.api.openflow.protocol.converter.Converter;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.OfToSalCopyTtlInCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.OfToSalCopyTtlOutCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.OfToSalDecMplsTtlCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.OfToSalDecNwTtlCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.OfToSalGroupCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.OfToSalOutputActionCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.OfToSalPopMplsCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.OfToSalPopPbbCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.OfToSalPopVlanCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.OfToSalPushMplsCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.OfToSalPushPbbCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.OfToSalPushVlanCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.OfToSalSetDlDstCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.OfToSalSetDlSrcCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.OfToSalSetFieldCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.OfToSalSetMplsTtlCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.OfToSalSetNwDstCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.OfToSalSetNwSrcCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.OfToSalSetNwTosCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.OfToSalSetNwTtlCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.OfToSalSetQueueCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.OfToSalSetTpDstCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.OfToSalSetTpSrcCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.OfToSalSetVlanIdCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.OfToSalSetVlanPcpCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.OfToSalStripVlanCase;
import org.opendaylight.openflowplugin.protocol.converter.data.ActionResponseConverterData;
import org.opendaylight.openflowplugin.protocol.converter.common.ConvertorProcessor;
import org.opendaylight.openflowplugin.protocol.extension.ActionExtensionHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.ActionChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;

/**
 * Converts OF actions associated with bucket to SAL Actions.
 *
 * Example usage:
 * <pre>
 * {@code
 * ActionResponseConverterData data = new ActionResponseConverterData(version);
 * data.setActionPath(actionPath);
 * Optional<List<Action>> salActions = converterManager.convert(ofActions, data);
 * }
 * </pre>
 */
public final class ActionResponseConverter extends Converter<
        List<Action>,
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action>,
        ActionResponseConverterData> {

    private static final ConvertorProcessor<ActionChoice, org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, ActionResponseConverterData> PROCESSOR = new ConvertorProcessor<
            ActionChoice,
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action,
            ActionResponseConverterData>()
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
    private static final Set<Class<?>> TYPES = Collections.singleton(Action.class);
    private final ExtensionConverterProvider extensionConverterProvider;

    public ActionResponseConverter(final ExtensionConverterProvider extensionConverterProvider) {
        this.extensionConverterProvider = extensionConverterProvider;
    }

    @Override
    public Collection<Class<?>> getTypes() {
        return TYPES;
    }

    @Override
    public List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> convert(List<Action> source, ActionResponseConverterData data) {
        final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> result = new ArrayList<>();
        final OpenflowVersion ofVersion = OpenflowVersion.get(data.getVersion());

        // Iterate over Openflow actions, run them through tokenizer and then add them to list of converted actions
        if (source != null) {
            for (final Action action : source) {
                final Optional<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> convertedAction = PROCESSOR
                        .process(action.getActionChoice(), data, getConverterExecutor());

                if (convertedAction.isPresent()) {
                    result.add(convertedAction.get());
                } else {
                    /*
                     * TODO: EXTENSION PROPOSAL (action, OFJava to MD-SAL)
                     * - we might also need a way on how to identify exact type of augmentation to be
                     *   used as match can be bound to multiple models
                     */
                    org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action processedAction =
                            ActionExtensionHelper.processAlienAction(action, ofVersion, data.getActionPath(),
                                    extensionConverterProvider);

                    if (processedAction != null) {
                        result.add(processedAction);
                    }
                }
            }
        }

        return result;
    }
}
