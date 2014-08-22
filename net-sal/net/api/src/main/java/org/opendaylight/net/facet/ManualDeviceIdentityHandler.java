/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.facet;

import org.opendaylight.util.driver.CoreDevicePropertyKeys;
import org.opendaylight.util.driver.DefaultDeviceIdentityHandler;
import org.opendaylight.util.driver.DeviceInfo;

public class ManualDeviceIdentityHandler extends DefaultDeviceIdentityHandler 
        implements ManualIdentity {

    public ManualDeviceIdentityHandler(DeviceInfo context) {
        super(context);
    }

    @Override
    public void setVendor(String vendor) {
        getContext().set(CoreDevicePropertyKeys.VENDOR, vendor);
    }

    @Override
    public void setProductNumber(String product) {
        getContext().set(CoreDevicePropertyKeys.PRODUCT, product);
    }

    @Override
    public void setModel(String model) {
        getContext().set(CoreDevicePropertyKeys.MODEL, model);
    }

    @Override
    public void setFirmwareVersion(String firmware) {
        getContext().set(CoreDevicePropertyKeys.FIRMWARE_VERSION, firmware);
    }

    @Override
    public void setSerialNumber(String serial) {
        getContext().set(CoreDevicePropertyKeys.SERIAL_NUMBER, serial);
    }
}
