/**
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services;

import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;

final class FlowMessageService<O extends DataObject> extends AbstractMessageService<Flow, FlowMessageBuilder, O> {
    protected FlowMessageService(final RequestContextStack requestContextStack, final DeviceContext deviceContext, final Class<O> clazz) {
        super(requestContextStack, deviceContext, clazz);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final Flow input) throws ServiceException {

        final FlowMessageBuilder flowMessageBuilder = new FlowMessageBuilder(input);
        final Class<? extends DataContainer> clazz = input.getImplementedInterface();

        if (clazz.equals(AddFlowInput.class)
                || clazz.equals(UpdatedFlow.class)) {
            flowMessageBuilder.setCommand(FlowModCommand.OFPFCADD);
        } else if (clazz.equals(RemoveFlowInput.class)
                || clazz.equals(OriginalFlow.class)) {
            flowMessageBuilder.setCommand(Boolean.TRUE.equals(input.isStrict())
                    ? FlowModCommand.OFPFCDELETESTRICT
                    : FlowModCommand.OFPFCDELETE);
        }

        return flowMessageBuilder
                .setVersion(getVersion())
                .setXid(xid.getValue())
                .build();
    }

    /**
     * Check this service is supported in current OpenFlowPlugin configuration
     * @return true if is used single layer serialization and
     */
    @Override
    public boolean isSupported() {
        return super.isSupported() && getVersion() >= OFConstants.OFP_VERSION_1_3;
    }

}
