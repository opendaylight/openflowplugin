/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.reconciliation.impl;

import org.opendaylight.openflowplugin.applications.reconciliation.NotificationRegistration;

public class ReconciliationServiceDelegate implements NotificationRegistration {

    private final AutoCloseable unregisterService;

    public ReconciliationServiceDelegate(AutoCloseable unregisterService) {
        this.unregisterService = unregisterService;
    }

    @Override
    public void close() throws Exception {
        this.unregisterService.close();
    }
}
