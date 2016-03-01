package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.lldp.speaker.rev141023;

import org.opendaylight.controller.config.api.DependencyResolver;
import org.opendaylight.controller.config.api.ModuleIdentifier;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.openflowplugin.applications.lldpspeaker.LLDPSpeaker;
import org.opendaylight.openflowplugin.applications.lldpspeaker.NodeConnectorInventoryEventTranslator;
import org.opendaylight.openflowplugin.applications.lldpspeaker.OperationalStatusChangeService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LLDPSpeakerModule extends AbstractLLDPSpeakerModule {
    private static final Logger LOG = LoggerFactory
            .getLogger(LLDPSpeakerModule.class);

    public LLDPSpeakerModule(ModuleIdentifier identifier,
            DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public LLDPSpeakerModule(ModuleIdentifier identifier,
            DependencyResolver dependencyResolver, LLDPSpeakerModule oldModule,
            AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public AutoCloseable createInstance() {
        LOG.trace("Creating LLDP speaker.");

        PacketProcessingService packetProcessingService = getRpcRegistryDependency().getRpcService(PacketProcessingService.class);
        MacAddress macDestination = getAddressDestination();

        final LLDPSpeaker lldpSpeaker = new LLDPSpeaker(packetProcessingService, macDestination);
        final NodeConnectorInventoryEventTranslator eventTranslator = new NodeConnectorInventoryEventTranslator(
                getDataBrokerDependency(), lldpSpeaker);

        OperationalStatusChangeService operationalStatusChangeService = new OperationalStatusChangeService(lldpSpeaker);
        final BindingAwareBroker.RpcRegistration<LldpSpeakerService> statusServiceRegistration =
                getRpcRegistryDependency().addRpcImplementation(LldpSpeakerService.class, operationalStatusChangeService);

        lldpSpeaker.setOperationalStatus(OperStatus.RUN);

        return new AutoCloseable() {
            @Override
            public void close() {
                LOG.trace("Closing LLDP speaker.");
                eventTranslator.close();
                lldpSpeaker.close();
                statusServiceRegistration.close();
            }
        };
    }

}
