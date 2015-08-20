/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.old.notification.supplier;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.applications.old.notification.supplier.tools.OldNotifProviderConfig;
import org.opendaylight.openflowplugin.applications.old.notification.supplier.tools.OldNotifProviderConfig.OldNotifProviderConfigBuilder;

public class OldNotifProviderImplTest {

    private NotificationProviderService notificationProviderService;
    private OldNotifProviderConfig config;
    private DataBroker dataBroker;

    @Before
    public void initialization() {
        dataBroker = mock(DataBroker.class);
        notificationProviderService = mock(NotificationProviderService.class);
    }

    @Test
    public void testCreateAllSuppliers() {
    }

    @Test
    public void testCreateRootSuppliersOnly() {
        final OldNotifProviderConfig config = createNonConfigSupplier();
        final OldNotifProviderImpl provider = new OldNotifProviderImpl(config, notificationProviderService, dataBroker);
        provider.start();
        final List<OldNotifSupplierDefinition<?>> listSuppliers = provider.getSupplierList();
        int nrOfSuppliers = 0;
        for (final OldNotifSupplierDefinition<?> supplier : listSuppliers) {
            if (supplier != null) {
                nrOfSuppliers++;
            }
        }
        assertEquals(2, nrOfSuppliers);
    }

    private OldNotifProviderConfig createAllConfigSupplier() {
        final OldNotifProviderConfigBuilder builder = new OldNotifProviderConfigBuilder();
        builder.setFlowStatSupport(true);
        builder.setFlowSupport(true);
        builder.setFlowTableStatSupport(true);
        builder.setGroupStatSupport(true);
        builder.setGroupSupport(true);
        builder.setMeterStatSupport(true);
        builder.setMeterSupport(true);
        builder.setNodeConnectorStatSupport(true);
        builder.setQueueStatSupport(true);
        return builder.build();
    }

    private OldNotifProviderConfig createNonConfigSupplier() {
        final OldNotifProviderConfigBuilder builder = new OldNotifProviderConfigBuilder();
        builder.setFlowStatSupport(false);
        builder.setFlowSupport(false);
        builder.setFlowTableStatSupport(false);
        builder.setGroupStatSupport(false);
        builder.setGroupSupport(false);
        builder.setMeterStatSupport(false);
        builder.setMeterSupport(false);
        builder.setNodeConnectorStatSupport(false);
        builder.setQueueStatSupport(false);
        return builder.build();
    }
}
