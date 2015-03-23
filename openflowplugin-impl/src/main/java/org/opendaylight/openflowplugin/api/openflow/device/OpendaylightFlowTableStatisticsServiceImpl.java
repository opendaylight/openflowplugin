/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.device;

import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.GetFlowTablesStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.GetFlowTablesStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.OpendaylightFlowTableStatisticsService;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * @author joe
 * 
 */
// TODO: implement this
public class OpendaylightFlowTableStatisticsServiceImpl extends CommonService implements
        OpendaylightFlowTableStatisticsService {

    /**
     * @param rpcContext
     */
    public OpendaylightFlowTableStatisticsServiceImpl(final RpcContext rpcContext) {
        super(rpcContext);
    }

    /*
         * (non-Javadoc)
         *
         * @see
         * org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.OpendaylightFlowTableStatisticsService
         * #getFlowTablesStatistics(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.
         * GetFlowTablesStatisticsInput)
         */
    @Override
    public Future<RpcResult<GetFlowTablesStatisticsOutput>> getFlowTablesStatistics(
            final GetFlowTablesStatisticsInput input) {
        // TODO Auto-generated method stub
        return null;
    }

}
