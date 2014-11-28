package org.opendaylight.openflowplugin.service;

import java.util.concurrent.Future;
import org.opendaylight.controller.sal.common.util.Futures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.of.operational.status.rev700101.ChangeOperationalStatusInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.of.operational.status.rev700101.GetOperationalStatusOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.of.operational.status.rev700101.GetOperationalStatusOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.of.operational.status.rev700101.OfOperationalStatusService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Created by Martin Bobak mbobak@cisco.com on 11/28/14.
 */
public class OfOperationalStatusServiceImpl implements OfOperationalStatusService {


    @Override
    public Future<RpcResult<Void>> changeOperationalStatus(ChangeOperationalStatusInput input) {
        OfOperationalStatusHolder.setOperationalStatus(input.getOperationalStatus());
        //TODO : implement propper OFP status change
        return null;
    }

    @Override
    public Future<RpcResult<GetOperationalStatusOutput>> getOperationalStatus() {
        GetOperationalStatusOutputBuilder getOperationalStatusOutputBuilder = new GetOperationalStatusOutputBuilder();
        getOperationalStatusOutputBuilder.setOperationalStatus(OfOperationalStatusHolder.getOperationalStatus());
        RpcResultBuilder<GetOperationalStatusOutput> rpcResultBuilder = RpcResultBuilder.<GetOperationalStatusOutput>success().withResult(getOperationalStatusOutputBuilder.build());
        return Futures.immediateFuture(rpcResultBuilder.build());
    }
}
