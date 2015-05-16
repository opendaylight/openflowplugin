/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.Future;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.GroupConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;

public class SalGroupServiceImpl extends CommonService implements SalGroupService {


    public SalGroupServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SalGroupServiceImpl.class);

    @Override
    public Future<RpcResult<AddGroupOutput>> addGroup(final AddGroupInput input) {
        getDeviceContext().getDeviceGroupRegistry().store(input.getGroupId());
        return this.<AddGroupOutput, Void>handleServiceCall(new Function<RequestContext<AddGroupOutput>, ListenableFuture<RpcResult<Void>>>() {
            @Override
            public ListenableFuture<RpcResult<Void>> apply(final RequestContext<AddGroupOutput> requestContext) {
                return convertAndSend(input, requestContext);
            }
        });
    }

    @Override
    public Future<RpcResult<UpdateGroupOutput>> updateGroup(final UpdateGroupInput input) {
        return this.<UpdateGroupOutput, Void>handleServiceCall(new Function<RequestContext<UpdateGroupOutput>, ListenableFuture<RpcResult<Void>>>() {

            @Override
            public ListenableFuture<RpcResult<Void>> apply(final RequestContext<UpdateGroupOutput> requestContext) {
                return convertAndSend(input.getUpdatedGroup(), requestContext);
            }
        });
    }

    @Override
    public Future<RpcResult<RemoveGroupOutput>> removeGroup(final RemoveGroupInput input) {
        getDeviceContext().getDeviceGroupRegistry().markToBeremoved(input.getGroupId());
        return this.<RemoveGroupOutput, Void>handleServiceCall(new Function<RequestContext<RemoveGroupOutput>, ListenableFuture<RpcResult<Void>>>() {

            @Override
            public ListenableFuture<RpcResult<Void>> apply(final RequestContext<RemoveGroupOutput> requestContext) {
                return convertAndSend(input, requestContext);
            }
        });
    }

    <T> ListenableFuture<RpcResult<Void>> convertAndSend(final Group iputGroup, final RequestContext<T> requestContext) {
        final OutboundQueue outboundQueue = getDeviceContext().getPrimaryConnectionContext().getOutboundQueueProvider();
        getMessageSpy().spyMessage(iputGroup.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_SUCCESS);
        final GroupModInputBuilder ofGroupModInput = GroupConvertor.toGroupModInput(iputGroup, getVersion(), getDatapathId());
        final Xid xid = requestContext.getXid();
        ofGroupModInput.setXid(xid.getValue());
        final SettableFuture<RpcResult<Void>> settableFuture = SettableFuture.create();
        final GroupModInput groupModInput = ofGroupModInput.build();
        outboundQueue.commitEntry(xid.getValue(), groupModInput, new FutureCallback<OfHeader>() {
            @Override
            public void onSuccess(final OfHeader ofHeader) {
                RequestContextUtil.closeRequstContext(requestContext);
                getDeviceContext().unhookRequestCtx(requestContext.getXid());
                getMessageSpy().spyMessage(groupModInput.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_SUCCESS);

                settableFuture.set(RpcResultBuilder.<Void>success().build());
            }

            @Override
            public void onFailure(final Throwable throwable) {
                RpcResultBuilder<Void> rpcResultBuilder = RpcResultBuilder.<Void>failed().withError(RpcError.ErrorType.APPLICATION, throwable.getMessage(), throwable);
                RequestContextUtil.closeRequstContext(requestContext);
                getDeviceContext().unhookRequestCtx(requestContext.getXid());
                getMessageSpy().spyMessage(groupModInput.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_FAILURE);
                settableFuture.set(rpcResultBuilder.build());
            }
        });
        return settableFuture;
    }
}
