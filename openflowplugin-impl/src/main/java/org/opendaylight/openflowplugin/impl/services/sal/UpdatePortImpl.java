/*
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
import org.opendaylight.openflowplugin.impl.services.singlelayer.SingleLayerPortService;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.PortConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.port.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

public final class UpdatePortImpl extends AbstractSimpleService<UpdatePortInput, UpdatePortOutput>
        implements UpdatePort {
    private final ConvertorExecutor convertorExecutor;
    private final VersionConvertorData data;
    private final SingleLayerPortService<UpdatePortOutput> portMessage;

    public UpdatePortImpl(final RequestContextStack requestContextStack,
                              final DeviceContext deviceContext,
                              final ConvertorExecutor convertorExecutor) {
        super(requestContextStack, deviceContext, UpdatePortOutput.class);
        this.convertorExecutor = convertorExecutor;
        data = new VersionConvertorData(getVersion());
        portMessage = new SingleLayerPortService<>(requestContextStack, deviceContext, UpdatePortOutput.class);
    }

    @Override
    public ListenableFuture<RpcResult<UpdatePortOutput>> invoke(final UpdatePortInput input) {
        return portMessage.canUseSingleLayerSerialization()
            ? portMessage.handleServiceCall(getPortFromInput(input))
            : handleServiceCall(input);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final UpdatePortInput input) {
        final Optional<PortModInput> ofPortModInput = convertorExecutor.convert(getPortFromInput(input), data);

        return new PortModInputBuilder(ofPortModInput.orElse(PortConvertor.defaultResult(getVersion())))
            .setXid(xid.getValue())
            .build();
    }

    private static Port getPortFromInput(final UpdatePortInput input) {
        return input.getUpdatedPort().getPort().nonnullPort().values().iterator().next();
    }
}
