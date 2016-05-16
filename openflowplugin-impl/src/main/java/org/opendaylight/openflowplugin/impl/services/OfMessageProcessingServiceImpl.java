/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import java.util.concurrent.Future;

import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializationFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializerRegistryImpl;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.of.message.service.rev160511.GetOfVersionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.of.message.service.rev160511.GetOfVersionOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.of.message.service.rev160511.OfMessageProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.of.message.service.rev160511.TransmitOfMessageInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.of.message.service.rev160511.GetOfVersionOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import com.google.common.util.concurrent.Futures;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class OfMessageProcessingServiceImpl extends AbstractVoidService<TransmitOfMessageInput>
        implements OfMessageProcessingService {

    private final DeviceContext deviceContext;

    public OfMessageProcessingServiceImpl(RequestContextStack requestContextStack,
            DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
        this.deviceContext = deviceContext;
    }

    @Override
    public Future<RpcResult<Void>> transmitOfMessage(TransmitOfMessageInput input) {
        return handleServiceCall(input);
    }

    @Override
    protected OfHeader buildRequest(Xid xid, TransmitOfMessageInput input) throws Exception {
        ByteBuf buffer = Unpooled.wrappedBuffer(input.getMessage());
        short ofVersion = buffer.readUnsignedByte();
        DeserializerRegistry registry = new DeserializerRegistryImpl();
        registry.init();
        DeserializationFactory factory = new DeserializationFactory();
        factory.setRegistry(registry);
        DataObject dObj = factory.deserialize(buffer, ofVersion);
        return (OfHeader) dObj;
    }

    @Override
    public Future<RpcResult<GetOfVersionOutput>> getOfVersion(GetOfVersionInput input) {
        RpcResultBuilder<GetOfVersionOutput> rpcResultBuilder = RpcResultBuilder.success();
        GetOfVersionOutputBuilder builder = new GetOfVersionOutputBuilder();
        builder.setNode(input.getNode());
        builder.setOfVersion(deviceContext.getDeviceState().getVersion());
        rpcResultBuilder.withResult(builder);
        return Futures.immediateFuture(rpcResultBuilder.build());
    }
}
