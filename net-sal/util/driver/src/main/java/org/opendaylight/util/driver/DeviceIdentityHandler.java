/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

/**
 * A generic {@link Facet} that provides read-write access concerning a device's identity.
 *
 * @author Simon Hunt
 */
public interface DeviceIdentityHandler extends DeviceIdentity, HandlerFacet {

    /** Set the device name.
     *
     * @param name the device name
     */
    public void setName(String name);

    /** Set the device location.
     *
     * @param location the device location
     */
    public void setLocation(String location);

    /** Set the device contact.
     *
     * @param contact the device contact
     */
    public void setContact(String contact);
    
    /**
     * Generates a globally unique device ID using device specific information
     * and a type-specific algorithm and stamps it into the associated device
     * info entity.
     */
    public void generateUniqueId();
    
    /**
     * Accepts an opaque memento containing device identity information,
     * presumably obtained from a prior communication with the device. This
     * allows information learned about the device in some protocol-specific
     * manner to be used to avoid a repeated trip (via {@link #fetch} method)
     * to the device in order to obtain the same information.
     * <p>
     * Implementations are to interpret and digest the information in this
     * memento into the associated device info instance.
     * 
     * @param memento opaque container of device identity information
     */
    public void digest(Object memento);

    /** Return an evolved device info context.
     * <p>
     * Note: implementations should use {@link DeviceDriverProvider#switchType}
     * if it is determined that the device type needs to change.
     *
     * @return the evolved info potentially backed by a different device type
     */
    public DeviceInfo evolve();
    

}
