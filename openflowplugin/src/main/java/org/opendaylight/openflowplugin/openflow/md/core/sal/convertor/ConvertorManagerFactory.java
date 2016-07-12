/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.ActionConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.ActionResponseConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow.FlowConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow.FlowInstructionResponseConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow.FlowStatsResponseConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchResponseConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchV10ResponseConvertor;

/**
 * Convertor manager factory.
 */
public class ConvertorManagerFactory {
    /**
     * Create default convertor manager.
     *
     * @return the convertor manager
     */
    public static ConvertorManager createDefaultManager() {
        final TableFeaturesConvertor tableFeaturesConvertor = new TableFeaturesConvertor();
        final TableFeaturesResponseConvertor tableFeaturesResponseConvertor = new TableFeaturesResponseConvertor();
        final MeterConvertor meterConvertor = new MeterConvertor();
        final MeterStatsResponseConvertor meterStatsResponseConvertor = new MeterStatsResponseConvertor();
        final MeterConfigStatsResponseConvertor meterConfigStatsResponseConvertor = new MeterConfigStatsResponseConvertor();
        final PortConvertor portConvertor = new PortConvertor();
        final MatchResponseConvertor matchResponseConvertor = new MatchResponseConvertor();
        final MatchV10ResponseConvertor matchV10ResponseConvertor = new MatchV10ResponseConvertor();
        final ActionConvertor actionConvertor = new ActionConvertor();
        final ActionResponseConvertor actionResponseConvertor = new ActionResponseConvertor();
        final GroupConvertor groupConvertor = new GroupConvertor();
        final GroupDescStatsResponseConvertor groupDescStatsResponseConvertor = new GroupDescStatsResponseConvertor();
        final GroupStatsResponseConvertor groupStatsResponseConvertor = new GroupStatsResponseConvertor();
        final PacketOutConvertor packetOutConvertor = new PacketOutConvertor();
        final FlowConvertor flowConvertor = new FlowConvertor();
        final FlowInstructionResponseConvertor flowInstructionResponseConvertor = new FlowInstructionResponseConvertor();
        final FlowStatsResponseConvertor flowStatsResponseConvertor = new FlowStatsResponseConvertor();

        return new ConvertorManager(OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3)
                .registerConvertor(OFConstants.OFP_VERSION_1_0, tableFeaturesConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_3, tableFeaturesConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_0, tableFeaturesResponseConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_3, tableFeaturesResponseConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_0, meterConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_3, meterConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_0, meterStatsResponseConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_3, meterStatsResponseConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_0, meterConfigStatsResponseConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_3, meterConfigStatsResponseConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_0, portConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_3, portConvertor)
                // TODO: Add MatchConvertorImpl and MatchConvertorV10Impl
                .registerConvertor(OFConstants.OFP_VERSION_1_3, matchResponseConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_0, matchV10ResponseConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_0, actionConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_3, actionConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_0, actionResponseConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_3, actionResponseConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_0, groupConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_3, groupConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_0, groupDescStatsResponseConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_3, groupDescStatsResponseConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_0, groupStatsResponseConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_3, groupStatsResponseConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_0, packetOutConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_3, packetOutConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_0, flowConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_3, flowConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_0, flowInstructionResponseConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_3, flowInstructionResponseConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_0, flowStatsResponseConvertor)
                .registerConvertor(OFConstants.OFP_VERSION_1_3, flowStatsResponseConvertor);
    }
}
