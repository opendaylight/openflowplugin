/*
 * Copyright (c) 2014 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalSetFieldCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalSetMplsTtlCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalSetNwDstCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalSetNwTtlCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalSetQueueCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases.OfToSalStripVlanCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionResponseConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorProcessor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ParametrizedConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.ActionChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;

/**
 * @author usha@ericsson Action List:This class takes data from SAL layer and
 *         converts into OF Data
 * @author avishnoi@in.ibm.com Added convertor for OF bucket actions to SAL
 *         actions
 */
public final class ActionResponseConvertor implements ParametrizedConvertor<
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
            .addCase(new OfToSalPopPbbCase())
            .addCase(new OfToSalPopVlanCase())
            .addCase(new OfToSalPushMplsCase())
            .addCase(new OfToSalPushPbbCase())
            .addCase(new OfToSalPushVlanCase())
            .addCase(new OfToSalSetFieldCase())
            .addCase(new OfToSalSetMplsTtlCase())
            .addCase(new OfToSalSetNwDstCase())
            .addCase(new OfToSalSetNwTtlCase())
            .addCase(new OfToSalSetQueueCase())
            .addCase(new OfToSalStripVlanCase());

    @Override
    public Class<?> getType() {
        return Action.class;
    }

    /**
     * Method to convert OF actions associated with bucket to SAL Actions.
     *
     * @param source action list
     * @return List of converted SAL Actions.
     */
    @Override
    public List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> convert(List<Action> source, ActionResponseConvertorData data) {
        final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> result = new ArrayList<>();
        final OpenflowVersion ofVersion = OpenflowVersion.get(data.getVersion());

        // Iterate over Openflow actions, run them through tokenizer and then add them to list of converted actions
        if (source != null) {
            for (final Action action : source) {
                final Optional<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> convertedAction = PROCESSOR.process(action.getActionChoice(), data);

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
