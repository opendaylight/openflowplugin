/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.helper;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Created by mirehak on 9/2/15.
 */
public class TestSupplierVerifyHelper {

    private TestSupplierVerifyHelper() {
        throw new UnsupportedOperationException("Test utility class");
    }

    /**
     * check if wildcarded path is not null
     *
     * @param dataBroker
     */
    public static void verifyDataChangeRegistration(DataBroker dataBroker) {
        Mockito.verify(dataBroker).registerDataChangeListener(
                Matchers.eq(LogicalDatastoreType.OPERATIONAL),
                Matchers.notNull(InstanceIdentifier.class),
                Matchers.notNull(DataChangeListener.class),
                Matchers.eq(AsyncDataBroker.DataChangeScope.BASE));
    }
}
