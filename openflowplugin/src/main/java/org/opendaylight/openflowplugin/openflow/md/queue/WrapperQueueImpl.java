package org.opendaylight.openflowplugin.openflow.md.queue;

import java.util.Queue;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.openflow.md.queue.QueueItem;
import org.opendaylight.openflowplugin.api.openflow.md.queue.QueueKeeper.QueueType;
import org.opendaylight.openflowplugin.api.openflow.md.queue.QueueListenerMark;
import org.opendaylight.openflowplugin.api.openflow.md.util.PollableQueuesPriorityZipper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WrapperQueueImpl {

    protected static final Logger LOG = LoggerFactory
            .getLogger(WrapperQueueImpl.class);

    private int capacity;
    private double lowWaterMark;
    private double highWaterMark;

    private QueueListenerMark queueListenerMark;
    private PollableQueuesPriorityZipper<QueueItem<OfHeader>> queueZipper;

    private ConnectionAdapter connectionAdapter;

    private Queue<QueueItem<OfHeader>> queueDefault;

    /**
     * @param capacity
     * @param queueZipper
     */
    public WrapperQueueImpl(int capacity,
            PollableQueuesPriorityZipper<QueueItem<OfHeader>> queueZipper) {
        this.capacity = capacity;
        this.queueZipper = queueZipper;

        this.highWaterMark = capacity * 0.8;
        this.lowWaterMark = capacity * 0.65;

        queueListenerMark = new QueueListenerMark();
    }

    /**
     * Marking checks size of {@link #queueDefault} and on the basis of this is
     * set autoRead
     */
    private void marking() {
        if (queueDefault != null) {
            if (queueDefault.size() >= highWaterMark) {
                queueListenerMark.onHighWaterMark(connectionAdapter);
            } else if (queueDefault.size() <= lowWaterMark
                    && !queueListenerMark.isAutoRead(connectionAdapter)) {
                queueListenerMark.onLowWaterMark(connectionAdapter);
            }
        }
    }

    /**
     * offer item {@link QueueType#DEFAULT} and call {@link #marking()} for
     * check marks and set autoRead if it need it
     * 
     * @param queueDefault
     * @param qItem
     * @param connectionAdapter
     * @return true if offer was succesful
     */
    public boolean checkItemOffer(Queue<QueueItem<OfHeader>> queueDefault,
            QueueItemOFImpl qItem, ConnectionAdapter connectionAdapter) {

        this.queueDefault = queueDefault;
        this.connectionAdapter = connectionAdapter;

        boolean enqueued;

        enqueued = queueDefault.offer(qItem);
        marking();

        return enqueued;
    }

    /**
     * poll {@link QueueItem} and call {@link #marking()} for check marks and
     * set autoRead if it need it
     * 
     * @return polled item
     */
    public QueueItem<OfHeader> poll() {
        QueueItem<OfHeader> nextQueueItem = queueZipper.poll();
        marking();
        return nextQueueItem;
    }
}
