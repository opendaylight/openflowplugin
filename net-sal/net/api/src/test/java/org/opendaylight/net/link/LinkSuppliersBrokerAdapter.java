/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.link;

import org.opendaylight.net.supplier.AbstractSuppliersBroker;

/**
 * An adapter for the {@link org.opendaylight.net.link.LinkSuppliersBroker} API,
 * provided specifically for unit tests and implementers to use, to insulate
 * from changes in the API.
 *
 * @author Thomas Vachuska
 */
public class LinkSuppliersBrokerAdapter
        extends AbstractSuppliersBroker<LinkSupplier, LinkSupplierService>
        implements LinkSuppliersBroker {

    @Override
    protected LinkSupplierService createSupplierService(LinkSupplier supplier) {
        return null;
    }

}
