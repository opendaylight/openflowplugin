/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.statistics;

import org.opendaylight.openflowplugin.api.openflow.OFPContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.OwnershipChangeListener;

/**
 * Context for statistics.
 */
public interface StatisticsContext extends RequestContextStack, OFPContext {
    /**
     * On / Off scheduling.
     * @param schedulingEnabled true if scheduling should be enabled
     */
    void setSchedulingEnabled(boolean schedulingEnabled);

    /**
     * In case of using reconciliation framework need to be initialization submit handled separately.
     * @return true if submitting was ok
     * @since 0.5.0 Nitrogen
     * @see OwnershipChangeListener#isReconciliationFrameworkRegistered()
     * @see org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeService
     */
    boolean initialSubmitAfterReconciliation();
}
