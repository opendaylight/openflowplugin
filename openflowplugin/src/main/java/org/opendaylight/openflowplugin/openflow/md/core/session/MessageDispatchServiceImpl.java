/**
 * Copyright IBM Corporation, 2013.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.session;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.controller.sal.common.util.RpcErrors;
import org.opendaylight.controller.sal.common.util.Rpcs;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.ConnectionException;
import org.opendaylight.openflowplugin.openflow.md.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev131103.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortOutputBuilder;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * message dispatch service to send the message to switch.
 *
 * @author AnilGujele
 */
public class MessageDispatchServiceImpl implements IMessageDispatchService {

    private static final Logger LOG = LoggerFactory.getLogger(MessageDispatchServiceImpl.class);
    private static final String CONNECTION_ERROR_MESSAGE = "Session for the cookie is invalid. Reason: "
            + "the switch has been recently disconnected OR inventory provides outdated information.";

    private SessionContext session;

    /**
     * constructor
     *
     * @param session - MessageDispatchService for this session
     */
    public MessageDispatchServiceImpl(SessionContext session) {
        this.session = session;
    }

    /**
     * get proper connection adapter to send the message to switch.
     *
     * @param cookie to identify the right connection, it can be null also.
     * @return connectionAdapter associated with cookie, otherwise return best
     * suitable connection.
     */

    private ConnectionAdapter getConnectionAdapter(SwitchConnectionDistinguisher cookie) throws ConnectionException {

        if (!session.isValid()) {
            LOG.warn("Session for the cookie {} is invalid.", cookie);
            throw new ConnectionException(CONNECTION_ERROR_MESSAGE);
        }
        LOG.debug("finding connecton for cookie value {}. ", cookie);
        // set main connection as default
        ConnectionAdapter connectionAdapter = session.getPrimaryConductor().getConnectionAdapter();
        if (null != cookie) {
            ConnectionConductor conductor = session.getAuxiliaryConductor(cookie);
            // check if auxiliary connection exist
            if (null != conductor) {
                LOG.debug("found auxiliary connection for the cookie.");
                connectionAdapter = conductor.getConnectionAdapter();
            }
        } else {
            // TODO: pick connection to utilize all the available connection.
        }
        return connectionAdapter;
    }

    @Override
    public Future<RpcResult<BarrierOutput>> barrier(BarrierInput input, SwitchConnectionDistinguisher cookie) {
        try {
            return getConnectionAdapter(cookie).barrier(input);
        } catch (ConnectionException e) {
            return getRpcErrorFuture(e);
        }
    }

    private <T> SettableFuture<RpcResult<T>> getRpcErrorFuture(ConnectionException e) {
        List<RpcError> rpcErrorList = getConnectionErrorAsRpcErrors(e);
        SettableFuture<RpcResult<T>> futureWithError = SettableFuture.create();
        futureWithError.set(Rpcs.<T>getRpcResult(false, rpcErrorList));
        return futureWithError;
    }

    private List<RpcError> getConnectionErrorAsRpcErrors(ConnectionException e) {
        List<RpcError> rpcErrorList = new ArrayList<>();
        rpcErrorList.add(RpcErrors.getRpcError(OFConstants.APPLICATION_TAG,
                OFConstants.ERROR_TAG_TIMEOUT,
                CONNECTION_ERROR_MESSAGE,
                RpcError.ErrorSeverity.WARNING,
                e.getMessage(),
                RpcError.ErrorType.TRANSPORT,
                e.getCause()));
        return rpcErrorList;
    }

    @Override
    public Future<RpcResult<Void>> experimenter(ExperimenterInput input, SwitchConnectionDistinguisher cookie) {
        try {
            return getConnectionAdapter(cookie).experimenter(input);
        } catch (ConnectionException e) {
            return getRpcErrorFuture(e);
        }
    }

