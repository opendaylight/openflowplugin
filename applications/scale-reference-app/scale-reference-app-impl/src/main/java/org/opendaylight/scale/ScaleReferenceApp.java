package org.opendaylight.scale;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.applications.prioritytaskmgr.api.IPriorityTaskManager;
import org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.api.IOpenflowFacade;
import org.opendaylight.scale.impl.ScaleRefAppImpl;
import org.opendaylight.scale.inventory.NodeEventListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.scale.ref.app.rev160923.ScaleRefAppModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by evijayd on 9/26/2016.
 */
public class ScaleReferenceApp implements AutoCloseable {

    private DataBroker dataBroker = null;
    private RpcProviderRegistry rpcProviderRegistry = null;
    private IPriorityTaskManager priorityTaskManager = null;
    private static IOpenflowFacade southboundmanagerDependency = null;
    private final BindingAwareBroker.RpcRegistration<ScaleRefAppModelService> serviceRpcRegistration;

    private static final Logger LOG = LoggerFactory.getLogger(ScaleReferenceApp.class);

    public ScaleReferenceApp(DataBroker dataBrokerDependency, RpcProviderRegistry rpcRegistryDependency, IPriorityTaskManager prioritytaskmgrDependency, IOpenflowFacade southboundmanagerDependency) {
        this.dataBroker = dataBrokerDependency;
        this.rpcProviderRegistry = rpcRegistryDependency;
        this.priorityTaskManager = prioritytaskmgrDependency;
        this.southboundmanagerDependency = southboundmanagerDependency;

        NodeEventListener nodeEventListener = registerNodeEvemtListener();
        ScaleRefAppModelService service = new ScaleRefAppImpl(nodeEventListener,prioritytaskmgrDependency,southboundmanagerDependency);
        serviceRpcRegistration = rpcProviderRegistry.addRpcImplementation(ScaleRefAppModelService.class, service);
    }

    private NodeEventListener registerNodeEvemtListener() {
        NodeEventListener nodeEventListener = new NodeEventListener(dataBroker);
        nodeEventListener.register();
        return nodeEventListener;
    }

    public static IOpenflowFacade getSouthboundmanagerDependency(){
        return southboundmanagerDependency;
    }

    @Override
    public void close() throws Exception {
        LOG.info("destroying scale-reference-app");
        serviceRpcRegistration.close();
    }
}
