/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.util;

import com.google.common.base.Function;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFRoleManager;
import org.opendaylight.openflowplugin.openflow.md.core.session.RolePushException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ControllerRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.common.config.impl.rev140326.OfpRole;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public final class RoleUtil {

    private static final Logger LOG = LoggerFactory.getLogger(RoleUtil.class);
    private static final Function<Exception, RolePushException> exceptionFunction = new Function<Exception, RolePushException>() {
        @Override
        public RolePushException apply(Exception input) {
            RolePushException output = null;
            if (input instanceof ExecutionException) {
                if (input.getCause() instanceof RolePushException) {
                    output = (RolePushException) input.getCause();
                }
            }

            if (output == null) {
                output = new RolePushException(input.getMessage(), input);
            }

            return output;
        }
    };

    private RoleUtil() {
        throw new UnsupportedOperationException("RoleUtil is not expected to be instantiated.");
    }

    /**
     * @param role openflow role for controller
     * @return protocol role
     */
    public static ControllerRole toOFJavaRole(OfpRole role) {
        ControllerRole ofJavaRole = null;
        switch (role) {
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
                LOG.warn("given role is not supported by protocol roles: {}", role);
                break;
        }
        return ofJavaRole;
    }

    /**
     * @param session switch session context
     * @param role  controller openflow role
     * @param generationId generate id for role negotiation
     * @return input builder
     */
    public static RoleRequestInputBuilder createRoleRequestInput(
            final SessionContext session, OfpRole role, BigInteger generationId) {

        ControllerRole ofJavaRole = RoleUtil.toOFJavaRole(role);

        return new RoleRequestInputBuilder()
                .setGenerationId(generationId)
                .setRole(ofJavaRole)
                .setVersion(session.getFeatures().getVersion())
                .setXid(session.getNextXid());
    }

    /**
     * @param sessionContext switch session context
     * @param ofpRole controller openflow role
     * @param generationId generate id for role negotiation
     * @return roleRequest future result
     */
    public static Future<RpcResult<RoleRequestOutput>> sendRoleChangeRequest(SessionContext sessionContext, OfpRole ofpRole, BigInteger generationId) {
        RoleRequestInputBuilder ruleRequestInputBld = RoleUtil.createRoleRequestInput(sessionContext, ofpRole, generationId);
        Future<RpcResult<RoleRequestOutput>> roleReply = sessionContext.getPrimaryConductor().getConnectionAdapter()
                .roleRequest(ruleRequestInputBld.build());
        return roleReply;
    }

    /**
     * @param sessionContext switch session context
     * @return generationId from future RpcResult
     */
    public static Future<BigInteger> readGenerationIdFromDevice(SessionContext sessionContext) {
        Future<RpcResult<RoleRequestOutput>> roleReply = sendRoleChangeRequest(sessionContext, OfpRole.NOCHANGE, BigInteger.ZERO);
        final SettableFuture<BigInteger> result = SettableFuture.create();

        Futures.addCallback(JdkFutureAdapters.listenInPoolThread(roleReply), new FutureCallback<RpcResult<RoleRequestOutput>>() {
            @Override
            public void onSuccess(RpcResult<RoleRequestOutput> input) {
                if(input != null && input.getResult() != null) {
                    result.set(input.getResult().getGenerationId());
                }
            }
            @Override
            public void onFailure(Throwable t) {
                //TODO
            }
        });
        return result;
    }

    /**
     * @param generationId generate id for role negotiation
     * @return next (incremented value)
     */
    public static BigInteger getNextGenerationId(BigInteger generationId) {
        BigInteger nextGenerationId = null;
        if (generationId.compareTo(OFRoleManager.MAX_GENERATION_ID) < 0) {
            nextGenerationId = generationId.add(BigInteger.ONE);
        } else {
            nextGenerationId = BigInteger.ZERO;
        }

        return nextGenerationId;
    }

    /**
     * @param rolePushResult result of role push request
     * @return future which throws {@link RolePushException}
     */
    public static CheckedFuture<Boolean, RolePushException> makeCheckedRuleRequestFxResult(
            ListenableFuture<Boolean> rolePushResult) {
        return Futures.makeChecked(
                rolePushResult, exceptionFunction
        );
    }
}
