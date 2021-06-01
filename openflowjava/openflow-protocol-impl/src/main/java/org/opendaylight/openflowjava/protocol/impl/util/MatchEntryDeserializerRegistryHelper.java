/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.util;

import static java.util.Objects.requireNonNull;

import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFGeneralDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Helper class for registering match entry deserializers.
 *
 * @author michal.polkorab
 */
public class MatchEntryDeserializerRegistryHelper {
    private final Uint8 version;
    private final DeserializerRegistry registry;
    private final int oxmClass;

    /**
     * Constructor.
     *
     * @param version wire protocol version
     * @param oxmClass oxm_class that will be used for match entry deserializer registration
     * @param deserializerRegistry registry to be filled with message deserializers
     */
    public MatchEntryDeserializerRegistryHelper(final Uint8 version, final int oxmClass,
            final DeserializerRegistry deserializerRegistry) {
        this.version = requireNonNull(version);
        this.oxmClass = oxmClass;
        this.registry = deserializerRegistry;
    }

    /**
     * Registers match entry deserializer under provided oxmfield.
     *
     * @param oxmField oxm_field value/code
     * @param deserializer deserializer instance
     */
    public void register(final int oxmField, final OFGeneralDeserializer deserializer) {
        MatchEntryDeserializerKey key = new MatchEntryDeserializerKey(version, oxmClass, oxmField);
        key.setExperimenterId(null);
        registry.registerDeserializer(key, deserializer);
    }

    public void registerExperimenter(final int oxmField, final Uint32 expId, final OFGeneralDeserializer deserializer) {
        MatchEntryDeserializerKey key =
                new MatchEntryDeserializerKey(version, OxmMatchConstants.EXPERIMENTER_CLASS, oxmField);
        key.setExperimenterId(expId);
        registry.registerDeserializer(key, deserializer);
    }
}
