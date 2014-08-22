/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.supplier;

/**
 * Base implementation of a supplier service that can be invalidated.
 *
 * @author Thomas Vachuska
 */
public abstract class AbstractSupplierService implements SupplierService {

    private static final String E_NOT_VALID = "Supplier service no longer valid";

    private volatile boolean isValid = true;

    /**
     * Marks the supplier service as invalid.
     */
    protected void invalidate() {
        isValid = false;
    }

    /**
     * Asserts whether or not the supplier service is still valid.
     *
     * @throws java.lang.IllegalStateException if no longer valid
     */
    protected void validate() {
        if (!isValid)
            throw new IllegalStateException(E_NOT_VALID);
    }

}
