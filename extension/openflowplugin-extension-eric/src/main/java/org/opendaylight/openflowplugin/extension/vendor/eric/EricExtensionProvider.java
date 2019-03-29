/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.vendor.eric;

import com.google.common.base.Preconditions;
import java.util.HashSet;
import java.util.Set;
import org.opendaylight.openflowjava.eric.codec.match.Icmpv6NDOptionsTypeCodec;
import org.opendaylight.openflowjava.eric.codec.match.Icmpv6NDReservedCodec;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.extension.api.ConverterExtensionKey;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterRegistrator;
import org.opendaylight.openflowplugin.extension.api.OpenFlowPluginExtensionRegistratorProvider;
import org.opendaylight.openflowplugin.extension.vendor.eric.convertor.match.Icmpv6NDOptionsTypeConvertor;
import org.opendaylight.openflowplugin.extension.vendor.eric.convertor.match.Icmpv6NDReservedConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.Icmpv6NdOptionsTypeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.Icmpv6NdReservedKey;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EricExtensionProvider implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(EricExtensionProvider.class);
    private static final Icmpv6NDReservedConvertor ICMPV6_ND_RESERVED_CONVERTOR = new Icmpv6NDReservedConvertor();
    private static final Icmpv6NDOptionsTypeConvertor ICMPV6_ND_OPTIONS_TYPE_CONVERTOR
            = new Icmpv6NDOptionsTypeConvertor();

    private final ExtensionConverterRegistrator extensionConverterRegistrator;
    private final Set<ObjectRegistration<?>> registrations;

    /**
     * Register appropriate converters.
     */
    public EricExtensionProvider(final OpenFlowPluginExtensionRegistratorProvider provider) {
        this.extensionConverterRegistrator = Preconditions.checkNotNull(provider.getExtensionConverterRegistrator());
        registrations = new HashSet<>();

        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(Icmpv6NdReservedKey.class, EncodeConstants.OF13_VERSION_ID),
                ICMPV6_ND_RESERVED_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(Icmpv6NDReservedCodec.SERIALIZER_KEY,
                ICMPV6_ND_RESERVED_CONVERTOR));

        registrations.add(extensionConverterRegistrator.registerMatchConvertor(
                new ConverterExtensionKey<>(Icmpv6NdOptionsTypeKey.class, EncodeConstants.OF13_VERSION_ID),
                ICMPV6_ND_OPTIONS_TYPE_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(Icmpv6NDOptionsTypeCodec.SERIALIZER_KEY,
                ICMPV6_ND_OPTIONS_TYPE_CONVERTOR));
    }

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public void close() {
        registrations.forEach(janitor -> {
            try {
                janitor.close();
            } catch (RuntimeException e) {
                LOG.warn("closing of extension converter failed", e);
            }
        });
        registrations.clear();
    }

}