    @Override
    public Future<RpcResult<UpdateFlowOutput>> flowMod(final FlowModInput input, SwitchConnectionDistinguisher cookie) {
        LOG.debug("Calling OFLibrary flowMod");
        Future<RpcResult<Void>> response = null;
        try {
            response = getConnectionAdapter(cookie).flowMod(input);
        } catch (ConnectionException e) {
            return getRpcErrorFuture(e);
        }

        // appending xid
        ListenableFuture<RpcResult<UpdateFlowOutput>> xidResult = Futures.transform(
                JdkFutureAdapters.listenInPoolThread(response),
                new Function<RpcResult<Void>, RpcResult<UpdateFlowOutput>>() {

                    @Override
                    public RpcResult<UpdateFlowOutput> apply(final RpcResult<Void> inputArg) {
                        UpdateFlowOutputBuilder flowModOutput = new UpdateFlowOutputBuilder();
                        BigInteger bigIntXid = BigInteger.valueOf(input.getXid());
                        flowModOutput.setTransactionId(new TransactionId(bigIntXid));

                        UpdateFlowOutput result = flowModOutput.build();
                        RpcResult<UpdateFlowOutput> rpcResult = Rpcs.getRpcResult(
                                inputArg.isSuccessful(), result, inputArg.getErrors());
                        return rpcResult;
                    }
                });

        return xidResult;
    }

    @Override
    public Future<RpcResult<GetAsyncOutput>> getAsync(GetAsyncInput input, SwitchConnectionDistinguisher cookie) {
        try {
            return getConnectionAdapter(cookie).getAsync(input);
        } catch (ConnectionException e) {
            return getRpcErrorFuture(e);
        }
    }

    @Override
    public Future<RpcResult<GetConfigOutput>> getConfig(GetConfigInput input, SwitchConnectionDistinguisher cookie) {
        try {
            return getConnectionAdapter(cookie).getConfig(input);
        } catch (ConnectionException e) {
            return getRpcErrorFuture(e);
        }
    }

    @Override
    public Future<RpcResult<GetFeaturesOutput>> getFeatures(GetFeaturesInput input, SwitchConnectionDistinguisher cookie) {
        try {
            return getConnectionAdapter(cookie).getFeatures(input);
        } catch (ConnectionException e) {
            return getRpcErrorFuture(e);
        }
    }

    @Override
    public Future<RpcResult<GetQueueConfigOutput>> getQueueConfig(GetQueueConfigInput input,
                                                                  SwitchConnectionDistinguisher cookie) {
        try {
            return getConnectionAdapter(cookie).getQueueConfig(input);
        } catch (ConnectionException e) {
            return getRpcErrorFuture(e);
        }
    }

    @Override
    public Future<RpcResult<UpdateGroupOutput>> groupMod(final GroupModInput input, SwitchConnectionDistinguisher cookie) {
        LOG.debug("Calling OFLibrary groupMod");
        Future<RpcResult<Void>> response = null;
        try {
            response = getConnectionAdapter(cookie).groupMod(input);
        } catch (ConnectionException e) {
            return getRpcErrorFuture(e);
        }

        // appending xid
        ListenableFuture<RpcResult<UpdateGroupOutput>> xidResult = Futures.transform(
                JdkFutureAdapters.listenInPoolThread(response),
                new Function<RpcResult<Void>, RpcResult<UpdateGroupOutput>>() {

                    @Override
                    public RpcResult<UpdateGroupOutput> apply(final RpcResult<Void> inputArg) {
                        UpdateGroupOutputBuilder groupModOutput = new UpdateGroupOutputBuilder();
                        BigInteger bigIntXid = BigInteger.valueOf(input.getXid());
                        groupModOutput.setTransactionId(new TransactionId(bigIntXid));

                        UpdateGroupOutput result = groupModOutput.build();
                        RpcResult<UpdateGroupOutput> rpcResult = Rpcs.getRpcResult(
                                inputArg.isSuccessful(), result, inputArg.getErrors());
                        return rpcResult;
                    }
                });

        return xidResult;
    }

