/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device.initialization;

import org.opendaylight.openflowplugin.api.OFConstants;

/**
 * Multipart writer provider factory
 */
public class DeviceInitializerProviderFactory {

    /**
     * Create default #{@link org.opendaylight.openflowplugin.impl.device.initialization.DeviceInitializerProvider}
     * @return the device initialization provider
     */
    public static DeviceInitializerProvider createDefaultProvider() {
        final DeviceInitializerProvider provider = new DeviceInitializerProvider();
        provider.register(OFConstants.OFP_VERSION_1_0, new OF10DeviceInitializer());
        provider.register(OFConstants.OFP_VERSION_1_3, new OF13DeviceInitializer());
        return provider;
    }

}
