package org.opendaylight.openflowplugin.openflow.md.core.session;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Port;

public class FeaturesV13Bandwidth implements IGetBandwith {

    private static FeaturesV13Bandwidth instance = new FeaturesV13Bandwidth();
    
    private FeaturesV13Bandwidth() {};
    
    public static FeaturesV13Bandwidth getInstance(){
        return instance;
    }
    
    @Override
    public boolean getBandwidth(Port port) {        
        return (port.getCurrentFeatures().is_100gbFd() | port.getCurrentFeatures().is_100mbFd() | port.getCurrentFeatures().is_100mbHd() | 
                port.getCurrentFeatures().is_10gbFd() | port.getCurrentFeatures().is_10mbFd() | port.getCurrentFeatures().is_10mbHd() | 
                port.getCurrentFeatures().is_1gbFd() | port.getCurrentFeatures().is_1gbHd() | port.getCurrentFeatures().is_1tbFd() | 
                port.getCurrentFeatures().is_40gbFd() | port.getCurrentFeatures().isAutoneg() | port.getCurrentFeatures().isCopper() |
                port.getCurrentFeatures().isFiber() | port.getCurrentFeatures().isOther() | port.getCurrentFeatures().isPause() | 
                port.getCurrentFeatures().isPauseAsym());
    }

}
