/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.util;

import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.IMessageDispatchService;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.core.MessageFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 *
 */
public final class TaskUtil {

    private TaskUtil() {
        throw new AssertionError("TaskUtil is not expected to be instantiated.");
    }

    /**
     * @param session switch session context
     * @param cookie connection distinguisher cookie value
     * @param messageService message dispatch service instance
     * @return barrier response
     */
    public static RpcInputOutputTuple<BarrierInput, ListenableFuture<RpcResult<BarrierOutput>>> sendBarrier(SessionContext session,
                                                                                                            SwitchConnectionDistinguisher cookie, IMessageDispatchService messageService) {
        BarrierInput barrierInput = MessageFactory.createBarrier(
                session.getFeatures().getVersion(), session.getNextXid());
        Future<RpcResult<BarrierOutput>> barrierResult = messageService.barrier(barrierInput, cookie);
        ListenableFuture<RpcResult<BarrierOutput>> output = JdkFutureAdapters.listenInPoolThread(barrierResult);

        return new RpcInputOutputTuple<>(barrierInput, output);
    }


}
