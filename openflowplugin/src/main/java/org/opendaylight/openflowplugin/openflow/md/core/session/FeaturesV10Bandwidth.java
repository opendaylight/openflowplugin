/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.session;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortGrouping;

/**
 * 
 * @author jsebin
 *
 * Singleton for extracting port features for OF 1.0	
 */
public class FeaturesV10Bandwidth implements IGetBandwith {
    
    private static FeaturesV10Bandwidth instance = new FeaturesV10Bandwidth();
    
    private FeaturesV10Bandwidth() {}
    
    /**
     * 
     * @return instance of class
     */
    public static FeaturesV10Bandwidth getInstance(){
        return instance;
    }
    
    @Override
    public boolean getBandwidth(PortGrouping port) {
        return (port.getCurrentFeatures().getPortFeaturesV10().is_100mbFd() | port.getCurrentFeatures().getPortFeaturesV10().is_100mbHd() | port.getCurrentFeatures().getPortFeaturesV10().is_10gbFd() | 
                port.getCurrentFeatures().getPortFeaturesV10().is_10mbFd() | port.getCurrentFeatures().getPortFeaturesV10().is_10mbHd() | port.getCurrentFeatures().getPortFeaturesV10().is_1gbFd() | 
                port.getCurrentFeatures().getPortFeaturesV10().is_1gbHd() | port.getCurrentFeatures().getPortFeaturesV10().isAutoneg() | port.getCurrentFeatures().getPortFeaturesV10().isCopper() | 
                port.getCurrentFeatures().getPortFeaturesV10().isFiber() | port.getCurrentFeatures().getPortFeaturesV10().isPause() | port.getCurrentFeatures().getPortFeaturesV10().isPauseAsym());        
    }
    
    

}
