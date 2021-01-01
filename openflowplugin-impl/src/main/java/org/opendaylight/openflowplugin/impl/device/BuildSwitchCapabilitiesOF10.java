/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowplugin.api.openflow.md.core.sal.BuildSwitchFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FeatureCapability;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowFeatureCapabilityArpMatchIp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowFeatureCapabilityFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowFeatureCapabilityIpReasm;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowFeatureCapabilityPortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowFeatureCapabilityQueueStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowFeatureCapabilityReserved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowFeatureCapabilityStp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowFeatureCapabilityTableStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.flow.node.SwitchFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.flow.node.SwitchFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;

/**
 * SwitchFeature builder for OF 1.0.
 */
public final class BuildSwitchCapabilitiesOF10 implements BuildSwitchFeatures {
    private static final BuildSwitchCapabilitiesOF10 INSTANCE = new BuildSwitchCapabilitiesOF10();

    private BuildSwitchCapabilitiesOF10() {
        // Hidden on purpose
    }

    /**
     * Get singleton instance.
     *
     * @return instance
     */
    public static BuildSwitchCapabilitiesOF10 getInstance() {
        return INSTANCE;
    }

    @Override
    public SwitchFeatures build(final GetFeaturesOutput features) {
        List<Class<? extends FeatureCapability>> capabilities = new ArrayList<>();
        if (features.getCapabilitiesV10().getOFPCARPMATCHIP()) {
            capabilities.add(FlowFeatureCapabilityArpMatchIp.class);
        }
        if (features.getCapabilitiesV10().getOFPCFLOWSTATS()) {
            capabilities.add(FlowFeatureCapabilityFlowStats.class);
        }
        if (features.getCapabilitiesV10().getOFPCIPREASM()) {
            capabilities.add(FlowFeatureCapabilityIpReasm.class);
        }
        if (features.getCapabilitiesV10().getOFPCPORTSTATS()) {
            capabilities.add(FlowFeatureCapabilityPortStats.class);
        }
        if (features.getCapabilitiesV10().getOFPCQUEUESTATS()) {
            capabilities.add(FlowFeatureCapabilityQueueStats.class);
        }
        if (features.getCapabilitiesV10().getOFPCRESERVED()) {
            capabilities.add(FlowFeatureCapabilityReserved.class);
        }
        if (features.getCapabilitiesV10().getOFPCSTP()) {
            capabilities.add(FlowFeatureCapabilityStp.class);
        }
        if (features.getCapabilitiesV10().getOFPCTABLESTATS()) {
            capabilities.add(FlowFeatureCapabilityTableStats.class);
        }

        return new SwitchFeaturesBuilder()
            .setMaxBuffers(features.getBuffers())
            .setMaxTables(features.getTables())
            .setCapabilities(capabilities)
            .build();
    }
}
