/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.converter;

import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.converter.action.ActionConverter;
import org.opendaylight.openflowplugin.openflow.md.core.sal.converter.action.ActionResponseConverter;
import org.opendaylight.openflowplugin.openflow.md.core.sal.converter.flow.FlowConverter;
import org.opendaylight.openflowplugin.openflow.md.core.sal.converter.flow.FlowInstructionResponseConverter;
import org.opendaylight.openflowplugin.openflow.md.core.sal.converter.flow.FlowStatsResponseConverter;
import org.opendaylight.openflowplugin.openflow.md.core.sal.converter.match.MatchResponseConverter;
import org.opendaylight.openflowplugin.openflow.md.core.sal.converter.match.MatchV10ResponseConverter;

/**
 * Convertor manager factory.
 */
public class ConvertorManagerFactory {
    /**
     * Create default converter manager.
     *
     * @return the converter manager
     */
    public static ConverterManager createDefaultManager() {
        final TableFeaturesConverter tableFeaturesConvertor = new TableFeaturesConverter();
        final TableFeaturesResponseConverter tableFeaturesResponseConvertor = new TableFeaturesResponseConverter();
        final MeterConverter meterConvertor = new MeterConverter();
        final MeterStatsResponseConverter meterStatsResponseConvertor = new MeterStatsResponseConverter();
        final MeterConfigStatsResponseConverter meterConfigStatsResponseConvertor = new MeterConfigStatsResponseConverter();
        final PortConverter portConvertor = new PortConverter();
        final MatchResponseConverter matchResponseConvertor = new MatchResponseConverter();
        final MatchV10ResponseConverter matchV10ResponseConvertor = new MatchV10ResponseConverter();
        final ActionConverter actionConvertor = new ActionConverter();
        final ActionResponseConverter actionResponseConvertor = new ActionResponseConverter();
        final GroupConverter groupConvertor = new GroupConverter();
        final GroupDescStatsResponseConverter groupDescStatsResponseConvertor = new GroupDescStatsResponseConverter();
        final GroupStatsResponseConverter groupStatsResponseConvertor = new GroupStatsResponseConverter();
        final PacketOutConverter packetOutConvertor = new PacketOutConverter();
        final FlowConverter flowConvertor = new FlowConverter();
        final FlowInstructionResponseConverter flowInstructionResponseConvertor = new FlowInstructionResponseConverter();
        final FlowStatsResponseConverter flowStatsResponseConvertor = new FlowStatsResponseConverter();

        return new ConverterManager(OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3)
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
                // TODO: Add MatchConvertorImpl and MatchConvertorV10Impl
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
