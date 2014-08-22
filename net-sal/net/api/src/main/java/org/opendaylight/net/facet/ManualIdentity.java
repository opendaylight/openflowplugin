/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.facet;

import org.opendaylight.util.driver.Facet;

/**
 * If unable to communicate with the device for discovery, input the
 * information provided out of band, ie the openflow handshake.
 */
public interface ManualIdentity extends Facet {

    /**
     * Set the vendor.
     *
     * @param vendor device vendor
     */
    void setVendor(String vendor);

    /**
     * Set the product number.
     *
     * @param product device product string
     */
    void setProductNumber(String product);

    /**
     * Set the model.
     *
     * @param model device product string
     */
    void setModel(String model);

    /**
     * Set the firmware version.
     *
     * @param firmware version
     */
    void setFirmwareVersion(String firmware);

    /**
     * Set the serial number.
     *
     * @param serial serial number
     */
    void setSerialNumber(String serial);

}
