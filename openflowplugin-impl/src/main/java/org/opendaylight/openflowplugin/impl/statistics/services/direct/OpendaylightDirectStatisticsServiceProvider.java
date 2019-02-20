/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services.direct;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import java.util.Optional;

/**
 * The Opendaylight direct statistics service provider.
 */
public class OpendaylightDirectStatisticsServiceProvider {
    private final ClassToInstanceMap<AbstractDirectStatisticsService> services = MutableClassToInstanceMap.create();

    /**
     * Register direct statistics service.
     *
     * @param type    the service type
     * @param service the service instance
     */
    public void register(Class<? extends AbstractDirectStatisticsService> type,
                               AbstractDirectStatisticsService service) {
        services.put(type, service);
    }

    /**
     * Lookup direct statistics service.
     *
     * @param type the service type
     * @return the service instance
     */
    public Optional<? extends AbstractDirectStatisticsService>
            lookup(Class<? extends AbstractDirectStatisticsService> type) {
        return Optional.ofNullable(services.getInstance(type));
    }
}
