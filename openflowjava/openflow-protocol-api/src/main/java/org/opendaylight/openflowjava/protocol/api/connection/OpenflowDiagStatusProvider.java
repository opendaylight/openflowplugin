/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.api.connection;

import org.opendaylight.infrautils.diagstatus.ServiceState;

public interface OpenflowDiagStatusProvider {

    void reportStatus(ServiceState serviceState);

    void reportStatus(String diagStatusService, Throwable throwable);

    void reportStatus(String diagStatusIdentifier, ServiceState serviceState, String description);

    void reportStatus(String diagStatusIdentifier, ServiceState serviceState);
}


