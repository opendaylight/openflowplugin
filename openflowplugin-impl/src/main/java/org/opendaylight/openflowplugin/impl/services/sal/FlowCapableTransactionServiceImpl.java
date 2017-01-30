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
import org.opendaylight.openflowplugin.impl.services.AbstractVoidService;
import org.opendaylight.openflowplugin.impl.services.util.ServiceException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class FlowCapableTransactionServiceImpl extends AbstractVoidService<SendBarrierInput> implements FlowCapableTransactionService {
    public FlowCapableTransactionServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    @Override
    public Future<RpcResult<Void>> sendBarrier(final SendBarrierInput input) {
        return handleServiceCall(input);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final SendBarrierInput input) throws ServiceException {
        final BarrierInputBuilder barrierInputOFJavaBuilder = new BarrierInputBuilder();
        barrierInputOFJavaBuilder.setVersion(getVersion());
        barrierInputOFJavaBuilder.setXid(xid.getValue());
        return barrierInputOFJavaBuilder.build();
    }
}
