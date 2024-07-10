/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core.connection;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.x500.X500Principal;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionReadyListener;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandler;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandlerRegistration;
import org.opendaylight.openflowjava.protocol.api.extensibility.AlienMessageListener;
import org.opendaylight.openflowjava.protocol.impl.core.OFVersionDetector;
import org.opendaylight.openflowjava.protocol.impl.core.PipelineHandlers;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.DisconnectEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SslConnectionError;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SslConnectionErrorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SwitchIdleEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927._switch.certificate.IssuerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927._switch.certificate.SubjectBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.ssl.connection.error.SwitchCertificate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.ssl.connection.error.SwitchCertificateBuilder;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles messages (notifications + rpcs) and connections.
 * @author mirehak
 * @author michal.polkorab
 */
public class ConnectionAdapterImpl extends AbstractConnectionAdapterStatistics implements ConnectionFacade {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionAdapterImpl.class);

    private ConnectionReadyListener connectionReadyListener;
    private MessageListener messageListener;
    private SystemListener systemListener;
    private AlienMessageListener alienMessageListener;
    private AbstractOutboundQueueManager<?, ?> outputManager;
    private OFVersionDetector versionDetector;
    private BigInteger datapathId;
    private ExecutorService executorService;
    private final boolean useBarrier;
    private X509Certificate switchCertificate;

    /**
     * Default constructor.
     * @param channel the channel to be set - used for communication
     * @param address client address (used only in case of UDP communication,
     *                as there is no need to store address over tcp (stable channel))
     * @param useBarrier value is configurable by configSubsytem
     */
    public ConnectionAdapterImpl(final Channel channel, final InetSocketAddress address, final boolean useBarrier,
                                 final int channelOutboundQueueSize) {
        super(channel, address, channelOutboundQueueSize);
        this.useBarrier = useBarrier;
        LOG.debug("ConnectionAdapter created");
    }

    @Override
    public void setMessageListener(final MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    @Override
    public void setConnectionReadyListener(final ConnectionReadyListener connectionReadyListener) {
        this.connectionReadyListener = connectionReadyListener;
    }

    @Override
    public void setSystemListener(final SystemListener systemListener) {
        this.systemListener = systemListener;
    }

    @Override
    public void setAlienMessageListener(final AlienMessageListener alienMessageListener) {
        this.alienMessageListener = alienMessageListener;
    }

    @Override
    public void consumeDeviceMessage(final DataObject message) {
        LOG.debug("ConsumeIntern msg {} for dpn {} on {}", message.implementedInterface().getSimpleName(),
                datapathId, channel);
        LOG.trace("ConsumeIntern msg {}", message);
        if (disconnectOccured) {
            return;
        }
        if (message instanceof Notification) {
            // System events
            if (message instanceof DisconnectEvent disconnect) {
                systemListener.onDisconnect(disconnect);
                responseCache.invalidateAll();
                disconnectOccured = true;
            } else if (message instanceof SwitchIdleEvent switchIdle) {
                systemListener.onSwitchIdle(switchIdle);
            } else if (message instanceof SslConnectionError sslError) {
                systemListener.onSslConnectionError(new SslConnectionErrorBuilder()
                    .setInfo(sslError.getInfo())
                    .setSwitchCertificate(buildSwitchCertificate())
                    .build());
            // OpenFlow messages
            } else if (message instanceof EchoRequestMessage echoRequest) {
                if (outputManager != null) {
                    outputManager.onEchoRequest(echoRequest, datapathId);
                } else {
                    messageListener.onEchoRequest(echoRequest);
                }
            } else if (message instanceof ErrorMessage error) {
                // Send only unmatched errors
                if (outputManager == null || !outputManager.onMessage(error)) {
                    messageListener.onError(error);
                }
            } else if (message instanceof ExperimenterMessage experimenter) {
                if (outputManager != null) {
                    outputManager.onMessage(experimenter);
                }
                messageListener.onExperimenter(experimenter);
            } else if (message instanceof FlowRemovedMessage flowRemoved) {
                messageListener.onFlowRemoved(flowRemoved);
            } else if (message instanceof HelloMessage hello) {
                LOG.info("Hello received");
                messageListener.onHello(hello);
            } else if (message instanceof MultipartReplyMessage multipartReply) {
                if (outputManager != null) {
                    outputManager.onMessage(multipartReply);
                }
                messageListener.onMultipartReply(multipartReply);
            } else if (message instanceof PacketInMessage packetIn) {
                messageListener.onPacketIn(packetIn);
            } else if (message instanceof PortStatusMessage portStatus) {
                messageListener.onPortStatus(portStatus);
            } else {
                LOG.warn("message listening not supported for type: {}", message.getClass());
            }
        } else if (message instanceof OfHeader header) {
            LOG.debug("OF header msg received");

            if (alienMessageListener != null && alienMessageListener.onAlienMessage(header)) {
                LOG.debug("Alien message {} received", header.implementedInterface());
            } else if (outputManager == null || !outputManager.onMessage(header) || header instanceof EchoOutput) {
                final RpcResponseKey key = createRpcResponseKey(header);
                final ResponseExpectedRpcListener<?> listener = findRpcResponse(key);
                if (listener != null) {
                    LOG.debug("Corresponding rpcFuture found");
                    listener.completed(header);
                    LOG.debug("After setting rpcFuture");
                    responseCache.invalidate(key);
                }
            }
        } else {
            LOG.warn("message listening not supported for type: {}", message.getClass());
        }
    }

    private static RpcResponseKey createRpcResponseKey(final OfHeader message) {
        return new RpcResponseKey(message.getXid().toJava(), message.implementedInterface().getName());
    }

    @Override
    public void checkListeners() {
        final StringBuilder buffer =  new StringBuilder();
        if (systemListener == null) {
            buffer.append("SystemListener ");
        }
        if (messageListener == null) {
            buffer.append("MessageListener ");
        }
        if (connectionReadyListener == null) {
            buffer.append("ConnectionReadyListener ");
        }

        Preconditions.checkState(buffer.length() == 0, "Missing listeners: %s", buffer.toString());
    }

    @Override
    public void fireConnectionReadyNotification() {
        versionDetector = (OFVersionDetector) channel.pipeline().get(PipelineHandlers.OF_VERSION_DETECTOR.name());
        Preconditions.checkState(versionDetector != null);
        executorService.execute(() -> connectionReadyListener.onConnectionReady());
    }

    @Override
    public void onSwitchCertificateIdentified(final List<X509Certificate> certificateChain) {
        if (certificateChain != null && !certificateChain.isEmpty()) {
            switchCertificate = certificateChain.get(0);
        }
    }

    @Override
    public <T extends OutboundQueueHandler> OutboundQueueHandlerRegistration<T> registerOutboundQueueHandler(
            final T handler, final int maxQueueDepth, final long maxBarrierNanos) {
        Preconditions.checkState(outputManager == null, "Manager %s already registered", outputManager);

        final AbstractOutboundQueueManager<T, ?> ret;
        if (useBarrier) {
            ret = new OutboundQueueManager<>(this, address, handler, maxQueueDepth, maxBarrierNanos);
        } else {
            LOG.warn("OutboundQueueManager without barrier is started.");
            ret = new OutboundQueueManagerNoBarrier<>(this, address, handler);
        }

        outputManager = ret;
        /* we don't need it anymore */
        channel.pipeline().remove(output);
        // OutboundQueueManager is put before DelegatingInboundHandler because otherwise channelInactive event would
        // be first processed in OutboundQueueManager and then in ConnectionAdapter (and Openflowplugin). This might
        // cause problems because we are shutting down the queue before Openflowplugin knows about it.
        channel.pipeline().addBefore(PipelineHandlers.DELEGATING_INBOUND_HANDLER.name(),
                PipelineHandlers.CHANNEL_OUTBOUND_QUEUE_MANAGER.name(), outputManager);

        return new OutboundQueueHandlerRegistrationImpl<>(handler) {
            @Override
            protected void removeRegistration() {
                outputManager.close();
                channel.pipeline().remove(outputManager);
                outputManager = null;
            }
        };
    }

    Channel getChannel() {
        return channel;
    }

    @Override
    public void setPacketInFiltering(final boolean enabled) {
        versionDetector.setFilterPacketIns(enabled);
        LOG.debug("PacketIn filtering {}abled", enabled ? "en" : "dis");
    }

    private SwitchCertificate buildSwitchCertificate() {
        if (switchCertificate == null) {
            return null;
        }

        final var builder = new SwitchCertificateBuilder();
        final var subjectMap = indexRds(switchCertificate.getSubjectX500Principal());
        if (subjectMap != null) {
            builder.setSubject(new SubjectBuilder()
                .setCommonName(subjectMap.get("CN"))
                .setCountry(subjectMap.get("C"))
                .setLocality(subjectMap.get("L"))
                .setOrganization(subjectMap.get("O"))
                .setOrganizationUnit(subjectMap.get("OU"))
                .setState(subjectMap.get("ST"))
                .build());
        }

        final var issuerMap = indexRds(switchCertificate.getIssuerX500Principal());
        if (issuerMap != null) {
            builder.setIssuer(new IssuerBuilder()
                .setCommonName(issuerMap.get("CN"))
                .setCountry(issuerMap.get("C"))
                .setLocality(issuerMap.get("L"))
                .setOrganization(issuerMap.get("O"))
                .setOrganizationUnit(issuerMap.get("OU"))
                .setState(issuerMap.get("ST"))
                .build());
        }

        Collection<List<?>> altNames = null;
        try {
            altNames = switchCertificate.getSubjectAlternativeNames();
        } catch (CertificateParsingException e) {
            LOG.error("Cannot parse certificate alternate names", e);
        }
        if (altNames != null) {
            builder.setSubjectAlternateNames(altNames.stream()
                .filter(list -> list.size() > 1)
                .map(list -> list.get(1))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .collect(Collectors.toUnmodifiableList()));
        }

        // FIXME: do not use SimpleDateFormat
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'-00:00'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        return builder
            .setSerialNumber(switchCertificate.getSerialNumber().toString())
            .setValidFrom(new DateAndTime(formatter.format(switchCertificate.getNotBefore())))
            .setValidTo(new DateAndTime(formatter.format(switchCertificate.getNotAfter())))
            .build();
    }

    @Override
    public void setDatapathId(final BigInteger datapathId) {
        this.datapathId = datapathId;
    }

    @Override
    public void setExecutorService(final ExecutorService executorService) {
        this.executorService = executorService;
    }

    private static Map<String, String> indexRds(final X500Principal principal) {
        final LdapName name;
        try {
            name = new LdapName(principal.getName());
        } catch (InvalidNameException e) {
            LOG.error("Cannot parse principal {}", principal, e);
            return null;
        }
        return name.getRdns().stream().collect(Collectors.toMap(Rdn::getType, rdn -> rdn.getValue().toString()));
    }
}
