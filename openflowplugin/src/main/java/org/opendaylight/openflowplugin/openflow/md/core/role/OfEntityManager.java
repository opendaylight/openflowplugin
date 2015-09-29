/**
 * Copyright (c) 2013, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.role;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;
import org.opendaylight.controller.md.sal.common.api.clustering.CandidateAlreadyRegisteredException;
import com.google.common.base.Optional;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChain;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipState;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipCandidateRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.openflow.md.ModelDrivenSwitch;
import org.opendaylight.openflowplugin.api.openflow.md.core.NotificationQueueWrapper;
import org.opendaylight.yangtools.concepts.CompositeObjectRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
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
import java.util.concurrent.ArrayBlockingQueue;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.CheckedFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

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

    public OfEntityManager( EntityOwnershipService entityOwnershipService ) {
        this.entityOwnershipService = entityOwnershipService;
        ownershipListener = new OpenflowOwnershipListener(this);
        entsession = new ConcurrentHashMap<>();
        entRegistrationMap = new ConcurrentHashMap<>();
        ThreadPoolLoggingExecutor delegate = new ThreadPoolLoggingExecutor(
            1, 5, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(5), "ofEntity");
        pool =  MoreExecutors.listeningDecorator(delegate);
    }

    public void setDataBroker(DataBroker dbBroker) {
        this.dataBroker = dbBroker;
    }

    public void requestOpenflowEntityOwnership(ModelDrivenSwitch ofSwitch,
                                               SessionContext context,
                                               NotificationQueueWrapper wrappedNotification,
                                               RpcProviderRegistry rpcProviderRegistry) {
        MDSwitchMetaData entityMetaData =
                new MDSwitchMetaData(ofSwitch,context,wrappedNotification,rpcProviderRegistry);

        if (registeredListener.compareAndSet(false, true)) {
            entityOwnershipService.registerListener(DEVICE_TYPE, ownershipListener);
        }
        final Entity entity = new Entity(DEVICE_TYPE, ofSwitch.getNodeId().getValue());
        entsession.put(entity, entityMetaData);

        //Register as soon as possible to avoid missing any entity ownership change event
        final EntityOwnershipCandidateRegistration entityRegistration;
        try {
            entityRegistration = entityOwnershipService.registerCandidate(entity);
            entRegistrationMap.put(entity, entityRegistration);
            LOG.info("requestOpenflowEntityOwnership: registered candidate(first) for entity {}", entity );
        } catch (CandidateAlreadyRegisteredException e) {
            // we can log and move for this error, as listener is present and role changes will be served.
            LOG.error("Candidate - Entity already registered with Openflow candidate {}", entity, e );
        }

        Optional <EntityOwnershipState> entityOwnershipStateOptional =
                entityOwnershipService.getOwnershipState(entity);

        if (entityOwnershipStateOptional.isPresent()) {
            final EntityOwnershipState entityOwnershipState = entityOwnershipStateOptional.get();
            if (entityOwnershipState.hasOwner()) {
                final OfpRole newRole ;
                if (entityOwnershipState.isOwner()) {
                    LOG.info("requestOpenflowEntityOwnership: Owner of the Device Entity, set *this* instance " +
                            "as a MASTER controller for the entity {}", entity);
                    newRole =  OfpRole.BECOMEMASTER;
                } else {
                    LOG.info("requestOpenflowEntityOwnership: *this* instance is not owner of the device, set" +
                            "*this* instance as a SLAVE controller for the entity {}", entity);
                    newRole = OfpRole.BECOMESLAVE;
                }
                RolePushTask task = new RolePushTask(newRole, context);
                ListenableFuture<Boolean> rolePushResult = pool.submit(task);
                CheckedFuture<Boolean, RolePushException> rolePushResultChecked =
                    RoleUtil.makeCheckedRuleRequestFxResult(rolePushResult);
                Futures.addCallback(rolePushResult, new FutureCallback<Boolean>(){
                    @Override
                    public void onSuccess(Boolean result){
                        LOG.info("requestOpenflowEntityOwnership: *this* instance of controller is now {} of " +
                                "the entity {}", newRole == OfpRole.BECOMEMASTER?"MASTER":"SLAVE",entity );
                        entsession.get(entity).getOfSwitch().setEntityOwnership(newRole==OfpRole.BECOMEMASTER);
                        registerRoutedRPCForSwitch(entsession.get(entity));
                        sendNodeAddedNotification(entsession.get(entity));
                    }
                    @Override
                    public void onFailure(Throwable t){
                        LOG.warn("*this* instance of controller is not able to set the role.", t);
                        if(newRole == OfpRole.BECOMEMASTER) {
                            LOG.info("..and this *instance* is owner of the device entity {}. Closing the " +
                                    "registration, so other entity can become owner " +
                                    "and attempt to be master controller.",entity);

                            EntityOwnershipCandidateRegistration ownershipRegistrent = entRegistrationMap.get(entity);
                            if (ownershipRegistrent != null) {
                                ownershipRegistrent.close();
                                entRegistrationMap.remove(entity);
                            }

                            LOG.info("..and registering it back to participate in ownership and re-try");

                            EntityOwnershipCandidateRegistration entityRegistration;
                            try {
                                entityRegistration = entityOwnershipService.registerCandidate(entity);
                                entRegistrationMap.put(entity, entityRegistration);
                                LOG.info("requestOpenflowEntityOwnership: re-registered candidate for " +
                                        "ownership of the entity {}", entity );
                            } catch (CandidateAlreadyRegisteredException e) {
                                // we can log and move for this error, as listener is present and role changes will be served.
                                LOG.error("Candidate - *Surprisingly* Entity is already registered with " +
                                        "EntityOwnershipService {}", entity, e );
                            }

                        } else {
                                LOG.error("requestOpenflowEntityOwnership : Not able to set role {} for " +
                                        "entity {}", newRole == OfpRole.BECOMEMASTER?"MASTER":"SLAVE", entity);
                        }
                    }
                 });
             }
         }
    }

    public void setSlaveRole(SessionContext sessionContext) {
        OfpRole newRole ;
        newRole = OfpRole.BECOMESLAVE;
        if (sessionContext != null) {
            LOG.info("setSlaveRole: triggering RolePushTask with role as BECOMESLAVE");
            RolePushTask task = new RolePushTask(newRole, sessionContext);
            ListenableFuture<Boolean> rolePushResult = pool.submit(task);
            final CheckedFuture<Boolean, RolePushException> rolePushResultChecked =
                RoleUtil.makeCheckedRuleRequestFxResult(rolePushResult);
            Futures.addCallback(rolePushResult, new FutureCallback<Boolean>(){
                @Override
                public void onSuccess(Boolean result){
                    LOG.info("setSlaveRole: succeeded");
                }
                @Override
                public void onFailure(Throwable e){
                    LOG.error("setSlaveRole: failed to process role request:", e);
                }
            });
        } else {
            LOG.warn("setSlaveRole: sessionContext is not set. Session might have been removed");
        }
    }

    public void onDeviceOwnershipChanged(EntityOwnershipChange ownershipChange) {
        final OfpRole newRole;
        final Entity entity = ownershipChange.getEntity();
        SessionContext sessionContext = entsession.get(entity)!=null?entsession.get(entity).getContext():null;
        if (ownershipChange.isOwner()) {
            LOG.info("onRoleChanged: BECOMEMASTER {}" , entity);
            newRole =  OfpRole.BECOMEMASTER;
        }
        else {
            LOG.info("onRoleChanged: BECOMESLAVE {}", entity);
            newRole = OfpRole.BECOMESLAVE;
            BindingTransactionChain txChain =  dataBroker.createTransactionChain(this);

            if(sessionContext == null && !ownershipChange.hasOwner()) {
                LOG.info("Entity {} don't have any owner, explicitly clean up the operational data store",entity);
                YangInstanceIdentifier yId = entity.getId();
                ReadWriteTransaction tx = txChain.newReadWriteTransaction();
                NodeIdentifierWithPredicates niWPredicates = (NodeIdentifierWithPredicates)yId.getLastPathArgument();
                Map<QName, Object> keyValMap = niWPredicates.getKeyValues();
                String nodeIdStr = (String)(keyValMap.get(ENTITY_NAME));
                BigInteger dpId = new BigInteger(nodeIdStr.split(":")[1]);
                NodeKey nodeKey = new NodeKey(new NodeId(nodeIdStr));
                InstanceIdentifier<Node> path = InstanceIdentifier.create(Nodes.class).child(Node.class, nodeKey);

                Optional<Node> flowNode = Optional.absent();

                try {
                    flowNode = tx.read(LogicalDatastoreType.OPERATIONAL, path).get();
                    if (flowNode.isPresent()) {
                        //final NodeRef ref = flowNode.getNodeRef();
                        LOG.info("onRoleChanged: removing node : {}", path);
                        tx.delete(LogicalDatastoreType.OPERATIONAL, path);
                        tx.submit();
                    }
                }
                catch (Exception e) {
                    LOG.error("Fail with read Config/DS for Node {} !", entity, e);
                }
            }

            if(sessionContext != null && ownershipChange.wasOwner()) {
                deregisterRoutedRPCForSwitch(entsession.get(entity));
            }
        }
        if (sessionContext != null) {
            RolePushTask task = new RolePushTask(newRole, sessionContext);
            ListenableFuture<Boolean> rolePushResult = pool.submit(task);
            final CheckedFuture<Boolean, RolePushException> rolePushResultChecked =
                RoleUtil.makeCheckedRuleRequestFxResult(rolePushResult);
            Futures.addCallback(rolePushResult, new FutureCallback<Boolean>(){
                @Override
                public void onSuccess(Boolean result){
                    LOG.info("onRoleChanged: succeeded {}", entity);
                    entsession.get(entity).getOfSwitch().setEntityOwnership(newRole==OfpRole.BECOMEMASTER);
                    registerRoutedRPCForSwitch(entsession.get(entity));
                    sendNodeAddedNotification(entsession.get(entity));

                }
                @Override
                public void onFailure(Throwable e){

                    LOG.warn("*this* instance of controller is not able to set the role.", e);
                    if(newRole == OfpRole.BECOMEMASTER) {
                        LOG.info("..and this *instance* is owner of the device entity {}. Closing the " +
                                "registration, so other entity can become owner " +
                                "and attempt to be master controller.",entity);

                        EntityOwnershipCandidateRegistration ownershipRegistrent = entRegistrationMap.get(entity);
                        if (ownershipRegistrent != null) {
                            ownershipRegistrent.close();
                            entRegistrationMap.remove(entity);
                        }

                        LOG.info("..and registering it back to participate in ownership and re-try");

                        EntityOwnershipCandidateRegistration entityRegistration;
                        try {
                            entityRegistration = entityOwnershipService.registerCandidate(entity);
                            entRegistrationMap.put(entity, entityRegistration);
                            LOG.info("requestOpenflowEntityOwnership: re-registered candidate for " +
                                    "ownership of the entity {}", entity );
                        } catch (CandidateAlreadyRegisteredException ex) {
                            // we can log and move for this error, as listener is present and role changes will be served.
                            LOG.error("Candidate - *Surprisingly* Entity is already registered with " +
                                    "EntityOwnershipService {}", entity, ex );
                        }

                    } else {
                        LOG.error("requestOpenflowEntityOwnership : Not able to set role {} for " +
                                "entity {}", newRole == OfpRole.BECOMEMASTER?"MASTER":"SLAVE", entity);
                    }
                }
            });
        } else {
            LOG.warn("onRoleChanged: sessionContext is not set. Session might have been removed {}", entity);
        }
    }

    public void removeSession(NodeId nodeId)
    {
        Entity entity = new Entity(DEVICE_TYPE, nodeId.getValue());
        entsession.remove(entity);
        EntityOwnershipCandidateRegistration entRegCandidate = entRegistrationMap.get(entity);
        LOG.info("removeSession: registration candidate exists for " +
                "unregistration " + (entRegCandidate != null) + " for entity {}", entity);
        if(entRegCandidate != null){
            entRegCandidate.close();
            entRegistrationMap.remove(entity);
        }
    }

    @Override
    public void onTransactionChainFailed(final TransactionChain<?, ?> chain, final AsyncTransaction<?, ?> transaction,
           final Throwable cause) {
       LOG.warn("OfEntityManager: Transaction {} failed.",transaction.getIdentifier(),cause);
    }

    @Override
    public void onTransactionChainSuccessful(final TransactionChain<?, ?> chain) {
       // NOOP
    }

    private void registerRoutedRPCForSwitch(MDSwitchMetaData entityMetadata) {
        // Routed RPC registration is only done when *this* instance is owner of
        // the entity.
        if(entityMetadata.ofSwitch.isEntityOwner()) {
            if (!entityMetadata.isRPCRegistrationDone.get()) {
                entityMetadata.setIsRPCRegistrationDone(true);
                CompositeObjectRegistration<ModelDrivenSwitch> registration =
                        entityMetadata.getOfSwitch().register(entityMetadata.getRpcProviderRegistry());

                entityMetadata.getContext().setProviderRegistration(registration);

                LOG.info("Routed rpc for ModelDrivenSwitch {} is registered", entityMetadata.getOfSwitch().getNodeId().getValue());
            }
        }
    }

    private void deregisterRoutedRPCForSwitch(MDSwitchMetaData entityMetadata) {
        LOG.info("Routed rpc for ModelDrivenSwitch {} is de-registered",
                entityMetadata.getOfSwitch().getNodeId().getValue());

        CompositeObjectRegistration<ModelDrivenSwitch> registration = entityMetadata.getContext().getProviderRegistration();
        if (null != registration) {
            registration.close();
            entityMetadata.getContext().setProviderRegistration(null);
        }
    }

    private void sendNodeAddedNotification(MDSwitchMetaData entityMetadata) {
        //Node added notification need to be sent irrespective of whether
        // *this* instance is owner of the entity or not. Because yang notifications
        // are local, and we should maintain the behavior across the application.
        LOG.info("ModelDrivenSwitch {} add notification sent",
                entityMetadata.getOfSwitch().getNodeId().getValue());

        entityMetadata.getContext().getNotificationEnqueuer().enqueueNotification(
                entityMetadata.getWrappedNotification());
    }
    private class MDSwitchMetaData {

        final private ModelDrivenSwitch ofSwitch;
        final private SessionContext context;
        final private NotificationQueueWrapper wrappedNotification;
        final private RpcProviderRegistry rpcProviderRegistry;
        final private AtomicBoolean isRPCRegistrationDone = new AtomicBoolean(false);

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
    }
}
