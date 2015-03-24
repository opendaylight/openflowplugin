/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.device;

import com.google.common.util.concurrent.Futures;
import java.math.BigInteger;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class CommonService {
    // protected OFRpcTaskContext rpcTaskContext;
    protected short version;
    protected BigInteger datapathId;
    protected RpcContext rpcContext;
    protected SwitchConnectionDistinguisher cookie;

    protected final static Future<RpcResult<Void>> errorRpcResult = Futures.immediateFuture(RpcResultBuilder
            .<Void> failed().withError(ErrorType.APPLICATION, "", "Request quota exceeded.").build());

    /**
     * 
     */
    public CommonService(final RpcContext rpcContext, final short version, final BigInteger datapathId) {
        this.rpcContext = rpcContext;
        this.version = version;
        this.datapathId = datapathId;
    }

}
