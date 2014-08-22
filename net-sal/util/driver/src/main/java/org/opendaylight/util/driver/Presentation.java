/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

/**
 * A generic {@link Facet} that provides access to presentation data for a device.
 *
 * @author Simon Hunt
 */
public interface Presentation extends Facet {

    /** Returns the reference to a resource (image) file for the image
     *  associated with the underlying device, that is to be displayed
     *  on its device properties tab.
     *
     * @return the URI to resource data
     */
    public String getPropertiesTabImageRef();

    /** Returns the reference to a resource (image) file for the image
     *  associated with the underlying device, that is to be displayed
     *  on topology maps.
     *
     * @return the URI to resource data
     */
    public String getTopologyMapImageRef();

}
