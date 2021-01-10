/*
 * Copyright (c) 2020 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.inject.Inject;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupCacheManager;
import org.opendaylight.openflowplugin.api.openflow.ReconciliationState;

public class ReconciliationJMXService implements ReconciliationJMXServiceMBean {
    private final FlowGroupCacheManager flowGroupCacheManager;

    @Inject
    public ReconciliationJMXService(final FlowGroupCacheManager floGroupCacheManager) {
        this.flowGroupCacheManager = floGroupCacheManager;
    }

    @Override
    public Map<String, String> acquireReconciliationStates() {
        return Maps.transformValues(flowGroupCacheManager.getReconciliationStates(), ReconciliationState::toString);
    }
}
