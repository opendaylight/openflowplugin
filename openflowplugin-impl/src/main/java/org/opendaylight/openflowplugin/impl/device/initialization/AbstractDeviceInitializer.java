/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device.initialization;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDeviceInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDeviceInitializer.class);

    /**
     * Perform initial information gathering and store them to operational datastore
     * @param deviceContext device context
     * @param multipartWriterProvider multipart writer provider
     */
    public void initialize(@Nonnull final DeviceContext deviceContext,
                           @Nonnull final MultipartWriterProvider multipartWriterProvider) throws ExecutionException,InterruptedException {
        Preconditions.checkNotNull(deviceContext);
        Preconditions.checkNotNull(multipartWriterProvider);

        LOG.trace("InitializeNodeInformation for node {}", deviceContext.getDeviceInfo().getLOGValue());
        deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, deviceContext
                .getDeviceInfo()
                .getNodeInstanceIdentifier(),
            new NodeBuilder()
                .setId(deviceContext.getDeviceInfo().getNodeId())
                .setNodeConnector(Collections.emptyList())
                .build());

        // Synchronously get information about device
        initializeNodeInformation(deviceContext, multipartWriterProvider).get();
    }

    protected abstract Future<Void> initializeNodeInformation(@Nonnull final DeviceContext deviceContext,
                                                              @Nonnull final MultipartWriterProvider multipartWriterProvider);
}
