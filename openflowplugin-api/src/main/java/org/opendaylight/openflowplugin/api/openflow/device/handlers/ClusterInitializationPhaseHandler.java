package org.opendaylight.openflowplugin.api.openflow.device.handlers;

import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;

/**
 * Interface handles MASTER initialization on ownership change
 */
public interface ClusterInitializationPhaseHandler {

    /**
     * Methods initialization cycle between contexts
     * @param connectionContext to check actual connection state
     */
    boolean onContextBecomeMasterInitialized(final ConnectionContext connectionContext);

    default void initialSubmitTransaction(){
        //This method need to be override only in device context to submit initial data
    }
}
