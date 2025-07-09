/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.arbitratorreconciliation.impl;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataListener;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.upgrade.rev180702.UpgradeConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.upgrade.rev180702.UpgradeConfigBuilder;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link UpgradeState}, driven by datastore updates.
 */
@Singleton
@Component(service = UpgradeState.class)
public final class DefaultUpgradeState implements UpgradeState, DataListener<UpgradeConfig>, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultUpgradeState.class);

    private final AtomicBoolean isUpgradeInProgress = new AtomicBoolean(false);
    private final DataBroker dataBroker;
    private final Registration registration;

    @Inject
    @Activate
    public DefaultUpgradeState(@Reference final DataBroker dataBroker) {
        this.dataBroker = requireNonNull(dataBroker);
        registration = dataBroker.registerDataListener(LogicalDatastoreType.CONFIGURATION,
            DataObjectIdentifier.builder(UpgradeConfig.class).build(), this);
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
        // FIXME: transaction chain for updates and do not .get() below
        var tx = dataBroker.newWriteOnlyTransaction();
        tx.put(LogicalDatastoreType.OPERATIONAL, DataObjectIdentifier.builder(UpgradeConfig.class).build(),
            new UpgradeConfigBuilder().setUpgradeInProgress(upgradeConfig).build());

        try {
            tx.commit().get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.warn("Failed to write operational state", e);
        }
    }
}
