package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.of._switch.config.pusher.rev141015;

import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.openflow.ofswitch.config.DefaultConfigPusher;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.NodeConfigService;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class DefaultConfigPusherModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.of._switch.config.pusher.rev141015.AbstractDefaultConfigPusherModule {
    public DefaultConfigPusherModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public DefaultConfigPusherModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.of._switch.config.pusher.rev141015.DefaultConfigPusherModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        InstanceIdentifier<FlowCapableNode> path = InstanceIdentifier.create(Nodes.class).child(Node.class).augmentation(FlowCapableNode.class);
        NodeConfigService nodeConfigService = getRpcRegistryDependency().getRpcService(NodeConfigService.class);
        return getDataBrokerDependency().registerDataChangeListener(
                LogicalDatastoreType.OPERATIONAL,
                path,
                new DefaultConfigPusher(nodeConfigService),
                AsyncDataBroker.DataChangeScope.BASE);
    }

}
