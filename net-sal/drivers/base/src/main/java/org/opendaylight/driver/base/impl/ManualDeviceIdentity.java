/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.driver.base.impl;

import org.opendaylight.util.driver.DefaultDeviceIdentity;
import org.opendaylight.util.driver.DeviceInfo;

/**
 * Extension of the default identity facet, but one where vendor and product
 * information is extracted first from the device info and then as a fall-back
 * from the device type.
 *
 * @author Uyen Chau
 * @author Thomas Vachuska
 */
public class ManualDeviceIdentity extends DefaultDeviceIdentity {

    private static final String VENDOR = "device.vendor";
    private static final String PRODUCT = "device.product";

    /**
     * Creates a new manual device identity facet.
     *
     * @param context device info context
     */
    public ManualDeviceIdentity(DeviceInfo context) {
        super(context);
    }

    @Override
    public String getVendor() {
        String vendor = getContext().get(VENDOR);
        if (vendor == null)
            vendor = super.getVendor();
        return vendor;
    }

    @Override
    public String getProductNumber() {
        String product = getContext().get(PRODUCT);
        if (product == null)
            product = super.getProductNumber();
        return product;
    }
}
