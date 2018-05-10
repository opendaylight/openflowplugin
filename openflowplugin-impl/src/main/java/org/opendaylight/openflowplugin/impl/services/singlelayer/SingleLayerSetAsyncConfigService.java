/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.singlelayer;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.AbstractSimpleService;
import org.opendaylight.openflowplugin.impl.services.util.ServiceException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.AsyncConfigMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.SetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.SetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

public class SingleLayerSetAsyncConfigService extends AbstractSimpleService<SetAsyncInput, SetAsyncOutput> {

    public SingleLayerSetAsyncConfigService(final RequestContextStack requestContextStack,
                                            final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext, SetAsyncOutput.class);
    }

    @Override
    protected OfHeader buildRequest(Xid xid, SetAsyncInput input) throws ServiceException {
        return new AsyncConfigMessageBuilder(input)
                .setVersion(getVersion())
                .setXid(xid.getValue())
                .build();
    }
}
