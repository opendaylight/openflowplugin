/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core;

import com.google.common.util.concurrent.ForwardingBlockingQueue;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.md.core.NotificationQueueWrapper;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.api.openflow.md.queue.PopListener;
import org.opendaylight.openflowplugin.api.openflow.statistics.MessageSpy;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.translator.ErrorTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.translator.ErrorV10Translator;
import org.opendaylight.openflowplugin.openflow.md.core.translator.ExperimenterTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.translator.FeaturesV10ToNodeConnectorUpdatedTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.translator.FlowRemovedTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.translator.MultiPartMessageDescToNodeUpdatedTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.translator.MultiPartReplyPortToNodeConnectorUpdatedTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.translator.MultipartReplyTableFeaturesToTableUpdatedTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.translator.MultipartReplyTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.translator.NotificationPlainTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.translator.PacketInTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.translator.PacketInV10Translator;
import org.opendaylight.openflowplugin.openflow.md.core.translator.PortStatusMessageToNodeConnectorUpdatedTranslator;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.NodeErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.AggregateFlowStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupDescStatsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupFeaturesUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupStatisticsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterConfigStatsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterFeaturesUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterStatisticsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.BadActionErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.BadInstructionErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.BadMatchErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.BadRequestErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.ExperimenterErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.FlowModErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.GroupModErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.HelloFailedErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.MeterModErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.PortModErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.QueueOpErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.RoleRequestErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.SwitchConfigErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.TableFeaturesErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.TableModErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.NodeConnectorStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.QueueStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.TableUpdated;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class MDController implements IMDController, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(MDController.class);
    private final ConvertorExecutor convertorExecutor;

    private Collection<SwitchConnectionProvider> switchConnectionProviders;

    private ConcurrentMap<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, List<DataObject>>>> messageTranslators;
    private Map<Class<? extends DataObject>, Collection<PopListener<DataObject>>> popListeners;
    private MessageSpy<DataContainer> messageSpyCounter;

    final private int OF10 = OFConstants.OFP_VERSION_1_0;
    final private int OF13 = OFConstants.OFP_VERSION_1_3;

    private ErrorHandlerSimpleImpl errorHandler;

    private ExtensionConverterProvider extensionConverterProvider;

    public MDController(ConvertorExecutor convertorExecutor) {
        this.convertorExecutor = convertorExecutor;
    }

    /**
     * @return translator mapping
     */
    public Map<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, List<DataObject>>>> getMessageTranslators() {
        return messageTranslators;
    }

    /**
     * provisioning of translator mapping
     */
    public void init() {
        LOG.debug("init");

        messageTranslators = new ConcurrentHashMap<>();
        popListeners = new ConcurrentHashMap<>();
        //TODO: move registration to factory
        addMessageTranslator(ErrorMessage.class, OF10, new ErrorV10Translator());
        addMessageTranslator(ErrorMessage.class, OF13, new ErrorTranslator());
        addMessageTranslator(FlowRemovedMessage.class, OF10, new FlowRemovedTranslator(convertorExecutor));
        addMessageTranslator(FlowRemovedMessage.class, OF13, new FlowRemovedTranslator(convertorExecutor));
        addMessageTranslator(PacketInMessage.class,OF10, new PacketInV10Translator());
        addMessageTranslator(PacketInMessage.class,OF13, new PacketInTranslator(convertorExecutor));
        addMessageTranslator(PortStatusMessage.class,OF10, new PortStatusMessageToNodeConnectorUpdatedTranslator());
        addMessageTranslator(PortStatusMessage.class,OF13, new PortStatusMessageToNodeConnectorUpdatedTranslator());
        addMessageTranslator(MultipartReplyMessage.class,OF13,new MultiPartReplyPortToNodeConnectorUpdatedTranslator());
        addMessageTranslator(MultipartReplyMessage.class,OF10, new MultiPartMessageDescToNodeUpdatedTranslator());
        addMessageTranslator(MultipartReplyMessage.class,OF13, new MultiPartMessageDescToNodeUpdatedTranslator());
        addMessageTranslator(ExperimenterMessage.class, OF10, new ExperimenterTranslator());
        addMessageTranslator(MultipartReplyMessage.class,OF10, new MultipartReplyTranslator(convertorExecutor));
        addMessageTranslator(MultipartReplyMessage.class,OF13, new MultipartReplyTranslator(convertorExecutor));
        addMessageTranslator(MultipartReplyMessage.class,OF13,new MultipartReplyTableFeaturesToTableUpdatedTranslator(convertorExecutor));
        addMessageTranslator(GetFeaturesOutput.class,OF10, new FeaturesV10ToNodeConnectorUpdatedTranslator());
        addMessageTranslator(NotificationQueueWrapper.class, OF10, new NotificationPlainTranslator());
        addMessageTranslator(NotificationQueueWrapper.class, OF13, new NotificationPlainTranslator());

        NotificationPopListener<DataObject> notificationPopListener = new NotificationPopListener<DataObject>();
        notificationPopListener.setNotificationProviderService(
                OFSessionUtil.getSessionManager().getNotificationProviderService());
        notificationPopListener.setMessageSpy(messageSpyCounter);

        //TODO: move registration to factory
        addMessagePopListener(NodeErrorNotification.class, notificationPopListener);
        addMessagePopListener(BadActionErrorNotification.class, notificationPopListener);
        addMessagePopListener(BadInstructionErrorNotification.class, notificationPopListener);
        addMessagePopListener(BadMatchErrorNotification.class, notificationPopListener);
        addMessagePopListener(BadRequestErrorNotification.class, notificationPopListener);
        addMessagePopListener(ExperimenterErrorNotification.class, notificationPopListener);
        addMessagePopListener(FlowModErrorNotification.class, notificationPopListener);
        addMessagePopListener(GroupModErrorNotification.class, notificationPopListener);
        addMessagePopListener(HelloFailedErrorNotification.class, notificationPopListener);
        addMessagePopListener(MeterModErrorNotification.class, notificationPopListener);
        addMessagePopListener(PortModErrorNotification.class, notificationPopListener);
        addMessagePopListener(QueueOpErrorNotification.class, notificationPopListener);
        addMessagePopListener(RoleRequestErrorNotification.class, notificationPopListener);
        addMessagePopListener(SwitchConfigErrorNotification.class, notificationPopListener);
        addMessagePopListener(TableFeaturesErrorNotification.class, notificationPopListener);
        addMessagePopListener(TableModErrorNotification.class, notificationPopListener);
        addMessagePopListener(NodeConnectorUpdated.class,notificationPopListener);
        addMessagePopListener(NodeConnectorRemoved.class,notificationPopListener);
        addMessagePopListener(PacketReceived.class,notificationPopListener);
        addMessagePopListener(TransmitPacketInput.class, notificationPopListener);
        addMessagePopListener(NodeUpdated.class, notificationPopListener);
        addMessagePopListener(NodeRemoved.class, notificationPopListener);

        addMessagePopListener(SwitchFlowRemoved.class, notificationPopListener);
        addMessagePopListener(TableUpdated.class, notificationPopListener);

        //Notification registration for flow statistics
        addMessagePopListener(FlowsStatisticsUpdate.class, notificationPopListener);
        addMessagePopListener(AggregateFlowStatisticsUpdate.class, notificationPopListener);

        //Notification registrations for group-statistics
        addMessagePopListener(GroupStatisticsUpdated.class, notificationPopListener);
        addMessagePopListener(GroupFeaturesUpdated.class, notificationPopListener);
        addMessagePopListener(GroupDescStatsUpdated.class, notificationPopListener);

        //Notification registrations for meter-statistics
        addMessagePopListener(MeterStatisticsUpdated.class, notificationPopListener);
        addMessagePopListener(MeterConfigStatsUpdated.class, notificationPopListener);
        addMessagePopListener(MeterFeaturesUpdated.class, notificationPopListener);

        //Notification registration for port-statistics
        addMessagePopListener(NodeConnectorStatisticsUpdate.class, notificationPopListener);

        //Notification registration for flow-table statistics
        addMessagePopListener(FlowTableStatisticsUpdate.class, notificationPopListener);

        //Notification registration for queue-statistics
        addMessagePopListener(QueueStatisticsUpdate.class, notificationPopListener);

        // Push the updated Listeners to Session Manager which will be then picked up by ConnectionConductor eventually
        OFSessionUtil.getSessionManager().setTranslatorMapping(messageTranslators);
        OFSessionUtil.getSessionManager().setPopListenerMapping(popListeners);
        OFSessionUtil.getSessionManager().setMessageSpy(messageSpyCounter);

        // prepare worker pool for rpc
        // TODO: get size from configSubsystem
        int rpcThreadLimit = 10;
        ListeningExecutorService rpcPoolDelegator = createRpcPoolSpyDecorated(rpcThreadLimit, messageSpyCounter);
        OFSessionUtil.getSessionManager().setRpcPool(rpcPoolDelegator);
        OFSessionUtil.getSessionManager().setExtensionConverterProvider(extensionConverterProvider);

    }

    /**
     * @param rpcThreadLimit
     * @param messageSpy
     * @return
     */
    private static ListeningExecutorService createRpcPoolSpyDecorated(final int rpcThreadLimit, final MessageSpy<DataContainer> messageSpy) {
        final BlockingQueue<Runnable> delegate = new LinkedBlockingQueue<>(100000);
        final BlockingQueue<Runnable> queue = new ForwardingBlockingQueue<Runnable>() {
            @Override
            protected BlockingQueue<Runnable> delegate() {
                return delegate;
            }

            @Override
            public boolean offer(final Runnable r) {
                // ThreadPoolExecutor will spawn a new thread after core size is reached only
                // if the queue.offer returns false.
                return false;
            }
        };

        ThreadPoolLoggingExecutor rpcPool = new ThreadPoolLoggingExecutor(rpcThreadLimit, rpcThreadLimit, 0L,
                TimeUnit.MILLISECONDS, queue, "OFRpc");
        rpcPool.setRejectedExecutionHandler(new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(final Runnable r, final ThreadPoolExecutor executor) {
				try {
					executor.getQueue().put(r);
				} catch (InterruptedException e) {
					throw new RejectedExecutionException("Interrupted while waiting on queue", e);
				}

			}
		});
        ListeningExecutorService listeningRpcPool = MoreExecutors.listeningDecorator(rpcPool);
        RpcListeningExecutorService rpcPoolDecorated = new RpcListeningExecutorService(listeningRpcPool);
        rpcPoolDecorated.setMessageSpy(messageSpy);
        return rpcPoolDecorated;
    }

    /**
     * @param switchConnectionProviders
     *            the switchConnectionProviders to set
     */
    public void setSwitchConnectionProviders(final Collection<SwitchConnectionProvider> switchConnectionProviders) {
        this.switchConnectionProviders = switchConnectionProviders;
    }

    /**
     * Function called by dependency manager after "init ()" is called and after
     * the services provided by the class are registered in the service registry
     *
     */
    public void start() {
        LOG.debug("starting ..");
        LOG.debug("switchConnectionProvider: " + switchConnectionProviders);
        // setup handler
        SwitchConnectionHandlerImpl switchConnectionHandler = new SwitchConnectionHandlerImpl();
        switchConnectionHandler.setMessageSpy(messageSpyCounter);

        errorHandler = new ErrorHandlerSimpleImpl();

        switchConnectionHandler.setErrorHandler(errorHandler);
        switchConnectionHandler.init();

        List<ListenableFuture<Boolean>> starterChain = new ArrayList<>(switchConnectionProviders.size());
        for (SwitchConnectionProvider switchConnectionPrv : switchConnectionProviders) {
            switchConnectionPrv.setSwitchConnectionHandler(switchConnectionHandler);
            ListenableFuture<Boolean> isOnlineFuture = switchConnectionPrv.startup();
            starterChain.add(isOnlineFuture);
        }

        Future<List<Boolean>> srvStarted = Futures.allAsList(starterChain);
    }

    /**
     * Function called by the dependency manager before the services exported by
     * the component are unregistered, this will be followed by a "destroy ()"
     * calls
     *
     */
    public void stop() {
        LOG.debug("stopping");
        List<ListenableFuture<Boolean>> stopChain = new ArrayList<>(switchConnectionProviders.size());
        try {
            for (SwitchConnectionProvider switchConnectionPrv : switchConnectionProviders) {
                ListenableFuture<Boolean> shutdown =  switchConnectionPrv.shutdown();
                stopChain.add(shutdown);
            }
            Futures.allAsList(stopChain).get(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOG.warn("failed to stop MDController: {}", e.getMessage());
            LOG.debug("failed to stop MDController.. ", e);
        }
        close();
    }

    /**
     * Function called by the dependency manager when at least one dependency
     * become unsatisfied or when the component is shutting down because for
     * example bundle is being stopped.
     *
     */
    public void destroy() {
        close();
    }

    @Override
    public void addMessageTranslator(final Class<? extends DataObject> messageType, final int version, final IMDMessageTranslator<OfHeader, List<DataObject>> translator) {
        TranslatorKey tKey = new TranslatorKey(version, messageType.getName());

        Collection<IMDMessageTranslator<OfHeader, List<DataObject>>> existingValues = messageTranslators.get(tKey);
        if (existingValues == null) {
            existingValues = new LinkedHashSet<>();
            messageTranslators.put(tKey, existingValues);
        }
        existingValues.add(translator);
        LOG.debug("{} is now translated by {}", messageType, translator);
    }

    @Override
    public void removeMessageTranslator(final Class<? extends DataObject> messageType, final int version, final IMDMessageTranslator<OfHeader, List<DataObject>> translator) {
        TranslatorKey tKey = new TranslatorKey(version, messageType.getName());
        Collection<IMDMessageTranslator<OfHeader, List<DataObject>>> values = messageTranslators.get(tKey);
        if (values != null) {
            values.remove(translator);
            if (values.isEmpty()) {
                messageTranslators.remove(tKey);
            }
            LOG.debug("{} is now removed from translators", translator);
         }
    }

    @Override
    public void addMessagePopListener(final Class<? extends DataObject> messageType, final PopListener<DataObject> popListener) {
        Collection<PopListener<DataObject>> existingValues = popListeners.get(messageType);
        if (existingValues == null) {
            existingValues = new LinkedHashSet<>();
            popListeners.put(messageType, existingValues);
        }
        existingValues.add(popListener);
        LOG.debug("{} is now popListened by {}", messageType, popListener);
    }

    @Override
    public void removeMessagePopListener(final Class<? extends DataObject> messageType, final PopListener<DataObject> popListener) {
        Collection<PopListener<DataObject>> values = popListeners.get(messageType);
        if (values != null) {
            values.remove(popListener);
            if (values.isEmpty()) {
                popListeners.remove(messageType);
            }
            LOG.debug("{} is now removed from popListeners", popListener);
         }
    }

    /**
     * @param messageSpyCounter the messageSpyCounter to set
     */
    public void setMessageSpyCounter(
            final MessageSpy<DataContainer> messageSpyCounter) {
        this.messageSpyCounter = messageSpyCounter;
    }

    @Override
    public void close() {
        LOG.debug("close");
        messageSpyCounter = null;
        messageTranslators = null;
        popListeners = null;
        for (SwitchConnectionProvider switchConnectionPrv : switchConnectionProviders) {
            switchConnectionPrv.setSwitchConnectionHandler(null);
        }
        switchConnectionProviders = null;
        OFSessionUtil.releaseSessionManager();
        errorHandler = null;
    }

    /**
     * @param extensionConverterProvider extension convertor provider
     */
    public void setExtensionConverterProvider(ExtensionConverterProvider extensionConverterProvider) {
        this.extensionConverterProvider = extensionConverterProvider;
    }
}
