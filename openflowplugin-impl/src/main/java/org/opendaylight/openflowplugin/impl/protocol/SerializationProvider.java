/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import java.util.List;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.impl.OpenFlowPluginProviderImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerializationProvider {
    private static final Logger LOG = LoggerFactory.getLogger(OpenFlowPluginProviderImpl.class);
    private final boolean isAvailable;
    private SwitchConnectionProvider switchConnectionProvider;

    public SerializationProvider(final OpenflowProviderConfig providerConfig,
                                 final List<SwitchConnectionProvider> switchConnectionProviders) {
        boolean available = providerConfig.isUseSingleLayerSerialization()
                && providerConfig.isEnableDataPreserialization();

        boolean switchConnectionProviderAvailable = !switchConnectionProviders.isEmpty();

        if (!available || !switchConnectionProviderAvailable) {
            if (!switchConnectionProviderAvailable) {
                LOG.warn("No switch connection provider found, disabling pre-serialization.");
            }

            isAvailable = false;
            return;
        }

        switchConnectionProvider = switchConnectionProviders.get(0);
        isAvailable = true;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public byte[] serialize(final short version, final DataObject message) {
        if (!isAvailable()) {
            return new byte[0];
        }

        final ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        switchConnectionProvider.messageToBuffer(version, byteBuf, message);
        final byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(byteBuf.readerIndex(), bytes);
        byteBuf.release();
        return bytes;
    }
}