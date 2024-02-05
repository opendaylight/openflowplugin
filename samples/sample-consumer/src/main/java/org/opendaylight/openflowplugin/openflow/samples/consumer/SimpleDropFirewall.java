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
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.api.RpcService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Singleton
@Component(service = { })
public final class SimpleDropFirewall {
    private final AddFlow addFlow;

    @Inject
    @Activate
    public SimpleDropFirewall(@Reference final RpcService rpcService) {
        addFlow = rpcService.getRpc(AddFlow.class);
    }

    public boolean addFlow(final AddFlowInput flow) throws InterruptedException, ExecutionException, TimeoutException {
        return addFlow.invoke(flow).get(5, TimeUnit.SECONDS).isSuccessful();
    }
}
