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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.CommonPortWithMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;

public final class SingleLayerPortService<O extends DataObject> extends AbstractSimpleService<CommonPortWithMask, O> {

    public SingleLayerPortService(
        final RequestContextStack requestContextStack,
        final DeviceContext deviceContext,
        final Class<O> clazz) {
        super(requestContextStack, deviceContext, clazz);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final CommonPortWithMask input) throws ServiceException {
        return new PortMessageBuilder(input)
                .setVersion(getVersion())
                .setXid(xid.getValue())
                .build();
    }

}
