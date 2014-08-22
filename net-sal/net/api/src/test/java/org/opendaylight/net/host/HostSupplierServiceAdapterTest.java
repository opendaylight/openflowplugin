/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.host;

import org.opendaylight.util.junit.GenericAdapterTest;

/**
 * A unit test for the {@link HostSupplierServiceAdapter} API
 *
 * @author Vikram Bobade
 */

public class HostSupplierServiceAdapterTest extends GenericAdapterTest<HostSupplierServiceAdapter> {
    @Override
    protected HostSupplierServiceAdapter instance() {
        return new HostSupplierServiceAdapter();
    }
}
