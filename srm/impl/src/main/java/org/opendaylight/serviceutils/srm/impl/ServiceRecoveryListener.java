/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.serviceutils.srm.impl;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.serviceutils.tools.mdsal.listener.AbstractClusteredSyncDataTreeChangeListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.ops.rev180626.ServiceOps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.ops.rev180626.service.ops.Services;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.ops.rev180626.service.ops.services.Operations;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ServiceRecoveryListener extends AbstractClusteredSyncDataTreeChangeListener<Operations> {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceRecoveryListener.class);

    private final ServiceRecoveryManager serviceRecoveryManager;

    @Inject
    public ServiceRecoveryListener(DataBroker dataBroker, ServiceRecoveryManager serviceRecoveryManager) {
        super(dataBroker, LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(ServiceOps.class)
                .child(Services.class).child(Operations.class));
        this.serviceRecoveryManager = serviceRecoveryManager;
    }

    @Override
    public void add(@Nonnull InstanceIdentifier<Operations> instanceIdentifier, @Nonnull Operations operations) {
        LOG.info("Service Recovery operation triggered for service: {}", operations);
        serviceRecoveryManager.recoverService(operations.getEntityType(), operations.getEntityName(),
                operations.getEntityId());
    }

    @Override
    public void remove(@Nonnull InstanceIdentifier<Operations> instanceIdentifier,
                       @Nonnull Operations removedDataObject) {
    }

    @Override
    public void update(@Nonnull InstanceIdentifier<Operations> instanceIdentifier,
                       @Nonnull Operations originalDataObject, @Nonnull Operations updatedDataObject) {
        add(instanceIdentifier, updatedDataObject);
    }
}
