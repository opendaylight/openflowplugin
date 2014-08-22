/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

import java.util.Set;

/**
 * A partial implementation of {@link DeviceInfo} that has default
 * implementations of a couple of {@link FacetProvider} methods.
 * Note that this class implements {@link MutableDeviceInfo}, allowing
 * implementations access to their {@link DeviceType} reference.
 * 
 * @author Simon Hunt
 * @author Thomas Vachuska
 */
public abstract class AbstractDeviceInfo implements MutableDeviceInfo {

    private DefaultDeviceType deviceType;

    /**
     * Creates a device info associated with the specified device type.
     * 
     * @param deviceType backing device type
     */
    public AbstractDeviceInfo(DefaultDeviceType deviceType) {
        if (deviceType == null)
            throw new NullPointerException("device type cannot be null");
        this.deviceType = deviceType;
    }

    /** Downcast the DeviceType to AbstractDeviceType.
     *
     * @return the device type downcast
     */
    public DefaultDeviceType getType() {
        return deviceType;
    }

    @Override
    public String getTypeName() {
        return deviceType.getTypeName();
    }


    // FACET PROVIDER
    
    @Override
    public Set<Class<? extends Facet>> getFacetClasses() {
        return deviceType.getFacetClasses(false);
    }

    @Override
    public Set<String> getFacetClassNames() {
        return deviceType.getFacetClassNames(false);
    }

    @Override
    public boolean isSupported(Class<? extends Facet> facetClass) {
        return deviceType.isSupported(false, facetClass);
    }

    @Override
    public <T extends Facet> T getFacet(Class<T> facetClass) {
        return deviceType.getFacet(false, this, facetClass);
    }

    // MUTABLE DEVICE INFO

    @Override
    public DeviceType getDeviceType() {
        return deviceType;
    }

    @Override
    public void setDeviceType(DeviceType type) {
        if (!(type instanceof DefaultDeviceType))
            throw new IllegalArgumentException("device type does not extend DefaultDeviceType");

        this.deviceType = (DefaultDeviceType) type;
    }
}
