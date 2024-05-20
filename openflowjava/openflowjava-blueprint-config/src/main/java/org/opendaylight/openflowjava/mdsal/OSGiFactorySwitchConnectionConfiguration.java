/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.mdsal;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.List;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.KeystoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.PathType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.TransportProtocol;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow._switch.connection.config.rev160506.SwitchConnectionConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow._switch.connection.config.rev160506.SwitchConnectionConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow._switch.connection.config.rev160506._switch.connection.config.TlsBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A component responsible for populating default (factory) configuration.
 */
@Component(service = { })
public final class OSGiFactorySwitchConnectionConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiFactorySwitchConnectionConfiguration.class);

    @Activate
    public OSGiFactorySwitchConnectionConfiguration(@Reference final DataBroker dataBroker) {
        // Common for both cases
        final var builder = new SwitchConnectionConfigBuilder()
            .setTransportProtocol(TransportProtocol.TCP)
            .setGroupAddModEnabled(Boolean.FALSE)
            .setChannelOutboundQueueSize(Uint16.valueOf(1024))
            .setTls(new TlsBuilder()
                .setKeystore("configuration/ssl/ctl.jks")
                .setKeystoreType(KeystoreType.JKS)
                .setKeystorePathType(PathType.PATH)
                .setKeystorePassword("opendaylight")
                .setTruststore("configuration/ssl/truststore.jks")
                .setTruststoreType(KeystoreType.JKS)
                .setTruststorePathType(PathType.PATH)
                .setTruststorePassword("opendaylight")
                .setCertificatePassword("opendaylight")
                .setCipherSuites(List.of())
                .build());

        // Create OF switch connection provider on port 6653 (default)
        writeIfNotPresent(dataBroker, builder
            .setInstanceName("openflow-switch-connection-provider-default-impl")
            .setPort(Uint16.valueOf(6653))
            .build());
        // Create OF switch connection provider on port 6633 (legacy)
        writeIfNotPresent(dataBroker, builder
            .setInstanceName("openflow-switch-connection-provider-legacy-impl")
            .setPort(Uint16.valueOf(6633))
            .build());
    }

    private static void writeIfNotPresent(final DataBroker dataBroker, final SwitchConnectionConfig config) {
        final var instanceName = config.getInstanceName();
        LOG.info("Checking presence of configuration for {}", instanceName);

        final var sw = Stopwatch.createStarted();
        final var iid = InstanceIdentifier.builder(SwitchConnectionConfig.class, config.key()).build();
        final var tx = dataBroker.newReadWriteTransaction();
        tx.exists(LogicalDatastoreType.CONFIGURATION, iid).addCallback(new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(final Boolean result) {
                LOG.debug("Presence of configuration for {} ascertained in {}", instanceName, sw);
                if (result) {
                    LOG.info("Configuration for {} already present", instanceName);
                    tx.cancel();
                    return;
                }

                tx.put(LogicalDatastoreType.CONFIGURATION, iid, config);
                tx.commit().addCallback(new FutureCallback<CommitInfo>() {
                    @Override
                    public void onSuccess(final CommitInfo result) {
                        LOG.info("Configuration for {} set to factory-default", instanceName);
                    }

                    @Override
                    public void onFailure(final Throwable cause) {
                        LOG.warn("Failed to set configuration for {} set to factory-default", instanceName, cause);
                    }
                }, MoreExecutors.directExecutor());
            }

            @Override
            public void onFailure(final Throwable cause) {
                LOG.warn("Failed to ascertain presence of configuration for {} after {}", instanceName, sw, cause);
                tx.cancel();
            }
        }, MoreExecutors.directExecutor());
    }
}
