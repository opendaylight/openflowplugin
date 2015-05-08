/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.queue;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.opendaylight.openflowplugin.api.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.api.openflow.md.queue.HarvesterHandle;
import org.opendaylight.openflowplugin.api.openflow.md.queue.PopListener;
import org.opendaylight.openflowplugin.api.openflow.md.queue.QueueItem;
import org.opendaylight.openflowplugin.api.openflow.md.queue.QueueKeeper;
import org.opendaylight.openflowplugin.api.openflow.md.queue.QueueProcessor;
import org.opendaylight.openflowplugin.api.openflow.statistics.MessageSpy;
import org.opendaylight.openflowplugin.api.openflow.statistics.MessageSpy.STATISTIC_GROUP;
import org.opendaylight.openflowplugin.openflow.md.core.ThreadPoolLoggingExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * {@link org.opendaylight.openflowplugin.api.openflow.md.queue.QueueKeeper} implementation focused to keep order and use up mutiple threads for translation phase.
 * <br>
 * There is internal thread pool of limited size ({@link QueueProcessorLightImpl#setProcessingPoolSize(int)})
 * dedicated to translation. Then there is singleThreadPool dedicated to publishing (via popListeners)
 * <br>
 * Workflow:
 * <ol>
 * <li>upon message push ticket is created and enqueued</li>
 * <li>available threads from internal pool translate the massage wrapped in ticket</li>
 * <li>when translation of particular message is finished, result is set in future result of wrapping ticket<br>
 *     (order of tickets in queue is not touched during translate)
 * </li>
 * <li>at the end of queue there is {@link TicketFinisher} running in singleThreadPool and for each ticket it does:
 *    <ol>
 *      <li>invoke blocking {@link BlockingQueue#take()} method in order to get the oldest ticket</li>
 *      <li>invoke blocking {@link Future#get()} on the dequeued ticket</li>
 *      <li>as soon as the result of translation is available, appropriate popListener is invoked</li>
 *    </ol>
 *    and this way the order of messages is preserved and also multiple threads are used by translating
 * </li>
 * </ol>
 *
 *
 */
public class QueueProcessorLightImpl implements QueueProcessor<OfHeader, DataObject> {

    private static final Logger LOG = LoggerFactory
            .getLogger(QueueProcessorLightImpl.class);

    private BlockingQueue<TicketResult<DataObject>> ticketQueue;
    private ThreadPoolExecutor processorPool;
    private int processingPoolSize = 4;
    private ExecutorService harvesterPool;
    private ExecutorService finisherPool;

    protected Map<Class<? extends DataObject>, Collection<PopListener<DataObject>>> popListenersMapping;
    private Map<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, List<DataObject>>>> translatorMapping;
    private TicketProcessorFactory<OfHeader, DataObject> ticketProcessorFactory;
    private MessageSpy<DataContainer> messageSpy;
    protected Collection<QueueKeeper<OfHeader>> messageSources;
    private QueueKeeperHarvester<OfHeader> harvester;

    protected TicketFinisher<DataObject> finisher;

    /**
     * prepare queue
     */
    public void init() {
        int ticketQueueCapacity = 1500;
        ticketQueue = new ArrayBlockingQueue<>(ticketQueueCapacity);
        /*
         * TODO FIXME - DOES THIS REALLY NEED TO BE CONCURRENT?  Can we figure out
         * a better lifecycle?  Why does this have to be a Set?
         */
        messageSources = new CopyOnWriteArraySet<>();

        processorPool = new ThreadPoolLoggingExecutor(processingPoolSize, processingPoolSize, 0,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(ticketQueueCapacity),
                "OFmsgProcessor");
        // force blocking when pool queue is full
        processorPool.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                try {
                    executor.getQueue().put(r);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                }
            }
        });

        harvesterPool = new ThreadPoolLoggingExecutor(1, 1, 0,
                TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1), "OFmsgHarvester");
        finisherPool = new ThreadPoolLoggingExecutor(1, 1, 0,
                TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1), "OFmsgFinisher");
        finisher = new TicketFinisherImpl(
                ticketQueue, popListenersMapping);
        finisherPool.execute(finisher);

        harvester = new QueueKeeperHarvester<OfHeader>(this, messageSources);
        harvesterPool.execute(harvester);

        ticketProcessorFactory = new TicketProcessorFactoryImpl();
        ticketProcessorFactory.setTranslatorMapping(translatorMapping);
        ticketProcessorFactory.setSpy(messageSpy);
        ticketProcessorFactory.setTicketFinisher(finisher);
    }

    /**
     * stop processing queue
     */
    public void shutdown() {
        processorPool.shutdown();
    }

    @Override
    public void enqueueQueueItem(QueueItem<OfHeader> queueItem) {
        messageSpy.spyMessage(queueItem.getMessage(), STATISTIC_GROUP.FROM_SWITCH_ENQUEUED);
        TicketImpl<OfHeader, DataObject> ticket = new TicketImpl<>();
        ticket.setConductor(queueItem.getConnectionConductor());
        ticket.setMessage(queueItem.getMessage());
        ticket.setQueueType(queueItem.getQueueType());

        LOG.trace("ticket scheduling: {}, ticket: {}",
                queueItem.getMessage().getImplementedInterface().getSimpleName(),
                System.identityHashCode(queueItem));
        scheduleTicket(ticket);
    }


    @Override
    public void directProcessQueueItem(QueueItem<OfHeader> queueItem) {
        messageSpy.spyMessage(queueItem.getMessage(), STATISTIC_GROUP.FROM_SWITCH_ENQUEUED);
        TicketImpl<OfHeader, DataObject> ticket = new TicketImpl<>();
        ticket.setConductor(queueItem.getConnectionConductor());
        ticket.setMessage(queueItem.getMessage());

        LOG.debug("ticket scheduling: {}, ticket: {}",
                queueItem.getMessage().getImplementedInterface().getSimpleName(),
                System.identityHashCode(queueItem));

        ticketProcessorFactory.createProcessor(ticket).run();

        // publish notification
        finisher.firePopNotification(ticket.getDirectResult());
    }

    /**
     * @param ticket
     */
    private void scheduleTicket(Ticket<OfHeader, DataObject> ticket) {
        switch (ticket.getQueueType()) {
        case DEFAULT:
            Runnable ticketProcessor = ticketProcessorFactory.createProcessor(ticket);
            processorPool.execute(ticketProcessor);
            try {
                ticketQueue.put(ticket);
            } catch (InterruptedException e) {
                LOG.warn("enqeueue of unordered message ticket failed", e);
            }
            break;
        case UNORDERED:
            Runnable ticketProcessorSync = ticketProcessorFactory.createSyncProcessor(ticket);
            processorPool.execute(ticketProcessorSync);
            break;
        default:
            LOG.warn("unsupported enqueue type: {}", ticket.getQueueType());
        }
    }

    /**
     * @param poolSize the poolSize to set
     */
    public void setProcessingPoolSize(int poolSize) {
        this.processingPoolSize = poolSize;
    }

    @Override
    public void setTranslatorMapping(
            Map<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, List<DataObject>>>> translatorMapping) {
        this.translatorMapping = translatorMapping;
    }

    @Override
    public void setPopListenersMapping(
            Map<Class<? extends DataObject>, Collection<PopListener<DataObject>>> popListenersMapping) {
        this.popListenersMapping = popListenersMapping;
    }

    /**
     * @param messageSpy the messageSpy to set
     */
    public void setMessageSpy(MessageSpy<DataContainer> messageSpy) {
        this.messageSpy = messageSpy;
    }

    @Override
    public AutoCloseable registerMessageSource(QueueKeeper<OfHeader> queue) {
        boolean added = messageSources.add(queue);
        if (! added) {
            LOG.debug("registration of message source queue failed - already registered");
        }
        MessageSourcePollRegistration<QueueKeeper<OfHeader>> queuePollRegistration =
                new MessageSourcePollRegistration<>(this, queue);
        return queuePollRegistration;
    }

    @Override
    public boolean unregisterMessageSource(QueueKeeper<OfHeader> queue) {
        return messageSources.remove(queue);
    }

    @Override
    public Collection<QueueKeeper<OfHeader>> getMessageSources() {
        return messageSources;
    }

    @Override
    public HarvesterHandle getHarvesterHandle() {
        return harvester;
    }
}
