/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.statistics.compatibility;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Prescribes handling and transforming of backward compatible services in order to provide simple to use service
 * with notification support
 *
 * @param <O> rpc input
 * @param <I> rpc input
 * @deprecated for backward compatibility only (expected to extinct after Be-release)
 */
@Deprecated
public interface BackwardCompatibleAtomicService<I, O> {

    /**
     * process rpc request and publish corresponding notification upon success
     *
     * @param input                      rpc input
     * @param notificationPublishService publisher handle
     */
    ListenableFuture<RpcResult<O>> handleAndNotify(I input, NotificationPublishService notificationPublishService);
}
