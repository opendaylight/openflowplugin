/**
 * Copyright (c) 2013, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.role;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.CheckForNull;
import org.opendaylight.controller.md.sal.common.api.clustering.CandidateAlreadyRegisteredException;
import com.google.common.base.Optional;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipState;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListener;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipCandidateRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
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
import java.util.concurrent.ArrayBlockingQueue;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;

public class OfEntityManager {
	private static final Logger LOG = LoggerFactory.getLogger(OfEntityManager.class);
	private EntityOwnershipService entityOwnershipService;
	private EntityOwnershipCandidateRegistration entityRegistration;
	private NodeId nodeId;
	private SessionContext sessionContext;
	private final OpenflowOwnershipListener ownershipListener;
	private final AtomicBoolean registeredListener = new AtomicBoolean();
	private ConcurrentHashMap<Entity, SessionContext> entsession;
    	private static final long TIMEOUT = 8000;
    	private static final TimeUnit TIMEOUT_UNIT = TimeUnit.MILLISECONDS;

	private final ListeningExecutorService pool;

	public OfEntityManager( EntityOwnershipService entityOwnershipService ) {
		this.entityOwnershipService = entityOwnershipService;
		ownershipListener = new OpenflowOwnershipListener(this);
		entsession = new ConcurrentHashMap<>();
		ThreadPoolLoggingExecutor delegate = new ThreadPoolLoggingExecutor(
			1, 1, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1), "ofEntity");
		pool =  MoreExecutors.listeningDecorator(delegate);
	}

	public void requestOpenflowEntityOwnership(NodeId nodeId, SessionContext sessionContext) {
		if (registeredListener.compareAndSet(false, true)) {
			entityOwnershipService.registerListener("openflow", ownershipListener);
		}
		Entity entity = new Entity("openflow", nodeId.getValue());
		entsession.put(entity, sessionContext);

		Optional <EntityOwnershipState> entityOwnershipStateOptional = 
								entityOwnershipService.getOwnershipState(entity);

		if (entityOwnershipStateOptional != null && entityOwnershipStateOptional.isPresent()) {
			final EntityOwnershipState entityOwnershipState = entityOwnershipStateOptional.get();
			if (entityOwnershipState.hasOwner()) {
				OfpRole newRole ;
				LOG.debug("requestOpenflowEntityOwnership: An owner exist for entity {} ", 
						entity);
				System.out.println("requestOpenflowEntityOwnership: An owner exist for entity {}" + entity);
				if (entityOwnershipState.isOwner()) {
					LOG.debug("requestOpenflowEntityOwnership: Becoming Master for entity ",
							entity);
					System.out.println("requestOpenflowEntityOwnership: Becoming Master for entity " + entity);
					newRole =  OfpRole.BECOMEMASTER;
				} else {
					LOG.debug("requestOpenflowEntityOwnership: Becoming Slave for entity ",
							entity);
					System.out.println("requestOpenflowEntityOwnership: Becoming Slave for entity " + entity);
					newRole = OfpRole.BECOMESLAVE;
				}
				RolePushTask task = new RolePushTask(newRole, sessionContext);
				ListenableFuture<Boolean> rolePushResult = pool.submit(task);
				CheckedFuture<Boolean, RolePushException> rolePushResultChecked =
					RoleUtil.makeCheckedRuleRequestFxResult(rolePushResult);

				try {
					Boolean succeeded = rolePushResultChecked.checkedGet(TIMEOUT, TIMEOUT_UNIT);
					LOG.debug("requestOpenflowEntityOwnership: RolePush succeeded" , succeeded);
					System.out.println("requestOpenflowEntityOwnership: RolePush succeeded"	+ succeeded); 
				} catch (RolePushException | TimeoutException e) {
					LOG.warn("requestOpenflowEntityOwnership: failed to process role request: {}", e);
					System.out.println("requestOpenflowEntityOwnership: failed to process role request: {}"+ e);
				}
			}
		}

		try {
			entityRegistration = entityOwnershipService.registerCandidate(entity);
		} catch (CandidateAlreadyRegisteredException e) {
			// we can log and move for this error, as listener is present and role changes will be served.
			LOG.error("Candidate - Entity already registered with Openflow candidate ", entity, e );
			System.out.println("Candidate - Entity already registered with Openflow candidate "+ entity+ e );
		}
	}

	public void setRole(SessionContext sessionContext) {
		OfpRole newRole ;
		newRole = OfpRole.BECOMESLAVE;
		if (sessionContext != null)
		{
		RolePushTask task = new RolePushTask(newRole, sessionContext);
		ListenableFuture<Boolean> rolePushResult = pool.submit(task);
		CheckedFuture<Boolean, RolePushException> rolePushResultChecked =
			RoleUtil.makeCheckedRuleRequestFxResult(rolePushResult);

		try {
			Date date = new Date();
			System.out.println("setRole: Start Time "+ date.toString());
			Boolean succeeded = rolePushResultChecked.checkedGet(TIMEOUT, TIMEOUT_UNIT);
			Date date1 = new Date();
			LOG.debug("setRole: succeeded" , succeeded);
			System.out.println("setRole: succeeded " + succeeded + " " + date1.toString());

		} catch (RolePushException | TimeoutException e) {
			LOG.warn("setRole: failed to process role request: {}", e);
			Date date1 = new Date();
			System.out.println("setRole: failed to process role request: {}"+ e + " " + date1.toString());
		}
		} else
		{
			System.out.println("setRole: sessionContext is not set. Session might have been removed");
			LOG.debug("setRole: sessionContext is not set. Session might have been removed");
		}
	}

	public void onRoleChanged(EntityOwnershipChange ownershipChange) {
		OfpRole newRole;
		Entity entity = ownershipChange.getEntity();
		if (ownershipChange.isOwner())
		{
			LOG.debug("onRoleChanged: BECOMEMASTER " , entity);
			System.out.println("onRoleChanged: BECOMEMASTER " + entity);
			newRole =  OfpRole.BECOMEMASTER;
		}
		else
		{
			LOG.debug("onRoleChanged: BECOMESLAVE ", entity);
			System.out.println("onRoleChanged: BECOMESLAVE "+ entity);
			newRole = OfpRole.BECOMESLAVE;
		}
		SessionContext sessionContext = entsession.get(entity);
		if (sessionContext != null)
		{
		RolePushTask task = new RolePushTask(newRole, sessionContext);
		ListenableFuture<Boolean> rolePushResult = pool.submit(task);
		CheckedFuture<Boolean, RolePushException> rolePushResultChecked =
			RoleUtil.makeCheckedRuleRequestFxResult(rolePushResult);

		try {
			Boolean succeeded = rolePushResultChecked.checkedGet(TIMEOUT, TIMEOUT_UNIT);
			LOG.debug("onRoleChanged: succeeded" + succeeded);
			System.out.println("onRoleChanged: succeeded" + succeeded);

		} catch (RolePushException | TimeoutException e) {
			LOG.warn("onRoleChanged: failed to process role request: {}", e);
			System.out.println("onRoleChanged: failed to process role request: {}"+ e);
		}
		} else
		{
			System.out.println("onRoleChanged: sessionContext is not set. Session might have been removed");
			LOG.debug("onRoleChanged: sessionContext is not set. Session might have been removed");
		}
	}

	public void removeSession(NodeId nodeId)
	{
		Entity entity = new Entity("openflow", nodeId.getValue());
		entsession.remove(entity);
		entityRegistration.close();
	}




}
