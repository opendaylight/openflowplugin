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

import org.opendaylight.openflowplugin.openflow.md.OFConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FeatureCapability;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FeatureCapabilityOFPCFLOWSTATS;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FeatureCapabilityOFPCGROUPSTATS;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FeatureCapabilityOFPCIPREASM;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FeatureCapabilityOFPCPORTBLOCKED;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FeatureCapabilityOFPCPORTSTATS;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FeatureCapabilityOFPCQUEUESTAT;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FeatureCapabilityOFPCTABLESTATS;
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
        
        if(features.getCapabilities().isOFPCFLOWSTATS() == true) {
            capabilities.add(FeatureCapabilityOFPCFLOWSTATS.class);
        }
        if(features.getCapabilities().isOFPCGROUPSTATS() == true) {
            capabilities.add(FeatureCapabilityOFPCGROUPSTATS.class);
        }
        if(features.getCapabilities().isOFPCIPREASM() == true) {
            capabilities.add(FeatureCapabilityOFPCIPREASM.class);
        }
        if(features.getCapabilities().isOFPCPORTBLOCKED() == true) {
            capabilities.add(FeatureCapabilityOFPCPORTBLOCKED.class);
        }
        if(features.getCapabilities().isOFPCPORTSTATS() == true) {
            capabilities.add(FeatureCapabilityOFPCPORTSTATS.class);
        }
        if(features.getCapabilities().isOFPCQUEUESTATS() == true) {
            capabilities.add(FeatureCapabilityOFPCQUEUESTAT.class);
        }
        if(features.getCapabilities().isOFPCTABLESTATS() == true) {
            capabilities.add(FeatureCapabilityOFPCTABLESTATS.class);
        }
        
        builderSwFeatures.setCapabilities(capabilities);
        
        return builderSwFeatures.build();
    }

}
