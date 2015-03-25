/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import java.util.concurrent.Future;
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
 */
public class SalGroupServiceImpl extends CommonService implements SalGroupService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SalGroupServiceImpl.class);

    @Override
    public Future<RpcResult<AddGroupOutput>> addGroup(final AddGroupInput input) {
        // LOG.debug("Calling the GroupMod RPC method on MessageDispatchService");
        //
        // ListenableFuture<RpcResult<UpdateGroupOutput>> result = SettableFuture.create();
        //
        // // Convert the AddGroupInput to GroupModInput
        // final GroupModInputBuilder ofGroupModInput = GroupConvertor.toGroupModInput(input, version, datapathId);
        // final Xid xId = deviceContext.getNextXid();
        // ofGroupModInput.setXid(xId.getValue());
        //
        // final Future<RpcResult<UpdateGroupOutput>> resultFromOFLib = messageService.groupMod(ofGroupModInput.build(),
        // cookie);
        // result = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);
        //
        // result = chainFutureBarrier(result);
        // hookFutureNotification(result, notificationProviderService, createGroupAddedNotification(input));
        //
        // return Futures.transform(result, OFRpcFutureResultTransformFactory.createForAddGroupOutput());
        return null;
    }

    @Override
    public Future<RpcResult<UpdateGroupOutput>> updateGroup(final UpdateGroupInput input) {
        // LOG.debug("Calling the update Group Mod RPC method on MessageDispatchService");
        //
        // // use primary connection
        //
        // ListenableFuture<RpcResult<UpdateGroupOutput>> result = null;
        //
        // // Convert the UpdateGroupInput to GroupModInput
        // final GroupModInputBuilder ofGroupModInput = GroupConvertor.toGroupModInput(input.getUpdatedGroup(), version,
        // datapathId);
        // final Xid xId = deviceContext.getNextXid();
        // ofGroupModInput.setXid(xId.getValue());
        //
        // final Future<RpcResult<UpdateGroupOutput>> resultFromOFLib = messageService.groupMod(ofGroupModInput.build(),
        // cookie);
        // result = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);
        //
        // result = chainFutureBarrier(result);
        // hookFutureNotification(result, notificationProviderService, createGroupUpdatedNotification(input));
        //
        // return result;
        return null;
    }

    @Override
    public Future<RpcResult<RemoveGroupOutput>> removeGroup(final RemoveGroupInput input) {
        // LOG.debug("Calling the Remove Group RPC method on MessageDispatchService");
        //
        // ListenableFuture<RpcResult<UpdateGroupOutput>> result = SettableFuture.create();
        //
        // // Convert the AddGroupInput to GroupModInput
        // final GroupModInputBuilder ofGroupModInput = GroupConvertor.toGroupModInput(input, version, datapathId);
        // final Xid xId = deviceContext.getNextXid();
        // ofGroupModInput.setXid(xId.getValue());
        //
        // final Future<RpcResult<UpdateGroupOutput>> resultFromOFLib = messageService.groupMod(ofGroupModInput.build(),
        // cookie);
        // result = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);
        //
        // result = chainFutureBarrier(result);
        // hookFutureNotification(result, notificationProviderService, createGroupRemovedNotification(input));
        //
        // return Futures.transform(result, OFRpcFutureResultTransformFactory.createForRemoveGroupOutput());
        return null;
    }

}
