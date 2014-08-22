/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

/**
 * A facet is a particular "view" of a specific device info context.
 *
 * @author Simon Hunt
 */
public interface Facet {

    /** Returns the device info context of which this facet is a "view".
     *
     * @return the device info context
     */
    public DeviceInfo getContext();

    /** Returns the name of the underlying device type.
     *
     * @return device type name
     */
    public String getTypeName();

}
