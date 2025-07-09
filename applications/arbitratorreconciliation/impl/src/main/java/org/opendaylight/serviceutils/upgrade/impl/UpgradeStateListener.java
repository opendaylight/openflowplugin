/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.upgrade.impl;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.serviceutils.upgrade.UpgradeState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.upgrade.rev180702.UpgradeConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.upgrade.rev180702.UpgradeConfigBuilder;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(service = UpgradeState.class, immediate = true)
public final class UpgradeStateListener implements UpgradeState, DataListener<UpgradeConfig>, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(UpgradeStateListener.class);

    private final AtomicBoolean isUpgradeInProgress = new AtomicBoolean(false);
    private final DataBroker dataBroker;
    private final Registration registration;

    @Inject
    @Activate
    public UpgradeStateListener(@Reference final DataBroker dataBroker) {
        this.dataBroker = requireNonNull(dataBroker);
        registration = dataBroker.registerDataListener(DataTreeIdentifier.of(
            LogicalDatastoreType.CONFIGURATION, InstanceIdentifier.create(UpgradeConfig.class)), this);
    }

    @PreDestroy
    @Deactivate
    @Override
    public void close() {
        registration.close();
    }

    @Override
    public boolean isUpgradeInProgress() {
        return isUpgradeInProgress.get();
    }

    @Override
    public void dataChangedTo(final UpgradeConfig data) {
        final var upgradeConfig = data != null && data.requireUpgradeInProgress();
        isUpgradeInProgress.set(upgradeConfig);

        // FIXME: use ClusterSingletonService for these updates
        var tx = dataBroker.newWriteOnlyTransaction();
        tx.put(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(UpgradeConfig.class),
            new UpgradeConfigBuilder().setUpgradeInProgress(upgradeConfig).build());

        try {
            tx.commit().get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Failed to write mdsal config", e);
        }
    }
}
