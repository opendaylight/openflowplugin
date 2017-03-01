/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
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
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.ApplyActionsMissTablePropertySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.ApplyActionsTablePropertySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.ApplySetfieldMissTablePropertySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.ApplySetfieldTablePropertySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.InstructionsMissTablePropertySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.InstructionsTablePropertySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.MatchTablePropertySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.NextTableMissTablePropertySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.NextTableTablePropertySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.WildcardsTablePropertySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.WriteActionsMissTablePropertySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.WriteActionsTablePropertySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.WriteSetfieldMissTablePropertySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.WriteSetfieldTablePropertySerializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.TableFeaturePropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActionsMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplySetfield;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplySetfieldMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.InstructionsMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTableMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Wildcards;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActionsMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteSetfield;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteSetfieldMiss;

/**
 * Util class for injecting new multipart table features serializers into OpenflowJava
 */
class MultipartTableFeaturesSerializerInjector {

    /**
     * Injects multipart table features serializers into provided {@link org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider}
     * @param provider OpenflowJava serializer extension provider
     */
    static void injectSerializers(final SerializerExtensionProvider provider) {
        // Inject new message serializers here using injector created by createInjector method
        final Function<Class<? extends TableFeaturePropType>, Consumer<OFSerializer<TableFeaturePropType>>> injector =
            createInjector(provider, EncodeConstants.OF13_VERSION_ID);

        injector.apply(Instructions.class).accept(new InstructionsTablePropertySerializer());
        injector.apply(InstructionsMiss.class).accept(new InstructionsMissTablePropertySerializer());
        injector.apply(NextTable.class).accept(new NextTableTablePropertySerializer());
        injector.apply(NextTableMiss.class).accept(new NextTableMissTablePropertySerializer());
        injector.apply(ApplyActions.class).accept(new ApplyActionsTablePropertySerializer());
        injector.apply(ApplyActionsMiss.class).accept(new ApplyActionsMissTablePropertySerializer());
        injector.apply(WriteActions.class).accept(new WriteActionsTablePropertySerializer());
        injector.apply(WriteActionsMiss.class).accept(new WriteActionsMissTablePropertySerializer());
        injector.apply(Match.class).accept(new MatchTablePropertySerializer());
        injector.apply(Wildcards.class).accept(new WildcardsTablePropertySerializer());
        injector.apply(WriteSetfield.class).accept(new WriteSetfieldTablePropertySerializer());
        injector.apply(WriteSetfieldMiss.class).accept(new WriteSetfieldMissTablePropertySerializer());
        injector.apply(ApplySetfield.class).accept(new ApplySetfieldTablePropertySerializer());
        injector.apply(ApplySetfieldMiss.class).accept(new ApplySetfieldMissTablePropertySerializer());
        // TODO: Add support for experimenters
    }

    /**
     * Create injector that will inject new multipart table features serializers into #{@link org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider}
     * @param provider OpenflowJava serializer extension provider
     * @param version Openflow version
     * @return injector
     */
    @VisibleForTesting
    static Function<Class<? extends TableFeaturePropType>, Consumer<OFSerializer<TableFeaturePropType>>> createInjector(
        final SerializerExtensionProvider provider,
        final byte version) {
        return type -> serializer ->
            provider.registerSerializer(
                new MessageTypeKey<>(version, type),
                serializer);
    }

}
