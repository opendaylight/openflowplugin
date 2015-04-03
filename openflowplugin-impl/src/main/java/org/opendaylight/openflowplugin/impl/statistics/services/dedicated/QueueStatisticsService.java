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
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.impl.common.MultipartRequestInputFactory;
import org.opendaylight.openflowplugin.impl.services.CommonService;
import org.opendaylight.openflowplugin.impl.services.DataCrate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 2.4.2015.
 */
public class QueueStatisticsService extends CommonService {

    public QueueStatisticsService(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    public Future<RpcResult<List<MultipartReply>>> getAllQueuesStatisticsFromAllPorts(final MultiMsgCollector multiMsgCollector) {
        return handleServiceCall(
                PRIMARY_CONNECTION, new Function<DataCrate<List<MultipartReply>>, ListenableFuture<RpcResult<Void>>>() {

                    @Override
                    public ListenableFuture<RpcResult<Void>> apply(final DataCrate<List<MultipartReply>> data) {

                        final Xid xid = deviceContext.getNextXid();
                        data.getRequestContext().setXid(xid);
                        multiMsgCollector.registerMultipartXid(xid.getValue());

                        final Future<RpcResult<Void>> resultFromOFLib = deviceContext.getPrimaryConnectionContext()
                                .getConnectionAdapter().multipartRequest(
                                        MultipartRequestInputFactory.makeMultipartRequestInput(
                                                xid.getValue(),
                                                version,
                                                MultipartType.OFPMPQUEUE));
                        return JdkFutureAdapters.listenInPoolThread(resultFromOFLib);
                    }
                });
    }

}
