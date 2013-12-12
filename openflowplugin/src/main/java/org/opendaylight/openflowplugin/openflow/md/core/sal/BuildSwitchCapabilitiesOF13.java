/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */ 	  				 	 	 
package org.opendaylight.openflowplugin.openflow.md.core.sal;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.CapabilitiesOf13;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FeaturesCapabilitiesOf13;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.SwitchFeaturesCapabilitiesOf13;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.SwitchFeaturesCapabilitiesOf13Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.flow.node.SwitchFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.flow.node.SwitchFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.flow.node._switch.features.CapabilityBuilder;
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
        
        CapabilitiesOf13 capability = new CapabilitiesOf13(
            features.getCapabilities().isOFPCFLOWSTATS(), 
            features.getCapabilities().isOFPCGROUPSTATS(), 
            features.getCapabilities().isOFPCIPREASM(), 
            features.getCapabilities().isOFPCPORTBLOCKED(), 
            features.getCapabilities().isOFPCPORTSTATS(), 
            features.getCapabilities().isOFPCQUEUESTATS(), 
            features.getCapabilities().isOFPCTABLESTATS());
                   
        SwitchFeaturesCapabilitiesOf13Builder builderSwFeaturesCapabilities = new SwitchFeaturesCapabilitiesOf13Builder();
        builderSwFeaturesCapabilities.setCapabilitiesOf13(capability);
       
        CapabilityBuilder builderCapability = new CapabilityBuilder();      
        builderCapability.setCapabilityType(FeaturesCapabilitiesOf13.class);
        builderCapability.addAugmentation(SwitchFeaturesCapabilitiesOf13.class, builderSwFeaturesCapabilities.build());
       
        SwitchFeaturesBuilder builderSwFeatures = new SwitchFeaturesBuilder();
        builderSwFeatures.setAuxiliaryId(features.getAuxiliaryId());
        builderSwFeatures.setDatapathId(features.getDatapathId());
        builderSwFeatures.setMaxBuffers(features.getBuffers());
        builderSwFeatures.setMaxTables(features.getTables());       
        builderSwFeatures.setCapability(builderCapability.build());

        return builderSwFeatures.build();
    }

}
