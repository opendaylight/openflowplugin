/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

import org.opendaylight.util.net.IpAddress;

/**
 * A generic {@link Facet} that provides read-only information about a device's identity.
 *
 * @author Simon Hunt
 */
public interface DeviceIdentity extends Facet {

    /**
     * Gets the globally unique device ID generated via
     * {@link DeviceIdentityHandler#generateUniqueId()}.
     * 
     * @return globally unique device id; maybe null if the device is in the
     *         process of being classified during discovery
     */
    public String getUniqueId();

    /** Returns the name of the vendor of the device.
     *
     * @return the vendor name
     */
    public String getVendor();

    /** Returns the product family of the device.
     *
     * @return the product family
     */
    public String getFamily();

    /** Returns the product number of the device.
     *
     * @return the product number
     */
    public String getProductNumber();

    /** Returns the model number of the device.
     *
     * @return the model number
     */
    public String getModelNumber();

    /** Returns the serial number of the device.
     *
     * @return the serial number
     */
    public String getSerialNumber();

    /** Returns the firmware version of the device driver
     * (which is distinct from the version reported by the device via
     * {@link #getFirmwareVersion()}).
     * Usually these will be the same, but there may be cases where a firmware version is
     * reported by the device for which we don't have a device driver. In these cases the
     * driver with the closest matching firmware version will be used.
     *
     * @return the driver firmware version.
     */
    public String getDriverFirmwareVersion();

    /** Returns the firmware version of the device. Note that this method may return null
     * if the firmware version has not yet been identified.
     *
     * @return the firmware version.
     */
    public String getFirmwareVersion();

    /** Returns the name of this device.
     *
     * @return the name
     */
    public String getName();

    /** Returns the description of this device.
     *
     * @return the description
     */
    public String getDescription();

    /** Returns the location of this device.
     *
     * @return the location
     */
    public String getLocation();

    /** Returns the contact for this device.
     *
     * @return the contact
     */
    public String getContact();
    
    /**
     * Get the IP address of the device.
     * 
     * @return device IP address
     */
    public IpAddress getIpAddress();
    
}
