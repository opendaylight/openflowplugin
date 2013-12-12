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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.flow.node.SwitchFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.flow.node.SwitchFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;

/**
 * SwitchFeature builder for OF 1.0
 * 
 * @author jsebin
 *
 */
public class BuildSwitchCapabilitiesOF10 implements BuildSwitchFeatures {

    private static BuildSwitchCapabilitiesOF10 instance = new BuildSwitchCapabilitiesOF10();
    
    private BuildSwitchCapabilitiesOF10() {}
    
    /**
     * Get singleton instance
     * 
     * @return instance
     */
    public static BuildSwitchCapabilitiesOF10 getInstance() {
        return instance;
    }

    @Override
    public SwitchFeatures build(GetFeaturesOutput features) {
               
       SwitchFeaturesBuilder builderSwFeatures = new SwitchFeaturesBuilder();
       builderSwFeatures.setMaxBuffers(features.getBuffers());
       builderSwFeatures.setMaxTables(features.getTables());       
       
       List<String> capabilities = new ArrayList<>();
       
       if(features.getCapabilitiesV10().isOFPCARPMATCHIP() == true) {
           capabilities.add(OFConstants.OFPC_ARP_MATCH_IP);
       }
       if(features.getCapabilitiesV10().isOFPCFLOWSTATS() == true) {
           capabilities.add(OFConstants.OFPC_FLOW_STATS);
       }
       if(features.getCapabilitiesV10().isOFPCIPREASM() == true) {
           capabilities.add(OFConstants.OFPC_IP_REASM);
       }
       if(features.getCapabilitiesV10().isOFPCPORTSTATS() == true) {
           capabilities.add(OFConstants.OFPC_PORT_STATS);
       }
       if(features.getCapabilitiesV10().isOFPCQUEUESTATS() == true) {
           capabilities.add(OFConstants.OFPC_QUEUE_STATS);
       }
       if(features.getCapabilitiesV10().isOFPCRESERVED() == true) {
           capabilities.add(OFConstants.OFPC_RESERVED);
       }
       if(features.getCapabilitiesV10().isOFPCSTP() == true) {
           capabilities.add(OFConstants.OFPC_RESERVED);
       }
       if(features.getCapabilitiesV10().isOFPCTABLESTATS() == true) {
           capabilities.add(OFConstants.OFPC_TABLE_STATS);
       }
       
       builderSwFeatures.setCapabilities(capabilities);
       
       return builderSwFeatures.build();
    }
    
    
}
