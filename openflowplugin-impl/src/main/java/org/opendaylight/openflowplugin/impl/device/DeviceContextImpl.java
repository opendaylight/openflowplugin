/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceReplyProcessor;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.device.exception.DeviceDataException;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.impl.device.translator.PacketReceivedTranslator;
import org.opendaylight.openflowplugin.impl.device.translator.PortUpdateTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.session.SwitchConnectionCookieOFImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Error;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableFeatures;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DeviceContextImpl implements DeviceContext, DeviceReplyProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceContextImpl.class);

    private final ConnectionContext primaryConnectionContext;
    private final DeviceState deviceState;
    private final DataBroker dataBroker;
    private final XidGenerator xidGenerator;
    private final HashedWheelTimer hashedWheelTimer;
    private Map<Long, RequestContext> requests = new HashMap<Long, RequestContext>();

    private final Map<SwitchConnectionDistinguisher, ConnectionContext> auxiliaryConnectionContexts;
    private final TransactionChainManager txChainManager;
    private TranslatorLibrary translatorLibrary;

    @VisibleForTesting
    DeviceContextImpl(@Nonnull final ConnectionContext primaryConnectionContext,
                      @Nonnull final DeviceState deviceState, @Nonnull final DataBroker dataBroker,
                      @Nonnull final HashedWheelTimer hashedWheelTimer) {
        this.primaryConnectionContext = Preconditions.checkNotNull(primaryConnectionContext);
        this.deviceState = Preconditions.checkNotNull(deviceState);
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        this.hashedWheelTimer = Preconditions.checkNotNull(hashedWheelTimer);
        xidGenerator = new XidGenerator();
        txChainManager = new TransactionChainManager(dataBroker, 500L);
        auxiliaryConnectionContexts = new HashMap<>();
        requests = new HashMap<>();
    }

    /**
     * This method is called from {@link DeviceManagerImpl} only. So we could say "posthandshake process finish"
     * and we are able to set a scheduler for an automatic transaction submitting by time (0,5sec).
     */
    void submitTransaction() {
        txChainManager.submitTransaction();
        hashedWheelTimer.newTimeout(new TimerTask() {
            @Override
            public void run(final Timeout timeout) throws Exception {
                submitTransaction();
            }
        }, 0, TimeUnit.MILLISECONDS);
    }

    @Override
    public <M extends ChildOf<DataObject>> void onMessage(final M message, final RequestContext requestContext) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addAuxiliaryConenctionContext(final ConnectionContext connectionContext) {
        final SwitchConnectionDistinguisher connectionDistinguisher = new SwitchConnectionCookieOFImpl(connectionContext.getFeatures().getAuxiliaryId());
        auxiliaryConnectionContexts.put(connectionDistinguisher, connectionContext);
    }

    @Override
    public void removeAuxiliaryConenctionContext(final ConnectionContext connectionContext) {
        // TODO Auto-generated method stub
    }

    @Override
    public DeviceState getDeviceState() {
        return deviceState;
    }

    @Override
    public ReadTransaction getReadTransaction() {
        return dataBroker.newReadOnlyTransaction();
    }

    @Override
    public <T extends DataObject> void writeToTransaction(final LogicalDatastoreType store,
            final InstanceIdentifier<T> path, final T data) {
        txChainManager.writeToTransaction(store, path, data);
    }

    @Override
    public TableFeatures getCapabilities() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConnectionContext getPrimaryConnectionContext() {
        return primaryConnectionContext;
    }

    @Override
    public ConnectionContext getAuxiliaryConnectiobContexts(final BigInteger cookie) {
        return auxiliaryConnectionContexts.get(new SwitchConnectionCookieOFImpl(cookie.longValue()));
    }

    @Override
    public Xid getNextXid() {
        return xidGenerator.generate();
    }

    public Map<Long, RequestContext> getRequests() {
        return requests;
    }

    @Override
    public void hookRequestCtx(final Xid xid, final RequestContext requestFutureContext) {
        // TODO Auto-generated method stub
        requests.put(xid.getValue(), requestFutureContext);
    }

    @Override
    public void processReply(final OfHeader ofHeader) {
        final RequestContext requestContext = getRequests().get(ofHeader.getXid());
        final SettableFuture replyFuture = requestContext.getFuture();
        getRequests().remove(ofHeader.getXid());
        RpcResult<OfHeader> rpcResult;

        if(ofHeader instanceof Error) {
            final Error error = (Error) ofHeader;
            final String message = "Operation on device failed";
            rpcResult= RpcResultBuilder
                    .<OfHeader>failed()
                    .withError(RpcError.ErrorType.APPLICATION, message, new DeviceDataException(message, error))
                    .build();
        } else {
            rpcResult = RpcResultBuilder
                    .<OfHeader>success()
                    .withResult(ofHeader)
                    .build();
        }

        replyFuture.set(rpcResult);
        try {
            requestContext.close();
        } catch (final Exception e) {
            LOG.error("Closing RequestContext failed: ", e);
        }
    }

    @Override
    public void processReply(final Xid xid, final List<MultipartReply> ofHeaderList) {
        final RequestContext requestContext = getRequests().get(xid.getValue());
        final SettableFuture replyFuture = requestContext.getFuture();
        getRequests().remove(xid.getValue());
        final RpcResult<List<MultipartReply>> rpcResult= RpcResultBuilder
                                                .<List<MultipartReply>>success()
                                                .withResult(ofHeaderList)
                                                .build();
        replyFuture.set(rpcResult);
        try {
            requestContext.close();
        } catch (final Exception e) {
            LOG.error("Closing RequestContext failed: ", e);
        }
    }

    @Override
    public void processException(final Xid xid, final DeviceDataException deviceDataException) {
        final RequestContext requestContext = getRequests().get(xid.getValue());

        final SettableFuture replyFuture = requestContext.getFuture();
        getRequests().remove(xid.getValue());
        final RpcResult<List<OfHeader>> rpcResult= RpcResultBuilder
                .<List<OfHeader>>failed()
                .withError(RpcError.ErrorType.APPLICATION, "Message processing failed", deviceDataException)
                .build();
        replyFuture.set(rpcResult);
        try {
            requestContext.close();
        } catch (final Exception e) {
            LOG.error("Closing RequestContext failed: ", e);
        }
    }

    @Override
    public void processFlowRemovedMessage(final FlowRemoved flowRemoved) {
        //TODO: will be defined later
    }

    @Override
    public void processPortStatusMessage(final PortStatusMessage portStatus) {
        final TranslatorKey translatorKey = new TranslatorKey(portStatus.getVersion(), PortUpdateTranslator.class.getName());
        final MessageTranslator<PortStatusMessage, FlowCapableNodeConnector> messageTranslator = translatorLibrary.lookupTranslator(translatorKey);
        final FlowCapableNodeConnector nodeConnector = messageTranslator.translate(portStatus, this, null);
        //TODO write into datastore
    }

    @Override
    public void processPacketInMessage(final PacketInMessage packetInMessage) {
        final TranslatorKey translatorKey = new TranslatorKey(packetInMessage.getVersion(), PacketReceivedTranslator.class.getName());
        final MessageTranslator<PacketInMessage, PacketReceived> messageTranslator = translatorLibrary.lookupTranslator(translatorKey);
        final PacketReceived packetReceived = messageTranslator.translate(packetInMessage, this, null);
        //TODO publish to MD-SAL
    }

    public void setTranslatorLibrary(final TranslatorLibrary translatorLibrary) {
        this.translatorLibrary = translatorLibrary;
    }


    private class XidGenerator {

        private final AtomicLong xid = new AtomicLong(0);

        public Xid generate() {
            return new Xid(xid.incrementAndGet());
        }
    }
}
