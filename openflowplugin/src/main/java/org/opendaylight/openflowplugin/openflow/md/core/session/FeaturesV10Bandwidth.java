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
        return (port.getCurrentFeaturesV10().is_100mbFd() | port.getCurrentFeaturesV10().is_100mbHd() | port.getCurrentFeaturesV10().is_10gbFd() | 
                port.getCurrentFeaturesV10().is_10mbFd() | port.getCurrentFeaturesV10().is_10mbHd() | port.getCurrentFeaturesV10().is_1gbFd() | 
                port.getCurrentFeaturesV10().is_1gbHd() | port.getCurrentFeaturesV10().isAutoneg() | port.getCurrentFeaturesV10().isCopper() | 
                port.getCurrentFeaturesV10().isFiber() | port.getCurrentFeaturesV10().isPause() | port.getCurrentFeaturesV10().isPauseAsym());        
    }
    
    

}
