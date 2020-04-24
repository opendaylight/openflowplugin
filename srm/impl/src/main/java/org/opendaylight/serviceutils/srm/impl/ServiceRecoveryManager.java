/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.srm.impl;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.EntityNameBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.EntityTypeBase;

@Singleton
public final class ServiceRecoveryManager {

    private final ServiceRecoveryRegistryImpl serviceRecoveryRegistry;

    @Inject
    public ServiceRecoveryManager(ServiceRecoveryRegistryImpl serviceRecoveryRegistry) {
        this.serviceRecoveryRegistry = serviceRecoveryRegistry;
    }

    private static String getServiceRegistryKey(Class<? extends EntityNameBase> entityName) {
        return entityName.toString();
    }

    /**
     * Initiates recovery mechanism for a particular interface-manager entity.
     * This method tries to check whether there is a registered handler for the incoming
     * service recovery request within interface-manager and redirects the call
     * to the respective handler if found.
     *  @param entityType
     *            The type of service recovery. eg :SERVICE or INSTANCE.
     * @param entityName
     *            The type entity for which recovery has to be started. eg : INTERFACE or DPN.
     * @param entityId
     *            The unique id to represent the entity to be recovered
     */
    public void recoverService(Class<? extends EntityTypeBase> entityType,
                                      Class<? extends EntityNameBase> entityName, String entityId) {
        String serviceRegistryKey = getServiceRegistryKey(entityName);
        serviceRecoveryRegistry.getRegisteredServiceRecoveryHandler(serviceRegistryKey).recoverService(entityId);
    }
}
