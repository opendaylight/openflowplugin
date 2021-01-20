/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core;

import static com.google.common.base.Verify.verifyNotNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceState;
import org.opendaylight.openflowjava.protocol.api.connection.OpenflowDiagStatusProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
// FIXME: integrate with DefaultOpenflowDiagStatusProvider once we have OSGi R7
public final class OSGiOpenflowDiagStatusProvider implements OpenflowDiagStatusProvider {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiOpenflowDiagStatusProvider.class);

    @Reference
    DiagStatusService diagStatus;

    private DefaultOpenflowDiagStatusProvider delegate = null;

    @Override
    public void reportStatus(final ServiceState serviceState) {
        delegate().reportStatus(serviceState);
    }

    @Override
    public void reportStatus(final String diagStatusService, final Throwable throwable) {
        delegate().reportStatus(diagStatusService, throwable);
    }

    @Override
    public void reportStatus(final String diagStatusIdentifier, final ServiceState serviceState,
            final String description) {
        delegate().reportStatus(diagStatusIdentifier, serviceState, description);
    }

    @Override
    public void reportStatus(final String diagStatusIdentifier, final ServiceState serviceState) {
        delegate().reportStatus(diagStatusIdentifier, serviceState);
    }

    @Activate
    void activate() {
        delegate = new DefaultOpenflowDiagStatusProvider(diagStatus);
        LOG.info("OpenFlow diagnostic status provider activated");
    }

    @Deactivate
    void deactivate() {
        delegate.close();
        delegate = null;
        LOG.info("OpenFlow diagnostic status provider deactivated");
    }

    private @NonNull DefaultOpenflowDiagStatusProvider delegate() {
        return verifyNotNull(delegate);
    }
}
