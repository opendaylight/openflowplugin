/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.connection;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.DeviceConnectionStatusProvider;
import org.opendaylight.openflowplugin.api.openflow.md.core.ErrorHandler;
import org.opendaylight.openflowplugin.api.openflow.md.core.HandshakeListener;
import org.opendaylight.openflowplugin.api.openflow.md.core.HandshakeManager;
import org.opendaylight.openflowplugin.impl.common.DeviceConnectionRateLimiter;
import org.opendaylight.openflowplugin.impl.util.MessageFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.Elements;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandshakeManagerImpl implements HandshakeManager {
    private static final Logger LOG = LoggerFactory.getLogger(HandshakeManagerImpl.class);
    private static final long ACTIVE_XID = 20L;

    private Short lastProposedVersion;
    private Short lastReceivedVersion;
    private final List<Short> versionOrder;

    private final ConnectionAdapter connectionAdapter;
    private Short version;
    private final ErrorHandler errorHandler;

    private final Short highestVersion;

    private Long activeXid;

    private final HandshakeListener handshakeListener;

    // not final just for unit test
    private boolean useVersionBitmap;

    private final DeviceConnectionRateLimiter deviceConnectionRateLimiter;
    private final int deviceConnectionHoldTime;
    private final DeviceConnectionStatusProvider deviceConnectionStatusProvider;

    /**
     * Constructor.
     *
     * @param connectionAdapter connection adaptor for switch
     * @param highestVersion    highest openflow version
     * @param versionOrder      list of version in order for connection protocol negotiation
     * @param errorHandler      the ErrorHandler
     * @param handshakeListener the HandshakeListener
     * @param useVersionBitmap  should use negotiation bit map
     * @param deviceConnectionRateLimiter  device connection rate limiter utility
     * @param deviceConnectionHoldTime  deivce connection hold time in seconds
     * @param deviceConnectionStatusProvider  utility for maintaining device connection states
     */
    public HandshakeManagerImpl(final ConnectionAdapter connectionAdapter, final Short highestVersion,
                                final List<Short> versionOrder, final ErrorHandler errorHandler,
                                final HandshakeListener handshakeListener, final boolean useVersionBitmap,
                                final DeviceConnectionRateLimiter deviceConnectionRateLimiter,
                                final int deviceConnectionHoldTime,
                                final DeviceConnectionStatusProvider deviceConnectionStatusProvider) {
        this.highestVersion = highestVersion;
        this.versionOrder = versionOrder;
        this.connectionAdapter = connectionAdapter;
        this.errorHandler = errorHandler;
        this.handshakeListener = handshakeListener;
        this.useVersionBitmap = useVersionBitmap;
        this.deviceConnectionRateLimiter = deviceConnectionRateLimiter;
        this.deviceConnectionHoldTime = deviceConnectionHoldTime;
        this.deviceConnectionStatusProvider = deviceConnectionStatusProvider;
    }

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public synchronized void shake(final HelloMessage receivedHello) {

        if (version != null) {
            // Some switches respond with a second HELLO acknowledging our HELLO
            // but we've already completed the handshake based on the negotiated
            // version and have registered this switch.
            LOG.debug("Hello recieved after handshake already settled ... ignoring.");
            return;
        }

        LOG.trace("handshake STARTED");
        setActiveXid(ACTIVE_XID);

        try {
            if (receivedHello == null) {
                // first Hello sending
                sendHelloMessage(highestVersion, getNextXid());
                lastProposedVersion = highestVersion;
                LOG.trace("ret - firstHello+wait");
                return;
            }

            // process the 2. and later hellos
            Uint8 remoteVersion = receivedHello.getVersion();
            List<Elements> elements = receivedHello.getElements();
            setActiveXid(receivedHello.getXid().toJava());
            List<Boolean> remoteVersionBitmap = MessageFactory.digVersions(elements);
            LOG.debug("Hello message: version={}, xid={}, bitmap={}", remoteVersion, receivedHello.getXid(),
                      remoteVersionBitmap);

            if (useVersionBitmap && remoteVersionBitmap != null) {
                // versionBitmap on both sides -> ONE STEP DECISION
                handleVersionBitmapNegotiation(elements);
            } else {
                // versionBitmap missing at least on one side -> STEP-BY-STEP NEGOTIATION applying
                handleStepByStepVersionNegotiation(remoteVersion.toJava());
            }
        } catch (Exception ex) {
            errorHandler.handleException(ex);
            LOG.trace("ret - shake fail - closing");
            handshakeListener.onHandshakeFailure();
        }
    }

    /**
     * Handles the version negotiation step by step.
     *
     * @param remoteVersion remote version
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    private void handleStepByStepVersionNegotiation(final Short remoteVersion) {
        LOG.debug("remoteVersion:{} lastProposedVersion:{}, highestVersion:{}", remoteVersion, lastProposedVersion,
                  highestVersion);

        if (lastProposedVersion == null) {
            // first hello has not been sent yet, send it and either wait for next remote
            // version or proceed
            lastProposedVersion = proposeNextVersion(remoteVersion);
            final Long nextHelloXid = getNextXid();
            ListenableFuture<Void> helloResult = sendHelloMessage(lastProposedVersion, nextHelloXid);
            Futures.addCallback(helloResult, new FutureCallback<Void>() {
                @Override
                public void onSuccess(final Void result) {
                    try {
                        stepByStepVersionSubStep(remoteVersion);
                    } catch (Exception e) {
                        errorHandler.handleException(e);
                        handshakeListener.onHandshakeFailure();
                    }
                }

                @Override
                public void onFailure(final Throwable throwable) {
                    LOG.info("hello sending seriously failed [{}]", nextHelloXid);
                    LOG.trace("detail of hello send problem", throwable);
                }
            }, MoreExecutors.directExecutor());
        } else {
            stepByStepVersionSubStep(remoteVersion);
        }
    }

    private void stepByStepVersionSubStep(final Short remoteVersion) {
        if (remoteVersion >= lastProposedVersion) {
            postHandshake(lastProposedVersion, getNextXid());
            LOG.trace("ret - OK - switch answered with lastProposedVersion");
        } else {
            checkNegotiationStalling(remoteVersion);

            if (remoteVersion > (lastProposedVersion == null ? highestVersion : lastProposedVersion)) {
                // wait for next version
                LOG.trace("ret - wait");
            } else {
                //propose lower version
                handleLowerVersionProposal(remoteVersion);
            }
        }
    }

    /**
     * Handles a proposal for a lower version.
     *
     * @param remoteVersion remote version
     * @throws Exception exception
     */
    private void handleLowerVersionProposal(final Short remoteVersion) {
        Short proposedVersion;
        // find the version from header version field
        proposedVersion = proposeNextVersion(remoteVersion);
        lastProposedVersion = proposedVersion;
        sendHelloMessage(proposedVersion, getNextXid());

        if (!Objects.equals(proposedVersion, remoteVersion)) {
            LOG.trace("ret - sent+wait");
        } else {
            LOG.trace("ret - sent+OK");
            postHandshake(proposedVersion, getNextXid());
        }
    }

    /**
     * Handles the negotiation of the version bitmap.
     *
     * @param elements version elements
     * @throws Exception exception
     */
    private void handleVersionBitmapNegotiation(final List<Elements> elements) {
        final Short proposedVersion = proposeCommonBitmapVersion(elements);
        if (lastProposedVersion == null) {
            // first hello has not been sent yet
            Long nexHelloXid = getNextXid();
            ListenableFuture<Void> helloDone = sendHelloMessage(proposedVersion, nexHelloXid);
            Futures.addCallback(helloDone, new FutureCallback<Void>() {
                @Override
                public void onSuccess(final Void result) {
                    LOG.trace("ret - DONE - versionBitmap");
                    postHandshake(proposedVersion, getNextXid());
                }

                @Override
                public void onFailure(final Throwable throwable) {
                    // NOOP
                }
            }, MoreExecutors.directExecutor());
            LOG.trace("next proposal [{}] with versionBitmap hooked ..", nexHelloXid);
        } else {
            LOG.trace("ret - DONE - versionBitmap");
            postHandshake(proposedVersion, getNextXid());
        }
    }

    private Long getNextXid() {
        activeXid += 1;
        return activeXid;
    }

    private void setActiveXid(final Long xid) {
        this.activeXid = xid;
    }

    /**
     * Checks negotiation stalling.
     *
     * @param remoteVersion remove version
     */
    private void checkNegotiationStalling(final Short remoteVersion) {
        if (lastReceivedVersion != null && lastReceivedVersion.equals(remoteVersion)) {
            throw new IllegalStateException("version negotiation stalled: version = " + remoteVersion);
        }
        lastReceivedVersion = remoteVersion;
    }

    @Override
    public Short getVersion() {
        return version;
    }

    /**
     * find common highest supported bitmap version.
     *
     * @param list bitmap list
     * @return proposed bitmap value
     */
    protected Short proposeCommonBitmapVersion(final List<Elements> list) {
        Short supportedHighestVersion = null;
        if (null != list && 0 != list.size()) {
            for (Elements element : list) {
                List<Boolean> bitmap = element.getVersionBitmap();
                // check for version bitmap
                for (short bitPos : OFConstants.VERSION_ORDER) {
                    // with all the version it should work.
                    if (bitmap.get(bitPos % Integer.SIZE)) {
                        supportedHighestVersion = bitPos;
                        break;
                    }
                }
            }

            if (null == supportedHighestVersion) {
                LOG.trace("versionBitmap: no common version found");
                throw new IllegalArgumentException("no common version found in versionBitmap");
            }
        }

        return supportedHighestVersion;
    }

    /**
     * find supported version based on remoteVersion.
     *
     * @param remoteVersion openflow version supported by remote entity
     * @return openflow version
     */
    protected short proposeNextVersion(final short remoteVersion) {
        Short proposal = null;
        for (short offer : versionOrder) {
            if (offer <= remoteVersion) {
                proposal = offer;
                break;
            }
        }
        if (proposal == null) {
            throw new IllegalArgumentException(
                    "no equal or lower version found, unsupported version: " + remoteVersion);
        }
        return proposal;
    }

    /**
     * send hello reply without versionBitmap.
     *
     * @param helloVersion initial hello version for openflow connection negotiation
     * @param helloXid     transaction id
     */
    private ListenableFuture<Void> sendHelloMessage(final Short helloVersion, final Long helloXid) {


        HelloInput helloInput = MessageFactory.createHelloInput(helloVersion, helloXid, versionOrder);

        final SettableFuture<Void> resultFtr = SettableFuture.create();

        LOG.debug("sending hello message: version{}, xid={}, version bitmap={}", helloVersion, helloXid,
                  MessageFactory.digVersions(helloInput.getElements()));

        Futures.addCallback(connectionAdapter.hello(helloInput), new FutureCallback<RpcResult<HelloOutput>>() {
            @Override
            public void onSuccess(final RpcResult<HelloOutput> result) {
                if (result.isSuccessful()) {
                    LOG.debug("hello successfully sent, xid={}, addr={}", helloXid,
                              connectionAdapter.getRemoteAddress());
                    resultFtr.set(null);
                } else {
                    for (RpcError error : result.getErrors()) {
                        LOG.debug("hello sending failed [{}]: i:{} s:{} m:{}, addr:{}", helloXid, error.getInfo(),
                                  error.getSeverity(), error.getMessage(), connectionAdapter.getRemoteAddress());
                        if (error.getCause() != null) {
                            LOG.trace("DETAIL of sending hello failure", error.getCause());
                        }
                    }
                    resultFtr.cancel(false);
                    handshakeListener.onHandshakeFailure();
                }
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.warn("sending of hello failed seriously [{}, addr:{}]: {}", helloXid,
                         connectionAdapter.getRemoteAddress(), throwable.getMessage());
                LOG.trace("DETAIL of sending of hello failure:", throwable);
                resultFtr.cancel(false);
                handshakeListener.onHandshakeFailure();
            }
        }, MoreExecutors.directExecutor());
        LOG.trace("sending hello message [{}] - result hooked ..", helloXid);
        return resultFtr;
    }


    /**
     * after handshake set features, register to session.
     *
     * @param proposedVersion proposed openflow version
     * @param xid             transaction id
     */
    protected void postHandshake(final Short proposedVersion, final Long xid) {
        // set version
        version = proposedVersion;

        LOG.debug("version set: {}", proposedVersion);
        // request features
        GetFeaturesInputBuilder featuresBuilder = new GetFeaturesInputBuilder();
        featuresBuilder.setVersion(version).setXid(xid);
        LOG.debug("sending feature request for version={} and xid={}", version, xid);

        Futures.addCallback(connectionAdapter.getFeatures(featuresBuilder.build()),
                new FutureCallback<RpcResult<GetFeaturesOutput>>() {
                    @Override
                    public void onSuccess(final RpcResult<GetFeaturesOutput> rpcFeatures) {
                        LOG.trace("features are back");
                        if (rpcFeatures.isSuccessful()) {
                            GetFeaturesOutput featureOutput = rpcFeatures.getResult();

                            final Uint64 dpId = featureOutput.getDatapathId();
                            BigInteger datapathId = dpId == null ? null : dpId.toJava();
                            connectionAdapter.setDatapathId(datapathId);
                            if (datapathId == null || !isAllowedToConnect(datapathId)) {
                                connectionAdapter.disconnect();
                                return;
                            }

                            LOG.debug("obtained features: datapathId={}", featureOutput.getDatapathId());
                            LOG.debug("obtained features: auxiliaryId={}", featureOutput.getAuxiliaryId());
                            LOG.trace("handshake SETTLED: version={}, datapathId={}, auxiliaryId={}",
                                      version, featureOutput.getDatapathId(),
                                      featureOutput.getAuxiliaryId());
                            handshakeListener.onHandshakeSuccessful(featureOutput, proposedVersion);
                        } else {
                            // handshake failed
                            LOG.warn("issuing disconnect during handshake [{}]",
                                     connectionAdapter.getRemoteAddress());
                            for (RpcError rpcError : rpcFeatures.getErrors()) {
                                LOG.debug("handshake - features failure [{}]: i:{} | m:{} | s:{}", xid,
                                          rpcError.getInfo(), rpcError.getMessage(), rpcError.getSeverity(),
                                          rpcError.getCause());
                            }
                            handshakeListener.onHandshakeFailure();
                        }

                        LOG.debug("postHandshake DONE");
                    }

                    @Override
                    public void onFailure(final Throwable throwable) {
                        LOG.warn("getting feature failed seriously [{}, addr:{}]: {}", xid,
                                 connectionAdapter.getRemoteAddress(), throwable.getMessage());
                        LOG.trace("DETAIL of sending of hello failure:", throwable);
                    }
                }, MoreExecutors.directExecutor());
        LOG.debug("future features [{}] hooked ..", xid);
    }

    public boolean isAllowedToConnect(final BigInteger nodeId) {
        // The device isn't allowed for connection till device connection hold time is over
        if (deviceConnectionHoldTime > 0) {
            LocalDateTime lastConnectionTime = deviceConnectionStatusProvider.getDeviceLastConnectionTime(nodeId);
            if (lastConnectionTime == null) {
                LOG.debug("Initial connection attempt by device {} to the controller node. Allowing to connect after {}"
                        + "seconds", nodeId, deviceConnectionHoldTime);
                deviceConnectionStatusProvider.addDeviceLastConnectionTime(nodeId, LocalDateTime.now());
                return false;
            } else if (LocalDateTime.now().isBefore(lastConnectionTime.plusSeconds(deviceConnectionHoldTime))) {
                LOG.trace("Device trying to connect before the connection delay {} seconds, disconnecting the device "
                                + "{}", deviceConnectionHoldTime, nodeId);
                return false;
            }
        }

        if (!deviceConnectionRateLimiter.tryAquire()) {
            LOG.debug("Permit not acquired for device {}, disconnecting the device.", nodeId);
            connectionAdapter.disconnect();
            return false;
        }
        return true;
    }

    /**
     * Method for unit testing, only.
     * This method is not thread safe and can only safely be used from a test.
     */
    @VisibleForTesting
    @SuppressFBWarnings(value = "IS2_INCONSISTENT_SYNC", justification = "because shake() is synchronized")
    void setUseVersionBitmap(final boolean useVersionBitmap) {
        this.useVersionBitmap = useVersionBitmap;
    }
}
