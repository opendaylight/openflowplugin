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
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.impl.services.CommonService;
import org.opendaylight.openflowplugin.impl.services.DataCrate;
import org.opendaylight.openflowplugin.impl.services.RequestInputUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table._case.MultipartRequestTableBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 2.4.2015.
 */
public class FlowTableStatisticsService extends CommonService {
    public FlowTableStatisticsService(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    public Future<RpcResult<List<MultipartReply>>> getFlowTablesStatistics(final MultiMsgCollector multiMsgCollector) {
        return handleServiceCall(
                PRIMARY_CONNECTION, new Function<DataCrate<List<MultipartReply>>, Future<RpcResult<Void>>>() {
                    @Override
                    public Future<RpcResult<Void>> apply(final DataCrate<List<MultipartReply>> data) {

                        // Create multipart request body for fetch all the group stats
                        final MultipartRequestTableCaseBuilder multipartRequestTableCaseBuilder = new MultipartRequestTableCaseBuilder();
                        final MultipartRequestTableBuilder multipartRequestTableBuilder = new MultipartRequestTableBuilder();
                        multipartRequestTableBuilder.setEmpty(true);
                        multipartRequestTableCaseBuilder.setMultipartRequestTable(multipartRequestTableBuilder.build());

                        // Set request body to main multipart request
                        final Xid xid = deviceContext.getNextXid();
                        data.getRequestContext().setXid(xid);
                        multiMsgCollector.registerMultipartXid(xid.getValue());
                        final MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                                MultipartType.OFPMPFLOW, xid.getValue(), version);

                        mprInput.setMultipartRequestBody(multipartRequestTableCaseBuilder.build());
                        final Future<RpcResult<Void>> resultFromOFLib = deviceContext.getPrimaryConnectionContext()
                                .getConnectionAdapter().multipartRequest(mprInput.build());

                        return JdkFutureAdapters.listenInPoolThread(resultFromOFLib);
                    }
                });
    }

}
