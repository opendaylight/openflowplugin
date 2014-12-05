/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.session;

import com.google.common.base.Preconditions;
import java.math.BigInteger;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.core.MessageFactory;
import org.opendaylight.openflowplugin.openflow.md.util.RoleUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.common.config.impl.rev140326.OfpRole;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * push role to device - basic step:<br/>
 * <ul>
 * <li>here we read generationId from device and</li>
 * <li>push role request with incremented generationId</li>
 * <li>{@link #call()} returns true if role request was successful</li>
 * </ul>
 */
final class RolePushTask implements Callable<Boolean> {

    public static final Logger LOG = LoggerFactory
            .getLogger(RolePushTask.class);

    public static final long TIMEOUT = 2000;
    public static final TimeUnit TIMEOUT_UNIT = TimeUnit.MILLISECONDS;
    private OfpRole role;
    private SessionContext session;
    private int priority;
    private int retryCounter;

    /**
     * @param role
     * @param session
     */
    public RolePushTask(OfpRole role, SessionContext session) {
        Preconditions.checkNotNull("OfpRole can not be empty.", role);
        Preconditions.checkNotNull("Session context can not be empty.", session);
        this.role = role;
        this.session = session;
    }

    /**
     * @return the retryCounter
     */
    public int getRetryCounter() {
        return retryCounter;
    }

    /**
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public Boolean call() throws RolePushException {
        if (!session.isValid()) {
            String msg = "giving up role change: current session is invalid";
            LOG.debug(msg);
            throw new RolePushException(msg);
        }

        // adopt actual generationId from device (first shot failed and this is retry)
        BigInteger generationId = null;
        try {
            generationId = RoleUtil.readGenerationIdFromDevice(session).get(TIMEOUT, TIMEOUT_UNIT);
        } catch (Exception e) {
            LOG.debug("generationId request failed: ", e);
        }

        if (generationId == null) {
            String msg = "giving up role change: current generationId can not be read";
            LOG.debug(msg);
            throw new RolePushException(msg);
        }

        generationId = RoleUtil.getNextGenerationId(generationId);

        // try to possess role on device
        Future<RpcResult<RoleRequestOutput>> roleReply = RoleUtil.sendRoleChangeRequest(session, role, generationId);
        // flush election result with barrier
        BarrierInput barrierInput = MessageFactory.createBarrier(
                session.getFeatures().getVersion(), session.getNextXid());
        Future<RpcResult<BarrierOutput>> barrierResult = session.getPrimaryConductor().getConnectionAdapter().barrier(barrierInput);
        try {
            barrierResult.get(TIMEOUT, TIMEOUT_UNIT);
        } catch (Exception e) {
            String msg = String.format("giving up role change: barrier after role change failed: %s", e.getMessage());
            LOG.warn(msg);
            throw new RolePushException(msg);
        }
        // after barrier replied there must be election result or error
        try {
            roleReply.get(0, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            // no election result received - let's retry
            retryCounter += 1;
            return false;
        }

        // here we expect that role on device is successfully possessed
        return true;
    }
}