/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device.initialization;

import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;

/**
 * Multipart writer provider factory
 */
public class DeviceInitializerProviderFactory {

    /**
     * Create default #{@link org.opendaylight.openflowplugin.impl.device.initialization.DeviceInitializerProvider}
     * @return the device initialization provider
     */
    public static DeviceInitializerProvider createDefaultProvider(@Nonnull OpenflowProviderConfig configuration) {
        final DeviceInitializerProvider provider = new DeviceInitializerProvider();
        final ValidDeviceChecker checker = new ValidDeviceChecker(configuration);
        provider.register(OFConstants.OFP_VERSION_1_0, new OF10DeviceInitializer(checker));
        provider.register(OFConstants.OFP_VERSION_1_3, new OF13DeviceInitializer(checker));
        return provider;
    }

}
