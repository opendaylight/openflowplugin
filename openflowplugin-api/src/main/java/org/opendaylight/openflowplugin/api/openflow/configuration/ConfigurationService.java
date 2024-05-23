/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.configuration;

import java.util.Map;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
import org.opendaylight.yangtools.concepts.Registration;

/**
 * Manages OpenFlowPlugin configuration.
 */
@NonNullByDefault
public interface ConfigurationService {
    /**
     * Update map of properties.
     *
     * @param properties properties
     */
    void update(Map<String, String> properties);

    /**
     * Register listener for configuration changes.
     *
     * @param listener the listener
     * @return the auto closeable listener registration
     */
    Registration registerListener(ConfigurationListener listener);

    /**
     * Get single property from configuration service.
     *
     * @param <T>         property type
     * @param key         property key
     * @param transformer property type transformer
     * @return property property
     */
    <T> T getProperty(String key, Function<String, T> transformer);

    /**
     * Return this service as a {@link OpenflowProviderConfig}.
     *
     * @return A {@link OpenflowProviderConfig} instance reflecting this services' state
     */
    OpenflowProviderConfig toProviderConfig();
}
