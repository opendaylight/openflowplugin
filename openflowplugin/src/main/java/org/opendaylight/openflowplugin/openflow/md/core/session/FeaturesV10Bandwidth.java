package org.opendaylight.openflowplugin.openflow.md.core.session;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Port;

public class FeaturesV10Bandwidth implements IGetBandwith {
    
    private static FeaturesV10Bandwidth instance = new FeaturesV10Bandwidth();
    
    private FeaturesV10Bandwidth() {};
    
    public static FeaturesV10Bandwidth getInstance(){
        return instance;
    }
    
    @Override
    public boolean getBandwidth(Port port) {
        return (port.getCurrentFeaturesV10().is_100mbFd() | port.getCurrentFeaturesV10().is_100mbHd() | port.getCurrentFeaturesV10().is_10gbFd() | 
                port.getCurrentFeaturesV10().is_10mbFd() | port.getCurrentFeaturesV10().is_10mbHd() | port.getCurrentFeaturesV10().is_1gbFd() | 
                port.getCurrentFeaturesV10().is_1gbHd() | port.getCurrentFeaturesV10().isAutoneg() | port.getCurrentFeaturesV10().isCopper() | 
                port.getCurrentFeaturesV10().isFiber() | port.getCurrentFeaturesV10().isPause() | port.getCurrentFeaturesV10().isPauseAsym());        
    }
    
    

}
