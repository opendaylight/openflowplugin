/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.util;

import static java.util.Objects.requireNonNull;

import org.opendaylight.openflowjava.protocol.api.extensibility.OFGeneralSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.InstructionSerializerKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.InstructionChoice;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Helper class for registering instruction serializers.
 *
 * @author michal.polkorab
 */
public class InstructionSerializerRegistryHelper {

    private final Uint8 version;
    private final SerializerRegistry serializerRegistry;

    /**
     * Constructor.
     *
     * @param version Openflow wire version
     * @param serializerRegistry registry to be filled with message serializers
     */
    public InstructionSerializerRegistryHelper(final Uint8 version, final SerializerRegistry serializerRegistry) {
        this.version = requireNonNull(version);
        this.serializerRegistry = serializerRegistry;
    }

    /**
     * Registers given serializer.
     *
     * @param instructionType instruction type
     * @param serializer serializer instance
     */
    public <T extends InstructionChoice> void registerSerializer(final Class<T> instructionType,
            final OFGeneralSerializer serializer) {
        serializerRegistry.registerSerializer(new InstructionSerializerKey<>(version, instructionType, null),
            serializer);
    }
}
