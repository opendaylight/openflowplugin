package org.opendaylight.openflowplugin.service;

import com.google.common.base.Preconditions;
import java.util.concurrent.Future;
import org.opendaylight.controller.sal.common.util.Futures;
import org.opendaylight.openflowplugin.openflow.md.core.sal.OpenflowPluginProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.common.config.impl.rev140326.OfpRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.of.operational.status.rev700101.ChangeOperationalStatusInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.of.operational.status.rev700101.GetOperationalStatusOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.of.operational.status.rev700101.GetOperationalStatusOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.of.operational.status.rev700101.OfOperationalStatusService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.of.operational.status.rev700101.OperStatus;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Created by Martin Bobak mbobak@cisco.com on 11/28/14.
 */
public class OfOperationalStatusServiceImpl implements OfOperationalStatusService {

    private static OpenflowPluginProvider openflowPluginProvider;

    public OfOperationalStatusServiceImpl(OpenflowPluginProvider openflowPluginProviderReference) {
        Preconditions.checkNotNull(openflowPluginProviderReference, "Reference to OpenflowPluginProvider can't be null.");
        this.openflowPluginProvider = openflowPluginProviderReference;
    }

    @Override
    public Future<RpcResult<Void>> changeOperationalStatus(ChangeOperationalStatusInput input) {
        OfOperationalStatusHolder.setOperationalStatus(input.getOperationalStatus());
        if (OperStatus.RUN.equals(input.getOperationalStatus())) {
            //we suggest that OperStat.RUN means we are with master controller
            openflowPluginProvider.fireRoleChange(OfpRole.BECOMEMASTER);
        } else if (OperStatus.STANDBY.equals(input.getOperationalStatus())) {
            //we suggest that OperStat.STANDBY means we are with slave controller
            openflowPluginProvider.fireRoleChange(OfpRole.BECOMESLAVE);
        }
        return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
    }

    @Override
    public Future<RpcResult<GetOperationalStatusOutput>> getOperationalStatus() {
        GetOperationalStatusOutputBuilder getOperationalStatusOutputBuilder = new GetOperationalStatusOutputBuilder();
        getOperationalStatusOutputBuilder.setOperationalStatus(OfOperationalStatusHolder.getOperationalStatus());
        RpcResultBuilder<GetOperationalStatusOutput> rpcResultBuilder = RpcResultBuilder.<GetOperationalStatusOutput>success().withResult(getOperationalStatusOutputBuilder.build());
        return Futures.immediateFuture(rpcResultBuilder.build());
    }
}
