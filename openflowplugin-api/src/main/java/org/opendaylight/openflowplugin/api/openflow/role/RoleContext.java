/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.role;

import com.google.common.util.concurrent.ListenableFuture;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;
import org.opendaylight.openflowplugin.api.openflow.OFPManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SalRoleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Role context for change role on cluster
 */
public interface RoleContext extends  RequestContextStack, OFPContext {

    /**
     * Setter for sal role service
     * @param salRoleService
     */
    void setSalRoleService(@Nonnull final SalRoleService salRoleService);

    /**
     * Getter for sal role service
     * @return
     */
    SalRoleService getSalRoleService();

    ListenableFuture<RpcResult<SetRoleOutput>> makeDeviceSlave();
}
