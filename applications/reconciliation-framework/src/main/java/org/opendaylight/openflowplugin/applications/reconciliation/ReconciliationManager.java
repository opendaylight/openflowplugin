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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.framework.config.rev170712.Intent;

public interface ReconciliationManager {
    /*
     * Application who are interested in reconcilation should use this API to
     * register their services to the RF
     *
     * @param object - the reference to the ReconciliationNotificationListener
     */
    void registerService(ReconciliationNotificationListener object);

    /*
     * Plugin will use this api to trigger reconciliation when dpn connects
     *
     * @param nodeId - Node InstanceIdentifier
     *
     * @return Intent
     */
    Future<Intent> triggerReconciliation(NodeId nodeId);

    /*
     * Plugin will use this api to stop reconciliation when dpn disconnects
     *
     * @param nodeId - Node InstanceIdentifier
     */
    void haltReconciliation(NodeId nodeId);

    /*
     * API exposed by RF for get list of registered services
     *
     * @return the Map containing registered services with priority as Key
     */
    Map<Integer, List<ReconciliationNotificationListener>> getRegisteredServices();

}
