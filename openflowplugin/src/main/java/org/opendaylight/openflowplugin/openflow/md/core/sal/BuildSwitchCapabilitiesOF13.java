/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.openflowplugin.api.openflow.md.core.sal.BuildSwitchFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FeatureCapability;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowFeatureCapabilityFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowFeatureCapabilityGroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowFeatureCapabilityIpReasm;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowFeatureCapabilityPortBlocked;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowFeatureCapabilityPortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowFeatureCapabilityQueueStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowFeatureCapabilityTableStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.flow.node.SwitchFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.flow.node.SwitchFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;

/**
 * SwitchFeature builder for OF 1.3
 * 
 * @author jsebin
 *
 */
public class BuildSwitchCapabilitiesOF13 implements BuildSwitchFeatures {

private static BuildSwitchCapabilitiesOF13 instance = new BuildSwitchCapabilitiesOF13();
    
    private BuildSwitchCapabilitiesOF13() {}
    
    /**
     * Get singleton instance
     * 
     * @return instance
     */
    public static BuildSwitchCapabilitiesOF13 getInstance() {
        return instance;
    }

    @Override
    public SwitchFeatures build(GetFeaturesOutput features) {
        
        SwitchFeaturesBuilder builderSwFeatures = new SwitchFeaturesBuilder();
        builderSwFeatures.setMaxBuffers(features.getBuffers());
        builderSwFeatures.setMaxTables(features.getTables());
        
        List<Class<? extends FeatureCapability>> capabilities = new ArrayList<>();
        
        if(features.getCapabilities().isOFPCFLOWSTATS()) {
            capabilities.add(FlowFeatureCapabilityFlowStats.class);
        }
        if(features.getCapabilities().isOFPCGROUPSTATS()) {
            capabilities.add(FlowFeatureCapabilityGroupStats.class);
        }
        if(features.getCapabilities().isOFPCIPREASM()) {
            capabilities.add(FlowFeatureCapabilityIpReasm.class);
        }
        if(features.getCapabilities().isOFPCPORTBLOCKED()) {
            capabilities.add(FlowFeatureCapabilityPortBlocked.class);
        }
        if(features.getCapabilities().isOFPCPORTSTATS()) {
            capabilities.add(FlowFeatureCapabilityPortStats.class);
        }
        if(features.getCapabilities().isOFPCQUEUESTATS()) {
            capabilities.add(FlowFeatureCapabilityQueueStats.class);
        }
        if(features.getCapabilities().isOFPCTABLESTATS()) {
            capabilities.add(FlowFeatureCapabilityTableStats.class);
        }
        
        builderSwFeatures.setCapabilities(capabilities);
        
        return builderSwFeatures.build();
    }

}
