package org.opendaylight.openflowplugin.applications.ownershipstatusservice;

public interface DeviceOwnershipStatusService {

        boolean isEntityOwned(final String nodeId);
}