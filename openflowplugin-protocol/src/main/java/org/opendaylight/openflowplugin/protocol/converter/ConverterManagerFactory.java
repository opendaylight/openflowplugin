/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.protocol.converter;

import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.protocol.converter.action.ActionConverter;
import org.opendaylight.openflowplugin.protocol.converter.action.ActionResponseConverter;
import org.opendaylight.openflowplugin.protocol.converter.flow.FlowConverter;
import org.opendaylight.openflowplugin.protocol.converter.flow.FlowInstructionResponseConverter;
import org.opendaylight.openflowplugin.protocol.converter.flow.FlowStatsResponseConverter;
import org.opendaylight.openflowplugin.protocol.converter.flow.flowflag.FlowFlagsConverter;
import org.opendaylight.openflowplugin.protocol.converter.flow.flowflag.FlowFlagsV10Converter;
import org.opendaylight.openflowplugin.protocol.converter.match.MatchConverter;
import org.opendaylight.openflowplugin.protocol.converter.match.MatchV10Converter;
import org.opendaylight.openflowplugin.protocol.converter.match.MatchResponseConverter;
import org.opendaylight.openflowplugin.protocol.converter.match.MatchV10ResponseConverter;

/**
 * Converter manager factory.
 */
public class ConverterManagerFactory {
    /**
     * Create default converter manager.
     *
     * @return the converter manager
     */
    public ConverterManagerImpl newInstance(final ExtensionConverterProvider extensionConverterProvider) {
        final TableFeaturesConverter tableFeaturesConvertor = new TableFeaturesConverter();
        final TableFeaturesResponseConverter tableFeaturesResponseConvertor = new TableFeaturesResponseConverter();
        final MeterConverter meterConvertor = new MeterConverter();
        final MeterStatsResponseConverter meterStatsResponseConvertor = new MeterStatsResponseConverter();
        final MeterConfigStatsResponseConverter meterConfigStatsResponseConvertor = new MeterConfigStatsResponseConverter();
        final PortConverter portConvertor = new PortConverter();
        final MatchResponseConverter matchResponseConvertor = new MatchResponseConverter();
        final MatchV10ResponseConverter matchV10ResponseConvertor = new MatchV10ResponseConverter();
        final ActionConverter actionConvertor = new ActionConverter(extensionConverterProvider);
        final ActionResponseConverter actionResponseConvertor = new ActionResponseConverter(extensionConverterProvider);
        final GroupConverter groupConvertor = new GroupConverter();
        final GroupDescStatsResponseConverter groupDescStatsResponseConvertor = new GroupDescStatsResponseConverter();
        final GroupStatsResponseConverter groupStatsResponseConvertor = new GroupStatsResponseConverter();
        final PacketOutConverter packetOutConvertor = new PacketOutConverter();
        final FlowConverter flowConvertor = new FlowConverter(extensionConverterProvider);
        final FlowInstructionResponseConverter flowInstructionResponseConvertor = new FlowInstructionResponseConverter();
        final FlowStatsResponseConverter flowStatsResponseConvertor = new FlowStatsResponseConverter(extensionConverterProvider);
        final MatchConverter matchConvertor = new MatchConverter(extensionConverterProvider);
        final MatchV10Converter matchConvertorV10 = new MatchV10Converter();
        final FlowFlagsConverter flowFlagsConvertor = new FlowFlagsConverter();
        final FlowFlagsV10Converter flowFlagsConvertorV10 = new FlowFlagsV10Converter();

        return new ConverterManagerImpl(OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3)
                .registerConverter(OFConstants.OFP_VERSION_1_0, flowFlagsConvertorV10)
                .registerConverter(OFConstants.OFP_VERSION_1_3, flowFlagsConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_0, matchConvertorV10)
                .registerConverter(OFConstants.OFP_VERSION_1_3, matchConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_0, tableFeaturesConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_3, tableFeaturesConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_0, tableFeaturesResponseConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_3, tableFeaturesResponseConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_0, meterConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_3, meterConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_0, meterStatsResponseConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_3, meterStatsResponseConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_0, meterConfigStatsResponseConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_3, meterConfigStatsResponseConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_0, portConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_3, portConvertor)
                // TODO: Add MatchConverter and MatchV10Converter
                .registerConverter(OFConstants.OFP_VERSION_1_3, matchResponseConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_0, matchV10ResponseConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_0, actionConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_3, actionConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_0, actionResponseConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_3, actionResponseConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_0, groupConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_3, groupConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_0, groupDescStatsResponseConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_3, groupDescStatsResponseConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_0, groupStatsResponseConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_3, groupStatsResponseConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_0, packetOutConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_3, packetOutConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_0, flowConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_3, flowConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_0, flowInstructionResponseConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_3, flowInstructionResponseConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_0, flowStatsResponseConvertor)
                .registerConverter(OFConstants.OFP_VERSION_1_3, flowStatsResponseConvertor);
    }
}