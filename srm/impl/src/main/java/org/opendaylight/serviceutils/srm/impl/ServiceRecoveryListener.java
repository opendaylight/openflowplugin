/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.serviceutils.srm.impl;

import static java.util.Objects.requireNonNull;

import java.util.List;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.serviceutils.srm.ServiceRecoveryRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.ops.rev180626.ServiceOps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.ops.rev180626.service.ops.Services;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.ops.rev180626.service.ops.services.Operations;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.EntityNameBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.EntityTypeBase;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(service = { })
public final class ServiceRecoveryListener implements AutoCloseable, DataTreeChangeListener<Operations>,
        ChainableDataTreeChangeListener<Operations> {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceRecoveryListener.class);

    private final ChainableDataTreeChangeListenerImpl<Operations> chainingDelegate =
        new ChainableDataTreeChangeListenerImpl<>();
    private final ServiceRecoveryRegistry serviceRecoveryRegistry;
    private final Registration dataChangeListenerRegistration;

    @Inject
    @Activate
    public ServiceRecoveryListener(@Reference final DataBroker dataBroker,
            @Reference final ServiceRecoveryRegistry serviceRecoveryRegistry) {
        this.serviceRecoveryRegistry = requireNonNull(serviceRecoveryRegistry);
        dataChangeListenerRegistration = dataBroker.registerTreeChangeListener(LogicalDatastoreType.OPERATIONAL,
            DataObjectReference.builder(ServiceOps.class).child(Services.class).child(Operations.class).build(), this);
    }

    @Override
    @PreDestroy
    @Deactivate
    public void close() {
        dataChangeListenerRegistration.close();
    }

    @Override
    @Deprecated
    public void addBeforeListener(final DataTreeChangeListener<Operations> listener) {
        chainingDelegate.addBeforeListener(listener);
    }

    @Override
    @Deprecated
    public void addAfterListener(final DataTreeChangeListener<Operations> listener) {
        chainingDelegate.addAfterListener(listener);
    }

    @Override
    public void onDataTreeChanged(final List<DataTreeModification<Operations>> changes) {
        for (var dataTreeModification : changes) {
            var instanceIdentifier = dataTreeModification.path();
            var dataObjectModification = dataTreeModification.getRootNode();
            var dataBefore = dataObjectModification.dataBefore();
            var dataAfter = dataObjectModification.dataAfter();

            switch (dataObjectModification.modificationType()) {
                case null -> throw new NullPointerException();
                case SUBTREE_MODIFIED -> update(instanceIdentifier, dataBefore, dataAfter);
                case DELETE -> remove(instanceIdentifier, dataBefore);
                case WRITE -> {
                    if (dataBefore == null) {
                        add(instanceIdentifier, dataAfter);
                    } else {
                        update(instanceIdentifier, dataBefore, dataAfter);
                    }
                }
            }
        }
    }

    private void add(final DataObjectIdentifier<Operations> instanceIdentifier, final Operations operations) {
        LOG.info("Service Recovery operation triggered for service: {}", operations);
        recoverService(operations.getEntityType(), operations.getEntityName(), operations.getEntityId());
    }

    private void remove(final DataObjectIdentifier<Operations> instanceIdentifier, final Operations removedDataObject) {
        // FIXME: this should be doing something, right?
    }

    private void update(final DataObjectIdentifier<Operations> instanceIdentifier, final Operations originalDataObject,
            final Operations updatedDataObject) {
        add(instanceIdentifier, updatedDataObject);
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
    private void recoverService(final EntityTypeBase entityType, final EntityNameBase entityName,
            final String entityId) {
        String serviceRegistryKey = entityName.toString();
        serviceRecoveryRegistry.getRegisteredServiceRecoveryHandler(serviceRegistryKey).recoverService(entityId);
    }
}
