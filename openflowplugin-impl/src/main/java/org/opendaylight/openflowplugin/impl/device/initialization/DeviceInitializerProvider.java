/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device.initialization;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DeviceInitializerProvider {

    private final Map<Short, AbstractDeviceInitializer> initializers = new HashMap<>();

    /**
     * Register device initializer.
     *
     * @param version     the initializer version
     * @param initializer the initializer instance
     */
    void register(final Short version, final AbstractDeviceInitializer initializer) {
        initializers.put(version, initializer);
    }

    /**
     * Lookup  device initializer.
     *
     * @param version the initializer version
     * @return the initializer instance
     */
    public Optional<AbstractDeviceInitializer> lookup(final Short version) {
        return Optional.ofNullable(initializers.get(version));
    }

}
