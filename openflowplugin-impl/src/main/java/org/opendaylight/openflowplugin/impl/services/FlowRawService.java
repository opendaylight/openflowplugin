/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowRawInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRawBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcResult;

final class FlowRawService<O extends DataObject> extends AbstractSimpleService<FlowRawBuilder, O> {
    protected FlowRawService(final RequestContextStack requestContextStack, final DeviceContext deviceContext, final Class<O> clazz) {
        super(requestContextStack, deviceContext, clazz);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final FlowRawBuilder input) throws ServiceException {
        return input
                .setVersion(getVersion())
                .setXid(xid.getValue())
                .build();
    }

    /**
     * Converts input to {@link org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRawBuilder}
     * and calls #{@link org.opendaylight.openflowplugin.impl.services.FlowRawService#handleServiceCall(Object)} on it.
     * @param input rpc input
     * @return future containing result of service call
     */
    public <I extends Flow> ListenableFuture<RpcResult<O>> processInput(I input) {
        final FlowRawBuilder flowRawBuilder = new FlowRawBuilder(input);
        final Class<? extends DataContainer> clazz = input.getImplementedInterface();

        if (clazz.equals(AddFlowInput.class)
                || clazz.equals(AddFlowRawInput.class)
                || clazz.equals(UpdatedFlow.class)) {
            flowRawBuilder.setCommand(FlowModCommand.OFPFCADD);
        } else if (clazz.equals(RemoveFlowInput.class)) {
            flowRawBuilder.setCommand(Boolean.TRUE.equals(input.isStrict())
                    ? FlowModCommand.OFPFCDELETESTRICT
                    : FlowModCommand.OFPFCDELETE);
        }

        return handleServiceCall(flowRawBuilder);
    }
}
