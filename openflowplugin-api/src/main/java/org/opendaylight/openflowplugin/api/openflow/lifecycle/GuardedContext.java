package org.opendaylight.openflowplugin.api.openflow.lifecycle;

import com.google.common.util.concurrent.Service;
import java.util.function.Function;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;

/**
 * Stateful OpenFlow context wrapper
 */
public interface GuardedContext extends OFPContext {
    /**
     * Returns the lifecycle state of the service.
     *
     * @return the service state
     */
    Service.State state();

    /**
     * Maps delegate inside guarded context to T
     *
     * @param <T>         the type parameter
     * @param transformer the transformer
     * @return the t
     */
    <T> T map(Function<OFPContext, T> transformer);
}