/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.direct;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The Opendaylight direct statistics service provider.
 */
public class OpendaylightDirectStatisticsServiceProvider {
    private Map<Class<? extends AbstractDirectStatisticsService>, AbstractDirectStatisticsService> services = new HashMap<>();

    /**
     * Register direct statistics service.
     *
     * @param type    the service type
     * @param service the service instance
     */
    public void register(Class<? extends AbstractDirectStatisticsService> type, AbstractDirectStatisticsService service) {
        if (services.containsKey(type)) return;

        services.put(type, service);
    }

    /**
     * Lookup direct statistics service.
     *
     * @param <T>  the type parameter
     * @param type the service type
     * @return the service instance
     */
    public <T extends AbstractDirectStatisticsService> Optional<T> lookup(Class<T> type) {
        if (!services.containsKey(type)) return Optional.empty();

        return Optional.of(type.cast(services.get(type)));
    }
}
