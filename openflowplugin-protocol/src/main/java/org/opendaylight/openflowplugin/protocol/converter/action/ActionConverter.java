/*
 * Copyright (c) 2014 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.protocol.converter.action;

import com.google.common.collect.Ordering;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.openflowplugin.api.openflow.protocol.converter.Converter;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfCopyTtlInCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfCopyTtlOutCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfDecMplsTtlCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfDecNwTtlCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfDropActionCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfGeneralExtensionGroupingCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfGroupActionCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfOutputActionCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfPopMplsActionCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfPopPbbActionCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfPopVlanActionCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfPopVlanActionV10Case;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfPushMplsActionCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfPushPbbActionCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfPushVlanActionCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfSetDlDstActionCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfSetDlDstActionV10Case;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfSetDlSrcActionCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfSetDlSrcActionV10Case;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfSetFieldCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfSetFieldV10Case;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfSetMplsTtlActionCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfSetNwDstActionCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfSetNwDstActionV10Case;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfSetNwSrcActionCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfSetNwSrcActionV10Case;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfSetNwTosActionCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfSetNwTosActionV10Case;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfSetNwTtlActionCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfSetQueueActionCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfSetTpDstActionCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfSetTpDstActionV10Case;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfSetTpSrcActionCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfSetTpSrcActionV10Case;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfSetVlanIdActionCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfSetVlanIdActionV10Case;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfSetVlanPcpActionCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfSetVlanPcpActionV10Case;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfStripVlanActionCase;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfStripVlanActionV10Case;
import org.opendaylight.openflowplugin.protocol.converter.action.cases.SalToOfVendorCodecCase;
import org.opendaylight.openflowplugin.protocol.converter.action.data.ActionConverterData;
import org.opendaylight.openflowplugin.protocol.converter.common.ConvertorProcessor;
import org.opendaylight.openflowplugin.protocol.converter.common.OrderComparator;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;

/**
 * Converts SAL actions into OF Library actions
 *
 * Example usage:
 * <pre>
 * {@code
 * ActionConverterData data = new ActionConverterData(version);
 * data.setDatapathId(datapathId);
 * data.setIpProtocol(ipProtocol);
 * Optional<List<Action>> ofActions = converterManager.convert(salActions, data);
 * }
 * </pre>
 */
public final class ActionConverter extends Converter<
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>,
        List<Action>,
        ActionConverterData> {

    private final ConvertorProcessor<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, Action, ActionConverterData> processor;
    private static final Set<Class<?>> TYPES = Collections.singleton(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action.class);
    private static final Ordering<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> ACTION_ORDERING =
            Ordering.from(OrderComparator.<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>build());

    public ActionConverter(final ExtensionConverterProvider extensionConverterProvider) {
        this.processor = new ConvertorProcessor<
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action,
            Action,
            ActionConverterData>()
            // Set default rule, what will be used if no rule match is found
            .setDefaultCase(new SalToOfVendorCodecCase(extensionConverterProvider))
            // Add rules for each action type
            .addCase(new SalToOfCopyTtlInCase())
            .addCase(new SalToOfCopyTtlOutCase())
            .addCase(new SalToOfDecMplsTtlCase())
            .addCase(new SalToOfDecNwTtlCase())
            .addCase(new SalToOfDropActionCase())
            .addCase(new SalToOfGroupActionCase())
            .addCase(new SalToOfOutputActionCase())
            .addCase(new SalToOfPopMplsActionCase())
            .addCase(new SalToOfPopPbbActionCase())
            .addCase(new SalToOfPopVlanActionCase())
            .addCase(new SalToOfPopVlanActionV10Case())
            .addCase(new SalToOfPushMplsActionCase())
            .addCase(new SalToOfPushPbbActionCase())
            .addCase(new SalToOfPushVlanActionCase())
            .addCase(new SalToOfSetFieldCase())
            .addCase(new SalToOfSetFieldV10Case())
            .addCase(new SalToOfSetMplsTtlActionCase())
            .addCase(new SalToOfSetNwTtlActionCase())
            .addCase(new SalToOfSetQueueActionCase())
            // Openflow 1.0 actions, with support for Openflow 1.3
            .addCase(new SalToOfSetVlanIdActionCase())
            .addCase(new SalToOfSetVlanIdActionV10Case())
            .addCase(new SalToOfSetVlanPcpActionCase())
            .addCase(new SalToOfSetVlanPcpActionV10Case())
            .addCase(new SalToOfStripVlanActionCase())
            .addCase(new SalToOfStripVlanActionV10Case())
            .addCase(new SalToOfSetDlSrcActionCase())
            .addCase(new SalToOfSetDlSrcActionV10Case())
            .addCase(new SalToOfSetDlDstActionCase())
            .addCase(new SalToOfSetDlDstActionV10Case())
            .addCase(new SalToOfSetNwSrcActionCase())
            .addCase(new SalToOfSetNwSrcActionV10Case())
            .addCase(new SalToOfSetNwDstActionCase())
            .addCase(new SalToOfSetNwDstActionV10Case())
            .addCase(new SalToOfSetTpSrcActionCase())
            .addCase(new SalToOfSetTpSrcActionV10Case())
            .addCase(new SalToOfSetTpDstActionCase())
            .addCase(new SalToOfSetTpDstActionV10Case())
            .addCase(new SalToOfSetNwTosActionCase())
            .addCase(new SalToOfSetNwTosActionV10Case())
            // Try to convert action grouping using converters from openflowplugin-extension
            .addCase(new SalToOfGeneralExtensionGroupingCase(extensionConverterProvider));
    }

    @Override
    public Collection<Class<?>> getTypes() {
        return TYPES;
    }

    @Override
    public List<Action> convert(List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> source, ActionConverterData data) {
        // Prepare list of converted actions
        final List<Action> result = new ArrayList<>();

        // Iterate over SAL actions, run them through tokenizer and then add them to list of converted actions
        if (source != null) {
            final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> sortedActions =
                    ACTION_ORDERING.sortedCopy(source);

            for (final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action action : sortedActions) {
                final Optional<Action> convertedAction = processor.process(action.getAction(), data, getConverterExecutor());
                convertedAction.ifPresent(result::add);
            }
        }

        return result;
    }
}
