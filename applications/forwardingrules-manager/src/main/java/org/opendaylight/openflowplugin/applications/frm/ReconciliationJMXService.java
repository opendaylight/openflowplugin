/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupCacheManager;

public class ReconciliationJMXService implements ReconciliationJMXServiceMBean {
    private FlowGroupCacheManager flowGroupCacheManager;

    @Inject
    public ReconciliationJMXService(final FlowGroupCacheManager floGroupCacheManager) {
        this.flowGroupCacheManager = floGroupCacheManager;
    }

    @Override
    public Map<String, String> acquireReconciliationStates() {
        Map<String, String> reconciliationStatesMap = new HashMap<>();
        flowGroupCacheManager.getReconciliationStates().forEach((datapathId, reconciliationState) ->
                reconciliationStatesMap.put(datapathId, reconciliationState.toString()));
        return reconciliationStatesMap;
    }
}
