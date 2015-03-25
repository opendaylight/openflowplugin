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
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;

/**
 * @author joe
 * 
 */
public class SalTableServiceImpl extends CommonService implements SalTableService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SalTableServiceImpl.class);

    @Override
    public Future<RpcResult<UpdateTableOutput>> updateTable(final UpdateTableInput input) {
        // LOG.debug("Calling the updateTable RPC method on MessageDispatchService");
        //
        // final OFRpcTask<UpdateTableInput, RpcResult<UpdateTableOutput>> task =
        // OFRpcTaskFactory.createUpdateTableTask(
        // rpcTaskContext, input, null);
        // return task.submit();
        return null;
    }

}
