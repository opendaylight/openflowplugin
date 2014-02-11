/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionConfiguration;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.openflow.md.OFConstants;
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
import org.opendaylight.openflowplugin.openflow.md.core.translator.PacketInTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.translator.PacketInV10Translator;
import org.opendaylight.openflowplugin.openflow.md.core.translator.PortStatusMessageToNodeConnectorUpdatedTranslator;
import org.opendaylight.openflowplugin.openflow.md.lldp.LLDPSpeakerPopListener;
import org.opendaylight.openflowplugin.openflow.md.queue.MessageSpy;
import org.opendaylight.openflowplugin.openflow.md.queue.PopListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.NodeErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.AggregateFlowStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupDescStatsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupFeaturesUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupStatisticsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterConfigStatsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterFeaturesUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterStatisticsUpdated;
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
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * @author mirehak
 *
 */
public class MDController implements IMDController, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(MDController.class);

    private SwitchConnectionProvider switchConnectionProvider;

    private ConcurrentMap<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, List<DataObject>>>> messageTranslators;
    private Map<Class<? extends DataObject>, Collection<PopListener<DataObject>>> popListeners;
    private MessageSpy<OfHeader, DataObject> messageSpyCounter; 

    final private int OF10 = OFConstants.OFP_VERSION_1_0;
    final private int OF13 = OFConstants.OFP_VERSION_1_3;


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
        addMessageTranslator(FlowRemovedMessage.class, OF10, new FlowRemovedTranslator());
        addMessageTranslator(FlowRemovedMessage.class, OF13, new FlowRemovedTranslator());
        addMessageTranslator(PacketInMessage.class,OF10, new PacketInV10Translator());
        addMessageTranslator(PacketInMessage.class,OF13, new PacketInTranslator());
        addMessageTranslator(PortStatusMessage.class,OF10, new PortStatusMessageToNodeConnectorUpdatedTranslator());
        addMessageTranslator(PortStatusMessage.class,OF13, new PortStatusMessageToNodeConnectorUpdatedTranslator());
        addMessageTranslator(MultipartReplyMessage.class,OF13,new MultiPartReplyPortToNodeConnectorUpdatedTranslator());
        addMessageTranslator(MultipartReplyMessage.class,OF10, new MultiPartMessageDescToNodeUpdatedTranslator());
        addMessageTranslator(MultipartReplyMessage.class,OF13, new MultiPartMessageDescToNodeUpdatedTranslator());
        addMessageTranslator(ExperimenterMessage.class, OF10, new ExperimenterTranslator());
        addMessageTranslator(MultipartReplyMessage.class,OF10, new MultipartReplyTranslator());
        addMessageTranslator(MultipartReplyMessage.class,OF13, new MultipartReplyTranslator());
        addMessageTranslator(MultipartReplyMessage.class,OF13,new MultipartReplyTableFeaturesToTableUpdatedTranslator());
        addMessageTranslator(GetFeaturesOutput.class,OF10, new FeaturesV10ToNodeConnectorUpdatedTranslator());

        //TODO: move registration to factory
        NotificationPopListener<DataObject> notificationPopListener = new NotificationPopListener<DataObject>();
        addMessagePopListener(NodeErrorNotification.class, notificationPopListener);
        addMessagePopListener(NodeConnectorUpdated.class,notificationPopListener);
        addMessagePopListener(PacketReceived.class,notificationPopListener);
        addMessagePopListener(TransmitPacketInput.class, notificationPopListener);
        addMessagePopListener(NodeUpdated.class, notificationPopListener);

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

        //Notification for LLDPSpeaker
        LLDPSpeakerPopListener<DataObject> lldpPopListener  = new LLDPSpeakerPopListener<DataObject>();
        addMessagePopListener(NodeConnectorUpdated.class,lldpPopListener);
        
        // Push the updated Listeners to Session Manager which will be then picked up by ConnectionConductor eventually
        OFSessionUtil.getSessionManager().setTranslatorMapping(messageTranslators);
        OFSessionUtil.getSessionManager().setPopListenerMapping(popListeners);
    }

    /**
     * @param switchConnectionProvider
     *            the switchConnectionProvider to set
     */
    public void setSwitchConnectionProvider(SwitchConnectionProvider switchConnectionProvider) {
        this.switchConnectionProvider = switchConnectionProvider;
    }

    /**
     * @param switchConnectionProviderToUnset
     *            the switchConnectionProvider to unset
     */
    public void unsetSwitchConnectionProvider(SwitchConnectionProvider switchConnectionProviderToUnset) {
        if (this.switchConnectionProvider == switchConnectionProviderToUnset) {
            this.switchConnectionProvider = null;
        }
    }

    /**
     * Function called by dependency manager after "init ()" is called and after
     * the services provided by the class are registered in the service registry
     *
     */
    public void start() {
        LOG.debug("starting ..");
        LOG.debug("switchConnectionProvider: " + switchConnectionProvider);
        // setup handler
        SwitchConnectionHandlerImpl switchConnectionHandler = new SwitchConnectionHandlerImpl();
        switchConnectionHandler.setMessageSpy(messageSpyCounter);

        ErrorHandlerQueueImpl errorHandler = new ErrorHandlerQueueImpl();
        new Thread(errorHandler).start();
        
        switchConnectionHandler.setErrorHandler(errorHandler);
        switchConnectionHandler.init();
        
        switchConnectionProvider.setSwitchConnectionHandler(switchConnectionHandler);

        // configure and startup library servers
        switchConnectionProvider.configure(getConnectionConfiguration());
        Future<List<Boolean>> srvStarted = switchConnectionProvider.startup();
    }

    /**
     * @return wished connections configurations
     */
    private static Collection<ConnectionConfiguration> getConnectionConfiguration() {
        // TODO:: get config from state manager
        ConnectionConfiguration configuration = ConnectionConfigurationFactory.getDefault();
        ConnectionConfiguration configurationLegacy = ConnectionConfigurationFactory.getLegacy();
        return Lists.newArrayList(configuration, configurationLegacy);
    }

    /**
     * Function called by the dependency manager before the services exported by
     * the component are unregistered, this will be followed by a "destroy ()"
     * calls
     *
     */
    public void stop() {
        LOG.debug("stopping");
        Future<List<Boolean>> srvStopped = switchConnectionProvider.shutdown();
        try {
            srvStopped.get(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOG.error(e.getMessage(), e);
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
    public void addMessageTranslator(Class<? extends DataObject> messageType, int version, IMDMessageTranslator<OfHeader, List<DataObject>> translator) {
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
    public void removeMessageTranslator(Class<? extends DataObject> messageType, int version, IMDMessageTranslator<OfHeader, List<DataObject>> translator) {
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
    public void addMessagePopListener(Class<? extends DataObject> messageType, PopListener<DataObject> popListener) {
        Collection<PopListener<DataObject>> existingValues = popListeners.get(messageType);
        if (existingValues == null) {
            existingValues = new LinkedHashSet<>();
            popListeners.put(messageType, existingValues);
        }
        existingValues.add(popListener);
        LOG.debug("{} is now popListened by {}", messageType, popListener);
    }

    @Override
    public void removeMessagePopListener(Class<? extends DataObject> messageType, PopListener<DataObject> popListener) {
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
            MessageSpy<OfHeader, DataObject> messageSpyCounter) {
        this.messageSpyCounter = messageSpyCounter;
    }
    
    @Override
    public void close() {
        LOG.debug("close");
        messageSpyCounter = null;
        messageTranslators = null;
        popListeners = null;
        switchConnectionProvider.setSwitchConnectionHandler(null);
        switchConnectionProvider = null;
        OFSessionUtil.releaseSessionManager();
    }
}
