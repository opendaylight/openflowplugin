/**
 * Copyright (c) 2013, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.role;

import java.math.BigInteger;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.opendaylight.controller.md.sal.common.api.clustering.CandidateAlreadyRegisteredException;
import com.google.common.base.Optional;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncTransaction;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChain;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipState;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipCandidateRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.openflow.md.ModelDrivenSwitch;
import org.opendaylight.openflowplugin.api.openflow.md.ModelDrivenSwitchRegistration;
import org.opendaylight.openflowplugin.api.openflow.md.core.NotificationQueueWrapper;
import org.opendaylight.openflowplugin.openflow.md.core.sal.OpenflowPluginConfig;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipChange;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.common.config.impl.rev140326.OfpRole;
import org.opendaylight.openflowplugin.openflow.md.core.session.RolePushTask;
import org.opendaylight.openflowplugin.openflow.md.core.session.RolePushException;
import org.opendaylight.openflowplugin.openflow.md.util.RoleUtil;
import org.opendaylight.openflowplugin.openflow.md.core.ThreadPoolLoggingExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentHashMap;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.CheckedFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OfEntityManager implements TransactionChainListener{
    private static final Logger LOG = LoggerFactory.getLogger(OfEntityManager.class);

    private static final QName ENTITY_QNAME =
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.core.general.entity.rev150820.Entity.QNAME;
    private static final QName ENTITY_NAME = QName.create(ENTITY_QNAME, "name");

    private DataBroker dataBroker;
    private EntityOwnershipService entityOwnershipService;
    private final OpenflowOwnershipListener ownershipListener;
    private final AtomicBoolean registeredListener = new AtomicBoolean();
    private ConcurrentHashMap<Entity, MDSwitchMetaData> entsession;
    private ConcurrentHashMap<Entity, EntityOwnershipCandidateRegistration> entRegistrationMap;
    private final String DEVICE_TYPE = "openflow";

    private final ListeningExecutorService pool;

    private final OpenflowPluginConfig openflowPluginConfig;

    public OfEntityManager(EntityOwnershipService entityOwnershipService, OpenflowPluginConfig ofPluginConfig) {
        this.entityOwnershipService = entityOwnershipService;
        openflowPluginConfig = ofPluginConfig;
        ownershipListener = new OpenflowOwnershipListener(this);
        entsession = new ConcurrentHashMap<>();
        entRegistrationMap = new ConcurrentHashMap<>();
        ThreadPoolLoggingExecutor delegate = new ThreadPoolLoggingExecutor(
            20, 20, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), "ofEntity");
        pool =  MoreExecutors.listeningDecorator(delegate);
    }

    public void setDataBroker(DataBroker dbBroker) {
        this.dataBroker = dbBroker;
    }

    public void init(){
        registerEntityOwnershipChangeListener();
    }

    public void registerEntityOwnershipChangeListener() {
        if(entityOwnershipService!=null) {
            if(LOG.isDebugEnabled()) {
                LOG.debug("registerEntityOwnershipChangeListener: Registering entity ownership change listener for entitier of type {}", DEVICE_TYPE);
            }
        entityOwnershipService.registerListener(DEVICE_TYPE, ownershipListener);
        }
    }

    public void requestOpenflowEntityOwnership(final ModelDrivenSwitch ofSwitch,
                                               final SessionContext context,
                                               final NotificationQueueWrapper wrappedNotification,
                                               final RpcProviderRegistry rpcProviderRegistry) {
        MDSwitchMetaData entityMetaData =
                new MDSwitchMetaData(ofSwitch,context,wrappedNotification,rpcProviderRegistry);

        final Entity entity = new Entity(DEVICE_TYPE, ofSwitch.getNodeId().getValue());
        entsession.put(entity, entityMetaData);

        //Register as soon as possible to avoid missing any entity ownership change event
        final EntityOwnershipCandidateRegistration entityRegistration;
        try {
            entityRegistration = entityOwnershipService.registerCandidate(entity);
            entRegistrationMap.put(entity, entityRegistration);
            LOG.info("requestOpenflowEntityOwnership: Registered controller for the ownership of {}", ofSwitch.getNodeId() );
        } catch (CandidateAlreadyRegisteredException e) {
            // we can log and move for this error, as listener is present and role changes will be served.
            LOG.error("requestOpenflowEntityOwnership : Controller registration for ownership of {} failed ", ofSwitch.getNodeId(), e );
        }

        Optional <EntityOwnershipState> entityOwnershipStateOptional =
                entityOwnershipService.getOwnershipState(entity);

        if (entityOwnershipStateOptional.isPresent()) {
            final EntityOwnershipState entityOwnershipState = entityOwnershipStateOptional.get();
            if (entityOwnershipState.hasOwner()) {
                final OfpRole newRole ;
                if (entityOwnershipState.isOwner()) {
                    LOG.info("requestOpenflowEntityOwnership: Set controller as a MASTER controller " +
                            "because it's the OWNER of the {}", ofSwitch.getNodeId());
                    newRole =  OfpRole.BECOMEMASTER;
                    setDeviceOwnershipState(entity,true);
                    registerRoutedRPCForSwitch(entsession.get(entity));
                } else {
                    LOG.info("requestOpenflowEntityOwnership: Set controller as a SLAVE controller " +
                            "because it's is not the owner of the {}", ofSwitch.getNodeId());
                    newRole = OfpRole.BECOMESLAVE;
                    setDeviceOwnershipState(entity,false);
                }
                RolePushTask task = new RolePushTask(newRole, context);
                ListenableFuture<Boolean> rolePushResult = pool.submit(task);
                CheckedFuture<Boolean, RolePushException> rolePushResultChecked =
                    RoleUtil.makeCheckedRuleRequestFxResult(rolePushResult);
                Futures.addCallback(rolePushResult, new FutureCallback<Boolean>(){
                    @Override
                    public void onSuccess(Boolean result){
                        LOG.info("requestOpenflowEntityOwnership: Controller is now {} of the {}",
                                newRole == OfpRole.BECOMEMASTER?"MASTER":"SLAVE",ofSwitch.getNodeId() );

                        sendNodeAddedNotification(entsession.get(entity));
                    }
                    @Override
                    public void onFailure(Throwable t){
                        LOG.warn("requestOpenflowEntityOwnership: Controller is not able to set " +
                                "the role for {}",ofSwitch.getNodeId(), t);

                        if(newRole == OfpRole.BECOMEMASTER) {
                            LOG.info("requestOpenflowEntityOwnership: ..and controller is the owner of the " +
                                    "device {}. Closing the registration, so other controllers can try to " +
                                    "become owner and attempt to be master controller.",ofSwitch.getNodeId());

                            EntityOwnershipCandidateRegistration ownershipRegistrent = entRegistrationMap.get(entity);
                            if (ownershipRegistrent != null) {
                                ownershipRegistrent.close();
                                entRegistrationMap.remove(entity);
                            }

                            LOG.info("requestOpenflowEntityOwnership: ..and registering it back to participate" +
                                    " in ownership of the entity.");

                            EntityOwnershipCandidateRegistration entityRegistration;
                            try {
                                entityRegistration = entityOwnershipService.registerCandidate(entity);
                                entRegistrationMap.put(entity, entityRegistration);
                                LOG.info("requestOpenflowEntityOwnership: re-registered controller for " +
                                        "ownership of the {}", ofSwitch.getNodeId() );
                            } catch (CandidateAlreadyRegisteredException e) {
                                // we can log and move for this error, as listener is present and role changes will be served.
                                LOG.error("requestOpenflowEntityOwnership: *Surprisingly* Entity is already " +
                                        "registered with EntityOwnershipService : {}", ofSwitch.getNodeId(), e );
                            }

                        } else {
                                LOG.error("requestOpenflowEntityOwnership : Not able to set role {} for {}"
                                        , newRole == OfpRole.BECOMEMASTER?"MASTER":"SLAVE", ofSwitch.getNodeId());
                        }
                    }
                 });
             }
         }
    }

    public void setSlaveRole(SessionContext sessionContext) {
        OfpRole newRole = OfpRole.BECOMESLAVE;
        if (sessionContext != null) {
            final BigInteger targetSwitchDPId = sessionContext.getFeatures().getDatapathId();
            LOG.debug("setSlaveRole: Set controller as a SLAVE controller for {}", targetSwitchDPId.toString());

            RolePushTask task = new RolePushTask(newRole, sessionContext);
            ListenableFuture<Boolean> rolePushResult = pool.submit(task);
            final CheckedFuture<Boolean, RolePushException> rolePushResultChecked =
                RoleUtil.makeCheckedRuleRequestFxResult(rolePushResult);
            Futures.addCallback(rolePushResult, new FutureCallback<Boolean>(){
                @Override
                public void onSuccess(Boolean result){
                    LOG.debug("setSlaveRole: Controller is set as a SLAVE for {}", targetSwitchDPId.toString());
                }
                @Override
                public void onFailure(Throwable e){
                    LOG.error("setSlaveRole: Role request to set controller as a SLAVE failed for {}",
                            targetSwitchDPId.toString(), e);
                }
            });
        } else {
            LOG.warn("setSlaveRole: sessionContext is not set. Device is not connected anymore");
        }
    }

    public void onDeviceOwnershipChanged(final EntityOwnershipChange ownershipChange) {
        final OfpRole newRole;
        final Entity entity = ownershipChange.getEntity();
        SessionContext sessionContext = entsession.get(entity)!=null?entsession.get(entity).getContext():null;
        if (!ownershipChange.inJeopardy()) {
            if (ownershipChange.isOwner()) {
                LOG.info("onDeviceOwnershipChanged: Set controller as a MASTER controller because " +
                        "it's the OWNER of the {}", entity);
                newRole = OfpRole.BECOMEMASTER;
            } else {
                newRole = OfpRole.BECOMESLAVE;
                if (sessionContext != null && ownershipChange.hasOwner()) {
                    LOG.info("onDeviceOwnershipChanged: Set controller as a SLAVE controller because " +
                            "it's not the OWNER of the {}", entity);

                    if (ownershipChange.wasOwner()) {
                        setDeviceOwnershipState(entity, false);
                        deregisterRoutedRPCForSwitch(entsession.get(entity));
                        // You don't have to explicitly set role to Slave in this case,
                        // because other controller will be taking over the master role
                        // and that will force other controller to become slave.
                    } else {
                        boolean isOwnershipInitialized = entsession.get(entity).getIsOwnershipInitialized();
                        setDeviceOwnershipState(entity, false);
                        if (!isOwnershipInitialized) {
                            setSlaveRole(sessionContext);
                            sendNodeAddedNotification(entsession.get(entity));
                        }
                    }
                }
                return;
            }
        } else {
            LOG.error("onDeviceOwnershipChanged: inJeopardy{}", ownershipChange.inJeopardy());
            //if i am the owner at present , i have lost quorum
            //thus transitioning to slave
             //if i am not the owner , election will be triggered
            if(entsession.get(entity).getOfSwitch().isEntityOwner()){
                newRole = OfpRole.BECOMESLAVE;
                setSlaveRole(sessionContext);
                setDeviceOwnershipState(entity, false);
                deregisterRoutedRPCForSwitch(entsession.get(entity));
            }else{
                LOG.error(" owner of the switch {}",entsession.get(entity).getOfSwitch().isEntityOwner());
            }
          return;
        }

        if (sessionContext != null) {
            //Register the RPC, given *this* controller instance is going to be master owner.
            //If role registration fails for this node, it will deregister as a candidate for
            //ownership and that will make this controller non-owner and it will deregister the
            // router rpc.
            setDeviceOwnershipState(entity,newRole==OfpRole.BECOMEMASTER);
            registerRoutedRPCForSwitch(entsession.get(entity));

            final String targetSwitchDPId = sessionContext.getFeatures().getDatapathId().toString();
            RolePushTask task = new RolePushTask(newRole, sessionContext);
            ListenableFuture<Boolean> rolePushResult = pool.submit(task);

            final CheckedFuture<Boolean, RolePushException> rolePushResultChecked =
                RoleUtil.makeCheckedRuleRequestFxResult(rolePushResult);
            Futures.addCallback(rolePushResult, new FutureCallback<Boolean>(){
                @Override
                public void onSuccess(Boolean result){
                    LOG.info("onDeviceOwnershipChanged: Controller is successfully set as a " +
                            "MASTER controller for {}", targetSwitchDPId);
                    if(!openflowPluginConfig.skipTableFeatures()) {
                        if(LOG.isDebugEnabled()){
                            LOG.debug("Send table feature request for entity {}",entity.getId());
                        }
                        entsession.get(entity).getOfSwitch().sendEmptyTableFeatureRequest();
                    }
                    sendNodeAddedNotification(entsession.get(entity));

                }
                @Override
                public void onFailure(Throwable e){

                    LOG.warn("onDeviceOwnershipChanged: Controller is not able to set the " +
                            "MASTER role for {}.", targetSwitchDPId,e);
                    if(newRole == OfpRole.BECOMEMASTER) {
                        LOG.info("onDeviceOwnershipChanged: ..and this *instance* is owner of the device {}. " +
                                "Closing the registration, so other entity can become owner " +
                                "and attempt to be master controller.",targetSwitchDPId);

                        EntityOwnershipCandidateRegistration ownershipRegistrent = entRegistrationMap.get(entity);
                        if (ownershipRegistrent != null) {
                            setDeviceOwnershipState(entity,false);
                            ownershipRegistrent.close();
                            MDSwitchMetaData switchMetadata = entsession.get(entity);
                            if(switchMetadata != null){
                                switchMetadata.setIsOwnershipInitialized(false);
                                //We can probably leave deregistration till the node ownerhsip change.
                                //But that can probably cause some race condition.
                                deregisterRoutedRPCForSwitch(switchMetadata);
                            }
                        }

                        LOG.info("onDeviceOwnershipChanged: ..and registering it back to participate in " +
                                "ownership and re-try");

                        EntityOwnershipCandidateRegistration entityRegistration;
                        try {
                            entityRegistration = entityOwnershipService.registerCandidate(entity);
                            entRegistrationMap.put(entity, entityRegistration);
                            LOG.info("onDeviceOwnershipChanged: re-registered candidate for " +
                                    "ownership of the {}", targetSwitchDPId );
                        } catch (CandidateAlreadyRegisteredException ex) {
                            // we can log and move for this error, as listener is present and role changes will be served.
                            LOG.error("onDeviceOwnershipChanged: *Surprisingly* Entity is already " +
                                    "registered with EntityOwnershipService : {}", targetSwitchDPId, ex );
                        }

                    } else {
                        LOG.error("onDeviceOwnershipChanged : Not able to set role {} for " +
                                " {}", newRole == OfpRole.BECOMEMASTER?"MASTER":"SLAVE", targetSwitchDPId);
                    }
                }
            });
        } else {
            LOG.warn("onDeviceOwnershipChanged: sessionContext is not available. Releasing ownership of the device");
            EntityOwnershipCandidateRegistration ownershipRegistrant = entRegistrationMap.get(entity);
            if (ownershipRegistrant != null) {
                ownershipRegistrant.close();
            }
        }
    }

    public void unregisterEntityOwnershipRequest(NodeId nodeId) {
        Entity entity = new Entity(DEVICE_TYPE, nodeId.getValue());
        entsession.remove(entity);
        EntityOwnershipCandidateRegistration entRegCandidate = entRegistrationMap.get(entity);
        if(entRegCandidate != null){
            LOG.info("unregisterEntityOwnershipRequest: Unregister controller entity ownership " +
                    "request for {}", nodeId);
            entRegCandidate.close();
            entRegistrationMap.remove(entity);
        }
    }

    @Override
    public void onTransactionChainFailed(final TransactionChain<?, ?> chain, final AsyncTransaction<?, ?> transaction,
           final Throwable cause) {
    }

    @Override
    public void onTransactionChainSuccessful(final TransactionChain<?, ?> chain) {
       // NOOP
    }

    private static void registerRoutedRPCForSwitch(MDSwitchMetaData entityMetadata) {
        // Routed RPC registration is only done when *this* instance is owner of
        // the entity.
        if(entityMetadata.getOfSwitch().isEntityOwner()) {
            if (!entityMetadata.isRPCRegistrationDone.get()) {
                entityMetadata.setIsRPCRegistrationDone(true);
                ModelDrivenSwitchRegistration registration =
                        entityMetadata.getOfSwitch().register(entityMetadata.getRpcProviderRegistry());

                entityMetadata.getContext().setProviderRegistration(registration);

                LOG.info("registerRoutedRPCForSwitch: Registered routed rpc for ModelDrivenSwitch {}",
                        entityMetadata.getOfSwitch().getNodeId().getValue());
            }
        } else {
            LOG.info("registerRoutedRPCForSwitch: Skipping routed rpc registration for ModelDrivenSwitch {}",
                    entityMetadata.getOfSwitch().getNodeId().getValue());
        }
    }

    private static void deregisterRoutedRPCForSwitch(MDSwitchMetaData entityMetadata) {

        ModelDrivenSwitchRegistration registration = entityMetadata.getContext().getProviderRegistration();
        if (null != registration) {
            registration.close();
            entityMetadata.getContext().setProviderRegistration(null);
            entityMetadata.setIsRPCRegistrationDone(false);
        }
        LOG.info("deregisterRoutedRPCForSwitch: De-registered routed rpc for ModelDrivenSwitch {}",
                entityMetadata.getOfSwitch().getNodeId().getValue());
    }

    private static void sendNodeAddedNotification(MDSwitchMetaData entityMetadata) {
        //Node added notification need to be sent irrespective of whether
        // *this* instance is owner of the entity or not. Because yang notifications
        // are local, and we should maintain the behavior across the application.
        if (entityMetadata != null && entityMetadata.getOfSwitch() != null) {
            LOG.info("sendNodeAddedNotification: Node Added notification is sent for ModelDrivenSwitch {}",
                    entityMetadata.getOfSwitch().getNodeId().getValue());

            entityMetadata.getContext().getNotificationEnqueuer().enqueueNotification(
                    entityMetadata.getWrappedNotification());

            //Send multipart request to get other details of the switch.
            entityMetadata.getOfSwitch().requestSwitchDetails();
        } else {
            LOG.debug("Switch got disconnected, skip node added notification.");
        }
    }

    private void setDeviceOwnershipState(Entity entity, boolean isMaster) {
        MDSwitchMetaData entityMetadata = entsession.get(entity);
        entityMetadata.setIsOwnershipInitialized(true);
        entityMetadata.getOfSwitch().setEntityOwnership(isMaster);
    }

    private class MDSwitchMetaData {

        final private ModelDrivenSwitch ofSwitch;
        final private SessionContext context;
        final private NotificationQueueWrapper wrappedNotification;
        final private RpcProviderRegistry rpcProviderRegistry;
        final private AtomicBoolean isRPCRegistrationDone = new AtomicBoolean(false);
        final private AtomicBoolean isOwnershipInitialized = new AtomicBoolean(false);

        MDSwitchMetaData(ModelDrivenSwitch ofSwitch,
                         SessionContext context,
                         NotificationQueueWrapper wrappedNotification,
                         RpcProviderRegistry rpcProviderRegistry) {
            this.ofSwitch = ofSwitch;
            this.context = context;
            this.wrappedNotification = wrappedNotification;
            this.rpcProviderRegistry = rpcProviderRegistry;
        }

        public ModelDrivenSwitch getOfSwitch() {
            return ofSwitch;
        }

        public SessionContext getContext() {
            return context;
        }

        public NotificationQueueWrapper getWrappedNotification() {
            return wrappedNotification;
        }

        public RpcProviderRegistry getRpcProviderRegistry() {
            return rpcProviderRegistry;
        }

        public AtomicBoolean getIsRPCRegistrationDone() {
            return isRPCRegistrationDone;
        }

        public void setIsRPCRegistrationDone(boolean isRPCRegistrationDone) {
            this.isRPCRegistrationDone.set(isRPCRegistrationDone);
        }

        public boolean getIsOwnershipInitialized() {
            return isOwnershipInitialized.get();
        }

        public void setIsOwnershipInitialized( boolean ownershipState) {
            this.isOwnershipInitialized.set(ownershipState);
        }
    }
}