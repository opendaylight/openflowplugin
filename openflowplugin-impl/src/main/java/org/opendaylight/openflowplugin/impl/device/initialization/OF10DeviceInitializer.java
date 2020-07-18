/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device.initialization;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.services.multilayer.MultiLayerMultipartCollectorService;
import org.opendaylight.openflowplugin.impl.services.singlelayer.SingleLayerMultipartCollectorService;
import org.opendaylight.openflowplugin.impl.util.DeviceInitializationUtil;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.CapabilitiesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsDataBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OF10DeviceInitializer extends AbstractDeviceInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(OF10DeviceInitializer.class);

    @Override
    protected ListenableFuture<Void> initializeNodeInformation(@NonNull final DeviceContext deviceContext,
                                                     final boolean switchFeaturesMandatory,
                                                     final boolean skipTableFeatures,
                                                     @Nullable final MultipartWriterProvider multipartWriterProvider,
                                                     @Nullable final ConvertorExecutor convertorExecutor) {
        final ConnectionContext connectionContext =
                Preconditions.checkNotNull(deviceContext.getPrimaryConnectionContext());
        final DeviceState deviceState = Preconditions.checkNotNull(deviceContext.getDeviceState());
        final DeviceInfo deviceInfo = Preconditions.checkNotNull(deviceContext.getDeviceInfo());
        final CapabilitiesV10 capabilitiesV10 = connectionContext.getFeatures().getCapabilitiesV10();

        // Set capabilities for this device based on capabilities of connection context
        LOG.debug("Setting capabilities for device {}", deviceInfo);
        DeviceStateUtil.setDeviceStateBasedOnV10Capabilities(deviceState, capabilitiesV10);
        final ListenableFuture<Boolean> future = requestMultipart(MultipartType.OFPMPDESC, deviceContext);

        Futures.addCallback(future, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(final Boolean result) {
                if (Boolean.TRUE.equals(result)) {
                    LOG.debug("Creating empty flow capable node: {}", deviceInfo);
                    makeEmptyFlowCapableNode(deviceContext, deviceInfo);

                    LOG.debug("Creating empty tables for {}", deviceInfo);
                    DeviceInitializationUtil.makeEmptyTables(
                        deviceContext,
                        deviceInfo,
                        deviceContext.getPrimaryConnectionContext().getFeatures().getTables().toJava());
                }
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.warn("Error occurred in preparation node {} for protocol 1.0", deviceInfo);
                LOG.trace("Error for node {} : ", deviceInfo, throwable);
            }
        }, MoreExecutors.directExecutor());

        return Futures.transform(future, input -> {
            LOG.debug("Writing physical port information for {}", deviceInfo);
            writePhyPortInformation(deviceContext);
            return null;
        }, MoreExecutors.directExecutor());
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private static void writePhyPortInformation(final DeviceContext deviceContext) {
        final DeviceInfo deviceInfo = deviceContext.getDeviceInfo();
        final ConnectionContext connectionContext = deviceContext.getPrimaryConnectionContext();
        final MessageTranslator<PortGrouping, FlowCapableNodeConnector> translator = deviceContext
            .oook()
            .lookupTranslator(new TranslatorKey(deviceInfo.getVersion(), PortGrouping.class.getName()));

        connectionContext.getFeatures().getPhyPort().forEach(port -> {
            final NodeConnectorId nodeConnectorId = InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(
                deviceInfo.getDatapathId(),
                port.getPortNo(),
                OpenflowVersion.get(deviceInfo.getVersion()));

            final NodeConnectorBuilder connectorBuilder = new NodeConnectorBuilder()
                    .setId(nodeConnectorId)
                    .addAugmentation(new FlowCapableNodeConnectorStatisticsDataBuilder().build());
            final FlowCapableNodeConnector augment = translator.translate(port, deviceInfo, null);
            if (augment != null) {
                connectorBuilder.addAugmentation(augment);
            }

            try {
                deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL,
                    deviceInfo
                        .getNodeInstanceIdentifier()
                        .child(NodeConnector.class, new NodeConnectorKey(nodeConnectorId)),
                        connectorBuilder.build());
            } catch (final Exception e) {
                LOG.debug("Failed to write node {} to DS ", deviceInfo, e);
            }
        });
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "https://github.com/spotbugs/spotbugs/issues/811")
    private static void makeEmptyFlowCapableNode(final TxFacade txFacade, final DeviceInfo deviceInfo) {
        try {
            txFacade.writeToTransaction(LogicalDatastoreType.OPERATIONAL,
                deviceInfo
                    .getNodeInstanceIdentifier()
                    .augmentation(FlowCapableNode.class),
                new FlowCapableNodeBuilder().build());
        } catch (final Exception e) {
            LOG.debug("Failed to write empty node {} to DS ", deviceInfo, e);
        }
    }

    private static ListenableFuture<Boolean> requestMultipart(final MultipartType multipartType,
                                                              final DeviceContext deviceContext) {
        if (deviceContext.canUseSingleLayerSerialization()) {
            final SingleLayerMultipartCollectorService service =
                    new SingleLayerMultipartCollectorService(deviceContext, deviceContext);

            return Futures.transform(service.handleServiceCall(multipartType),
                RpcResult::isSuccessful, MoreExecutors.directExecutor());
        }

        final MultiLayerMultipartCollectorService service =
                new MultiLayerMultipartCollectorService(deviceContext, deviceContext);

        return Futures.transform(service.handleServiceCall(multipartType),
            RpcResult::isSuccessful, MoreExecutors.directExecutor());
    }

}
