/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

/**
 * A default implementation of the presentation facet. This implementation
 * makes the assumption that the context is a {@link DefaultDeviceInfo} implementation.
 *
 * @author Simon Hunt
 */
public class DefaultPresentation extends AbstractDefaultFacet implements Presentation {
    /**
     * Constructs a presentation facet that is projected onto the
     * specified device info context.
     *
     * @param context the device info of which this is a facet
     */
    public DefaultPresentation(DeviceInfo context) {
        super(context);
    }

    @Override
    public String getPropertiesTabImageRef() {
        return getContext().getType().getPresentationResources().getPropImagePath();
    }

    @Override
    public String getTopologyMapImageRef() {
        return getContext().getType().getPresentationResources().getMapImagePath();
    }
}
