/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SalEchoService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SendEchoInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SendEchoOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcResult;

public final class SalEchoServiceImpl extends AbstractSimpleService<SendEchoInput, SendEchoOutput> implements SalEchoService {
    public SalEchoServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext, SendEchoOutput.class);
    }

    @Override
    public Future<RpcResult<SendEchoOutput>> sendEcho(final SendEchoInput sendEchoInput) {
        return handleServiceCall(sendEchoInput);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final SendEchoInput input) {
        final EchoInputBuilder echoInputOFJavaBuilder = new EchoInputBuilder();
        echoInputOFJavaBuilder.setVersion(getVersion());
        echoInputOFJavaBuilder.setXid(xid.getValue());
        echoInputOFJavaBuilder.setData(input.getData());
        return echoInputOFJavaBuilder.build();
    }
}
