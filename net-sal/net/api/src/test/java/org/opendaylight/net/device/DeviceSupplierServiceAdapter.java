/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.device;

import java.net.URI;
import java.util.List;
import java.util.Set;

import org.opendaylight.util.driver.DeviceInfo;
import org.opendaylight.net.model.Device;
import org.opendaylight.net.model.DeviceId;
import org.opendaylight.net.model.Interface;
import org.opendaylight.net.model.InterfaceInfo;
import org.opendaylight.net.supplier.SupplierId;

/**
 * Adapter implementation of the {@link DeviceSupplierService}
 *
 * @author Sean Humphress
 */
public class DeviceSupplierServiceAdapter implements DeviceSupplierService {
    @Override public Device createOrUpdateDevice(DeviceId deviceId, Set<URI> uris, DeviceInfo info) { return null; }
    @Override public void removeDevice(DeviceId deviceId) { }
    @Override public void setOnline(DeviceId deviceId, boolean online) { }
    @Override public void updateInterfaces(DeviceId deviceId, List<InterfaceInfo> infos) { }
}
