/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.reconciliation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.framework.config.rev170712.IntentType;

public interface ReconciliationManager {
    void registerService(ReconciliationTaskFactory object);

    Future<IntentType> startReconciliation(NodeId nodeId);

    void cancelReconciliation(NodeId nodeId);

    Map<Integer, List<ReconciliationTaskFactory>> getRegisteredServices();

}
