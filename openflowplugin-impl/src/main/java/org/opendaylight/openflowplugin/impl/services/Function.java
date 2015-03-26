/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import java.math.BigInteger;
import java.util.concurrent.Future;
import org.opendaylight.yangtools.yang.common.RpcResult;

interface Function<T> {
    Future<RpcResult<T>> apply(final BigInteger IDConnection);
}
