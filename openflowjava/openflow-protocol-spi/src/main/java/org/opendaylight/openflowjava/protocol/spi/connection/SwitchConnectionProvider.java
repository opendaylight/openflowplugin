/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.spi.connection;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionConfiguration;
import org.opendaylight.openflowjava.protocol.api.connection.SwitchConnectionHandler;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerExtensionProvider;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider;

/**
 * Provides handling for a switch connection.
 *
 * @author mirehak
 * @author michal.polkorab
 */
public interface SwitchConnectionProvider extends SerializerExtensionProvider, DeserializerExtensionProvider {
    /**
     * Returns the connection configuration.
     *
     * @return configuration [protocol, port, address and supported features]
     */
    ConnectionConfiguration getConfiguration();

    /**
     * Start listening to switches using specified {@link SwitchConnectionHandler}.
     *
     * @param connectionHandler instance being informed when new switch connects
     * @return future completing when the channel has been resolved
     */
    ListenableFuture<Void> startup(SwitchConnectionHandler connectionHandler);

    /**
     * Stop listening to switches.
     *
     * @return future, triggered to true, when all listening channels are down
     */
    ListenableFuture<Boolean> shutdown();
}
