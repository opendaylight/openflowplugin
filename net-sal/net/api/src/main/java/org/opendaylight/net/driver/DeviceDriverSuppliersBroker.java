/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.driver;

import org.opendaylight.util.driver.DeviceDriverBroker;

/**
 * Abstraction of a device driver broker, capable of being primed with
 * primordial info to device type mappings.
 *
 * @author Thomas Vachuska
 */
public interface DeviceDriverSuppliersBroker extends DeviceDriverBroker {

    /**
     * Adds a binding of the specified primordial device info to a device type.
     *
     * @param mfr      device manufacturer
     * @param hw       device hardware model
     * @param fw       device firmware revision
     * @param typeName device type name
     */
    void addTypeName(String mfr, String hw, String fw, String typeName);

    /**
     * Removes a binding of the specified primordial device info to a device type.
     *
     * @param mfr      device manufacturer
     * @param hw       device hardware model
     * @param fw       device firmware revision
     */
    void removeTypeName(String mfr, String hw, String fw);

}
