/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core;

import static java.util.Objects.requireNonNull;

import java.net.InetAddress;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionConfiguration;
import org.opendaylight.openflowjava.protocol.api.connection.ThreadConfiguration;
import org.opendaylight.openflowjava.protocol.api.connection.TlsConfiguration;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.KeystoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.TransportProtocol;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow._switch.connection.config.rev160506.SwitchConnectionConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow._switch.connection.config.rev160506._switch.connection.config.Threads;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow._switch.connection.config.rev160506._switch.connection.config.Tls;

final class ConnectionConfigurationImpl implements ConnectionConfiguration {
    private final SwitchConnectionConfig config;
    private final InetAddress address;

    ConnectionConfigurationImpl(final SwitchConnectionConfig config) {
        this.config = requireNonNull(config);
        final var addr = config.getAddress();
        address = addr != null ? IetfInetUtil.inetAddressFor(addr) : null;
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
                return requireNonNull(tlsConfig.getTruststoreType());
            }

            @Override
            public String getTlsTruststore() {
                return requireNonNull(tlsConfig.getTruststore());
            }

            @Override
            public KeystoreType getTlsKeystoreType() {
                return requireNonNull(tlsConfig.getKeystoreType());
            }

            @Override
            public String getTlsKeystore() {
                return requireNonNull(tlsConfig.getKeystore());
            }

            @Override
            public org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.PathType
                    getTlsKeystorePathType() {
                return requireNonNull(tlsConfig.getKeystorePathType());
            }

            @Override
            public org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.PathType
                    getTlsTruststorePathType() {
                return requireNonNull(tlsConfig.getTruststorePathType());
            }

            @Override
            public String getKeystorePassword() {
                return requireNonNull(tlsConfig.getKeystorePassword());
            }

            @Override
            public String getCertificatePassword() {
                return requireNonNull(tlsConfig.getCertificatePassword());
            }

            @Override
            public String getTruststorePassword() {
                return requireNonNull(tlsConfig.getTruststorePassword());
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
}