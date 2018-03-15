/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.samples.consumer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class SimpleDropFirewall {

    private final SalFlowService flowService;

    public SimpleDropFirewall(SalFlowService flowService) {
        this.flowService = flowService;
    }

    public void start() {
    }

    public boolean addFlow(AddFlowInput flow) throws InterruptedException,
            ExecutionException, TimeoutException {
        Future<RpcResult<AddFlowOutput>> result = flowService.addFlow(flow);

        return result.get(5, TimeUnit.SECONDS).isSuccessful();
    }
}
