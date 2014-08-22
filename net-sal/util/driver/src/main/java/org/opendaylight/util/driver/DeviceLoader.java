/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

/**
 * A set of behaviors specific to persistence of data about a type of device.
 *
 * @author Simon Hunt
 * @author Thomas Vachuska
 */
public interface DeviceLoader {

    /** Returns the DeviceInfo instance associated with this loader.
     *
     * @return the device info instance
     */
    public DeviceInfo getDeviceInfo();

    /** Returns the device UID to which this loader is bound.
     *
     * @return the UID
     */
    public String getUID();
    
    /**
     * Use the device loader UID to locate all information that pertains to
     * the device, in the persistent store and populate the backing device
     * info instance.
     * 
     * @throws DeviceException if a problem occurs while loading the info
     */
    public void load() throws DeviceException;

    /**
     * Store the information in the backing device info into the persistent
     * store.
     * 
     * @throws DeviceException if a problem occurs while storing the info
     */
    public void save() throws DeviceException;
    
}
