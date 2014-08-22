/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.topology.compute.impl;

import org.opendaylight.net.model.AbstractModel;
import org.opendaylight.util.driver.DeviceInfo;
import org.opendaylight.net.model.Device;
import org.opendaylight.net.model.DeviceId;
import org.opendaylight.net.supplier.SupplierId;
import org.opendaylight.of.lib.dt.DataPathId;

import java.net.URI;
import java.util.Set;

/**
 * Test device adapter
 *
 * @author Thomas Vachuska
 */
// FIXME: rename to DeviceAdapter and move
public class FakeDevice extends AbstractModel implements Device {

    private final DeviceId id;

    FakeDevice(DeviceId deviceId) {
        this.id = deviceId;
    }

    @Override
    public DeviceId id() {
        return id;
    }

    @Override
    public String name() {
        return id.toString();
    }

    @Override
    public Type type() {
        return null;
    }

    @Override
    public DeviceInfo info() {
        return null;
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public Set<URI> managementURIs() {
        return null;
    }

    @Override
    public DataPathId dpid() {
        return null;
    }

    @Override
    public Device realizedBy() {
        return null;
    }

    @Override
    public SupplierId supplierId() {
        return null;
    }
}
