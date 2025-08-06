/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.connection;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.math.BigInteger;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionReadyListener;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionManager;
import org.opendaylight.openflowplugin.api.openflow.connection.DeviceConnectionStatusProvider;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionManagerImpl implements ConnectionManager {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionManagerImpl.class);
    private static final Logger OF_EVENT_LOG = LoggerFactory.getLogger("OfEventLog");
    private static final boolean BITMAP_NEGOTIATION_ENABLED = true;
    private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("ConnectionHandler-%d")
            .setDaemon(false)
            .setUncaughtExceptionHandler((thread, ex) -> LOG.error("Uncaught exception {}", thread, ex))
            .build();
    private final ExecutorService executorsService = Executors.newCachedThreadPool(threadFactory);

    private DeviceConnectedHandler deviceConnectedHandler;
    private final OpenflowProviderConfig config;
    private final ExecutorService executorService;
    private final DeviceConnectionRateLimiter deviceConnectionRateLimiter;
    private final DataBroker dataBroker;
    private final int deviceConnectionHoldTime;
    private DeviceDisconnectedHandler deviceDisconnectedHandler;
    private DeviceConnectionStatusProvider deviceConnectionStatusProvider;
    private final NotificationPublishService notificationPublishService;

    public ConnectionManagerImpl(final OpenflowProviderConfig config, final ExecutorService executorService,
            final DataBroker dataBroker, final NotificationPublishService notificationPublishService) {
        this.config = config;
        this.executorService = executorService;
        deviceConnectionRateLimiter = new DeviceConnectionRateLimiter(config);
        this.dataBroker = dataBroker;
        deviceConnectionHoldTime = config.getDeviceConnectionHoldTimeInSeconds().toJava();
        deviceConnectionStatusProvider = new DeviceConnectionStatusProviderImpl();
        deviceConnectionStatusProvider.init();
        this.notificationPublishService = notificationPublishService;
    }

    @Override
    public void onSwitchConnected(final ConnectionAdapter connectionAdapter) {
        connectionAdapter.setExecutorService(executorsService);
        OF_EVENT_LOG.debug("OnSwitchConnected event received for device {}", connectionAdapter.getRemoteAddress());
        LOG.trace("prepare connection context");
        final ConnectionContext connectionContext = new ConnectionContextImpl(connectionAdapter,
                deviceConnectionStatusProvider);
        connectionContext.setDeviceDisconnectedHandler(deviceDisconnectedHandler);

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

        connectionAdapter.setMessageListener(
            new OpenflowProtocolListenerInitialImpl(connectionContext, handshakeContext));

        connectionAdapter.setSystemListener(new SystemNotificationsListenerImpl(connectionContext,
                config.getEchoReplyTimeout().getValue().toJava(), executorService, notificationPublishService));

        LOG.trace("connection ballet finished");
    }

    private HandshakeManager createHandshakeManager(final ConnectionAdapter connectionAdapter,
                                                    final HandshakeListener handshakeListener) {
        HandshakeManagerImpl handshakeManager = new HandshakeManagerImpl(connectionAdapter,
                OFConstants.VERSION_ORDER.get(0),
                OFConstants.VERSION_ORDER, new ErrorHandlerSimpleImpl(), handshakeListener, BITMAP_NEGOTIATION_ENABLED,
                deviceConnectionRateLimiter, deviceConnectionHoldTime, deviceConnectionStatusProvider);

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

    @VisibleForTesting
    DeviceConnectionStatusProvider getDeviceConnectionStatusProvider() {
        return deviceConnectionStatusProvider;
    }

    @Override
    public void close() throws Exception {
        if (deviceConnectionStatusProvider != null) {
            deviceConnectionStatusProvider.close();
            deviceConnectionStatusProvider = null;
        }
        if (executorsService != null) {
            executorsService.shutdownNow();
        }
    }

    class DeviceConnectionStatusProviderImpl implements DeviceConnectionStatusProvider, DataTreeChangeListener<Node> {
        private final Map<BigInteger, LocalDateTime> deviceConnectionMap = new ConcurrentHashMap<>();

        private Registration listenerRegistration;

        @Override
        public void init() {
            listenerRegistration = dataBroker.registerTreeChangeListener(LogicalDatastoreType.OPERATIONAL,
                DataObjectReference.builder(Nodes.class).child(Node.class).build(), this);
        }

        @Override
        public LocalDateTime getDeviceLastConnectionTime(final BigInteger nodeId) {
            return deviceConnectionMap.get(nodeId);
        }

        @Override
        public void addDeviceLastConnectionTime(final BigInteger nodeId, final LocalDateTime time) {
            deviceConnectionMap.put(nodeId, time);
        }

        @Override
        public void removeDeviceLastConnectionTime(final BigInteger nodeId) {
            deviceConnectionMap.remove(nodeId);
        }

        @Override
        public void onDataTreeChanged(final List<DataTreeModification<Node>> changes) {
            requireNonNull(changes, "Changes must not be null!");
            for (DataTreeModification<Node> change : changes) {
                final DataObjectModification<Node> mod = change.getRootNode();
                switch (mod.modificationType()) {
                    case DELETE, SUBTREE_MODIFIED:
                        break;
                    case WRITE:
                        processNodeModification(change);
                        break;
                    default:
                        throw new IllegalArgumentException("Unhandled modification type " + mod.modificationType());
                }
            }
        }

        private void processNodeModification(final DataTreeModification<Node> change) {
            String[] nodeIdentity = change.path().getFirstKeyOf(Node.class).getId().getValue().split(":");
            String nodeId = nodeIdentity[1];
            LOG.info("Clearing the device connection timer for the device {}", nodeId);
            removeDeviceLastConnectionTime(new BigInteger(nodeId));
        }

        @Override
        public void close() {
            if (listenerRegistration != null) {
                listenerRegistration.close();
                listenerRegistration = null;
            }
        }
    }
}