    @Override
    public Future<RpcResult<UpdateMeterOutput>> meterMod(final MeterModInput input, SwitchConnectionDistinguisher cookie) {
        LOG.debug("Calling OFLibrary meterMod");
        Future<RpcResult<Void>> response = null;
        try {
            response = getConnectionAdapter(cookie).meterMod(input);
        } catch (ConnectionException e) {
            return getRpcErrorFuture(e);
        }

        // appending xid
        ListenableFuture<RpcResult<UpdateMeterOutput>> xidResult = Futures.transform(
                JdkFutureAdapters.listenInPoolThread(response),
                new Function<RpcResult<Void>, RpcResult<UpdateMeterOutput>>() {

                    @Override
                    public RpcResult<UpdateMeterOutput> apply(final RpcResult<Void> inputArg) {
                        UpdateMeterOutputBuilder meterModOutput = new UpdateMeterOutputBuilder();
                        BigInteger bigIntXid = BigInteger.valueOf(input.getXid());
                        meterModOutput.setTransactionId(new TransactionId(bigIntXid));

                        UpdateMeterOutput result = meterModOutput.build();
                        RpcResult<UpdateMeterOutput> rpcResult = Rpcs.getRpcResult(
                                inputArg.isSuccessful(), result, inputArg.getErrors());
                        return rpcResult;
                    }
                });

        return xidResult;
    }

    @Override
    public Future<RpcResult<java.lang.Void>> multipartRequest(MultipartRequestInput input, SwitchConnectionDistinguisher cookie) {
        try {
            return getConnectionAdapter(cookie).multipartRequest(input);
        } catch (ConnectionException e) {
            return getRpcErrorFuture(e);
        }
    }

    @Override
    public Future<RpcResult<Void>> packetOut(PacketOutInput input, SwitchConnectionDistinguisher cookie) {
        try {
            return getConnectionAdapter(cookie).packetOut(input);
        } catch (ConnectionException e) {
            return getRpcErrorFuture(e);
        }
    }

    @Override
    public Future<RpcResult<UpdatePortOutput>> portMod(final PortModInput input, SwitchConnectionDistinguisher cookie) {
        LOG.debug("Calling OFLibrary portMod");
        Future<RpcResult<Void>> response = null;
        try {
            response = getConnectionAdapter(cookie).portMod(input);
        } catch (ConnectionException e) {
            return getRpcErrorFuture(e);
        }

        // appending xid
        ListenableFuture<RpcResult<UpdatePortOutput>> xidResult = Futures.transform(
                JdkFutureAdapters.listenInPoolThread(response),
                new Function<RpcResult<Void>, RpcResult<UpdatePortOutput>>() {

                    @Override
                    public RpcResult<UpdatePortOutput> apply(final RpcResult<Void> inputArg) {
                        UpdatePortOutputBuilder portModOutput = new UpdatePortOutputBuilder();
                        BigInteger bigIntXid = BigInteger.valueOf(input.getXid());
                        portModOutput.setTransactionId(new TransactionId(bigIntXid));

                        UpdatePortOutput result = portModOutput.build();
                        RpcResult<UpdatePortOutput> rpcResult = Rpcs.getRpcResult(
                                inputArg.isSuccessful(), result, inputArg.getErrors());
                        return rpcResult;
                    }
                });

        return xidResult;
    }

    @Override
    public Future<RpcResult<RoleRequestOutput>> roleRequest(RoleRequestInput input, SwitchConnectionDistinguisher cookie) {

        try {
            return getConnectionAdapter(cookie).roleRequest(input);
        } catch (ConnectionException e) {
            return getRpcErrorFuture(e);
        }
    }

    @Override
    public Future<RpcResult<Void>> setAsync(SetAsyncInput input, SwitchConnectionDistinguisher cookie) {
        try {
            return getConnectionAdapter(cookie).setAsync(input);
        } catch (ConnectionException e) {
            return getRpcErrorFuture(e);
        }
    }

    @Override
    public Future<RpcResult<Void>> setConfig(SetConfigInput input, SwitchConnectionDistinguisher cookie) {
        try {
            return getConnectionAdapter(cookie).setConfig(input);
        } catch (ConnectionException e) {
            return getRpcErrorFuture(e);
        }
    }

    @Override
    public Future<RpcResult<Void>> tableMod(TableModInput input, SwitchConnectionDistinguisher cookie) {
        try {
            return getConnectionAdapter(cookie).tableMod(input);
        } catch (ConnectionException e) {
            return getRpcErrorFuture(e);
        }
    }
}
