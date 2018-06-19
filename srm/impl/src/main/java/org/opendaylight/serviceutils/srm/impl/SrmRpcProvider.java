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

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.serviceutils.tools.mdsal.rpc.FutureRpcResults;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpcs.rev170711.RecoverInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpcs.rev170711.RecoverOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpcs.rev170711.ReinstallInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpcs.rev170711.ReinstallOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpcs.rev170711.SrmRpcsService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SrmRpcProvider implements SrmRpcsService {

    private static final Logger LOG = LoggerFactory.getLogger(SrmRpcProvider.class);

    private final DataBroker dataBroker;

    @Inject
    public SrmRpcProvider(final DataBroker dataBroker) {
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
