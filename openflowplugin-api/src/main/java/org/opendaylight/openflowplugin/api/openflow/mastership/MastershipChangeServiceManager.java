/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.mastership;

import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.OwnershipChangeListener;

/**
 * Provider to register mastership change listener.
 * Provider to set mastership reconciliation framework.
 * @since 0.5.0 Nitrogen
 */
public interface MastershipChangeServiceManager extends OwnershipChangeListener, AutoCloseable {

    /**
     * Register of mastership change listener. Returned registration need to be closed by client.
     * It doesn't contain event for reconciliation framework event.
     * @param service implementation of {@link MastershipChangeService}
     * @return registration
     * @since 0.5.0 Nitrogen
     * @see ReconciliationFrameworkEvent
     */
    @Nonnull
    MastershipChangeRegistration register(@Nonnull MastershipChangeService service);

    /**
     * Setter for reconciliation framework event listener. It can be registered only once.
     * Another registrations will be ignored.
     * @see ReconciliationFrameworkEvent
     * @since 0.5.0 Nitrogen
     * @param mastershipRFRegistration reconciliation framework
     */
    void reconciliationFrameworkRegistration(@Nonnull ReconciliationFrameworkEvent mastershipRFRegistration);

    /**
     * Unregister of listener. Registration after unregister need to be closed by client.
     * @param service implementation of {@link MastershipChangeRegistration}
     * @since 0.5.0 Nitrogen
     */
    void unregister(@Nonnull MastershipChangeRegistration service);

    @Override
    void close();
}
