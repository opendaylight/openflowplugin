/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.spi.connection;

import java.util.List;
import org.opendaylight.infrautils.diagstatus.ServiceState;

public interface OpenflowPluginDiagStatusProviders {

    void reportStatus(ServiceState serviceState);

    void reportStatus(ServiceState serviceState, Throwable throwable);

    void reportStatus(String diagStatusService, Throwable throwable);

    void reportStatus(String diagStatusIdentifier, String threadName);

    void reportStatus(String diagStatusIdentifier);

    void reportStatus(ServiceState serviceState, String description);

    void reportStatus(String diagStatusIdentifier, List<Boolean> result);
}


