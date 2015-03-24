/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.openflow.md.core.sal.OFRpcTask;
import org.opendaylight.openflowplugin.openflow.md.core.sal.OFRpcTaskFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.SalPortService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;

public class SalPortServiceImpl extends CommonService implements SalPortService {
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SalPortServiceImpl.class);

    /**
     * @param rpcContext
     */
    public SalPortServiceImpl(final RpcContext rpcContext) {
        super(rpcContext);
    }

    @Override
    public Future<RpcResult<UpdatePortOutput>> updatePort(final UpdatePortInput input) {
        LOG.debug("Calling the updatePort RPC method on MessageDispatchService");

        final OFRpcTask<UpdatePortInput, RpcResult<UpdatePortOutput>> task = OFRpcTaskFactory.createUpdatePortTask(
                rpcTaskContext, input, null);
        return task.submit();
    }

}
