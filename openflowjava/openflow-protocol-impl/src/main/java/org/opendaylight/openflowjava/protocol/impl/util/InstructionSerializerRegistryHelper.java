/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.util;

import org.opendaylight.openflowjava.protocol.api.extensibility.OFGeneralSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.InstructionSerializerKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.InstructionChoice;

/**
 * @author michal.polkorab
 */
public class InstructionSerializerRegistryHelper {

    private short version;
    private SerializerRegistry serializerRegistry;

    /**
     * @param version Openflow wire version
     * @param serializerRegistry
     */
    public InstructionSerializerRegistryHelper(short version, SerializerRegistry serializerRegistry) {
        this.version = version;
        this.serializerRegistry = serializerRegistry;
    }

    /**
     * Registers given serializer
     * @param instructionType
     * @param serializer
     */
    public <T extends InstructionChoice> void registerSerializer(Class<T> instructionType,
            OFGeneralSerializer serializer) {
        serializerRegistry.registerSerializer(new InstructionSerializerKey<>(version,
                instructionType, null), serializer);
    }
}