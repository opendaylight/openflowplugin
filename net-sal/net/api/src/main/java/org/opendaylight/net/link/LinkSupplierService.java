/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.link;

import org.opendaylight.net.supplier.SupplierService;
import org.opendaylight.net.model.ConnectionPoint;
import org.opendaylight.net.model.DeviceId;
import org.opendaylight.net.model.Link;
import org.opendaylight.net.model.LinkInfo;

/**
 * Set of services available for link suppliers to contribute information about
 * infrastructure links.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 * @author Simon Hunt
 * @author Marjorie Krueger
 */
public interface LinkSupplierService extends SupplierService {

    /**
     * Returns a new or updated infrastructure {@link org.opendaylight.net.model.Link}
     * and updates the link information store. Note that the link.info returned
     * may not match the linkInfo passed in as the supplier updates the stored
     * info if appropriate.
     *
     * @param src source connection point
     * @param dst destination connection point
     * @param linkInfo infrastructure link information to update
     * @return a copy of the Link saved as a result of this call
     */
    Link createOrUpdateLink(ConnectionPoint src, ConnectionPoint dst,
                            LinkInfo linkInfo);

    /**
     * Removes an infrastructure link from the cache.
     *
     * @param src source connection point
     * @param dst destination connection point
     */
    void removeLink(ConnectionPoint src, ConnectionPoint dst);

    /**
     * Removes all infrastructure links to/from the given device.
     *
     * @param deviceId device whose links will be removed from the cache.
     */
    void removeAllLinks(DeviceId deviceId);

    /**
     *  Removes all infrastructure links to/from the given connection point.
     *
     * @param cp connection point whose links will be removed from the cache.
     */
    void removeAllLinks(ConnectionPoint cp);

}
