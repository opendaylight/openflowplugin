/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization;

import com.google.common.annotations.VisibleForTesting;
import java.util.function.Consumer;
import java.util.function.Function;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.impl.protocol.serialization.instructions.ApplyActionsSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.instructions.ClearActionsSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.instructions.GoToTableSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.instructions.MeterSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.instructions.WriteActionsSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.instructions.WriteMetadataSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.keys.InstructionSerializerKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ClearActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.GoToTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.MeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteMetadataCase;

/**
 * Util class for injecting new instruction serializers into OpenflowJava
 */
class InstructionSerializerInjector {

    /**
     * Injects serializers into provided {@link org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider}
     * @param provider OpenflowJava serializer extension provider
     */
    static void injectSerializers(final SerializerExtensionProvider provider) {
        // Inject new instruction serializers here using injector created by createInjector method
        final Function<Class<? extends Instruction>, Consumer<OFSerializer<? extends Instruction>>> injector =
                createInjector(provider, EncodeConstants.OF13_VERSION_ID);
        injector.apply(ApplyActionsCase.class).accept(new ApplyActionsSerializer());
        injector.apply(ClearActionsCase.class).accept(new ClearActionsSerializer());
        injector.apply(GoToTableCase.class).accept(new GoToTableSerializer());
        injector.apply(MeterCase.class).accept(new MeterSerializer());
        injector.apply(WriteActionsCase.class).accept(new WriteActionsSerializer());
        injector.apply(WriteMetadataCase.class).accept(new WriteMetadataSerializer());
    }

    /**
     * Create injector that will inject new serializers into #{@link org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider}
     * @param provider OpenflowJava serializer extension provider
     * @param version Openflow version
     * @return injector
     */
    @VisibleForTesting
    static Function<Class<? extends Instruction>, Consumer<OFSerializer<? extends Instruction>>> createInjector(final SerializerExtensionProvider provider,
                                                                                                final byte version) {
        return type -> serializer ->
                provider.registerSerializer(
                        new InstructionSerializerKey<>(version, type, null),
                        serializer);
    }
}
