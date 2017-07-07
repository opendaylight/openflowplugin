/*
 * Copyright (c) 2015, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.rpc;

import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.openflow.OFPManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;

/**
 * The RPC Manager will maintain an RPC Context for each online switch.
 */
public interface RpcManager extends OFPManager {

    void setStatisticsRpcEnabled(boolean statisticsRpcEnabled);

    void setRpcRequestQuota(int rpcRequestQuota);

    RpcContext createContext(@Nonnull DeviceContext deviceContext);

}
