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
import org.opendaylight.net.supplier.SupplierService;

/**
 * Provides write access to host-related information in the network model.
 * For read access, see {@link HostService}.
 *
 * @author Shaun Wackerly
 * @author Vikram Bobade
 */
public interface HostSupplierService extends SupplierService {

    /**
     * Creates a new host or updates an existing host with the given
     * host-related information. Any specified information will overwrite
     * existing information. Any information left unspecified will not
     * overwrite what already exists.
     *
     * @param id the identifier for the host to create/update
     * @param info host-related information to overwrite
     * @return the created or updated host
     */
    Host createOrUpdateHost(HostId id, HostInfo info);

    /**
     * Removes a specific host from the data store.
     *
     * @param id the identifier for the host to remove
     */
    void removeHost(HostId id);

}
