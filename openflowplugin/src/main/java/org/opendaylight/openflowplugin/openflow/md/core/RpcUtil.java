/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core;

import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * @author mirehak
 *
 */
public abstract class RpcUtil {

    /**
     * @param result rpc result
     * @throws Exception exception thrown by method if rpc fails
     */
    public static void smokeRpc(RpcResult<?> result) throws Exception {
        if (!result.isSuccessful()) {
            Throwable firstCause = null;
            StringBuilder sb = new StringBuilder();
            for (RpcError error : result.getErrors()) {
                if (firstCause != null) {
                    firstCause = error.getCause();
                }
                
                sb.append("rpcError:").append(error.getCause().getMessage()).append(';');
            }
            throw new Exception(sb.toString(), firstCause);
        }
    }

}
