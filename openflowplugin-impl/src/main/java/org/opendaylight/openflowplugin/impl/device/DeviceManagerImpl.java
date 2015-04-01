/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import javax.annotation.CheckForNull;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceContextReadyHandler;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestDescCaseBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DeviceManagerImpl implements DeviceManager {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceManagerImpl.class);

    @Override
    public void deviceConnected(@CheckForNull final ConnectionContext connectionContext) {
        Preconditions.checkArgument(connectionContext != null);
        final DeviceState deviceState = new DeviceStateImpl(connectionContext.getFeatures(), connectionContext.getNodeId());
//        final DeviceContextImpl deviceContextImpl = new DeviceContextImpl(connectionContext, deviceState);

//        try {
//            final FlowCapableNode description = queryDescription(connectionContext, deviceContextImpl.getNextXid()).get();

//        } catch (InterruptedException | ExecutionException e) {
//            // TODO Auto-generated catch block
//            LOG.info("Failed to retrieve node static info: {}", e.getMessage());
//        }


        //TODO: inject translatorLibrary into deviceCtx
    }

    /**
     * @param connectionContext
     * @param xid
     */
    private static ListenableFuture<FlowCapableNode> queryDescription(final ConnectionContext connectionContext, final Xid xid) {
        final MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        builder.setType(MultipartType.OFPMPDESC);
        builder.setVersion(connectionContext.getFeatures().getVersion());
        builder.setFlags(new MultipartRequestFlags(false));
        builder.setMultipartRequestBody(new MultipartRequestDescCaseBuilder()
                .build());
        builder.setXid(xid.getValue());
        connectionContext.getConnectionAdapter().multipartRequest(builder.build());

        //TODO: involve general wait-for-answer mechanism and return future with complete value
        //TODO: translate message
        return Futures.immediateFuture(null);
    }

    @Override
    public void sendMessage(final DataObject dataObject, final RequestContext requestContext) {
        // TODO Auto-generated method stub

    }

    @Override
    public Xid sendRequest(final DataObject dataObject, final RequestContext requestContext) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addRequestContextReadyHandler(final DeviceContextReadyHandler deviceContextReadyHandler) {
        // TODO Auto-generated method stub

    }

}
