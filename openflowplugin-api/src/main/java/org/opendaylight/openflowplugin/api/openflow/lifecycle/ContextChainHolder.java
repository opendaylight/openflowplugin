/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.lifecycle;

import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.OFPManager;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;

/**
 * Generic interface for context chain holder, hold all created context chains
 */
public interface ContextChainHolder {

    <T extends OFPManager> void addManager(final T manager);
    Future<Void> createContextChain(final DeviceInfo deviceInfo);
    Future<Void> pairConnection(final DeviceInfo deviceInfo);
    Future<Void> connectionLost(final DeviceInfo deviceInfo);
    void destroyContextChain(final DeviceInfo deviceInfo);

    void addConnection(final ConnectionContext connectionContext);

}
