/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.base.Function;

import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.PortConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.port.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.SalPortService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import java.math.BigInteger;
import java.util.concurrent.Future;

public class SalPortServiceImpl extends CommonService implements SalPortService {
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SalPortServiceImpl.class);

    @Override
    public Future<RpcResult<UpdatePortOutput>> updatePort(final UpdatePortInput input) {
        return this.<UpdatePortOutput, Void> handleServiceCall(PRIMARY_CONNECTION,
                new Function<BigInteger, Future<RpcResult<Void>>>() {
                    @Override
                    public Future<RpcResult<Void>> apply(final BigInteger IDConnection) {
                        final Port inputPort = input.getUpdatedPort().getPort().getPort().get(0);
                        final PortModInput ofPortModInput = PortConvertor.toPortModInput(inputPort, version);
                        final PortModInputBuilder mdInput = new PortModInputBuilder(ofPortModInput);
                        mdInput.setXid(deviceContext.getNextXid().getValue());
                        return provideConnectionAdapter(IDConnection).portMod(mdInput.build());
                    }
                });
    }

}
