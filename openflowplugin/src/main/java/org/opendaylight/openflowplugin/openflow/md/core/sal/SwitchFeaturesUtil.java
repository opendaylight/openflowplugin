/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal;

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.flow.node.SwitchFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jsebin
 *
 */
public class SwitchFeaturesUtil {

    protected static final Logger LOG = LoggerFactory.getLogger(SwitchFeaturesUtil.class);
    
    private static SwitchFeaturesUtil instance = new SwitchFeaturesUtil();
    private Map<Short, BuildSwitchFeatures> swFeaturesBuilders;
    
    private SwitchFeaturesUtil() {
        swFeaturesBuilders = new HashMap<>();
        swFeaturesBuilders.put((short) 1, BuildSwitchCapabilitiesOF10.getInstance());
        swFeaturesBuilders.put((short) 4, BuildSwitchCapabilitiesOF13.getInstance());
    }
    
    /**
     * Get singleton instance
     * 
     * @return instance
     */
    public static SwitchFeaturesUtil getInstance() {
        return instance;
    }
    
    /**
     * @param features {@link org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput}
     * @return switch features
     */
    public SwitchFeatures buildSwitchFeatures(GetFeaturesOutput features) {

        if(swFeaturesBuilders.containsKey(features.getVersion()) == true) {
            LOG.debug("map contains version {}", features.getVersion());
            try {
                return swFeaturesBuilders.get(features.getVersion()).build(features);
            } catch (NullPointerException e) {
                LOG.error("error while building switch features {}", e);
            }
        }
        else {
            LOG.warn("unknown version: {}", features.getVersion());            
        }                
        
        return null;
    }
    
}
