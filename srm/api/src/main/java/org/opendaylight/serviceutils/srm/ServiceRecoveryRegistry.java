/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.srm;

import java.util.Queue;

public interface ServiceRecoveryRegistry {

    void registerServiceRecoveryRegistry(String entityName,
                                         ServiceRecoveryInterface serviceRecoveryHandler);

    void addRecoverableListener(String serviceName, RecoverableListener recoverableListener);

    void removeRecoverableListener(String serviceName, RecoverableListener recoverableListener);

    Queue<RecoverableListener> getRecoverableListeners(String serviceName);
}