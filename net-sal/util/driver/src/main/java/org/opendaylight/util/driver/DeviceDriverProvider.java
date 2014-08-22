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
 * A provider of {@link DeviceInfo}, {@link DeviceHandler} and {@link DeviceLoader} instances, backed by
 * implementations appropriate to the requested type of device.
 *
 * @author Simon Hunt
 * @author Thomas Vachuska
 */
public interface DeviceDriverProvider {

    /** Returns a set of device type names for which this provider can create instances of
     * {@link DeviceInfo}, {@link DeviceHandler} and {@link DeviceLoader}.
     *
     * @return array of device type names
     */
    public Set<String> getDeviceTypeNames();

    /** Creates an instance of DeviceInfo backed by the specified device type and void of any specific device
     * information. Returns null if there is no such mapping for the specified device type.
     *
     * @param typeName the name of the required device type
     * @return a new device info instance; or null
     */
    public DeviceInfo create(String typeName);

    /** Creates an instance of DeviceHandler, bound to the specified device target, and associated with
     * a new instance of DeviceInfo backed by the specified device type and void of any specific device information.
     * Returns null if there is no such mapping for the specified device type.
     *
     * @param typeName the name of the required device type
     * @param ip the device IP address
     * @return an available device handler instance; or null
     */
    public DeviceHandler create(String typeName, IpAddress ip);

    /** Creates an instance of DeviceHandler, bound to the specified device target, and associated with
     * the specified instance of DeviceInfo.
     * Returns null if there is no such mapping for the specified device type.
     *
     * @param info the device info instance
     * @param ip the device IP address
     * @return an available device handler instance; or null
     */
    public DeviceHandler create(DeviceInfo info, IpAddress ip);

    /** Creates an instance of DeviceLoader, bound to the specified device UID, and associated with
     * a new instance of DeviceInfo backed by the specified device type.
     * Returns null if there is no such mapping for the specified device type.
     *
     * @param typeName the name of the required device type
     * @param uid the key by which the set of information describing the device can be looked up
     *          in the persistence store
     * @return an available device loader instance; or null
     */
    public DeviceLoader create(String typeName, String uid);


    /** Creates an instance of DeviceLoader, bound to the specified device UID, and associated with
     * the specified instance of DeviceInfo.
     * Returns null if there is no such mapping for the specified device type.
     *
     * @param info the device info instance
     * @param uid the key by which the set of information describing the device can be looked up
     *          in the persistence store
     * @return an available device loader instance; or null
     */
    public DeviceLoader create(DeviceInfo info, String uid);

    /** Given a device info instance and a type name, replace the backing device type in that instance,
     *  with the named device type.
     *
     * @param mutableDeviceInfo the device info to be adjusted
     * @param newTypeName the name of the replacement device type
     */
    public void switchType(MutableDeviceInfo mutableDeviceInfo, String newTypeName);

}
