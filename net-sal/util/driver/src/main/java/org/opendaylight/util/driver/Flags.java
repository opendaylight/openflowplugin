/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

/**
 * A generic {@link Facet} that provides access to predicates about a device.
 *
 * @author Simon Hunt
 */
public interface Flags extends Facet {

    /** Returns true if the underlying device has the specified "flag" set.
     *
     * @param flag the flag name
     * @return true if the device has this flag "set"
     */
    public boolean hasFlag(String flag);

    /** Returns the total number of flags the underlying device type has.
     *
     * @return the number of flags
     */
    public int flagCount();
}
