/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.host;

import org.opendaylight.net.model.Host;
import org.opendaylight.net.model.HostId;
import org.opendaylight.net.model.HostInfo;

/**
 * An adapter for the {@link HostSupplierService} API, provided specifically for
 * unit tests and implementers to use, to insulate from changes in the API.
 *
 * @author Vikram Bobade
 */
public class HostSupplierServiceAdapter implements HostSupplierService {
    @Override public Host createOrUpdateHost(HostId id, HostInfo info) { return null; }
    @Override public void removeHost(HostId id) { }
}
