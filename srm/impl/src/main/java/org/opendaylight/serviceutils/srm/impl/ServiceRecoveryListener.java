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
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.serviceutils.srm.ServiceRecoveryRegistry;
import org.opendaylight.serviceutils.tools.mdsal.listener.AbstractClusteredSyncDataTreeChangeListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.ops.rev180626.ServiceOps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.ops.rev180626.service.ops.Services;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.ops.rev180626.service.ops.services.Operations;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.EntityNameBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.EntityTypeBase;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.RequireServiceComponentRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component
@RequireServiceComponentRuntime
public final class ServiceRecoveryListener extends AbstractClusteredSyncDataTreeChangeListener<Operations> {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceRecoveryListener.class);

    private final ServiceRecoveryRegistry serviceRecoveryRegistry;

    @Inject
    @Activate
    public ServiceRecoveryListener(@Reference DataBroker dataBroker,
                                   @Reference ServiceRecoveryRegistry serviceRecoveryRegistry) {
        super(dataBroker, LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(ServiceOps.class)
                .child(Services.class).child(Operations.class));
        this.serviceRecoveryRegistry = serviceRecoveryRegistry;
    }

    @Override
    @Deprecated
    public void add(InstanceIdentifier<Operations> instanceIdentifier, Operations operations) {
        LOG.info("Service Recovery operation triggered for service: {}", operations);
        recoverService(operations.getEntityType(), operations.getEntityName(), operations.getEntityId());
    }

    /**
     * Initiates recovery mechanism for a particular interface-manager entity. This method tries to check whether there
     * is a registered handler for the incoming service recovery request within interface-manager and redirects the call
     * to the respective handler if found.
     *
     * @param entityType The type of service recovery. eg :SERVICE or INSTANCE.
     * @param entityName The type entity for which recovery has to be started. eg : INTERFACE or DPN.
     * @param entityId The unique id to represent the entity to be recovered
     */
    private void recoverService(EntityTypeBase entityType, EntityNameBase entityName, String entityId) {
        String serviceRegistryKey = entityName.toString();
        serviceRecoveryRegistry.getRegisteredServiceRecoveryHandler(serviceRegistryKey).recoverService(entityId);
    }

    @Override
    @Deprecated
    public void remove(InstanceIdentifier<Operations> instanceIdentifier, Operations removedDataObject) {
        // FIXME: this should be doing something, right?
    }

    @Override
    @Deprecated
    public void update(InstanceIdentifier<Operations> instanceIdentifier,
                       Operations originalDataObject, Operations updatedDataObject) {
        add(instanceIdentifier, updatedDataObject);
    }
}
