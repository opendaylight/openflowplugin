/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.session;

import java.util.Comparator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.core.ThreadPoolLoggingExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ControllerRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.common.config.impl.rev140326.OfpRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * manage OF-role propagation to devices
 */
public class OFRoleManager implements AutoCloseable {
    
    private static final Logger LOG = LoggerFactory.getLogger(OFRoleManager.class);

    private static final long TIMEOUT = 2000;

    private static final TimeUnit TIMEOUT_UNIT = TimeUnit.MILLISECONDS;

    private static final int RETRY_LIMIT = 0;
    
    private ListeningExecutorService broadcastPool;

    private BlockingQueue<RolePushTask> workQueue;
    
    /**
     * default ctor
     */
    public OFRoleManager() {
        workQueue = new PriorityBlockingQueue<>(500, new Comparator<RolePushTask>() {
            @Override
            public int compare(RolePushTask o1, RolePushTask o2) {
                return Integer.compare(o1.getPriority(), o2.getPriority());
            }
        });
        ThreadPoolLoggingExecutor delegate = new ThreadPoolLoggingExecutor(
                1, 1, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1) , "ofRoleBroadcast");
        broadcastPool = MoreExecutors.listeningDecorator(
                delegate);
    }

    /**
     * change role on each connected device
     * 
     * @param role
     */
    public void manageRoleChange(final OfpRole role) {
        for (final SessionContext session : OFSessionUtil.getAllSessions()) {
            try {
                workQueue.put(new RolePushTask(role, session));
            } catch (InterruptedException e) {
                LOG.warn("Processing of role request failed while enqueueing role task: {}", e.getMessage());
            }
        }
        
        while (! workQueue.isEmpty()) {
            RolePushTask task = workQueue.poll();
            ListenableFuture<Boolean> rolePushResult = broadcastPool.submit(task);
            try {
                Boolean succeeded = rolePushResult.get(TIMEOUT, TIMEOUT_UNIT);
                if (! Objects.firstNonNull(succeeded, Boolean.FALSE)) {
                    if (task.getRetryCounter() < RETRY_LIMIT) {
                        workQueue.offer(task);
                    }
                }
            } catch (Exception e) {
                LOG.warn("failed to process role request: {}", e);
            }
        }
    }

    /**
     * @param session
     * @param role 
     * @return input builder
     */
    protected static RoleRequestInputBuilder createRuleRequestInput(
            final SessionContext session, OfpRole role) {
        
        ControllerRole ofJavaRole = toOFJavaRole(role);
        
        return new RoleRequestInputBuilder()
            .setGenerationId(session.getNextGenerationId())
            .setRole(ofJavaRole)
            .setVersion(session.getFeatures().getVersion())
            .setXid(session.getNextXid());
    }
    
    /**
     * @param role
     * @return
     */
    private static ControllerRole toOFJavaRole(OfpRole role) {
        ControllerRole ofJavaRole = null;
        switch(role) {
        case BECOMEMASTER:
            ofJavaRole = ControllerRole.OFPCRROLEMASTER;
            break;
        case BECOMESLAVE:
            ofJavaRole = ControllerRole.OFPCRROLESLAVE;
            break;
        case NOCHANGE:
            ofJavaRole = ControllerRole.OFPCRROLENOCHANGE;
            break;
        default:
            // no intention
            break;
        }
        return ofJavaRole;
    }

    @Override
    public void close() throws Exception {
        if (broadcastPool != null) {
            broadcastPool.shutdown();
        }
    }
}
