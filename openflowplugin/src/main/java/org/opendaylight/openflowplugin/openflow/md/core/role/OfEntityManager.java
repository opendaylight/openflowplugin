/**
 * Copyright (c) 2013, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.role;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.CheckForNull;
import org.opendaylight.controller.md.sal.common.api.clustering.CandidateAlreadyRegisteredException;
import com.google.common.base.Optional;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChain;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipState;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListener;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipCandidateRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipChange;
import org.opendaylight.openflowplugin.openflow.md.core.role.OpenflowOwnershipListener;
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
import com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;
import java.util.Map;

public class OfEntityManager implements TransactionChainListener{
    private static final Logger LOG = LoggerFactory.getLogger(OfEntityManager.class);	
    private static final QName ENTITY_QNAME =
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.core.general.entity.rev150820.Entity.QNAME;
    private static final QName ENTITY_NAME = QName.create(ENTITY_QNAME, "name");
    private DataBroker dataBroker;
    private EntityOwnershipService entityOwnershipService;
    private EntityOwnershipCandidateRegistration entityRegistration;
    private NodeId nodeId;
    private SessionContext sessionContext;
    private final OpenflowOwnershipListener ownershipListener;
    private final AtomicBoolean registeredListener = new AtomicBoolean();
    private ConcurrentHashMap<Entity, SessionContext> entsession;
    private ConcurrentHashMap<Entity, EntityOwnershipCandidateRegistration> entRegistrationMap;

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

    public void requestOpenflowEntityOwnership(NodeId nodeId, SessionContext sessionContext) {
        if (registeredListener.compareAndSet(false, true)) {
            entityOwnershipService.registerListener("openflow", ownershipListener);
        }
        final Entity entity = new Entity("openflow", nodeId.getValue());
        entsession.put(entity, sessionContext);
        Optional <EntityOwnershipState> entityOwnershipStateOptional = 
            entityOwnershipService.getOwnershipState(entity);
        if (entityOwnershipStateOptional != null && entityOwnershipStateOptional.isPresent()) {
            final EntityOwnershipState entityOwnershipState = entityOwnershipStateOptional.get();
            if (entityOwnershipState.hasOwner()) {
                OfpRole newRole ;
                LOG.info("requestOpenflowEntityOwnership: An owner exist for entity {} ", 
                    entity);
                if (entityOwnershipState.isOwner()) {
                    LOG.info("requestOpenflowEntityOwnership: Becoming Master for entity {}",
                        entity);
                    newRole =  OfpRole.BECOMEMASTER;
                } else {
                    LOG.info("requestOpenflowEntityOwnership: Becoming Slave for entity {}",
                        entity);
                    newRole = OfpRole.BECOMESLAVE;
                }
                RolePushTask task = new RolePushTask(newRole, sessionContext);
                ListenableFuture<Boolean> rolePushResult = pool.submit(task);
                CheckedFuture<Boolean, RolePushException> rolePushResultChecked =
                    RoleUtil.makeCheckedRuleRequestFxResult(rolePushResult);
                Futures.addCallback(rolePushResult, new FutureCallback<Boolean>(){
                    @Override
                    public void onSuccess(Boolean result){
                        try {
                            entityRegistration = entityOwnershipService.registerCandidate(entity);
                            entRegistrationMap.put(entity, entityRegistration);
                        } catch (CandidateAlreadyRegisteredException e) {
                            // we can log and move for this error, as listener is present and role changes will be served.
                            LOG.error("Candidate - Entity already registered with Openflow candidate ", entity, e );
                        }
                    }
                    @Override
                    public void onFailure(Throwable t){
                        LOG.error("Candidate - registration failued", t);
                    }
                 });
                 return;
             }
         }
         try {
             entityRegistration = entityOwnershipService.registerCandidate(entity);
             entRegistrationMap.put(entity, entityRegistration);
         } catch (CandidateAlreadyRegisteredException e) {
             // we can log and move for this error, as listener is present and role changes will be served.
             LOG.error("Candidate - Entity already registered with Openflow candidate {}", entity, e );
         }
    }

    public void setRole(SessionContext sessionContext) {
        OfpRole newRole ;
        newRole = OfpRole.BECOMESLAVE;
        if (sessionContext != null) {
            RolePushTask task = new RolePushTask(newRole, sessionContext);
            ListenableFuture<Boolean> rolePushResult = pool.submit(task);
            final CheckedFuture<Boolean, RolePushException> rolePushResultChecked =
                RoleUtil.makeCheckedRuleRequestFxResult(rolePushResult);
            Futures.addCallback(rolePushResult, new FutureCallback<Boolean>(){
                @Override
                public void onSuccess(Boolean result){
                    LOG.info("onRoleChanged: succeeded");
                }
                @Override
                public void onFailure(Throwable e){
                    LOG.error("onRoleChanged: failed to process role request:", e);
                }
            });
        } else {
            LOG.warn("setRole: sessionContext is not set. Session might have been removed");
        }
    }

    public void onRoleChanged(EntityOwnershipChange ownershipChange) {
        OfpRole newRole;
        final Entity entity = ownershipChange.getEntity();
        SessionContext sessionContext = entsession.get(entity);
        if (ownershipChange.isOwner()) {
            LOG.info("onRoleChanged: BECOMEMASTER {}" , entity);
            newRole =  OfpRole.BECOMEMASTER;
        }
        else {
            LOG.info("onRoleChanged: BECOMESLAVE {}", entity);
            newRole = OfpRole.BECOMESLAVE;
            BindingTransactionChain txChain =  dataBroker.createTransactionChain(this);
            if(sessionContext == null && !ownershipChange.hasOwner()) {
                try {
                    ReadWriteTransaction tx = txChain.newReadWriteTransaction();
                    YangInstanceIdentifier yId = entity.getId();
                    NodeIdentifierWithPredicates niWPredicates = (NodeIdentifierWithPredicates)yId.getLastPathArgument();
                    Map<QName, Object> keyValMap = niWPredicates.getKeyValues();
                    String nodeIdStr = (String)(keyValMap.get(ENTITY_NAME));
                    BigInteger dpId = new BigInteger(nodeIdStr.split(":")[1]);
                    NodeKey nodeKey = new NodeKey(new NodeId(nodeIdStr));
                    InstanceIdentifier<Node> path = InstanceIdentifier.create(Nodes.class).child(Node.class, nodeKey);

                    Optional<Node> flowNode = Optional.absent();

                    try {
                        flowNode = tx.read(LogicalDatastoreType.OPERATIONAL, path).get();
                    }
                    catch (Exception e) {
                        LOG.error("Fail with read Config/DS for Node {} !", entity, e);
                    }

                    if (flowNode.isPresent()) {
                        //final NodeRef ref = flowNode.getNodeRef();
                        LOG.debug("onRoleChanged: removing node : {}", path);
                        tx.delete(LogicalDatastoreType.OPERATIONAL, path);
                        tx.submit();
                    }
                } finally {
                    txChain.close();
                }
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
                }
                @Override
                public void onFailure(Throwable e){
                    LOG.error("onRoleChanged: failed to process role request: ", e);
                }
            });
        } else {
            LOG.warn("onRoleChanged: sessionContext is not set. Session might have been removed {}", entity);
        }
    }

    public void removeSession(NodeId nodeId)
    {
        Entity entity = new Entity("openflow", nodeId.getValue());
        entsession.remove(entity);
        EntityOwnershipCandidateRegistration entRegCandidate = entRegistrationMap.get(entity);
        LOG.info("removeSession: registration candidate exists for unregistration " + (entRegCandidate != null) + " for entity {}", entity);
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

}
