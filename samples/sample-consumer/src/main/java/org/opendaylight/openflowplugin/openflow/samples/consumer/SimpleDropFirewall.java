/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.samples.consumer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.opendaylight.openflowplugin.impl.services.sal.SalFlowRpcs;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;

public class SimpleDropFirewall {

    private final SalFlowRpcs flowService;

    public SimpleDropFirewall(final SalFlowRpcs flowService) {
        this.flowService = flowService;
    }

    public boolean addFlow(final AddFlowInput flow) throws InterruptedException, ExecutionException, TimeoutException {
        return flowService.getRpcClassToInstanceMap().getInstance(AddFlow.class).invoke(flow)
            .get(5, TimeUnit.SECONDS).isSuccessful();
    }
}
