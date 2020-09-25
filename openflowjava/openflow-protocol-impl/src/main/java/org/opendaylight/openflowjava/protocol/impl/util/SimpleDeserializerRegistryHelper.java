/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.util;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFGeneralDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Helper class for deserializer registration.
 *
 * @author michal.polkorab
 */
public class SimpleDeserializerRegistryHelper {

    private final short version;
    private final @NonNull Uint8 uintVersion;
    private final DeserializerRegistry registry;

    /**
     * Constructor.
     *
     * @param version wire protocol version
     * @param deserializerRegistry registry to be filled with message deserializers
     */
    public SimpleDeserializerRegistryHelper(final short version, final DeserializerRegistry deserializerRegistry) {
        this.version = version;
        this.registry = deserializerRegistry;
        this.uintVersion = Uint8.valueOf(version);
    }

    /**
     * Register deserializer in registry. If deserializer supports more protocol versions assign actual one.
     * @param code code / value to distinguish between deserializers
     * @param deserializedObjectClass class of object that will be deserialized by given deserializer
     * @param deserializer deserializer instance
     */
    public void registerDeserializer(final int code, final Class<?> deserializedObjectClass,
                                     final OFGeneralDeserializer deserializer) {
        registry.registerDeserializer(new MessageCodeKey(version, code, deserializedObjectClass), deserializer);

        if (deserializer instanceof VersionAssignableFactory) {
            ((VersionAssignableFactory<?>) deserializer).assignVersion(uintVersion);
        }
    }
}
