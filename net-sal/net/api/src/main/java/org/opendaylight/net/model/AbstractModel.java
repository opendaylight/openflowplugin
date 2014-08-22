/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.opendaylight.net.supplier.SupplierId;

/**
 * Base model entity.
 *
 * @author Thomas Vachuska
 */
public class AbstractModel implements Model {

    private final SupplierId supplierId;
    private long timestamp = System.currentTimeMillis();

    /**
     * Creates an abstract model w/o supplier id.
     */
    protected AbstractModel() {
        supplierId = null;
    }

    /**
     * Creates an abstract model tied to the given supplier.
     *
     * @param supplierId supplier id
     */
    protected AbstractModel(SupplierId supplierId) {
        this.supplierId = supplierId;
    }

    @Override
    public SupplierId supplierId() {
        return supplierId;
    }

    @Override
    public long timestamp() {
        return timestamp;
    }

    /**
     * Sets the last modification timestamp in millis since start of epoch.
     *
     * @param timestamp modification time
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
