/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.util;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import java.util.Collection;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util methods for {@link com.google.common.util.concurrent.ListenableFuture} chaining.
 */
public final class FxChainUtil {

    private static final Logger LOG = LoggerFactory.getLogger(FxChainUtil.class);

    private FxChainUtil() {
        throw new IllegalStateException("This class should not be instantiated.");
    }


    public static FutureCallback<RpcResult<Void>> logResultCallback(final NodeId nodeId, final String prefix) {
        return new FutureCallback<>() {
            @Override
            public void onSuccess(final RpcResult<Void> result) {
                if (result != null) {
                    if (result.isSuccessful()) {
                        LOG.debug("{} finished successfully: {}", prefix, nodeId.getValue());
                    } else {
                        final Collection<RpcError> errors = MoreObjects.firstNonNull(result.getErrors(),
                                ImmutableList.of());
                        LOG.debug("{} failed: {} -> {}", prefix, nodeId.getValue(), errors);
                    }
                } else {
                    LOG.debug("{} reconciliation failed: {} -> null result", prefix, nodeId.getValue());
                }
            }

            @Override
            public void onFailure(final Throwable failure) {
                LOG.debug("{} reconciliation failed seriously: {}", prefix, nodeId.getValue(), failure);
            }
        };
    }
}
