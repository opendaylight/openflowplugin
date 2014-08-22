/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.device;

import org.opendaylight.net.model.Device;

/**
 * A filter which matches against information in a {@link org.opendaylight.net.model.Device}.
 *
 * @author Thomas Vachuska
 * @author Steve Dean
 */
public interface DeviceFilter {

    /**
     * Returns whether the given node matches this filter or not.
     *
     * @param device the given device
     * @return true if the device matches, false if not
     */
    boolean matches(Device device);

}
