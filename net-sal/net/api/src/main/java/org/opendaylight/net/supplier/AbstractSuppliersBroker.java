/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.supplier;

import java.util.*;

/**
 * Base implementation of a broker for registering and tracking suppliers.
 *
 * @author Thomas Vachuska
 */
public abstract class AbstractSuppliersBroker<S extends Supplier, T extends SupplierService>
        implements SuppliersBroker<S, T> {

    private static final String E_ALREADY_REGISTERED = "Supplier already registered";

    private final Map<S, T> services = new HashMap<>();

    /**
     * Creates a supplier service for use by the specified supplier.
     *
     * @param supplier supplier for which to create a supplier service
     * @return new supplier service
     */
    protected abstract T createSupplierService(S supplier);

    /**
     * Invalidates the specified supplier service to prevent its further use.
     * <p>
     * The default implementation will attempt to use
     * {@link AbstractSupplierService#invalidate()} if the given supplier
     * service descends from {@link org.opendaylight.net.supplier.AbstractSupplierService}.
     * </p>
     *
     * @param service service to be invalidated
     */
    protected void invalidateSupplierService(T service) {
        if (service instanceof AbstractSupplierService)
            ((AbstractSupplierService) service).invalidate();
    }

    @Override
    public T registerSupplier(S supplier) {
        synchronized (services) {
            T service = services.get(supplier);
            if (service == null) {
                service = createSupplierService(supplier);
                services.put(supplier, service);
            }
            return service;
        }
    }

    @Override
    public void unregisterSupplier(S supplier) {
        synchronized (services) {
            T supplierService = services.remove(supplier);
            if (supplierService != null)
                invalidateSupplierService(supplierService);
        }
    }

    @Override
    public Set<S> getSuppliers() {
        return Collections.unmodifiableSet(services.keySet());
    }

}
