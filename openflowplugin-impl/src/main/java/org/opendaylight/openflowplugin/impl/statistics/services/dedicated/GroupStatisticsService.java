/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.dedicated;

import com.google.common.base.Function;
import com.google.common.util.concurrent.JdkFutureAdapters;
import org.opendaylight.openflowjava.protocol.api.util.BinContent;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.CommonService;
import org.opendaylight.openflowplugin.impl.services.DataCrate;
import org.opendaylight.openflowplugin.impl.services.RequestInputUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.group._case.MultipartRequestGroupBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 2.4.2015.
 */
public class GroupStatisticsService extends CommonService {
    public GroupStatisticsService(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    public Future<RpcResult<List<MultipartReply>>> getAllGroupStatistics() {
        return handleServiceCall(
                PRIMARY_CONNECTION, new Function<DataCrate<List<MultipartReply>>, Future<RpcResult<Void>>>() {
                    @Override
                    public Future<RpcResult<Void>> apply(final DataCrate<List<MultipartReply>> data) {
                        final MultipartRequestGroupCaseBuilder caseBuilder = new MultipartRequestGroupCaseBuilder();
                        final MultipartRequestGroupBuilder mprGroupBuild = new MultipartRequestGroupBuilder();
                        mprGroupBuild.setGroupId(new GroupId(
                                BinContent
                                        .intToUnsignedLong(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Group.OFPGALL
                                                .getIntValue())));
                        caseBuilder.setMultipartRequestGroup(mprGroupBuild.build());

                        // Create multipart request header
                        final Xid xid = deviceContext.getNextXid();
                        data.getRequestContext().setXid(xid);
                        final MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                                MultipartType.OFPMPGROUP, xid.getValue(), version);

                        // Set request body to main multipart request
                        mprInput.setMultipartRequestBody(caseBuilder.build());

                        // Send the request, no cookies associated, use any connection

                        final Future<RpcResult<Void>> resultFromOFLib = deviceContext.getPrimaryConnectionContext()
                                .getConnectionAdapter().multipartRequest(mprInput.build());
                        return JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

                    }
                });
    }
}
