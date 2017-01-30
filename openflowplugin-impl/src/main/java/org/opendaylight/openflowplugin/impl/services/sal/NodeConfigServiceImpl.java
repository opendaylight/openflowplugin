/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.AbstractSimpleService;
import org.opendaylight.openflowplugin.impl.services.util.ServiceException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.NodeConfigService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.SetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.SetConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.SwitchConfigFlag;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;

public final class NodeConfigServiceImpl extends AbstractSimpleService<SetConfigInput, SetConfigOutput> implements NodeConfigService {
    public NodeConfigServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext, SetConfigOutput.class);
    }

    @Override
    public Future<RpcResult<SetConfigOutput>> setConfig(final SetConfigInput input) {
        return handleServiceCall(input);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final SetConfigInput input) throws ServiceException {
        SetConfigInputBuilder builder = new SetConfigInputBuilder();
        SwitchConfigFlag flag = SwitchConfigFlag.valueOf(input.getFlag());

        builder.setXid(xid.getValue());
        builder.setFlags(flag);
        builder.setMissSendLen(input.getMissSearchLength());
        builder.setVersion(getVersion());
        return builder.build();
    }
}
