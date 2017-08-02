/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.lifecycle;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;

public interface ReconciliationFrameworkRegistrar {
    /**
     * Check if reconciliation framework is registered.
     * If not the event {@link OwnershipChangeListener#becomeMasterBeforeSubmittedDS(DeviceInfo)}
     * will not be triggered.
     * @return true if there exists any reconciliation framework registration
     */
    boolean isReconciliationFrameworkRegistered();
}