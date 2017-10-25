/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.test;

import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeContext;
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
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowpluginMeterTestServiceProvider implements AutoCloseable,
        SalMeterService {
    private static final Logger LOG = LoggerFactory
            .getLogger(OpenflowpluginMeterTestServiceProvider.class);
    private DataBroker dataService;
    private RoutedRpcRegistration<SalMeterService> meterRegistration;
    private NotificationProviderService notificationService;

    /**
     * get data service
     *
     * @return {@link #dataService}
     */
    public DataBroker getDataService() {
        return this.dataService;
    }

    /**
     * set {@link #dataService}
     *
     * @param dataService
     */
    public void setDataService(final DataBroker dataService) {
        this.dataService = dataService;
    }

    /**
     * get meter registration
     *
     * @return {@link #meterRegistration}
     */
    public RoutedRpcRegistration<SalMeterService> getMeterRegistration() {
        return this.meterRegistration;
    }

    /**
     * set {@link #meterRegistration}
     *
     * @param meterRegistration
     */
    public void setMeterRegistration(
            final RoutedRpcRegistration<SalMeterService> meterRegistration) {
        this.meterRegistration = meterRegistration;
    }

    /**
     * get notification service
     *
     * @return {@link #notificationService}
     */
    public NotificationProviderService getNotificationService() {
        return this.notificationService;
    }

    /**
     * set {@link #notificationService}
     *
     * @param notificationService
     */
    public void setNotificationService(
            final NotificationProviderService notificationService) {
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
    public Future<RpcResult<AddMeterOutput>> addMeter(final AddMeterInput input) {
        String plus = ("addMeter - " + input);
        OpenflowpluginMeterTestServiceProvider.LOG.info(plus);
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
    public Future<RpcResult<RemoveMeterOutput>> removeMeter(
            final RemoveMeterInput input) {
        String plus = ("removeMeter - " + input);
        OpenflowpluginMeterTestServiceProvider.LOG.info(plus);
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
    public Future<RpcResult<UpdateMeterOutput>> updateMeter(
            final UpdateMeterInput input) {
        String plus = ("updateMeter - " + input);
        OpenflowpluginMeterTestServiceProvider.LOG.info(plus);
        return null;
    }

    /**
     * @param ctx
     * @return {@link ObjectRegistration}
     */
    public ObjectRegistration<OpenflowpluginMeterTestServiceProvider> register(
            final ProviderContext ctx) {

        RoutedRpcRegistration<SalMeterService> addRoutedRpcImplementation = ctx
                .<SalMeterService> addRoutedRpcImplementation(
                        SalMeterService.class, this);

        setMeterRegistration(addRoutedRpcImplementation);

        InstanceIdentifierBuilder<Nodes> builder1 = InstanceIdentifier
                .<Nodes> builder(Nodes.class);

        NodeId nodeId = new NodeId(OpenflowpluginTestActivator.NODE_ID);
        NodeKey nodeKey = new NodeKey(nodeId);

        InstanceIdentifierBuilder<Node> nodeIndentifier = builder1
                .<Node, NodeKey> child(Node.class, nodeKey);

        InstanceIdentifier<Node> instance = nodeIndentifier.build();

        meterRegistration.registerPath(NodeContext.class, instance);

        RoutedRpcRegistration<SalMeterService> meterRegistration1 = this
                .getMeterRegistration();

        return new AbstractObjectRegistration<OpenflowpluginMeterTestServiceProvider>(this) {

            @Override
            protected void removeRegistration() {
                meterRegistration1.close();
            }
        };
    }

}
