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
import javax.annotation.Nonnull;

/**
 * Manages OpenFlowPlugin configuration
 */
public interface ConfigurationService extends AutoCloseable {

    /**
     * Update map of properties
     *
     * @param properties properties
     */
    void update(@Nonnull Map<String, String> properties);

    /**
     * Register listener for configuration changes.
     *
     * @param listener the listener
     * @return the auto closeable listener registration
     */
    @Nonnull
    AutoCloseable registerListener(@Nonnull ConfigurationListener listener);

    /**
     * Get single property from configuration service
     *
     * @param <T>         property type
     * @param key         property key
     * @param transformer property type transformer
     * @return property property
     */
    @Nonnull
    <T> T getProperty(@Nonnull String key, @Nonnull Function<String, T> transformer);

}
