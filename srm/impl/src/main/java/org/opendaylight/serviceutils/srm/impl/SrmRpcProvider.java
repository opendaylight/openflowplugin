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
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.serviceutils.tools.rpc.FutureRpcResults;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.OdlSrmRpcsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RecoverInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RecoverOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.ReinstallInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.ReinstallOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SrmRpcProvider implements OdlSrmRpcsService {
    private static final Logger LOG = LoggerFactory.getLogger(SrmRpcProvider.class);
    private final DataBroker dataBroker;

    @Inject
    public SrmRpcProvider(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
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
