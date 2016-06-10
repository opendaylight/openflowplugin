package org.opendaylight.openflowplugin.api.openflow.device;

import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.group.DeviceGroupRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.meter.DeviceMeterRegistry;

/**
 * Flows, Groups and Meter registry
 */
public interface DeviceRegistry {

    /**
     * Method exposes flow registry used for storing flow ids identified by calculated flow hash.
     *
     * @return
     */
    DeviceFlowRegistry getDeviceFlowRegistry();

    /**
     * Method exposes device group registry used for storing group ids.
     *
     * @return
     */
    DeviceGroupRegistry getDeviceGroupRegistry();

    /**
     * Method exposes device meter registry used for storing meter ids.
     *
     * @return
     */
    DeviceMeterRegistry getDeviceMeterRegistry();

}
