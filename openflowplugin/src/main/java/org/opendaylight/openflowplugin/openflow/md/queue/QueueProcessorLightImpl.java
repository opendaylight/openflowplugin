/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.queue;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.opendaylight.openflowplugin.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.ThreadPoolLoggingExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.openflow.md.queue.MessageSpy.STATISTIC_GROUP;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * {@link QueueKeeper} implementation focused to keep order and use up mutiple threads for translation phase.
 * <br/>
 * There is internal thread pool of limited size ({@link QueueProcessorLightImpl#setProcessingPoolSize(int)}) 
 * dedicated to translation. Then there is singleThreadPool dedicated to publishing (via popListeners)
 * <br/>
 * Workflow:
 * <ol>
 * <li>upon message push ticket is created and enqueued</li>
 * <li>available threads from internal pool translate the massage wrapped in ticket</li>
 * <li>when translation of particular message is finished, result is set in future result of wrapping ticket</br>
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
public class QueueProcessorLightImpl implements QueueProcessor<OfHeader, DataObject>, 
        MessageSourcePollRegistrator<QueueKeeper<OfHeader>>, Enqueuer<QueueItem<OfHeader>> {

    private static final Logger LOG = LoggerFactory
            .getLogger(QueueProcessorLightImpl.class);

    private BlockingQueue<TicketResult<DataObject>> ticketQueue;
    private ExecutorService processingPool;
    private int processingPoolSize = 10;
    private ExecutorService harvesterPool;
    private ExecutorService finisherPool;
    
    private Map<Class<? extends DataObject>, Collection<PopListener<DataObject>>> popListenersMapping;
    private Map<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, List<DataObject>>>> translatorMapping;
    private TicketProcessorFactory<OfHeader, DataObject> ticketProcessorFactory;
    private MessageSpy<DataContainer> messageSpy;
    protected Collection<QueueKeeper<OfHeader>> messageSources;
    private Object harvestLock;
    

    private VersionExtractor<OfHeader> versionExtractor = new VersionExtractor<OfHeader>() {
        @Override
        public Short extractVersion(OfHeader message) {
            return message.getVersion();
        }
    };

    private RegisteredTypeExtractor<OfHeader> registeredSrcTypeExtractor =
            new RegisteredTypeExtractor<OfHeader>() {
        @SuppressWarnings("unchecked")
        @Override
        public Class<? extends OfHeader> extractRegisteredType(
                OfHeader message) {
            return (Class<? extends OfHeader>) message.getImplementedInterface();
        }
    };

    private RegisteredTypeExtractor<DataObject> registeredOutTypeExtractor =
            new RegisteredTypeExtractor<DataObject>() {
        @SuppressWarnings("unchecked")
        @Override
        public Class<? extends DataObject> extractRegisteredType(
                DataObject message) {
            return (Class<? extends DataObject>) message.getImplementedInterface();
        }
    };

    /**
     * prepare queue
     */
    public void init() {
        ticketQueue = new ArrayBlockingQueue<>(1000);
        messageSources = new HashSet<>();
        harvestLock = new Object();
        
        processingPool = new ThreadPoolLoggingExecutor(processingPoolSize, processingPoolSize, 0, 
                TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(2*processingPoolSize));
        harvesterPool = new ThreadPoolLoggingExecutor(1, 1, 0, 
                TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1));
        finisherPool = new ThreadPoolLoggingExecutor(1, 1, 0, 
                TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1));
        
        harvesterPool.execute(new QueueKeeperHarvester<OfHeader>(this, messageSources, harvestLock));

        ticketProcessorFactory = new TicketProcessorFactory<>();
        ticketProcessorFactory.setRegisteredTypeExtractor(registeredSrcTypeExtractor);
        ticketProcessorFactory.setTranslatorMapping(translatorMapping);
        ticketProcessorFactory.setVersionExtractor(versionExtractor);
        ticketProcessorFactory.setSpy(messageSpy);
        
        TicketFinisher<DataObject> finisher = new TicketFinisher<>(
                ticketQueue, popListenersMapping, registeredOutTypeExtractor);
        finisherPool.execute(finisher);
    }

    /**
     * stop processing queue
     */
    public void shutdown() {
        processingPool.shutdown();
    }

    @Override
    public void enqueueQueueItem(QueueItem<OfHeader> queueItem) {
        messageSpy.spyMessage(queueItem.getMessage(), STATISTIC_GROUP.FROM_SWITCH_ENQUEUED);
        TicketImpl<OfHeader, DataObject> ticket = new TicketImpl<>();
        ticket.setConductor(queueItem.getConnectionConductor());
        ticket.setMessage(queueItem.getMessage());
        
        LOG.debug("ticket scheduling: {}, ticket: {}",
                queueItem.getMessage().getImplementedInterface().getSimpleName(), 
                System.identityHashCode(queueItem));
        try {
            ticketQueue.put(ticket);
            scheduleTicket(ticket);
        } catch (InterruptedException e) {
            LOG.warn("message enqueing interrupted", e);
        }
    }

    /**
     * @param ticket
     */
    private void scheduleTicket(Ticket<OfHeader, DataObject> ticket) {
        Runnable ticketProcessor = ticketProcessorFactory.createProcessor(ticket);
        processingPool.execute(ticketProcessor);
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
    public Object getHarvestLock() {
        return harvestLock;
    }
}
