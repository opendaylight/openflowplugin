/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerExtensionProvider;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.openflowplugin.api.openflow.protocol.deserialization.MessageCodeExperimenterKey;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.key.MessageCodeActionExperimenterKey;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.instruction.ApplyActionsInstructionDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.instruction.ClearActionsInstructionDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.instruction.GoToTableInstructionDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.instruction.MeterInstructionDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.instruction.WriteActionsInstructionDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.instruction.WriteMetadataInstructionDeserializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;

import com.google.common.annotations.VisibleForTesting;

class InstructionDeserializerInjector {

    /**
     * Injects instruction deserializers into provided {@link org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerExtensionProvider}
     * @param provider OpenflowJava deserializer extension provider
     */
    static void injectDeserializers(final DeserializerExtensionProvider provider) {
        // Inject new instruction deserializers here using injector created by createInjector method
        final Function<Byte, Function<ActionPath, Consumer<OFDeserializer<Instruction>>>> injector =
                createInjector(provider, EncodeConstants.OF13_VERSION_ID);

        injector.apply(InstructionConstants.GOTO_TABLE_TYPE).apply(null).accept(new GoToTableInstructionDeserializer());
        injector.apply(InstructionConstants.WRITE_METADATA_TYPE).apply(null).accept(new WriteMetadataInstructionDeserializer());
        injector.apply(InstructionConstants.CLEAR_ACTIONS_TYPE).apply(null).accept(new ClearActionsInstructionDeserializer());
        injector.apply(InstructionConstants.METER_TYPE).apply(null).accept(new MeterInstructionDeserializer());

        for (ActionPath path : ActionPath.values()) {
            injector.apply(InstructionConstants.WRITE_ACTIONS_TYPE).apply(path).accept(new WriteActionsInstructionDeserializer(path));
            injector.apply(InstructionConstants.APPLY_ACTIONS_TYPE).apply(path).accept(new ApplyActionsInstructionDeserializer(path));
        }
    }

    /**
     * Create injector that will inject new deserializers into #{@link org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerExtensionProvider}
     * @param provider OpenflowJava deserializer extension provider
     * @param version Openflow version
     * @return injector
     */
    @VisibleForTesting
    static Function<Byte, Function<ActionPath, Consumer<OFDeserializer<Instruction>>>> createInjector(
            final DeserializerExtensionProvider provider,
            final short version) {
        return code -> actionPath -> deserializer -> {
            provider.registerDeserializer((Objects.nonNull(actionPath)
                    ? new MessageCodeActionExperimenterKey(version, code, Instruction.class, actionPath, null)
                    : new MessageCodeExperimenterKey(version, code, Instruction.class, null)),
                    deserializer);
        };
    }

}
