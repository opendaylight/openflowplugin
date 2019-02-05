/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.test;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutput;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowpluginMeterTestServiceProvider implements AutoCloseable,
        SalMeterService {
    private static final Logger LOG = LoggerFactory
            .getLogger(OpenflowpluginMeterTestServiceProvider.class);
    private DataBroker dataService;
    private ObjectRegistration<SalMeterService> meterRegistration;
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
    public ObjectRegistration<SalMeterService> getMeterRegistration() {
        return this.meterRegistration;
    }

    /**
     * Sets the {@link #meterRegistration}.
     */
    public void setMeterRegistration(final ObjectRegistration<SalMeterService> meterRegistration) {
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
        OpenflowpluginMeterTestServiceProvider.LOG
                .info("SalMeterServiceProvider Started.");
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() {
        OpenflowpluginMeterTestServiceProvider.LOG
                .info("SalMeterServiceProvide stopped.");
        meterRegistration.close();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918
     * .SalMeterService
     * #addMeter(org.opendaylight.yang.gen.v1.urn.opendaylight.meter
     * .service.rev130918.AddMeterInput)
     */
    @Override
    public ListenableFuture<RpcResult<AddMeterOutput>> addMeter(final AddMeterInput input) {
        OpenflowpluginMeterTestServiceProvider.LOG.info("addMeter - {}", input);
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918
     * .SalMeterService
     * #removeMeter(org.opendaylight.yang.gen.v1.urn.opendaylight
     * .meter.service.rev130918.RemoveMeterInput)
     */
    @Override
    public ListenableFuture<RpcResult<RemoveMeterOutput>> removeMeter(
            final RemoveMeterInput input) {
        OpenflowpluginMeterTestServiceProvider.LOG.info("removeMeter - {}", input);
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918
     * .SalMeterService
     * #updateMeter(org.opendaylight.yang.gen.v1.urn.opendaylight
     * .meter.service.rev130918.UpdateMeterInput)
     */
    @Override
    public ListenableFuture<RpcResult<UpdateMeterOutput>> updateMeter(
            final UpdateMeterInput input) {
        OpenflowpluginMeterTestServiceProvider.LOG.info("updateMeter - {}", input);
        return null;
    }

    public ObjectRegistration<OpenflowpluginMeterTestServiceProvider> register(final RpcProviderService rpcRegistry) {
        setMeterRegistration(rpcRegistry.registerRpcImplementation(SalMeterService.class, this, ImmutableSet.of(
            InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(new NodeId(OpenflowpluginTestActivator.NODE_ID))))));

        return new AbstractObjectRegistration<OpenflowpluginMeterTestServiceProvider>(this) {

            @Override
            protected void removeRegistration() {
                meterRegistration.close();
            }
        };
    }
}
