package org.opendaylight.openflowplugin.api.openflow.md.queue;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;

public interface QueueListener {

    /**
     * @param connectionAdapter
     */
    void onHighWaterMark(ConnectionAdapter connectionAdapter);

    /**
     * @param connectionAdapter
     */
    void onLowWaterMark(ConnectionAdapter connectionAdapter);

    /**
     * @param connectionAdapter
     * @return
     */
    boolean isAutoRead(ConnectionAdapter connectionAdapter);
}
