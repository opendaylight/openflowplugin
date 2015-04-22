/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.base.Function;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;

public class SalGroupServiceImpl extends CommonService implements SalGroupService {


    public SalGroupServiceImpl(RequestContextStack requestContextStack, DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SalGroupServiceImpl.class);

    @Override
    public Future<RpcResult<AddGroupOutput>> addGroup(final AddGroupInput input) {
        deviceContext.getDeviceGroupRegistry().store(input.getGroupId());
        return this.<AddGroupOutput, Void>handleServiceCall(PRIMARY_CONNECTION,
                new Function<DataCrate<AddGroupOutput>, ListenableFuture<RpcResult<Void>>>() {

                    @Override
                    public ListenableFuture<RpcResult<Void>> apply(final DataCrate<AddGroupOutput> data) {
                        return convertAndSend(input, data);
                    }
                });
    }

    @Override
    public Future<RpcResult<UpdateGroupOutput>> updateGroup(final UpdateGroupInput input) {
        return this.<UpdateGroupOutput, Void>handleServiceCall(PRIMARY_CONNECTION,
                new Function<DataCrate<UpdateGroupOutput>, ListenableFuture<RpcResult<Void>>>() {

                    @Override
                    public ListenableFuture<RpcResult<Void>> apply(final DataCrate<UpdateGroupOutput> data) {
                        return convertAndSend(input.getUpdatedGroup(), data);
                    }
                });
    }

    @Override
    public Future<RpcResult<RemoveGroupOutput>> removeGroup(final RemoveGroupInput input) {
        deviceContext.getDeviceGroupRegistry().markToBeremoved(input.getGroupId());
        return this.<RemoveGroupOutput, Void>handleServiceCall(PRIMARY_CONNECTION,
                new Function<DataCrate<RemoveGroupOutput>, ListenableFuture<RpcResult<Void>>>() {

                    @Override
                    public ListenableFuture<RpcResult<Void>> apply(final DataCrate<RemoveGroupOutput> data) {
                        return convertAndSend(input, data);
                    }
                });
    }

    <T> ListenableFuture<RpcResult<Void>> convertAndSend(final Group iputGroup, final DataCrate<T> data) {
        messageSpy.spyMessage(iputGroup.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMITTED_SUCCESS);
        final GroupModInputBuilder ofGroupModInput = GroupConvertor.toGroupModInput(iputGroup, version, datapathId);
        final Xid xid = data.getRequestContext().getXid();
        ofGroupModInput.setXid(xid.getValue());
        return JdkFutureAdapters.listenInPoolThread(provideConnectionAdapter(data.getiDConnection()).groupMod(ofGroupModInput.build()));
    }
}
