/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.ConnectionException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.Elements;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mirehak
 *
 */
public class HandshakeManagerImpl implements HandshakeManager {
    
    private static final Logger LOG = LoggerFactory
            .getLogger(HandshakeManagerImpl.class);
    
    private Short lastProposedVersion;
    private Short lastReceivedVersion;
    private final List<Short> versionOrder;
    
    private HelloMessage receivedHello;
    private final ConnectionAdapter connectionAdapter;
    private GetFeaturesOutput features;
    private Short version;
    private ErrorHandler errorHandler;
    
    private long maxTimeout = 8000;
    private TimeUnit maxTimeoutUnit = TimeUnit.MILLISECONDS;
    private Short highestVersion;

    private Long activeXid;

    private HandshakeListener handshakeListener;

    private boolean useVersionBitmap;
    
    @Override
    public void setReceivedHello(HelloMessage receivedHello) {
        this.receivedHello = receivedHello;
    }
    
    /**
     * @param connectionAdapter 
     * @param highestVersion 
     * @param versionOrder
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
    public void shake() {

        if (version != null) {
            // Some switches respond with a second HELLO acknowledging our HELLO
            // but we've already completed the handshake based on the negotiated
            // version and have registered this switch.
            LOG.debug("Hello recieved after handshake already settled ... ignoring.");
            return;
        }

        LOG.trace("handshake STARTED");
        setActiveXid(20L);
        HelloMessage receivedHelloLoc = receivedHello;
        
        try {
            if (receivedHelloLoc == null) {
                // first Hello sending
                sendHelloMessage(highestVersion, getNextXid());
                lastProposedVersion = highestVersion;
                LOG.trace("ret - firstHello+wait");
                return;
            }

            // process the 2. and later hellos
            Short remoteVersion = receivedHelloLoc.getVersion();
            List<Elements> elements = receivedHelloLoc.getElements();
            setActiveXid(receivedHelloLoc.getXid());
            List<Boolean> remoteVersionBitmap = MessageFactory.digVersions(elements);
            LOG.debug("Hello message: version={}, bitmap={}, xid={}", remoteVersion, 
                    remoteVersionBitmap, receivedHelloLoc.getXid());
        
            if (useVersionBitmap && remoteVersionBitmap != null) {
                // versionBitmap on both sides -> ONE STEP DECISION
                handleVersionBitmapNegotiation(elements);
            } else { 
                // versionBitmap missing at least on one side -> STEP-BY-STEP NEGOTIATION applying 
                handleStepByStepVersionNegotiation(remoteVersion);
            }
        } catch (Exception ex) {
            errorHandler.handleException(ex, null);
            connectionAdapter.disconnect();
            LOG.trace("ret - shake fail: {}", ex.getMessage());
        }
    }

    /**
     * @param remoteVersion
     * @throws Exception 
     */
    private void handleStepByStepVersionNegotiation(Short remoteVersion) throws Exception {
        LOG.debug("remoteVersion:{} lastProposedVersion:{}, highestVersion:{}", 
                remoteVersion, lastProposedVersion, highestVersion);
        
        if (lastProposedVersion == null) {
            // first hello has not been sent yet, send it and either wait for next remote 
            // version or proceed
            lastProposedVersion = proposeNextVersion(remoteVersion);
            sendHelloMessage(lastProposedVersion, getNextXid());
        }
        
        if (remoteVersion == lastProposedVersion) {
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
     * @param remoteVersion
     * @throws Exception 
     */
    private void handleLowerVersionProposal(Short remoteVersion) throws Exception {
        Short proposedVersion;
        // find the version from header version field
        proposedVersion = proposeNextVersion(remoteVersion);
        lastProposedVersion = proposedVersion;
        sendHelloMessage(proposedVersion, getNextXid());

        if (proposedVersion != remoteVersion) {
            LOG.trace("ret - sent+wait");
        } else {
            LOG.trace("ret - sent+OK");
            postHandshake(proposedVersion, getNextXid());
        }
    }

    /**
     * @param elements
     * @throws Exception 
     */
    private void handleVersionBitmapNegotiation(List<Elements> elements) throws Exception {
        Short proposedVersion;
        proposedVersion = proposeCommonBitmapVersion(elements);
        if (lastProposedVersion == null) {
            // first hello has not been sent yet
            sendHelloMessage(proposedVersion, getNextXid());
        }
        postHandshake(proposedVersion, getNextXid());
        LOG.trace("ret - OK - versionBitmap");
    }
    
    /**
     * 
     * @return
     */
    private Long getNextXid() {
        activeXid += 1; 
        return activeXid;
    }

    /**
     * @param xid
     */
    private void setActiveXid(Long xid) {
        this.activeXid = xid;
    }
    
    /**
     * @param remoteVersion
     */
    private void checkNegotiationStalling(Short remoteVersion) {
        if (lastReceivedVersion != null && lastReceivedVersion.equals(remoteVersion)) {
            throw new IllegalStateException("version negotiation stalled: version = "+remoteVersion);
        }
        lastReceivedVersion = remoteVersion;
    }

    @Override
    public GetFeaturesOutput getFeatures() {
        return features;
    }
    
    @Override
    public Short getVersion() {
        return version;
    }

    /**
     * find common highest supported bitmap version
     * @param list
     * @return
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
                throw new IllegalArgumentException("no common version found in versionBitmap");
            }
        }

        return supportedHighestVersion;
    }

    /**
     * find supported version based on remoteVersion
     * @param remoteVersion
     * @return
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
     * @param helloVersion
     * @param helloXid
     * @throws Exception 
     */
    private void sendHelloMessage(Short helloVersion, Long helloXid) throws Exception {
        //Short highestVersion = ConnectionConductor.versionOrder.get(0);
        //final Long helloXid = 21L;
        HelloInput helloInput = MessageFactory.createHelloInput(helloVersion, helloXid, versionOrder);
        
        LOG.debug("sending hello message: version{}, xid={}, version bitmap={}", 
                helloVersion, helloXid, MessageFactory.digVersions(helloInput.getElements()));
        
        try {
            RpcResult<Void> helloResult = connectionAdapter.hello(helloInput).get(maxTimeout, maxTimeoutUnit);
            RpcUtil.smokeRpc(helloResult);
            LOG.debug("FIRST HELLO sent.");
        } catch (Exception e) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("FIRST HELLO sent.", e);
            }
            throw new ConnectionException("FIRST HELLO sending failed because of connection issue.");
        }
    }


