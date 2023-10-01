/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.test;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutput;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowpluginMeterTestRpcsProvider implements AutoCloseable {
    private static final Logger LOG = LoggerFactory
            .getLogger(OpenflowpluginMeterTestRpcsProvider.class);
    private DataBroker dataService;
    private Registration meterRegistration;
    private NotificationPublishService notificationService;

    /**
     * Gets the data service.
     *
     * @return {@link #dataService}
     */
    public DataBroker getDataService() {
        return this.dataService;
    }

    /**
     * Sets the {@link #dataService}.
     */
    public void setDataService(final DataBroker dataService) {
        this.dataService = dataService;
    }

    /**
     * Gets the meter registration.
     *
     * @return {@link #meterRegistration}
     */
    public Registration getMeterRegistration() {
        return this.meterRegistration;
    }

    /**
     * Sets the {@link #meterRegistration}.
     */
    public void setMeterRegistration(final Registration meterRegistration) {
        this.meterRegistration = meterRegistration;
    }

    /**
     * Gets the notification service.
     *
     * @return {@link #notificationService}
     */
    public NotificationPublishService getNotificationService() {
        return this.notificationService;
    }

    /**
     * Sets the {@link #notificationService}.
     */
    public void setNotificationService(final NotificationPublishService notificationService) {
        this.notificationService = notificationService;
    }

    public void start() {
        OpenflowpluginMeterTestRpcsProvider.LOG
                .info("SalMeterRpcsProvider Started.");
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() {
        OpenflowpluginMeterTestRpcsProvider.LOG
                .info("SalMeterRpcsProvide stopped.");
        meterRegistration.close();
    }

    private ListenableFuture<RpcResult<AddMeterOutput>> addMeter(final AddMeterInput input) {
        OpenflowpluginMeterTestRpcsProvider.LOG.info("addMeter - {}", input);
        return null;
    }

    private ListenableFuture<RpcResult<RemoveMeterOutput>> removeMeter(
            final RemoveMeterInput input) {
        OpenflowpluginMeterTestRpcsProvider.LOG.info("removeMeter - {}", input);
        return null;
    }

    private ListenableFuture<RpcResult<UpdateMeterOutput>> updateMeter(
            final UpdateMeterInput input) {
        OpenflowpluginMeterTestRpcsProvider.LOG.info("updateMeter - {}", input);
        return null;
    }

    public ClassToInstanceMap<Rpc<?,?>> getRpcClassToInstanceMap() {
        return ImmutableClassToInstanceMap.<Rpc<?, ?>>builder()
            .put(RemoveMeter.class, this::removeMeter)
            .put(AddMeter.class, this::addMeter)
            .put(UpdateMeter.class, this::updateMeter)
            .build();
    }

    public Registration register(final RpcProviderService rpcRegistry) {
        setMeterRegistration(rpcRegistry.registerRpcImplementations(this.getRpcClassToInstanceMap(), ImmutableSet.of(
            InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(new NodeId(OpenflowpluginTestActivator.NODE_ID))))));

        return new AbstractObjectRegistration<>(this) {
            @Override
            protected void removeRegistration() {
                meterRegistration.close();
            }
        };
    }
}
