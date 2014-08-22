/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.opendaylight.net.supplier.SupplierId;

/**
 * Base network information model entity.
 *
 * @author Thomas Vachuska
 */
public interface Model {

    /**
     * Returns the id of the supplier that provided this model entity.
     *
     * @return supplier id
     */
    SupplierId supplierId();

    /**
     * Returns the last refresh timestamp in millis since start of epoch.
     *
     * @return refresh timestamp
     */
    long timestamp();

}
