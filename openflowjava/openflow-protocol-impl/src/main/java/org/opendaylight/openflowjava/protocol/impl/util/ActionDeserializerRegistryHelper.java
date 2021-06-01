/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.util;

import static java.util.Objects.requireNonNull;

import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFGeneralDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.ActionDeserializerKey;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Helper for registering deserializers.
 *
 * @author michal.polkorab
 */
public class ActionDeserializerRegistryHelper {
    private final Uint8 version;
    private final DeserializerRegistry registry;

    /**
     * Constructor.
     *
     * @param version wire protocol version
     * @param deserializerRegistry registry to be filled with message deserializers
     */
    public ActionDeserializerRegistryHelper(final Uint8 version, final DeserializerRegistry deserializerRegistry) {
        this.version = requireNonNull(version);
        this.registry = deserializerRegistry;
    }

    /**
     * Registers a deserializer.
     *
     * @param code code / value to distinguish between deserializers
     * @param deserializer deserializer instance
     */
    public void registerDeserializer(final int code, final OFGeneralDeserializer deserializer) {
        registry.registerDeserializer(new ActionDeserializerKey(version, code, null), deserializer);
    }
}
