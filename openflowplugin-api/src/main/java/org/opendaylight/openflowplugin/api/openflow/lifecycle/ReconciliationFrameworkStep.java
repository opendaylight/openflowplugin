/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.lifecycle;

public interface ReconciliationFrameworkStep {
    /**
     * Allow to continue after reconciliation framework callback success.
     * @since 0.5.0 Nitrogen
     * @see org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeService
     * @see OwnershipChangeListener#isReconciliationFrameworkRegistered()
     */
    void continueInitializationAfterReconciliation();
}