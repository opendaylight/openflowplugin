/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.connection;

import java.math.BigInteger;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import com.google.common.base.Preconditions;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.mdsal.binding.api.*;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionReadyListener;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionManager;
import org.opendaylight.openflowplugin.api.openflow.connection.DpnConnectionStatusProvider;
import org.opendaylight.openflowplugin.api.openflow.connection.HandshakeContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceConnectedHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceDisconnectedHandler;
import org.opendaylight.openflowplugin.api.openflow.md.core.HandshakeListener;
import org.opendaylight.openflowplugin.api.openflow.md.core.HandshakeManager;
import org.opendaylight.openflowplugin.impl.common.DeviceConnectionRateLimiter;
import org.opendaylight.openflowplugin.impl.connection.listener.ConnectionReadyListenerImpl;
import org.opendaylight.openflowplugin.impl.connection.listener.HandshakeListenerImpl;
import org.opendaylight.openflowplugin.impl.connection.listener.OpenflowProtocolListenerInitialImpl;
import org.opendaylight.openflowplugin.impl.connection.listener.SystemNotificationsListenerImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OpenflowProtocolListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SystemNotificationsListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class ConnectionManagerImpl implements ConnectionManager {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionManagerImpl.class);
    private static final boolean BITMAP_NEGOTIATION_ENABLED = true;
    private DeviceConnectedHandler deviceConnectedHandler;
    private final OpenflowProviderConfig config;
    private final ExecutorService executorService;
    private final DeviceConnectionRateLimiter deviceConnectionRateLimiter;
    private DeviceDisconnectedHandler deviceDisconnectedHandler;
    private final int dpnHoldTimeInSeconds;
    private final DpnConnectionStatusProvider dpnConnectionStatusProvider;
    private final DataBroker dataBroker;

    public ConnectionManagerImpl(final OpenflowProviderConfig config, final ExecutorService executorService,
                                 final DataBroker dataBroker) {
        this.config = config;
        this.executorService = executorService;
        this.dataBroker = dataBroker;
        this.deviceConnectionRateLimiter = new DeviceConnectionRateLimiter(config);
        this.dpnHoldTimeInSeconds = config. getDpnHoldTimeInSeconds();
        dpnConnectionStatusProvider = new DpnConnectionStatusProviderImpl();
        dpnConnectionStatusProvider.init();
    }

    @Override
    public void onSwitchConnected(final ConnectionAdapter connectionAdapter) {
        LOG.trace("prepare connection context");
        final ConnectionContext connectionContext = new ConnectionContextImpl(connectionAdapter);
        connectionContext.setDeviceDisconnectedHandler(this.deviceDisconnectedHandler);

        HandshakeListener handshakeListener = new HandshakeListenerImpl(connectionContext, deviceConnectedHandler);
        final HandshakeManager handshakeManager = createHandshakeManager(connectionAdapter, handshakeListener);

        LOG.trace("prepare handshake context");
        HandshakeContext handshakeContext = new HandshakeContextImpl(executorService, handshakeManager);
        handshakeListener.setHandshakeContext(handshakeContext);
        connectionContext.setHandshakeContext(handshakeContext);

        LOG.trace("prepare connection listeners");
        final ConnectionReadyListener connectionReadyListener = new ConnectionReadyListenerImpl(
                connectionContext, handshakeContext);
        connectionAdapter.setConnectionReadyListener(connectionReadyListener);

        final OpenflowProtocolListener ofMessageListener =
                new OpenflowProtocolListenerInitialImpl(connectionContext, handshakeContext);
        connectionAdapter.setMessageListener(ofMessageListener);

        final SystemNotificationsListener systemListener = new SystemNotificationsListenerImpl(
                connectionContext, config.getEchoReplyTimeout().getValue(), executorService);
        connectionAdapter.setSystemListener(systemListener);

        LOG.trace("connection ballet finished");
    }

    private HandshakeManager createHandshakeManager(final ConnectionAdapter connectionAdapter,
                                                    final HandshakeListener handshakeListener) {
        HandshakeManagerImpl handshakeManager = new HandshakeManagerImpl(connectionAdapter,
                OFConstants.VERSION_ORDER.get(0),
                OFConstants.VERSION_ORDER, new ErrorHandlerSimpleImpl(), handshakeListener, BITMAP_NEGOTIATION_ENABLED,
                deviceConnectionRateLimiter, dpnHoldTimeInSeconds, dpnConnectionStatusProvider);

        return handshakeManager;
    }

    @Override
    public boolean accept(final InetAddress switchAddress) {
        // TODO add connection accept logic based on address
        return true;
    }

    @Override
    public void setDeviceConnectedHandler(final DeviceConnectedHandler deviceConnectedHandler) {
        this.deviceConnectedHandler = deviceConnectedHandler;
    }

    @Override
    public void setDeviceDisconnectedHandler(final DeviceDisconnectedHandler deviceDisconnectedHandler) {
        this.deviceDisconnectedHandler = deviceDisconnectedHandler;
    }

    class DpnConnectionStatusProviderImpl implements DpnConnectionStatusProvider, ClusteredDataTreeChangeListener<Node>,
            AutoCloseable {
        private final Map<BigInteger, LocalDateTime> dpnConnectionMap = new ConcurrentHashMap<>();

        private ListenerRegistration<DpnConnectionStatusProviderImpl> listenerRegistration;

        @Override
        @SuppressWarnings({"checkstyle:IllegalCatch"})
        public void init() {
            DataTreeIdentifier<Node> treeId = DataTreeIdentifier.create(org.opendaylight.mdsal.common.api.LogicalDatastoreType.OPERATIONAL, getWildCardPath());
            try {
                listenerRegistration = dataBroker.registerDataTreeChangeListener(treeId, this);
            } catch (Exception e) {
                LOG.error("DpnConnectionStatusProvider listener registration failed", e);
            }
        }

        @Override
        public LocalDateTime getDpnLastConnectionTime(BigInteger dpnId) {
            return dpnConnectionMap.get(dpnId);
        }

        @Override
        public void addDpnLastConnectionTime(BigInteger dpnId, LocalDateTime time) {
            dpnConnectionMap.put(dpnId, time);
        }

        @Override
        public void removeDpnLastConnectionTime(BigInteger dpnId) {
            dpnConnectionMap.remove(dpnId);
        }

        @Override
        public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<Node>> changes) {
            Preconditions.checkNotNull(changes, "Changes may not be null!");
            for (DataTreeModification<Node> change : changes) {
                final DataObjectModification<Node> mod = change.getRootNode();
                switch (mod.getModificationType()) {
                    case DELETE:
                        break;
                    case SUBTREE_MODIFIED:
                        break;
                    case WRITE:
                        processNodeModification(change);
                        break;
                    default:
                        throw new IllegalArgumentException("Unhandled modification type " + mod.getModificationType());
                }
            }
        }

        private InstanceIdentifier<Node> getWildCardPath() {
            return InstanceIdentifier.create(Nodes.class).child(Node.class);
        }

        private void processNodeModification(DataTreeModification<Node> change) {
            final InstanceIdentifier<Node> key = change.getRootPath().getRootIdentifier();
            final InstanceIdentifier<Node> nodeIdent = key.firstIdentifierOf(Node.class);
            String[] nodeId = nodeIdent.firstKeyOf(Node.class).getId().getValue().split(":");
            String dpnId = nodeId[1];
            LOG.info("Clearing the dpn connection timer for the dpn {}", dpnId);
            removeDpnLastConnectionTime(new BigInteger(dpnId));
        }

        @Override
        public void close() throws Exception {
            if (listenerRegistration != null) {
                listenerRegistration.close();
                listenerRegistration = null;
            }
        }
    }
}
