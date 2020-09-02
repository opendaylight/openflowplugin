/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization;

import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.instruction.ApplyActionsInstructionSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.instruction.ClearActionsInstructionSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.instruction.GoToTableInstructionSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.instruction.MeterInstructionSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.instruction.WriteActionsInstructionSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.instruction.WriteMetadataInstructionSerializer;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionSerializerRegistryHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.ClearActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.GotoTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.MeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.WriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.WriteMetadataCase;

/**
 * Initializes serializer registry with instruction serializers.
 *
 * @author michal.polkorab
 */
public final class InstructionsInitializer {
    private InstructionsInitializer() {
        throw new UnsupportedOperationException("Utility class shouldn't be instantiated");
    }

    /**
     * Registers instruction serializers into provided registry.
     *
     * @param serializerRegistry registry to be initialized with instruction serializers
     */
    public static void registerInstructionSerializers(final SerializerRegistry serializerRegistry) {
        // register OF v1.3 instruction serializers
        InstructionSerializerRegistryHelper helper = new InstructionSerializerRegistryHelper(
                EncodeConstants.OF13_VERSION_ID, serializerRegistry);
        helper.registerSerializer(GotoTableCase.class, new GoToTableInstructionSerializer());
        helper.registerSerializer(WriteMetadataCase.class, new WriteMetadataInstructionSerializer());
        helper.registerSerializer(WriteActionsCase.class, new WriteActionsInstructionSerializer(serializerRegistry));
        helper.registerSerializer(ApplyActionsCase.class, new ApplyActionsInstructionSerializer(serializerRegistry));
        helper.registerSerializer(ClearActionsCase.class, new ClearActionsInstructionSerializer());
        helper.registerSerializer(MeterCase.class, new MeterInstructionSerializer());
    }
}
