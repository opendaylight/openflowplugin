/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.SalPortService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import java.util.concurrent.Future;

public class SalPortServiceImpl extends CommonService implements SalPortService {
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SalPortServiceImpl.class);

    @Override
    public Future<RpcResult<UpdatePortOutput>> updatePort(final UpdatePortInput input) {
        // LOG.debug("Calling the updatePort RPC method on MessageDispatchService");
        //
        // final OFRpcTask<UpdatePortInput, RpcResult<UpdatePortOutput>> task = OFRpcTaskFactory.createUpdatePortTask(
        // rpcTaskContext, input, null);
        // return task.submit();
        return null;
    }

}
