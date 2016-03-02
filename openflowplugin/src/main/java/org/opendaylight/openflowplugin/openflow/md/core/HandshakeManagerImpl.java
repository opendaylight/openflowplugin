/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.api.openflow.md.core.ErrorHandler;
import org.opendaylight.openflowplugin.api.openflow.md.core.HandshakeListener;
import org.opendaylight.openflowplugin.api.openflow.md.core.HandshakeManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.Elements;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

/**
 * @author mirehak
 *
 */
public class HandshakeManagerImpl implements HandshakeManager {

    private static final long activeXID = 20L;

    private static final Logger LOG = LoggerFactory
            .getLogger(HandshakeManagerImpl.class);

    private Short lastProposedVersion;
    private Short lastReceivedVersion;
    private final List<Short> versionOrder;


    private final ConnectionAdapter connectionAdapter;
    private Short version;
    private ErrorHandler errorHandler;



    private Short highestVersion;

    private Long activeXid;

    private HandshakeListener handshakeListener;

    private boolean useVersionBitmap;

    /**
     * @param connectionAdapter connection adaptor for switch
     * @param highestVersion highest openflow version
     * @param versionOrder list of version in order for connection protocol negotiation
     */
    public HandshakeManagerImpl(ConnectionAdapter connectionAdapter, Short highestVersion,
            List<Short> versionOrder) {
        this.highestVersion = highestVersion;
        this.versionOrder = versionOrder;
        this.connectionAdapter = connectionAdapter;
    }

    @Override
    public void setHandshakeListener(HandshakeListener handshakeListener) {
        this.handshakeListener = handshakeListener;
    }

    @Override
    public synchronized void shake(HelloMessage receivedHello) {

        if (version != null) {
            // Some switches respond with a second HELLO acknowledging our HELLO
            // but we've already completed the handshake based on the negotiated
            // version and have registered this switch.
            LOG.debug("Hello recieved after handshake already settled ... ignoring.");
            return;
        }

        LOG.trace("handshake STARTED");
        setActiveXid(activeXID);

        try {
            if (receivedHello == null) {
                // first Hello sending
                sendHelloMessage(highestVersion, getNextXid());
                lastProposedVersion = highestVersion;
                LOG.trace("ret - firstHello+wait");
                return;
            }

            // process the 2. and later hellos
            Short remoteVersion = receivedHello.getVersion();
            List<Elements> elements = receivedHello.getElements();
            setActiveXid(receivedHello.getXid());
            List<Boolean> remoteVersionBitmap = MessageFactory.digVersions(elements);
            LOG.debug("Hello message: version={}, xid={}, bitmap={}", remoteVersion,
                    receivedHello.getXid(), remoteVersionBitmap);

            if (useVersionBitmap && remoteVersionBitmap != null) {
                // versionBitmap on both sides -> ONE STEP DECISION
                handleVersionBitmapNegotiation(elements);
            } else {
                // versionBitmap missing at least on one side -> STEP-BY-STEP NEGOTIATION applying
                handleStepByStepVersionNegotiation(remoteVersion);
            }
        } catch (Exception ex) {
            errorHandler.handleException(ex, null);
            LOG.trace("ret - shake fail - closing");
            handshakeListener.onHandshakeFailure();
        }
    }

