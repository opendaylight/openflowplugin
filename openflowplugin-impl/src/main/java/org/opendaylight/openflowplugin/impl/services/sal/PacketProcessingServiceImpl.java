/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import java.util.Optional;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.protocol.converter.ConverterExecutor;
import org.opendaylight.openflowplugin.impl.services.AbstractVoidService;
import org.opendaylight.openflowplugin.impl.services.util.ServiceException;
import org.opendaylight.openflowplugin.protocol.converter.PacketOutConverter;
import org.opendaylight.openflowplugin.protocol.converter.data.PacketOutConverterData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yangtools.yang.common.RpcResult;

public final class PacketProcessingServiceImpl extends AbstractVoidService<TransmitPacketInput> implements PacketProcessingService {

    private final ConverterExecutor converterExecutor;

    public PacketProcessingServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext, final ConverterExecutor converterExecutor) {
        super(requestContextStack, deviceContext);
        this.converterExecutor = converterExecutor;
    }

    @Override
    public Future<RpcResult<Void>> transmitPacket(final TransmitPacketInput input) {
        return handleServiceCall(input);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final TransmitPacketInput input) throws ServiceException {
        final PacketOutConverterData data = new PacketOutConverterData(getVersion());
        data.setDatapathId(getDatapathId());
        data.setXid(xid.getValue());

        final Optional<PacketOutInput> result = converterExecutor.convert(input, data);
        return result.orElse(PacketOutConverter.defaultResult(getVersion()));
    }
}
