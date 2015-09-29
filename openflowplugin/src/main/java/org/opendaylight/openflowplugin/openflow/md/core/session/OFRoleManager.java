/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.session;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionManager;
import org.opendaylight.openflowplugin.openflow.md.core.ThreadPoolLoggingExecutor;
import org.opendaylight.openflowplugin.openflow.md.util.RoleUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.common.config.impl.rev140326.OfpRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * manage OF-role propagation to devices
 */
public class OFRoleManager implements AutoCloseable {

    /**
     * starting value of generationId
     */
    public static final BigInteger MAX_GENERATION_ID = new BigInteger("ffffffffffffffff", 16);

    private static final Logger LOG = LoggerFactory.getLogger(OFRoleManager.class);

    private static final long TIMEOUT = 2000;

    private static final TimeUnit TIMEOUT_UNIT = TimeUnit.MILLISECONDS;

    private static final int RETRY_LIMIT = 42;

    private final ListeningExecutorService broadcastPool;

    private final BlockingQueue<RolePushTask> workQueue;

    private final SessionManager sessionManager;

    /**
     * @param sessionManager switch connection session manager
     */
    public OFRoleManager(final SessionManager sessionManager) {
        Preconditions.checkNotNull("Session manager can not be empty.", sessionManager);
        this.sessionManager = sessionManager;
        workQueue = new PriorityBlockingQueue<>(500, new Comparator<RolePushTask>() {
            @Override
            public int compare(final RolePushTask o1, final RolePushTask o2) {
                return Integer.compare(o1.getPriority(), o2.getPriority());
            }
        });
        ThreadPoolLoggingExecutor delegate = new ThreadPoolLoggingExecutor(
                1, 1, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1), "ofRoleBroadcast");
        broadcastPool = MoreExecutors.listeningDecorator(
                delegate);
    }

    /**
     * change role on each connected device
     *
     * @param role openflow role
     */
    public void manageRoleChange(final OfpRole role) {
        for (final SessionContext session : sessionManager.getAllSessions()) {
            try {
                workQueue.put(new RolePushTask(role, session));
            } catch (InterruptedException e) {
                LOG.warn("Processing of role request failed while enqueueing role task: {}", e.getMessage());
            }
        }

        while (!workQueue.isEmpty()) {
            RolePushTask task = workQueue.poll();
            ListenableFuture<Boolean> rolePushResult = broadcastPool.submit(task);
            CheckedFuture<Boolean, RolePushException> rolePushResultChecked =
                    RoleUtil.makeCheckedRuleRequestFxResult(rolePushResult);
            try {
                Boolean succeeded = rolePushResultChecked.checkedGet(TIMEOUT, TIMEOUT_UNIT);
                if (!MoreObjects.firstNonNull(succeeded, Boolean.FALSE)) {
                    if (task.getRetryCounter() < RETRY_LIMIT) {
                        workQueue.offer(task);
                    }
                }
            } catch (RolePushException | TimeoutException e) {
                LOG.warn("failed to process role request: {}", e);
            }
        }
    }

    @Override
    public void close() throws Exception {
        broadcastPool.shutdown();
    }
}
