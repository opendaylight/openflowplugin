/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.srm.impl;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.serviceutils.tools.rpc.FutureRpcResults;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.OdlSrmRpcsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RecoverInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RecoverOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.ReinstallInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.ReinstallOutput;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.RequireServiceComponentRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(immediate = true, service = OdlSrmRpcsService.class)
@RequireServiceComponentRuntime
public final class SrmRpcProvider implements OdlSrmRpcsService, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(SrmRpcProvider.class);

    private final DataBroker dataBroker;
    private final Registration reg;

    @Inject
    @Activate
    public SrmRpcProvider(@Reference DataBroker dataBroker, @Reference RpcProviderService rpcProvider) {
        this.dataBroker = dataBroker;
        reg = rpcProvider.registerRpcImplementation(OdlSrmRpcsService.class, this);
    }

    @Override
    @Deactivate
    @PreDestroy
    public void close() {
        reg.close();
    }

    @Override
    public ListenableFuture<RpcResult<RecoverOutput>> recover(RecoverInput input) {
        return FutureRpcResults.fromListenableFuture(LOG, "recover", input,
            () -> Futures.immediateFuture(SrmRpcUtils.callSrmOp(dataBroker, input))).build();
    }

    @Override
    public ListenableFuture<RpcResult<ReinstallOutput>> reinstall(ReinstallInput input) {
        return FutureRpcResults.fromListenableFuture(LOG, "reinstall", input,
            () -> Futures.immediateFuture(SrmRpcUtils.callSrmOp(dataBroker, input))).build();
    }
}
