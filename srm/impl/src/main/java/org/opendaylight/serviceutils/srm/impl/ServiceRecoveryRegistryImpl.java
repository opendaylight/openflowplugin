/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.srm.impl;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.inject.Singleton;
import org.apache.aries.blueprint.annotation.service.Service;
import org.opendaylight.serviceutils.srm.RecoverableListener;
import org.opendaylight.serviceutils.srm.ServiceRecoveryInterface;
import org.opendaylight.serviceutils.srm.ServiceRecoveryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Service(classes = ServiceRecoveryRegistry.class)
public class ServiceRecoveryRegistryImpl implements ServiceRecoveryRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceRecoveryRegistryImpl.class);

    private final Map<String, ServiceRecoveryInterface> serviceRecoveryRegistry = new ConcurrentHashMap<>();

    private final Map<String, Queue<RecoverableListener>> recoverableListenersMap =
            new ConcurrentHashMap<>();

    @Override
    public void registerServiceRecoveryRegistry(String entityName,
                                                ServiceRecoveryInterface serviceRecoveryHandler) {
        serviceRecoveryRegistry.put(entityName, serviceRecoveryHandler);
        LOG.trace("Registered service recovery handler for {}", entityName);
    }

    @Override
    public synchronized void addRecoverableListener(String serviceName, RecoverableListener recoverableListener) {
        recoverableListenersMap
                .computeIfAbsent(serviceName, k -> new ConcurrentLinkedQueue<>())
                .add(recoverableListener);
    }

    @Override
    public void removeRecoverableListener(String serviceName, RecoverableListener recoverableListener) {
        if (recoverableListenersMap.get(serviceName) != null) {
            recoverableListenersMap.get(serviceName).remove(recoverableListener);
        }
    }

    @Override
    public Queue<RecoverableListener> getRecoverableListeners(String serviceName) {
        return recoverableListenersMap.get(serviceName);
    }

    ServiceRecoveryInterface getRegisteredServiceRecoveryHandler(String entityName) {
        return serviceRecoveryRegistry.get(entityName);
    }
}
