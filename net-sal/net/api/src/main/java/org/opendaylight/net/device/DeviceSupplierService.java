/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.device;

import org.opendaylight.util.driver.DeviceIdentity;
import org.opendaylight.util.driver.DeviceInfo;
import org.opendaylight.net.supplier.SupplierId;
import org.opendaylight.net.supplier.SupplierService;
import org.opendaylight.net.model.Device;
import org.opendaylight.net.model.DeviceId;
import org.opendaylight.net.model.Interface;
import org.opendaylight.net.model.InterfaceInfo;

import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 * Service for adding/removing or otherwise supplying information about
 * infrastructure devices in the environment.
 *
 * @author Thomas Vachuska
 * @author Shaun Humphress
 * @author Julie Britt
 * @author Steve Dean
 */
public interface DeviceSupplierService extends SupplierService {

    /**
     * Creates, or updates, a device using the supplied discovery URIs and
     * device information populated with basic device identity data. If the
     * supplier service already knows about a device with the same unique
     * identifier (see {@link DeviceIdentity#getUniqueId()},
     * that device instance will be simply updated and returned.
     * <p/>
     * Note that it is the responsibility of the caller ({@link DeviceSupplier}
     * to populate the info with the identity information beforehand.
     *
     * @param deviceId unique identifier for the device
     * @param uris     set of discovery URIs
     * @param info     device info from the initial discovery/classification
     * @return newly created device or and updated one
     */
    Device createOrUpdateDevice(DeviceId deviceId, Set<URI> uris,
                                DeviceInfo info);

    /**
     * Removes the specified device from the inventory.
     *
     * @param deviceId device to be removed
     */
    void removeDevice(DeviceId deviceId);

    /**
     * Marks the device as on/off line.
     *
     * @param deviceId device to be updated
     * @param online   true to mark device online
     * @throws org.opendaylight.util.api.NotFoundException if device not found
     */
    void setOnline(DeviceId deviceId, boolean online);

    /**
     * Updates the interface inventory for the specified device. This means
     * that interfaces will be created, updated or removed as necessary.
     * An empty list will remove all interfaces from the device.
     *
     * @param deviceId      device whose interface inventory is to be updated
     * @param infos list of information about device interfaces
     */
    void updateInterfaces(DeviceId deviceId, List<InterfaceInfo> infos);

}
