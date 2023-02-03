/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures;

import static java.util.Objects.requireNonNull;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableFeaturesPropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Wildcards;

public class WildcardsTablePropertySerializer extends AbstractTablePropertySerializer<Wildcards> implements
        SerializerRegistryInjector {

    private SerializerRegistry registry;

    @Override
    protected void serializeProperty(final Wildcards property, final ByteBuf byteBuf) {
        property
            .getWildcardSetfield()
            .nonnullSetFieldMatch().values()
            .forEach(setFieldMatch -> requireNonNull(registry)
                .<MatchField, OFSerializer<SetFieldMatch>>getSerializer(new MessageTypeKey<>(
                    EncodeConstants.OF_VERSION_1_3, setFieldMatch.getMatchType().implementedInterface()))
                .serialize(setFieldMatch, byteBuf));
    }

    @Override
    protected TableFeaturesPropType getType() {
        return TableFeaturesPropType.OFPTFPTWILDCARDS;
    }

    @Override
    protected Class<Wildcards> getClazz() {
        return Wildcards.class;
    }

    @Override
    public void injectSerializerRegistry(final SerializerRegistry serializerRegistry) {
        registry = serializerRegistry;
    }
}
