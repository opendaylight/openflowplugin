/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.supplier;

import java.util.Set;

/**
 * Abstraction of a supplier broker, capable of tracking various suppliers.
 *
 * @author Thomas Vachuska
 * @author Simon Hunt
 * @author Uyen Chau
 * @author Shaun Wackerly
 * @author Marjorie Krueger
 * @author Vikram Bobade
 * @author Ryan Tidwell
 */
public interface SuppliersBroker<S extends Supplier, T extends SupplierService> {

    /**
     * Registers a new supplier and issues it a service interface through which
     * the supplier can submit information.
     *
     * @param supplier supplier of network information
     * @return issued supplier service
     */
    T registerSupplier(S supplier);

    /**
     * Cancels supplier registration using the supplier id.
     *
     * @param supplier the supplier to be unregistered
     */
    void unregisterSupplier(S supplier);

    /**
     * Returns the set of all currently registered suppliers.
     *
     * @return set of registered suppliers
     */
    Set<S> getSuppliers();

}
