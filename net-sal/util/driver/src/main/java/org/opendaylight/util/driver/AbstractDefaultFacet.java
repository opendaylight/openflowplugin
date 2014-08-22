/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

/**
 * A partial implementation of {@link Facet} that makes the assumption that
 * the underlying device info is a {@link DefaultDeviceInfo} instance.
 *
 * @author Simon Hunt
 */
public abstract class AbstractDefaultFacet extends AbstractFacet {


    /**
     * Constructs a facet that is projected onto the specified device info context.
     *
     * @param context the device info of which this is a facet
     */
    public AbstractDefaultFacet(DeviceInfo context) {
        super(context);
        if (!(context instanceof DefaultDeviceInfo))
            throw new IllegalArgumentException("context must be DefaultDeviceInfo instance");
    }

    // Override to downcast as our specific implementation class
    @Override
    public DefaultDeviceInfo getContext() {
        return (DefaultDeviceInfo) super.getContext();
    }

}
