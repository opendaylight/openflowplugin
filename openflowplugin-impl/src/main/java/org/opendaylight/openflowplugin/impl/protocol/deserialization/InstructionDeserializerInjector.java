/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization;

import java.util.function.Consumer;
import java.util.function.Function;

import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerExtensionProvider;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.openflowplugin.api.openflow.protocol.deserialization.MessageCodeExperimenterKey;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.instruction.ApplyActionsInstructionDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.instruction.ClearActionsInstructionDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.instruction.GoToTableInstructionDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.instruction.MeterInstructionDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.instruction.WriteActionsInstructionDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.instruction.WriteMetadataInstructionDeserializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;

import com.google.common.annotations.VisibleForTesting;

public class InstructionDeserializerInjector {

    /**
     * Injects instruction deserializers into provided {@link org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerExtensionProvider}
     * @param provider OpenflowJava deserializer extension provider
     */
    static void injectDeserializers(final DeserializerExtensionProvider provider) {
        // Inject new instruction deserializers here using injector created by createInjector method
        final Function<Byte, Consumer<OFDeserializer<Instruction>>> injector =
                createInjector(provider, EncodeConstants.OF13_VERSION_ID);

        injector.apply(InstructionConstants.GOTO_TABLE_TYPE).accept(new GoToTableInstructionDeserializer());
        injector.apply(InstructionConstants.WRITE_METADATA_TYPE).accept(new WriteMetadataInstructionDeserializer());
        injector.apply(InstructionConstants.WRITE_ACTIONS_TYPE).accept(new WriteActionsInstructionDeserializer());
        injector.apply(InstructionConstants.APPLY_ACTIONS_TYPE).accept(new ApplyActionsInstructionDeserializer());
        injector.apply(InstructionConstants.CLEAR_ACTIONS_TYPE).accept(new ClearActionsInstructionDeserializer());
        injector.apply(InstructionConstants.METER_TYPE).accept(new MeterInstructionDeserializer());
    }

    /**
     * Create injector that will inject new deserializers into #{@link org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerExtensionProvider}
     * @param provider OpenflowJava deserializer extension provider
     * @param version Openflow version
     * @return injector
     */
    @VisibleForTesting
    static Function<Byte, Consumer<OFDeserializer<Instruction>>> createInjector(
            final DeserializerExtensionProvider provider,
            final short version) {
        return code -> deserializer -> {
            provider.registerDeserializer(
                    new MessageCodeExperimenterKey(version, code, Instruction.class, null),
                    deserializer);
        };
    }

}
