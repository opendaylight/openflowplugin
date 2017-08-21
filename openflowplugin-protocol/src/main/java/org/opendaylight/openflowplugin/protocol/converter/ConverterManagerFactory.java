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
import org.opendaylight.openflowplugin.protocol.converter.flow.FlowFlagsConverter;
import org.opendaylight.openflowplugin.protocol.converter.flow.FlowFlagsV10Converter;
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
        final TableFeaturesConverter tableFeaturesConverter = new TableFeaturesConverter();
        final TableFeaturesResponseConverter tableFeaturesResponseConverter = new TableFeaturesResponseConverter();
        final MeterConverter meterConverter = new MeterConverter();
        final MeterStatsResponseConverter meterStatsResponseConverter = new MeterStatsResponseConverter();
        final MeterConfigStatsResponseConverter meterConfigStatsResponseConverter = new MeterConfigStatsResponseConverter();
        final PortConverter portConverter = new PortConverter();
        final MatchResponseConverter matchResponseConverter = new MatchResponseConverter();
        final MatchV10ResponseConverter matchV10ResponseConverter = new MatchV10ResponseConverter();
        final ActionConverter actionConverter = new ActionConverter(extensionConverterProvider);
        final ActionResponseConverter actionResponseConverter = new ActionResponseConverter(extensionConverterProvider);
        final GroupConverter groupConverter = new GroupConverter();
        final GroupDescStatsResponseConverter groupDescStatsResponseConverter = new GroupDescStatsResponseConverter();
        final GroupStatsResponseConverter groupStatsResponseConverter = new GroupStatsResponseConverter();
        final PacketOutConverter packetOutConverter = new PacketOutConverter();
        final FlowConverter flowConverter = new FlowConverter(extensionConverterProvider);
        final FlowInstructionResponseConverter flowInstructionResponseConverter = new FlowInstructionResponseConverter();
        final FlowStatsResponseConverter flowStatsResponseConverter = new FlowStatsResponseConverter(extensionConverterProvider);
        final MatchConverter matchConverter = new MatchConverter(extensionConverterProvider);
        final MatchV10Converter matchConverterV10 = new MatchV10Converter();
        final FlowFlagsConverter flowFlagsConverter = new FlowFlagsConverter();
        final FlowFlagsV10Converter flowFlagsConverterV10 = new FlowFlagsV10Converter();

        return new ConverterManagerImpl(OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3)
                .registerConverter(OFConstants.OFP_VERSION_1_0, flowFlagsConverterV10)
                .registerConverter(OFConstants.OFP_VERSION_1_3, flowFlagsConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_0, matchConverterV10)
                .registerConverter(OFConstants.OFP_VERSION_1_3, matchConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_0, tableFeaturesConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_3, tableFeaturesConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_0, tableFeaturesResponseConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_3, tableFeaturesResponseConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_0, meterConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_3, meterConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_0, meterStatsResponseConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_3, meterStatsResponseConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_0, meterConfigStatsResponseConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_3, meterConfigStatsResponseConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_0, portConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_3, portConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_3, matchResponseConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_0, matchV10ResponseConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_0, actionConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_3, actionConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_0, actionResponseConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_3, actionResponseConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_0, groupConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_3, groupConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_0, groupDescStatsResponseConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_3, groupDescStatsResponseConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_0, groupStatsResponseConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_3, groupStatsResponseConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_0, packetOutConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_3, packetOutConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_0, flowConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_3, flowConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_0, flowInstructionResponseConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_3, flowInstructionResponseConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_0, flowStatsResponseConverter)
                .registerConverter(OFConstants.OFP_VERSION_1_3, flowStatsResponseConverter);
    }
}