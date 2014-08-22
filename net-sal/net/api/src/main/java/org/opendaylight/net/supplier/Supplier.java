/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.supplier;

/**
 * Representation of an entity responsible for supplying information into
 * network information base.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 * @author Simon Hunt
 */
public interface Supplier {

    /**
     * Returns the supplier id.
     *
     * @return supplier identifier
     */
    SupplierId supplierId();

}
