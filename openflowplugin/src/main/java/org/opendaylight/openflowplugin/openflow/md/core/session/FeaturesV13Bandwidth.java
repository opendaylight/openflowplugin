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
 * Singleton for extracting port features for OF 1.3	
 */
public class FeaturesV13Bandwidth implements IGetBandwith {

    private static FeaturesV13Bandwidth instance = new FeaturesV13Bandwidth();
    
    private FeaturesV13Bandwidth() {}
    
    /**
     * 
     * @return instance of class
     */
    public static FeaturesV13Bandwidth getInstance(){
        return instance;
    }
    
    @Override
    public boolean getBandwidth(PortGrouping port) {        
        return (port.getCurrentFeatures().getPortFeaturesV13().is_100gbFd() | port.getCurrentFeatures().getPortFeaturesV13().is_100mbFd() | port.getCurrentFeatures().getPortFeaturesV13().is_100mbHd() | 
                port.getCurrentFeatures().getPortFeaturesV13().is_10gbFd() | port.getCurrentFeatures().getPortFeaturesV13().is_10mbFd() | port.getCurrentFeatures().getPortFeaturesV13().is_10mbHd() | 
                port.getCurrentFeatures().getPortFeaturesV13().is_1gbFd() | port.getCurrentFeatures().getPortFeaturesV13().is_1gbHd() | port.getCurrentFeatures().getPortFeaturesV13().is_1tbFd() | 
                port.getCurrentFeatures().getPortFeaturesV13().is_40gbFd() | port.getCurrentFeatures().getPortFeaturesV13().isAutoneg() | port.getCurrentFeatures().getPortFeaturesV13().isCopper() |
                port.getCurrentFeatures().getPortFeaturesV13().isFiber() | port.getCurrentFeatures().getPortFeaturesV13().isOther() | port.getCurrentFeatures().getPortFeaturesV13().isPause() | 
                port.getCurrentFeatures().getPortFeaturesV13().isPauseAsym());
    }

}
