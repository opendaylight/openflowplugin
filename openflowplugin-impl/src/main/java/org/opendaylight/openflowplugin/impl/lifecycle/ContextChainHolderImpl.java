/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.lifecycle;

import com.google.common.base.Verify;
import com.google.common.util.concurrent.Futures;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.OFPManager;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChain;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainHolder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.openflow.provider.config.ContextChainConfig;

public class ContextChainHolderImpl implements ContextChainHolder {

    private Set<OFPManager> managers = new HashSet<>();
    private ConcurrentHashMap<DeviceInfo, ContextChain> contextChainMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<DeviceInfo, ConnectionContext> latestConnections = new ConcurrentHashMap<>();
    private final ContextChainConfig config;

    public ContextChainHolderImpl(final ContextChainConfig config) {
        this.config = config;
    }

    @Override
    public <T extends OFPManager> void addManager(final T manager) {
        managers.add(manager);
    }

    @Override
    public Future<Void> createContextChain(final DeviceInfo deviceInfo) {
        Verify.verify(this.checkAllManagers(),"Not all manager were set.");
        return null;
    }

    @Override
    public Future<Void> connectionLost(final DeviceInfo deviceInfo) {
        if (!this.checkChainContext(deviceInfo)) {
            return Futures.immediateFuture(null);
        }
        return null;
    }

    @Override
    public void destroyContextChain(final DeviceInfo deviceInfo) {
        ContextChain chain = contextChainMap.get(deviceInfo);
        if (Objects.nonNull(chain)) {
            chain.close();
        }
    }

    @Override
    public void pairConnection(final ConnectionContext connectionContext) {
        DeviceInfo deviceInfo = connectionContext.getDeviceInfo();
        latestConnections.put(deviceInfo, connectionContext);
        if (checkChainContext(deviceInfo)) {
            contextChainMap.get(deviceInfo).changePrimaryConnection(connectionContext);
        }
    }

    private boolean checkAllManagers() {
        return config.getNumberOfNeededManagers().equals(managers.size());
    }

    private boolean checkChainContext(final DeviceInfo deviceInfo) {
        return contextChainMap.containsKey(deviceInfo);
    }

}
