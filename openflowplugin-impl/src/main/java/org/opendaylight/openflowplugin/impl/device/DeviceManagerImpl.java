/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
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
import java.util.concurrent.ExecutionException;

/**
 *
 */
public class DeviceManagerImpl implements DeviceManager {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceManagerImpl.class);


    @Override
    public void deviceConnected(ConnectionContext connectionContext) {
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
    private static ListenableFuture<FlowCapableNode> queryDescription(ConnectionContext connectionContext, Xid xid) {
        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
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
    public void sendMessage(DataObject dataObject, RequestContext requestContext) {
        // TODO Auto-generated method stub

    }

    @Override
    public Xid sendRequest(DataObject dataObject, RequestContext requestContext) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addRequestContextReadyHandler(DeviceContextReadyHandler deviceContextReadyHandler) {
        // TODO Auto-generated method stub

    }

}
