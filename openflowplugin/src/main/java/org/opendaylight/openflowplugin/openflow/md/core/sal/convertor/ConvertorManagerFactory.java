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
        final ConvertorManager convertorManager = new ConvertorManager();

        final TableFeaturesConvertor tableFeaturesConvertor = new TableFeaturesConvertor();
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_0, tableFeaturesConvertor);
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_3, tableFeaturesConvertor);

        final TableFeaturesResponseConvertor tableFeaturesResponseConvertor = new TableFeaturesResponseConvertor();
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_0, tableFeaturesResponseConvertor);
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_3, tableFeaturesResponseConvertor);

        final MeterConvertor meterConvertor = new MeterConvertor();
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_0, meterConvertor);
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_3, meterConvertor);

        final MeterStatsResponseConvertor meterStatsResponseConvertor = new MeterStatsResponseConvertor();
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_0, meterStatsResponseConvertor);
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_3, meterStatsResponseConvertor);

        final MeterConfigStatsResponseConvertor meterConfigStatsResponseConvertor = new MeterConfigStatsResponseConvertor();
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_0, meterConfigStatsResponseConvertor);
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_3, meterConfigStatsResponseConvertor);

        final PortConvertor portConvertor = new PortConvertor();
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_0, portConvertor);
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_3, portConvertor);

        final MatchResponseConvertor matchResponseConvertor = new MatchResponseConvertor();
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_0, matchResponseConvertor);
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_3, matchResponseConvertor);

        final MatchV10ResponseConvertor matchV10ResponseConvertor = new MatchV10ResponseConvertor();
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_0, matchV10ResponseConvertor);
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_3, matchV10ResponseConvertor);

        final ActionConvertor actionConvertor = new ActionConvertor();
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_0, actionConvertor);
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_3, actionConvertor);

        final ActionResponseConvertor actionResponseConvertor = new ActionResponseConvertor();
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_0, actionResponseConvertor);
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_3, actionResponseConvertor);

        final GroupConvertor groupConvertor = new GroupConvertor();
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_0, groupConvertor);
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_3, groupConvertor);

        final GroupDescStatsResponseConvertor groupDescStatsResponseConvertor = new GroupDescStatsResponseConvertor();
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_0, groupDescStatsResponseConvertor);
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_3, groupDescStatsResponseConvertor);

        final GroupStatsResponseConvertor groupStatsResponseConvertor = new GroupStatsResponseConvertor();
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_0, groupStatsResponseConvertor);
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_3, groupStatsResponseConvertor);

        final PacketOutConvertor packetOutConvertor = new PacketOutConvertor();
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_0, packetOutConvertor);
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_3, packetOutConvertor);

        final FlowConvertor flowConvertor = new FlowConvertor();
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_0, flowConvertor);
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_3, flowConvertor);

        final FlowInstructionResponseConvertor flowInstructionResponseConvertor = new FlowInstructionResponseConvertor();
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_0, flowInstructionResponseConvertor);
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_3, flowInstructionResponseConvertor);

        final FlowStatsResponseConvertor flowStatsResponseConvertor = new FlowStatsResponseConvertor();
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_0, flowStatsResponseConvertor);
        convertorManager.registerConvertor(OFConstants.OFP_VERSION_1_3, flowStatsResponseConvertor);

        return convertorManager;
    }
}
