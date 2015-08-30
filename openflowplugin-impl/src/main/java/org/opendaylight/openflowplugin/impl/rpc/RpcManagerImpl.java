package org.opendaylight.openflowplugin.impl.rpc;

import javax.annotation.CheckForNull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.impl.util.MdSalRegistratorUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;

/**
 * Created by kramesha on 9/1/15.
 */
public class RpcManagerImpl implements RpcManager{

    private DeviceInitializationPhaseHandler deviceInitPhaseHandler;

    @Override
    public void onDeviceContextLevelUp(@CheckForNull DeviceContext deviceContext) {

        if (deviceContext.getDeviceState().getRole() == null || deviceContext.getDeviceState().getRole() != OfpRole.BECOMESLAVE) {
            // if the role change was not successful the role in devicestate will be null, still we would be registering services
            MdSalRegistratorUtils.registerServices(deviceContext.getRpcContext(), deviceContext);
        }

        // finish device initialization cycle back to DeviceManager
        deviceInitPhaseHandler.onDeviceContextLevelUp(deviceContext);
    }

    @Override
    public void setDeviceInitializationPhaseHandler(DeviceInitializationPhaseHandler handler) {
        deviceInitPhaseHandler = handler;
    }

    @Override
    public void close() throws Exception {

    }
}
