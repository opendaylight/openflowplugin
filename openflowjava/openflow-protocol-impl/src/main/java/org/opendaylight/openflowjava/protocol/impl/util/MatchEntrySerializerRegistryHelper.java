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
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ExperimenterClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Helper class for registering match entry serializers.
 *
 * @author michal.polkorab
 * @param <C> OXM class
 */
public class MatchEntrySerializerRegistryHelper<C extends OxmClassBase> {

    private final Uint8 version;
    private final C generalClass;
    private final SerializerRegistry serializerRegistry;

    /**
     * Constructor.
     *
     * @param version Openflow wire version
     * @param generalClass class that will be used for match entry serializer registration
     * @param serializerRegistry registry to be filled with message serializers
     */
    public MatchEntrySerializerRegistryHelper(final Uint8 version, final C generalClass,
            final SerializerRegistry serializerRegistry) {
        this.version = requireNonNull(version);
        this.generalClass = generalClass;
        this.serializerRegistry = serializerRegistry;
    }

    /**
     * Registers the given serializer.
     *
     * @param specificClass the MatchField class
     * @param serializer the serializer instance
     */
    public <F extends MatchField> void registerSerializer(
            final F specificClass, final OFGeneralSerializer serializer) {
        MatchEntrySerializerKey<?, ?> key = new MatchEntrySerializerKey<>(version, generalClass, specificClass);
        key.setExperimenterId(null);
        serializerRegistry.registerSerializer(key, serializer);
    }

    /**
     * Registers ExperimenterClass type match serializer.
     *
     * @param specificClass the MatchField class
     * @param serializer the serializer instance
     */
    public <F extends MatchField> void registerExperimenterSerializer(
            final F specificClass, final Uint32 expId, final OFGeneralSerializer serializer) {
        MatchEntrySerializerKey<?, ?> key = new MatchEntrySerializerKey<>(
                version, ExperimenterClass.VALUE, specificClass);
        key.setExperimenterId(expId);
        serializerRegistry.registerSerializer(key, serializer);
    }
}
