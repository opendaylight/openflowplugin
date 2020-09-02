/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization;

import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.instruction.ApplyActionsInstructionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.instruction.ClearActionsInstructionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.instruction.GoToTableInstructionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.instruction.MeterInstructionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.instruction.WriteActionsInstructionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.instruction.WriteMetadataInstructionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionDeserializerRegistryHelper;

/**
 * Utilities for registering nstruction deserializer initializers.
 *
 * @author michal.polkorab
 */
public final class InstructionDeserializerInitializer {

    private InstructionDeserializerInitializer() {
        throw new UnsupportedOperationException("Utility class shouldn't be instantiated");
    }

    /**
     * Registers instruction deserializers.
     *
     * @param registry registry to be filled with deserializers
     */
    public static void registerDeserializers(final DeserializerRegistry registry) {
        // register OF v1.3 instruction deserializers
        InstructionDeserializerRegistryHelper helper =
                new InstructionDeserializerRegistryHelper(EncodeConstants.OF13_VERSION_ID, registry);
        helper.registerDeserializer(1, new GoToTableInstructionDeserializer());
        helper.registerDeserializer(2, new WriteMetadataInstructionDeserializer());
        helper.registerDeserializer(3, new WriteActionsInstructionDeserializer(registry));
        helper.registerDeserializer(4, new ApplyActionsInstructionDeserializer(registry));
        helper.registerDeserializer(5, new ClearActionsInstructionDeserializer());
        helper.registerDeserializer(6, new MeterInstructionDeserializer());
    }
}
