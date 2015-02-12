package org.opendaylight.openflowplugin.api.openflow.md.queue;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;

public class QueueListenerMark implements QueueListener {

    /*
     * (non-Javadoc)
     * 
     * @see org.opendaylight.openflowplugin.api.openflow.md.queue.QueueListener#
     * onHighWaterMark
     * (org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter)
     */
    @Override
    public void onHighWaterMark(ConnectionAdapter connectionAdapter) {
        connectionAdapter.setAutoRead(false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opendaylight.openflowplugin.api.openflow.md.queue.QueueListener#
     * onLowWaterMark
     * (org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter)
     */
    @Override
    public void onLowWaterMark(ConnectionAdapter connectionAdapter) {
        connectionAdapter.setAutoRead(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opendaylight.openflowplugin.api.openflow.md.queue.QueueListener#
     * isAutoRead
     * (org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter)
     */
    public boolean isAutoRead(ConnectionAdapter connectionAdapter) {
        return connectionAdapter.isAutoRead();
    }
}