    /**
     * @param remoteVersion remote version
     * @throws Exception exception
     */
    private void handleStepByStepVersionNegotiation(final Short remoteVersion) throws Exception {
        LOG.debug("remoteVersion:{} lastProposedVersion:{}, highestVersion:{}",
                remoteVersion, lastProposedVersion, highestVersion);

        if (lastProposedVersion == null) {
            // first hello has not been sent yet, send it and either wait for next remote
            // version or proceed
            lastProposedVersion = proposeNextVersion(remoteVersion);
            final Long nextHelloXid = getNextXid();
            ListenableFuture<Void> helloResult = sendHelloMessage(lastProposedVersion, nextHelloXid);
            Futures.addCallback(helloResult, new FutureCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    try {
                        stepByStepVersionSubStep(remoteVersion, lastProposedVersion);
                    } catch (Exception e) {
                        errorHandler.handleException(e, null);
                        handshakeListener.onHandshakeFailure();
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    LOG.info("hello sending seriously failed [{}]", nextHelloXid);
                    LOG.trace("detail of hello send problem", t);
                }
            });
        } else {
            stepByStepVersionSubStep(remoteVersion, lastProposedVersion);
        }
    }

    private void stepByStepVersionSubStep(Short remoteVersion, Short lastProposedVersion) throws Exception {
        if (remoteVersion.equals(lastProposedVersion)) {
            postHandshake(lastProposedVersion, getNextXid());
            LOG.trace("ret - OK - switch answered with lastProposedVersion");
        } else {
            checkNegotiationStalling(remoteVersion);

            if (remoteVersion > (lastProposedVersion == null ? highestVersion : this.lastProposedVersion)) {
                // wait for next version
                LOG.trace("ret - wait");
            } else {
                //propose lower version
                handleLowerVersionProposal(remoteVersion);
            }
        }
    }

    /**
     * @param remoteVersion remote version
     * @throws Exception exception
     */
    private void handleLowerVersionProposal(Short remoteVersion) throws Exception {
        Short proposedVersion;
        // find the version from header version field
        proposedVersion = proposeNextVersion(remoteVersion);
        lastProposedVersion = proposedVersion;
        sendHelloMessage(proposedVersion, getNextXid());

        if (! Objects.equals(proposedVersion, remoteVersion)) {
            LOG.trace("ret - sent+wait");
        } else {
            LOG.trace("ret - sent+OK");
            postHandshake(proposedVersion, getNextXid());
        }
    }

    /**
     * @param elements version elements
     * @throws Exception exception
     */
    private void handleVersionBitmapNegotiation(List<Elements> elements) throws Exception {
        final Short proposedVersion = proposeCommonBitmapVersion(elements);
        if (lastProposedVersion == null) {
            // first hello has not been sent yet
            Long nexHelloXid = getNextXid();
            ListenableFuture<Void> helloDone = sendHelloMessage(proposedVersion, nexHelloXid);
            Futures.addCallback(helloDone, new FutureCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    LOG.trace("ret - DONE - versionBitmap");
                    postHandshake(proposedVersion, getNextXid());
                }

                @Override
                public void onFailure(Throwable t) {
                    // NOOP
                }
            });
            LOG.trace("next proposal [{}] with versionBitmap hooked ..", nexHelloXid);
        } else {
            LOG.trace("ret - DONE - versionBitmap");
            postHandshake(proposedVersion, getNextXid());
        }
    }

    /**
     *
     * @return next tx id
     */
    private Long getNextXid() {
        activeXid += 1;
        return activeXid;
    }

    /**
     * @param xid tx id
     */
    private void setActiveXid(Long xid) {
        this.activeXid = xid;
    }

    /**
     * @param remoteVersion remove version
     */
    private void checkNegotiationStalling(Short remoteVersion) {
        if (lastReceivedVersion != null && lastReceivedVersion.equals(remoteVersion)) {
            throw new IllegalStateException("version negotiation stalled: version = "+remoteVersion);
        }
        lastReceivedVersion = remoteVersion;
    }

    @Override
    public Short getVersion() {
        return version;
    }

    /**
     * find common highest supported bitmap version
     * @param list bitmap list
     * @return proposed bitmap value
     */
    protected Short proposeCommonBitmapVersion(List<Elements> list) {
        Short supportedHighestVersion = null;
        if((null != list) && (0 != list.size())) {
            for(Elements element : list) {
                List<Boolean> bitmap = element.getVersionBitmap();
                // check for version bitmap
                for(short bitPos : ConnectionConductor.versionOrder) {
                    // with all the version it should work.
                    if(bitmap.get(bitPos % Integer.SIZE)) {
                        supportedHighestVersion = bitPos;
                        break;
                    }
                }
            }

            if(null == supportedHighestVersion) {
                LOG.trace("versionBitmap: no common version found");
                throw new IllegalArgumentException("no common version found in versionBitmap");
            }
        }

        return supportedHighestVersion;
    }

    /**
     * find supported version based on remoteVersion
     * @param remoteVersion openflow version supported by remote entity
     * @return openflow version
     */
    protected short proposeNextVersion(short remoteVersion) {
        Short proposal = null;
        for (short offer : versionOrder) {
            if (offer <= remoteVersion) {
                proposal = offer;
                break;
            }
        }
        if (proposal == null) {
            throw new IllegalArgumentException("no equal or lower version found, unsupported version: "
                    + remoteVersion);
        }
        return proposal;
    }

    /**
     * send hello reply without versionBitmap
     * @param helloVersion initial hello version for openflow connection negotiation
     * @param helloXid transaction id
     * @throws Exception
     */
    private ListenableFuture<Void> sendHelloMessage(Short helloVersion, final Long helloXid) throws Exception {


        HelloInput helloInput = MessageFactory.createHelloInput(helloVersion, helloXid, versionOrder);

        final SettableFuture<Void> resultFtr = SettableFuture.create();

        LOG.debug("sending hello message: version{}, xid={}, version bitmap={}",
                helloVersion, helloXid, MessageFactory.digVersions(helloInput.getElements()));

        Future<RpcResult<Void>> helloResult = connectionAdapter.hello(helloInput);

        ListenableFuture<RpcResult<Void>> rpcResultListenableFuture = JdkFutureAdapters.listenInPoolThread(helloResult);
        Futures.addCallback(rpcResultListenableFuture, new FutureCallback<RpcResult<Void>>() {
            @Override
            public void onSuccess(RpcResult<Void> result) {
                if (result.isSuccessful()) {
                    LOG.debug("hello successfully sent, xid={}, addr={}", helloXid, connectionAdapter.getRemoteAddress());
                    resultFtr.set(null);
                } else {
                    for (RpcError error : result.getErrors()) {
                        LOG.debug("hello sending failed [{}]: i:{} s:{} m:{}, addr:{}", helloXid,
                                error.getInfo(), error.getSeverity(), error.getMessage(),
                                connectionAdapter.getRemoteAddress());
                        if (error.getCause() != null) {
                            LOG.trace("DETAIL of sending hello failure", error.getCause());
                        }
                    }
                    resultFtr.cancel(false);
                    handshakeListener.onHandshakeFailure();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                LOG.warn("sending of hello failed seriously [{}, addr:{}]: {}", helloXid,
                        connectionAdapter.getRemoteAddress(), t.getMessage());
                LOG.trace("DETAIL of sending of hello failure:", t);
                resultFtr.cancel(false);
                handshakeListener.onHandshakeFailure();
            }
        });
        LOG.trace("sending hello message [{}] - result hooked ..", helloXid);
        return resultFtr;
    }


    /**
     * after handshake set features, register to session
     * @param proposedVersion proposed openflow version
     * @param xid transaction id
     */
    protected void postHandshake(final Short proposedVersion, final Long xid) {
        // set version
        version = proposedVersion;

        LOG.debug("version set: {}", proposedVersion);
        // request features
        GetFeaturesInputBuilder featuresBuilder = new GetFeaturesInputBuilder();
        featuresBuilder.setVersion(version).setXid(xid);
        LOG.debug("sending feature request for version={} and xid={}", version, xid);
        Future<RpcResult<GetFeaturesOutput>> featuresFuture = connectionAdapter
                .getFeatures(featuresBuilder.build());

        Futures.addCallback(JdkFutureAdapters.listenInPoolThread(featuresFuture),
                new FutureCallback<RpcResult<GetFeaturesOutput>>() {
                    @Override
                    public void onSuccess(RpcResult<GetFeaturesOutput> rpcFeatures) {
                        LOG.trace("features are back");
                        if (rpcFeatures.isSuccessful()) {
                            GetFeaturesOutput featureOutput = rpcFeatures.getResult();

                            LOG.debug("obtained features: datapathId={}",
                                    featureOutput.getDatapathId());
                            LOG.debug("obtained features: auxiliaryId={}",
                                    featureOutput.getAuxiliaryId());
                            LOG.trace("handshake SETTLED: version={}, datapathId={}, auxiliaryId={}",
                                    version, featureOutput.getDatapathId(), featureOutput.getAuxiliaryId());
                            handshakeListener.onHandshakeSuccessful(featureOutput, proposedVersion);
                        } else {
                            // handshake failed
                            LOG.warn("issuing disconnect during handshake [{}]", connectionAdapter.getRemoteAddress());
                            for (RpcError rpcError : rpcFeatures.getErrors()) {
                                LOG.debug("handshake - features failure [{}]: i:{} | m:{} | s:{}", xid,
                                        rpcError.getInfo(), rpcError.getMessage(), rpcError.getSeverity(),
                                        rpcError.getCause()
                                );
                            }
                            handshakeListener.onHandshakeFailure();
                        }

                        LOG.debug("postHandshake DONE");
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        LOG.warn("getting feature failed seriously [{}, addr:{}]: {}", xid,
                                connectionAdapter.getRemoteAddress(), t.getMessage());
                        LOG.trace("DETAIL of sending of hello failure:", t);
                    }
                });

        LOG.debug("future features [{}] hooked ..", xid);

    }

    @Override
    public void setUseVersionBitmap(boolean useVersionBitmap) {
        this.useVersionBitmap = useVersionBitmap;
    }

    @Override
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
}
