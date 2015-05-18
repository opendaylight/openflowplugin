package org.opendaylight.openflowplugin.api.openflow.md.queue;

import com.google.common.base.Preconditions;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaterMarkListenerImpl implements WaterMarkListener {

    private static final Logger LOG = LoggerFactory
            .getLogger(WaterMarkListenerImpl.class);

    private ConnectionAdapter connectionAdapter;

    public WaterMarkListenerImpl(ConnectionAdapter connectionAdapter) {
        this.connectionAdapter = Preconditions.checkNotNull(connectionAdapter);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.openflowplugin.api.openflow.md.queue.QueueListener#
     * onHighWaterMark
     * (org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter)
     */
    @Override
    public void onHighWaterMark() {
        connectionAdapter.setAutoRead(false);
        LOG.debug("AutoRead is set on false: {}", connectionAdapter.getRemoteAddress());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.openflowplugin.api.openflow.md.queue.QueueListener#
     * onLowWaterMark
     * (org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter)
     */
    @Override
    public void onLowWaterMark() {
        connectionAdapter.setAutoRead(true);
        LOG.debug("AutoRead is set on true: {}", connectionAdapter.getRemoteAddress());
    }
}
