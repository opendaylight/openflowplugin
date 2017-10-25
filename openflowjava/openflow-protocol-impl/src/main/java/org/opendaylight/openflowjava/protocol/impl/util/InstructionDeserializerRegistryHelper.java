/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.util;

import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFGeneralDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.InstructionDeserializerKey;

/**
 * @author michal.polkorab
 *
 */
public class InstructionDeserializerRegistryHelper {

    private short version;
    private DeserializerRegistry registry;

    /**
     * @param version wire protocol version
     * @param deserializerRegistry registry to be filled with message deserializers
     */
    public InstructionDeserializerRegistryHelper(short version, DeserializerRegistry deserializerRegistry) {
        this.version = version;
        this.registry = deserializerRegistry;
    }

    /**
     * @param code code / value to distinguish between deserializers
     * @param deserializer deserializer instance
     */
    public void registerDeserializer(int code, OFGeneralDeserializer deserializer) {
        registry.registerDeserializer(new InstructionDeserializerKey(version, code,
                null), deserializer);
    }
}
