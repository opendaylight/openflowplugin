/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.old.notification.supplier;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import java.util.List;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.applications.old.notification.supplier.tools.OldNotifProviderConfig;

/**
 * Provider Implementation
 */
public class OldNotifProviderImpl implements OldNotifProvider {

    private final DataBroker db;
    private final OldNotifProviderConfig config;
    private final NotificationProviderService nps;

    /* Supplier List property help for easy close method implementation and testing */
    private List<OldNotifSupplierDefinition<?>> supplierList;

    /**
     * Provider constructor set all needed final parameters
     *
     * @param config - Configuration Object
     * @param nps - notifProviderService
     * @param db - dataBroker
     */
    public OldNotifProviderImpl(final OldNotifProviderConfig config,
            final NotificationProviderService nps, final DataBroker db) {
        this.config = Preconditions.checkNotNull(config);
        this.db = Preconditions.checkNotNull(db);
        this.nps = Preconditions.checkNotNull(nps);
    }

    @Override
    public void start() {
    }

    @Override
    public void close() throws Exception {
        for (OldNotifSupplierDefinition<?> supplier : supplierList) {
            if (supplier != null) {
                supplier.close();
                supplier = null;
            }
        }
    }

    @VisibleForTesting
    List<OldNotifSupplierDefinition<?>> getSupplierList() {
        return supplierList;
    }
}

