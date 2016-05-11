package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.lldp.speaker.rev141023;

import org.opendaylight.controller.config.api.DependencyResolver;
import org.opendaylight.controller.config.api.ModuleIdentifier;
import org.opendaylight.controller.sal.common.util.NoopAutoCloseable;

/**
 * @deprecated Replaced by blueprint wiring
 */
@Deprecated
public class LLDPSpeakerModule extends AbstractLLDPSpeakerModule {
    public LLDPSpeakerModule(ModuleIdentifier identifier, DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public LLDPSpeakerModule(ModuleIdentifier identifier,
            DependencyResolver dependencyResolver, LLDPSpeakerModule oldModule, AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public AutoCloseable createInstance() {
        // Instances are created via blueprint so this in a no-op.
        return NoopAutoCloseable.INSTANCE;
    }
}
