/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

import org.opendaylight.util.net.IpAddress;

import java.util.Set;

/**
 * Abstraction of attributes of a specific type of device. Note that instances are
 * expected to be immutable (therefore shareable and threadsafe).
 * <p>
 * Note also that instances of device type are somewhat hidden from external consumers of
 * {@link DeviceInfo}. They do work on behalf of specific device info instances, under the covers.
 *
 * @author Simon Hunt
 * @author Thomas Vachuska
 */
public interface DeviceType {


    /** Returns the name of this device type.
     *
     * @return the type name
     */
    public String getTypeName();

    /** Returns the provider instance that created this device type.
     *
     * @return the provider
     */
    public DeviceDriverProvider getProvider();

    /** Returns the description of this device type.
     *
     * @return the description
     */
    public String getDescription();

    /** Returns the origin of this device type (i.e. the organization that created the device driver)
     *
     * @return the origin
     */
    public String getOrigin();

    /** Returns the product number associated with this device type.
     *
     * @return the product number
     */
    public String getProduct();

    /** Gets the model number associated with this type belongs.
     *
     * @return the model number
     */
    public String getModel();

    /** Returns the device type that this type extends (or null if no parent).
     *
     * @return the parent type
     */
    public DeviceType getParentType();

    /** Returns the device types that extend this device type directly.
     *
     * @return the child types
     */
    public Set<? extends DeviceType> getChildTypes();

    /** Return an evolved device info context.
     * <p>
     * Note: implementations should use {@link DeviceDriverProvider#switchType}
     * if it is determined that the device type needs to change.
     *
     * @param deviceInfo the info to evolve
     * @return the evolved info potentially backed by a different device type
     */
    public DeviceInfo evolve(DeviceInfo deviceInfo);

    /** Return a device handler appropriate for this device type.
     *
     * @param info the device info context
     * @param ip the IP address to bind to
     * @return a device handler
     */
    public DeviceHandler createHandler(DeviceInfo info, IpAddress ip);

    /** Return a device handler appropriate for this device type.
     *
     * @param info the device info context
     * @param uid the unique identifier to bind to
     * @return a device loader
     */
    public DeviceLoader createLoader(DeviceInfo info, String uid);

}
