/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core;

import java.util.Collection;
import java.util.Collections;
import org.opendaylight.controller.sal.common.util.Rpcs;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Created by Martin Bobak mbobak@cisco.com on 8/25/14.
 */
public class RpcResultsUtil {

    /**
     * @param success
     * @param result
     * @param errors
     * @return
     */
    public static <T> RpcResult<T> createRpcResult(boolean success, T result, Collection<RpcError> errorsArg) {
        Collection<RpcError> errors = errorsArg;
        if (errors == null) {
            errors = Collections.emptyList();
        }
        return Rpcs.getRpcResult(success, result, errors);
    }

}
