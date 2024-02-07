/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupCacheManager;
import org.opendaylight.openflowplugin.api.openflow.ReconciliationState;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Singleton
@Component
public class FlowGroupCacheManagerImpl implements FlowGroupCacheManager {
    private final ConcurrentMap<String, ReconciliationState> reconciliationStates = new ConcurrentHashMap<>();

    @Inject
    @Activate
    public FlowGroupCacheManagerImpl() {
        // Exposed for DI
    }

    @Override
    public Map<String, ReconciliationState> getReconciliationStates() {
        return reconciliationStates;
    }
}