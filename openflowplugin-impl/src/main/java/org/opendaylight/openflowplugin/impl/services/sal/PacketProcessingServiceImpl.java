/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.Optional;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.AbstractSimpleService;
import org.opendaylight.openflowplugin.impl.services.util.ServiceException;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.PacketOutConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.XidConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

public final class PacketProcessingServiceImpl extends AbstractSimpleService<TransmitPacketInput, TransmitPacketOutput>
                                               implements PacketProcessingService {

    private final ConvertorExecutor convertorExecutor;

    public PacketProcessingServiceImpl(final RequestContextStack requestContextStack,
                                       final DeviceContext deviceContext,
                                       final ConvertorExecutor convertorExecutor) {
        super(requestContextStack, deviceContext, TransmitPacketOutput.class);
        this.convertorExecutor = convertorExecutor;
    }

    @Override
    public ListenableFuture<RpcResult<TransmitPacketOutput>> transmitPacket(final TransmitPacketInput input) {
        return handleServiceCall(input);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final TransmitPacketInput input) throws ServiceException {
        final XidConvertorData data = new XidConvertorData(getVersion());
        data.setDatapathId(getDatapathId());
        data.setXid(xid.getValue());

        final Optional<PacketOutInput> result = convertorExecutor.convert(input, data);
        return result.orElse(PacketOutConvertor.defaultResult(getVersion()));
    }
}
