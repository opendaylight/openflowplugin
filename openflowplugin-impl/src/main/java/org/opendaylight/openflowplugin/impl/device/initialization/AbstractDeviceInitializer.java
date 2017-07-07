/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device.initialization;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import java.util.Collections;
import java.util.concurrent.Future;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.ConnectionException;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDeviceInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDeviceInitializer.class);

    /**
     * Perform initial information gathering and store them to operational datastore
     * @param deviceContext device context
     * @param switchFeaturesMandatory is switch features mandatory
     * @param skipTableFeatures skip collecting of table features
     * @param multipartWriterProvider multipart writer provider
     * @param convertorExecutor convertor executor
     */
    public Future<Void> initialize(@Nonnull final DeviceContext deviceContext,
                                   final boolean switchFeaturesMandatory,
                                   final boolean skipTableFeatures,
                                   @Nullable final MultipartWriterProvider multipartWriterProvider,
                                   @Nullable final ConvertorExecutor convertorExecutor) {
        Preconditions.checkNotNull(deviceContext);

        // Write node to datastore
        LOG.debug("Initializing node information for node {}", deviceContext.getDeviceInfo().getLOGValue());
        try {
            deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, deviceContext
                    .getDeviceInfo()
                    .getNodeInstanceIdentifier(),
                new NodeBuilder()
                    .setId(deviceContext.getDeviceInfo().getNodeId())
                    .setNodeConnector(Collections.emptyList())
                    .build());
        } catch (final Exception e) {
            LOG.warn("Failed to write node {} to DS ", deviceContext.getDeviceInfo().getNodeId(), e);
            return Futures.immediateFailedFuture(new ConnectionException(
                    "Failed to write node " + deviceContext.getDeviceInfo().getNodeId() + " to DS ", e));
        }

        // Get information about device
        return initializeNodeInformation(deviceContext, switchFeaturesMandatory, skipTableFeatures,
                multipartWriterProvider, convertorExecutor);
    }

    protected abstract Future<Void> initializeNodeInformation(@Nonnull final DeviceContext deviceContext,
                                                              final boolean switchFeaturesMandatory,
                                                              final boolean skipTableFeatures,
                                                              @Nullable final MultipartWriterProvider multipartWriterProvider,
                                                              @Nullable final ConvertorExecutor convertorExecutor);
}
