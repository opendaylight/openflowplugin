/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.device;

import org.opendaylight.net.supplier.AbstractSuppliersBroker;

/**
 * Adapter implementation of the {@link DeviceSuppliersBroker}
 *
 * @author Sean Humphress
 */
public class DeviceSuppliersBrokerAdapter
        extends AbstractSuppliersBroker<DeviceSupplier, DeviceSupplierService>
        implements DeviceSuppliersBroker {
    @Override protected DeviceSupplierService createSupplierService(DeviceSupplier supplier) { return null; }
}
