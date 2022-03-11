/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import com.google.common.collect.ImmutableSet;
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
        final var capabilities = features.getCapabilitiesV10();
        if (capabilities == null) {
            return null;
        }

        final var builder = ImmutableSet.<Class<? extends FeatureCapability>>builder();
        if (capabilities.getOFPCARPMATCHIP()) {
            builder.add(FlowFeatureCapabilityArpMatchIp.class);
        }
        if (capabilities.getOFPCFLOWSTATS()) {
            builder.add(FlowFeatureCapabilityFlowStats.class);
        }
        if (capabilities.getOFPCIPREASM()) {
            builder.add(FlowFeatureCapabilityIpReasm.class);
        }
        if (capabilities.getOFPCPORTSTATS()) {
            builder.add(FlowFeatureCapabilityPortStats.class);
        }
        if (capabilities.getOFPCQUEUESTATS()) {
            builder.add(FlowFeatureCapabilityQueueStats.class);
        }
        if (capabilities.getOFPCRESERVED()) {
            builder.add(FlowFeatureCapabilityReserved.class);
        }
        if (capabilities.getOFPCSTP()) {
            builder.add(FlowFeatureCapabilityStp.class);
        }
        if (capabilities.getOFPCTABLESTATS()) {
            builder.add(FlowFeatureCapabilityTableStats.class);
        }

        return new SwitchFeaturesBuilder()
            .setMaxBuffers(features.getBuffers())
            .setMaxTables(features.getTables())
            .setCapabilities(builder.build())
            .build();
    }
}
