package org.opendaylight.openflowplugin.openflow.md.util;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.api.openflow.md.queue.QueueItem;
import org.opendaylight.openflowplugin.api.openflow.md.queue.QueueKeeper.QueueType;
import org.opendaylight.openflowplugin.api.openflow.md.util.PollableQueuesPriorityZipper;
import org.opendaylight.openflowplugin.openflow.md.core.plan.ConnectionAdapterStackImpl;
import org.opendaylight.openflowplugin.openflow.md.queue.QueueItemOFImpl;
import org.opendaylight.openflowplugin.openflow.md.queue.WrapperQueueImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class WrapperQueueImplTest {

    protected static final Logger LOG = LoggerFactory
            .getLogger(WrapperQueueImplTest.class);

    @Mock
    private ConnectionConductor connectionConductor;

    private WrapperQueueImpl wrapperQueueImpl;
    private ArrayList<ErrorMessage> messages;
    private final int capacity = 100;
    private PollableQueuesPriorityZipper<QueueItem<OfHeader>> queueZipper;
    private Queue<QueueItem<OfHeader>> queueDefault;
    private ConnectionAdapterStackImpl connectionAdapter;

    private int highWaterMark;
    private int lowWaterMark;

    /**
     * Setup before tests
     */
    @Before
    public void setUp() {
        highWaterMark = (int) (capacity * 0.8);
        lowWaterMark = (int) (capacity * 0.65);

        connectionAdapter = new ConnectionAdapterStackImpl();

        messages = new ArrayList<>();
        generateMessages();

        queueDefault = new ArrayBlockingQueue<>(capacity);
        queueZipper = new PollableQueuesPriorityZipper<>();
        queueZipper.setPrioritizedSource(queueDefault);

        wrapperQueueImpl = new WrapperQueueImpl(capacity, queueZipper);
    }

    /**
     * Generate messages
     */
    private void generateMessages() {
        for (int i = 0; i < capacity; i++) {
            ErrorMessageBuilder errorMessageBuilder = new ErrorMessageBuilder();
            errorMessageBuilder.setVersion((short) 4);
            errorMessageBuilder.setCode(100 + i);

            messages.add(errorMessageBuilder.build());
        }
    }

    /**
     * Test for check if wrapper is not null
     */
    @Test
    public void testWrapperQueueImpl() {
        Assert.assertNotNull("Wrapper can not be null.", wrapperQueueImpl);
    }

    /**
     * Test for set setAutoRead on false on high water mark
     */
    @Test
    public void testReadOnHighWaterMark() {

        Assert.assertTrue("AutoRead has to be on start set on true.",
                connectionAdapter.isAutoRead());

        push();

        Assert.assertTrue("AutoRead has to be set on false on highWaterMark.",
                !connectionAdapter.isAutoRead());
        Assert.assertEquals(
                "Size of queue has to be equals to 80% of capacity of queue",
                highWaterMark, queueDefault.size());
    }

    /**
     * Pushing messages while isAutoRead is true
     */
    private void push() {
        int index = 0;

        while (connectionAdapter.isAutoRead()) {
            QueueItemOFImpl qItem = new QueueItemOFImpl(messages.get(index),
                    connectionConductor, QueueType.DEFAULT);
            try {
                wrapperQueueImpl.checkItemOffer(queueDefault, qItem,
                        connectionAdapter);
            } catch (Exception e) {
                LOG.error(e.toString());
            }
            index++;
        }
    }

    /**
     * Test for setAutoRead on true on low water mark
     */
    @Test
    public void testReadOnLowWaterMark() {
        push();

        poll();

        Assert.assertEquals(
                "Size of queue has to be equals to 65% on lowWaterMark.",
                lowWaterMark, queueDefault.size());
        Assert.assertTrue("AutoRead has to be true on lowWaterMark.",
                connectionAdapter.isAutoRead());
    }

    /**
     * Polling messages while isAutoRead is not true
     */
    private void poll() {
        while (!connectionAdapter.isAutoRead()) {
            wrapperQueueImpl.poll();
        }
    }

    /**
     * Test for start pushing on low water mark and stop on high water mark.
     * Test starts pushing messages until it is at the high water mark. On the
     * high water mark, test starts poll messages from queue. After achieve low
     * water mark, read is set on true and it is starting to push messages
     * again. Test ends on high water mark. This is one cycle.
     */
    @Test
    public void testEndReadOnHWMStartOnLWM() {
        int index = 0, p = 0;
        while (index < 2) {
            if (connectionAdapter.isAutoRead()) {
                p = 0;

                ErrorMessageBuilder errorMessageBuilder = new ErrorMessageBuilder();
                errorMessageBuilder.setVersion((short) 4);
                errorMessageBuilder.setCode(100);

                pushMessage(errorMessageBuilder.build());
            } else {
                if (p == 0) {
                    index++;
                }
                try {
                    wrapperQueueImpl.poll();
                } catch (NullPointerException e) {
                    LOG.error(e.toString());
                }
                p++;
            }
        }
        Assert.assertTrue(!connectionAdapter.isAutoRead());
        Assert.assertEquals(highWaterMark, queueDefault.size() + 1);
        Assert.assertNotEquals(lowWaterMark, queueDefault.size() + 1);
    }

    /**
     * Pushing message
     * 
     * @param message
     */
    private void pushMessage(ErrorMessage message) {
        QueueItemOFImpl qItem = new QueueItemOFImpl(message,
                connectionConductor, QueueType.DEFAULT);
        try {
            wrapperQueueImpl.checkItemOffer(queueDefault, qItem,
                    connectionAdapter);
        } catch (Exception e) {
            LOG.error(e.toString());
        }
    }

}
