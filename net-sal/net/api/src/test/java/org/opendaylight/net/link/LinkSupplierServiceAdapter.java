/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.link;

import org.opendaylight.net.model.ConnectionPoint;
import org.opendaylight.net.model.DeviceId;
import org.opendaylight.net.model.Link;
import org.opendaylight.net.model.LinkInfo;

/**
 * Adapter of {@link org.opendaylight.net.link.LinkSupplierService} for unit tests
 *
 * @author Marjorie Krueger
 */
public class LinkSupplierServiceAdapter implements LinkSupplierService {

    @Override
    public Link createOrUpdateLink(ConnectionPoint src, ConnectionPoint dst,
                                   LinkInfo linkInfo) {
        return null;
    }

    @Override
    public void removeLink(ConnectionPoint src, ConnectionPoint dst) {

    }

    @Override
    public void removeAllLinks(DeviceId deviceId) {

    }

    @Override
    public void removeAllLinks(ConnectionPoint cp) {

    }
}
