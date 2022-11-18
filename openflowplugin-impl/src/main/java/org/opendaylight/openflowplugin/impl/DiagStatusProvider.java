/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl;

import org.opendaylight.infrautils.diagstatus.ServiceState;

public interface DiagStatusProvider {

    void reportStatus(ServiceState serviceState);

    void reportStatus(ServiceState serviceState, Throwable throwable);

    void reportStatus(ServiceState serviceState, String description);
}
