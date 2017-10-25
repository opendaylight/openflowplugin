/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.notification.supplier.impl.helper;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;

public class TestSupplierVerifyHelper {

    private TestSupplierVerifyHelper() {
        throw new UnsupportedOperationException("Test utility class");
    }

    public static void verifyDataTreeChangeListenerRegistration(DataBroker dataBroker) {

        Mockito.verify(dataBroker).registerDataTreeChangeListener(
                Matchers.<DataTreeIdentifier>any(),
                Matchers.<DataTreeChangeListener>any());
    }
}
