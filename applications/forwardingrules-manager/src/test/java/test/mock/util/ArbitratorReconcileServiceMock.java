/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package test.mock.util;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.ArbitratorReconcileService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.CommitActiveBundleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.CommitActiveBundleOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.CommitActiveBundleOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.GetActiveBundleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.GetActiveBundleOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.GetActiveBundleOutputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class ArbitratorReconcileServiceMock implements ArbitratorReconcileService {
    @Override
    public ListenableFuture<RpcResult<CommitActiveBundleOutput>> commitActiveBundle(
            CommitActiveBundleInput input) {
        return RpcResultBuilder.success(new CommitActiveBundleOutputBuilder().setResult(null).build()).buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<GetActiveBundleOutput>> getActiveBundle(
            GetActiveBundleInput input) {
        return RpcResultBuilder.success((new GetActiveBundleOutputBuilder()
                .setResult(null).build())).buildFuture();
    }
}
