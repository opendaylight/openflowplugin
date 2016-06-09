package org.opendaylight.openflowplugin.api.openflow;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;

/**
 * Generic API for all managers
 */
public interface OFPManager {

    <T extends OFPContext> T gainContext(final DeviceInfo deviceInfo);
    
}
