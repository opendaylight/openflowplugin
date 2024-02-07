/*
 * Copyright (c) 2020 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupCacheManager;
import org.opendaylight.openflowplugin.api.openflow.ReconciliationState;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Singleton
@Component
public final class ReconciliationJMXService implements ReconciliationJMXServiceMBean {
    private final FlowGroupCacheManager flowGroupCacheManager;

    @Inject
    @Activate
    public ReconciliationJMXService(@Reference final FlowGroupCacheManager floGroupCacheManager) {
        flowGroupCacheManager = requireNonNull(floGroupCacheManager);
    }

    @Override
    public Map<String, String> acquireReconciliationStates() {
        return Map.copyOf(Maps.transformValues(flowGroupCacheManager.getReconciliationStates(),
            ReconciliationState::toString));
    }
}
