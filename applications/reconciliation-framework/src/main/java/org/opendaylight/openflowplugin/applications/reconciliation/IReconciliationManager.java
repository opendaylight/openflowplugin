/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.reconciliation;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public interface IReconciliationManager {
        public void registerService(IReconciliationTaskFactory object);

        public Future<NodeId> startReconciliation(NodeId nodeId);

        public void cancelReconciliation(NodeId nodeId);

        Map<Integer, List<IReconciliationTaskFactory>> getRegisteredServices();

}
