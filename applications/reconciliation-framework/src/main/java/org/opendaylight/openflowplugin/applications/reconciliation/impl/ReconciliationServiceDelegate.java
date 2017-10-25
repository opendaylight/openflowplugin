/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.reconciliation.impl;

import com.google.common.base.Preconditions;
import org.opendaylight.openflowplugin.applications.reconciliation.NotificationRegistration;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationNotificationListener;

public class ReconciliationServiceDelegate implements NotificationRegistration {

    private ReconciliationNotificationListener reconciliationNotificationListener;
    private AutoCloseable unregisterService;

    public ReconciliationServiceDelegate(ReconciliationNotificationListener reconciliationNotificationListener,
                                         AutoCloseable unregisterService) {
        this.reconciliationNotificationListener = Preconditions.checkNotNull(reconciliationNotificationListener,
                                                                             "ReconciliationNotificationListener can "
                                                                                     + "not be null!");
        this.unregisterService = unregisterService;
    }

    @Override
    public void close() throws Exception {
        this.unregisterService.close();
        this.reconciliationNotificationListener.close();
    }
}
