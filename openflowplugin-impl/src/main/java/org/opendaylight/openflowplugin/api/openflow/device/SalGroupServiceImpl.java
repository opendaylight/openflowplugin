/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.device;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.sal.OFRpcFutureResultTransformFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.OFRpcTask;
import org.opendaylight.openflowplugin.openflow.md.core.sal.OFRpcTaskFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;

/**
 * @author joe
 * 
 */
public class SalGroupServiceImpl extends CommonService implements SalGroupService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SalGroupServiceImpl.class);

    @Override
    public Future<RpcResult<AddGroupOutput>> addGroup(final AddGroupInput input) {
        LOG.debug("Calling the GroupMod RPC method on MessageDispatchService");

        // use primary connection
        final SwitchConnectionDistinguisher cookie = null;

        final OFRpcTask<AddGroupInput, RpcResult<UpdateGroupOutput>> task = OFRpcTaskFactory.createAddGroupTask(
                rpcTaskContext, input, cookie);
        final ListenableFuture<RpcResult<UpdateGroupOutput>> result = task.submit();

        return Futures.transform(result, OFRpcFutureResultTransformFactory.createForAddGroupOutput());
    }

    @Override
    public Future<RpcResult<UpdateGroupOutput>> updateGroup(final UpdateGroupInput input) {
        LOG.debug("Calling the update Group Mod RPC method on MessageDispatchService");

        // use primary connection
        final SwitchConnectionDistinguisher cookie = null;

        final OFRpcTask<UpdateGroupInput, RpcResult<UpdateGroupOutput>> task = OFRpcTaskFactory.createUpdateGroupTask(
                rpcTaskContext, input, cookie);
        final ListenableFuture<RpcResult<UpdateGroupOutput>> result = task.submit();

        return result;
    }

    @Override
    public Future<RpcResult<RemoveGroupOutput>> removeGroup(final RemoveGroupInput input) {
        LOG.debug("Calling the Remove Group RPC method on MessageDispatchService");

        final SwitchConnectionDistinguisher cookie = null;
        final OFRpcTask<RemoveGroupInput, RpcResult<UpdateGroupOutput>> task = OFRpcTaskFactory.createRemoveGroupTask(
                rpcTaskContext, input, cookie);
        final ListenableFuture<RpcResult<UpdateGroupOutput>> result = task.submit();

        return Futures.transform(result, OFRpcFutureResultTransformFactory.createForRemoveGroupOutput());
    }
}
