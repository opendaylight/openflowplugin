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
import org.opendaylight.openflowplugin.impl.services.AbstractSimpleService;
import org.opendaylight.openflowplugin.impl.services.singlelayer.SingleLayerPortService;
import org.opendaylight.openflowplugin.impl.services.util.ServiceException;
import org.opendaylight.openflowplugin.protocol.converter.PortConverter;
import org.opendaylight.openflowplugin.protocol.converter.data.VersionConverterData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.port.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.SalPortService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

public final class SalPortServiceImpl extends AbstractSimpleService<UpdatePortInput, UpdatePortOutput>
        implements SalPortService {
    private final ConverterExecutor converterExecutor;
    private final VersionConverterData data;
    private final SingleLayerPortService<UpdatePortOutput> portMessage;

    public SalPortServiceImpl(final RequestContextStack requestContextStack,
                              final DeviceContext deviceContext,
                              final ConverterExecutor converterExecutor) {
        super(requestContextStack, deviceContext, UpdatePortOutput.class);
        this.converterExecutor = converterExecutor;
        data = new VersionConverterData(getVersion());
        portMessage = new SingleLayerPortService<>(requestContextStack, deviceContext, UpdatePortOutput.class);
    }

    @Override
    public Future<RpcResult<UpdatePortOutput>> updatePort(final UpdatePortInput input) {
        return portMessage.canUseSingleLayerSerialization()
            ? portMessage.handleServiceCall(getPortFromInput(input))
            : handleServiceCall(input);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final UpdatePortInput input) throws ServiceException {
        final Optional<PortModInput> ofPortModInput = converterExecutor
            .convert(getPortFromInput(input), data);

        final PortModInputBuilder mdInput = new PortModInputBuilder(ofPortModInput
                .orElse(PortConverter.defaultResult(getVersion())))
                .setXid(xid.getValue());

        return mdInput.build();
    }

    private Port getPortFromInput(final UpdatePortInput input) {
        return input.getUpdatedPort().getPort().getPort().get(0);
    }
}
