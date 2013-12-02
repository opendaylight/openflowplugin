package org.opendaylight.openflowplugin.openflow.md.core.session;

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortFeaturesUtil {
        
    private static PortFeaturesUtil instance = new PortFeaturesUtil();
    
    private final Map<Short, IGetBandwith> portVersionBandwidth;
    protected static final Logger LOG = LoggerFactory.getLogger(PortFeaturesUtil.class); 
    
    private PortFeaturesUtil() {
        this.portVersionBandwidth = new HashMap<Short, IGetBandwith>();
        
        portVersionBandwidth.put((short) 1, FeaturesV10Bandwidth.getInstance());
        portVersionBandwidth.put((short) 4, FeaturesV13Bandwidth.getInstance());
    }
    
    public static PortFeaturesUtil getInstance() {
        return instance;    
    }
    
        
    public Boolean getPortBandwidth(PortStatus msg) {        
    
        if(portVersionBandwidth.containsKey(msg.getVersion()) == true) {
        	try {
        		return portVersionBandwidth.get(msg.getVersion()).getBandwidth(msg);
        	} catch (NullPointerException e) {
                LOG.error("error while getting port features {}", e);            
            }
        }
        else {
            LOG.warn("unknown port version: {}", msg.getVersion());                
        }                
        
        return null;
    }
        
}
