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

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SalGroupServiceImpl.class);

    @Override
    public Future<RpcResult<AddGroupOutput>> addGroup(final AddGroupInput input) {
        return ServiceCallProcessingUtil.<AddGroupOutput> handleServiceCall(rpcContext, PRIMARY_CONNECTION,
                provideWaitTime(), new Function<Void>() {

                    @Override
                    public Future<RpcResult<Void>> apply(final BigInteger IDConnection) {
                        return convertAndSend(input, IDConnection);
                    }
                });
    }

    @Override
    public Future<RpcResult<UpdateGroupOutput>> updateGroup(final UpdateGroupInput input) {
        return ServiceCallProcessingUtil.<UpdateGroupOutput> handleServiceCall(rpcContext, PRIMARY_CONNECTION,
                provideWaitTime(), new Function<Void>() {

                    @Override
                    public Future<RpcResult<Void>> apply(final BigInteger IDConnection) {
                        return convertAndSend(input.getUpdatedGroup(), IDConnection);
                    }
                });
    }

    @Override
    public Future<RpcResult<RemoveGroupOutput>> removeGroup(final RemoveGroupInput input) {
        return ServiceCallProcessingUtil.<RemoveGroupOutput> handleServiceCall(rpcContext, PRIMARY_CONNECTION,
                provideWaitTime(), new Function<Void>() {

                    @Override
                    public Future<RpcResult<Void>> apply(final BigInteger IDConnection) {
                        return convertAndSend(input, IDConnection);
                    }
                });
    }

    Future<RpcResult<Void>> convertAndSend(final Group iputGroup, final BigInteger IDConnection) {
        final GroupModInputBuilder ofGroupModInput = GroupConvertor.toGroupModInput(iputGroup, version, datapathId);
        ofGroupModInput.setXid(deviceContext.getNextXid().getValue());
        return provideConnectionAdapter(IDConnection).groupMod(ofGroupModInput.build());
    }
}
