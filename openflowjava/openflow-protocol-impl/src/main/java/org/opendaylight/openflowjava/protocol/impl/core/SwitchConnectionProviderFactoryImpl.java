/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core;

import com.google.common.base.MoreObjects;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import javax.inject.Singleton;
import org.apache.aries.blueprint.annotation.service.Service;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionConfiguration;
import org.opendaylight.openflowjava.protocol.api.connection.OpenflowDiagStatusProvider;
import org.opendaylight.openflowjava.protocol.api.connection.ThreadConfiguration;
import org.opendaylight.openflowjava.protocol.api.connection.TlsConfiguration;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProviderFactory;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.KeystoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.TransportProtocol;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow._switch.connection.config.rev160506.SwitchConnectionConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow._switch.connection.config.rev160506._switch.connection.config.Threads;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow._switch.connection.config.rev160506._switch.connection.config.Tls;

/**
 * Implementation of the SwitchConnectionProviderFactory interface.
 */
@Singleton
@Service(classes = SwitchConnectionProviderFactory.class)
public class SwitchConnectionProviderFactoryImpl implements SwitchConnectionProviderFactory {

    @Override
    public SwitchConnectionProvider newInstance(final SwitchConnectionConfig config,
                                                final OpenflowDiagStatusProvider openflowPluginDiagStatusProvider) {
        return new SwitchConnectionProviderImpl(new ConnectionConfigurationImpl(config),
                openflowPluginDiagStatusProvider);
    }

    private static class ConnectionConfigurationImpl implements ConnectionConfiguration {
        private final SwitchConnectionConfig config;
        private InetAddress address;

        ConnectionConfigurationImpl(final SwitchConnectionConfig config) {
            this.config = config;

            try {
                address = getInetAddress(config.getAddress());
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public InetAddress getAddress() {
            return address;
        }

        @Override
        public int getPort() {
            return config.getPort().toJava();
        }

        @Override
        public Object getTransferProtocol() {
            return config.getTransportProtocol();
        }

        @Override
        public int getChannelOutboundQueueSize() {
            return config.getChannelOutboundQueueSize().toJava();
        }

        @Override
        public TlsConfiguration getTlsConfiguration() {
            final Tls tlsConfig = config.getTls();
            if (tlsConfig == null || !TransportProtocol.TLS.equals(getTransferProtocol())) {
                return null;
            }

            return new TlsConfiguration() {
                @Override
                public KeystoreType getTlsTruststoreType() {
                    return MoreObjects.firstNonNull(tlsConfig.getTruststoreType(), null);
                }

                @Override
                public String getTlsTruststore() {
                    return MoreObjects.firstNonNull(tlsConfig.getTruststore(), null);
                }

                @Override
                public KeystoreType getTlsKeystoreType() {
                    return MoreObjects.firstNonNull(tlsConfig.getKeystoreType(), null);
                }

                @Override
                public String getTlsKeystore() {
                    return MoreObjects.firstNonNull(tlsConfig.getKeystore(), null);
                }

                @Override
                public org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.PathType
                        getTlsKeystorePathType() {
                    return MoreObjects.firstNonNull(tlsConfig.getKeystorePathType(), null);
                }

                @Override
                public org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.PathType
                        getTlsTruststorePathType() {
                    return MoreObjects.firstNonNull(tlsConfig.getTruststorePathType(), null);
                }

                @Override
                public String getKeystorePassword() {
                    return MoreObjects.firstNonNull(tlsConfig.getKeystorePassword(), null);
                }

                @Override
                public String getCertificatePassword() {
                    return MoreObjects.firstNonNull(tlsConfig.getCertificatePassword(), null);
                }

                @Override
                public String getTruststorePassword() {
                    return MoreObjects.firstNonNull(tlsConfig.getTruststorePassword(), null);
                }

                @Override
                public List<String> getCipherSuites() {
                    return tlsConfig.getCipherSuites();
                }
            };
        }

        @Override
        public long getSwitchIdleTimeout() {
            return config.getSwitchIdleTimeout().toJava();
        }

        @Override
        public Object getSslContext() {
            return null;
        }

        @Override
        public ThreadConfiguration getThreadConfiguration() {
            final Threads threads = config.getThreads();
            if (threads == null) {
                return null;
            }

            return new ThreadConfiguration() {
                @Override
                public int getWorkerThreadCount() {
                    return threads.getWorkerThreads().toJava();
                }

                @Override
                public int getBossThreadCount() {
                    return threads.getBossThreads().toJava();
                }
            };
        }

        @Override
        public boolean useBarrier() {
            return config.getUseBarrier();
        }

        @Override
        public boolean isGroupAddModEnabled() {
            return config.getGroupAddModEnabled();
        }

        private static InetAddress getInetAddress(final IpAddress address) throws UnknownHostException {
            if (address != null) {
                if (address.getIpv4Address() != null) {
                    return InetAddress.getByName(address.getIpv4Address().getValue());
                } else if (address.getIpv6Address() != null) {
                    return InetAddress.getByName(address.getIpv6Address().getValue());
                }
            }
            return null;
        }
    }
}
