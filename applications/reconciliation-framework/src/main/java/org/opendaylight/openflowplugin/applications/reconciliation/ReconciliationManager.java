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

/*
 * Provider to register the service to reconciliation framework
 *
 */
public interface ReconciliationManager {
    /*
     * Application who are interested in reconciliation should use this API to
     * register their services to the Reconciliation Framework
     *
     * @param object - the reference to the ReconciliationNotificationListener
     */
    NotificationRegistration registerService(ReconciliationNotificationListener service);

    /*
     * API exposed by RF for get list of registered services
     *
     * @return the Map containing registered services with priority as Key
     */
    Map<Integer, List<ReconciliationNotificationListener>> getRegisteredServices();
}