    /**
     * after handshake set features, register to session
     * @param proposedVersion
     * @param xId
     * @throws Exception 
     */
    protected void postHandshake(Short proposedVersion, Long xid) throws Exception {
        // set version
        version = proposedVersion;

        LOG.debug("version set: {}", proposedVersion);
        // request features
        GetFeaturesInputBuilder featuresBuilder = new GetFeaturesInputBuilder();
        featuresBuilder.setVersion(version).setXid(xid);
        LOG.debug("sending feature request for version={} and xid={}", version, xid);
        Future<RpcResult<GetFeaturesOutput>> featuresFuture = connectionAdapter
                .getFeatures(featuresBuilder.build());
        LOG.debug("waiting for features");
        try {
            RpcResult<GetFeaturesOutput> rpcFeatures = 
                    featuresFuture.get(maxTimeout, maxTimeoutUnit);
            RpcUtil.smokeRpc(rpcFeatures);
            
            GetFeaturesOutput featureOutput =  rpcFeatures.getResult();
            
            LOG.debug("obtained features: datapathId={}",
                    featureOutput.getDatapathId());
            LOG.debug("obtained features: auxiliaryId={}",
                    featureOutput.getAuxiliaryId());
            LOG.trace("handshake SETTLED: version={}, datapathId={}, auxiliaryId={}", 
                    version, featureOutput.getDatapathId(), featureOutput.getAuxiliaryId());
            
            handshakeListener.onHandshakeSuccessfull(featureOutput, proposedVersion);
        } catch (TimeoutException e) {
            // handshake failed
            LOG.warn("issuing disconnect during handshake, reason: future expired", e);
            connectionAdapter.disconnect();
            throw e;
        } catch (Exception e) {
            // handshake failed
            LOG.warn("issuing disconnect during handshake, reason - RPC: {}", e.getMessage(), e);
            connectionAdapter.disconnect();
            throw e;
        }
        
        LOG.debug("postHandshake DONE");
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
