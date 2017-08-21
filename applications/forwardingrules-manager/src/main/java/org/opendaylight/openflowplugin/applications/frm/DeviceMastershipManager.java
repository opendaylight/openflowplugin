package org.opendaylight.openflowplugin.applications.frm;

import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

/**
 * Manager for device mastership state.
 */
public interface DeviceMastershipManager extends MastershipChangeService {
    boolean isDeviceMastered(final NodeId nodeId);
}