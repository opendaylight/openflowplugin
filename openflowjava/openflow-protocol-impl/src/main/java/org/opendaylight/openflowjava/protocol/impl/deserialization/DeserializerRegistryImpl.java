/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.deserialization;

import java.util.HashMap;
import java.util.Map;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFGeneralDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.MatchDeserializer;
import org.opendaylight.openflowjava.protocol.impl.util.OF10MatchDeserializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores and registers deserializers.
 *
 * @author michal.polkorab
 */
public class DeserializerRegistryImpl implements DeserializerRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(DeserializerRegistryImpl.class);
    private Map<MessageCodeKey, OFGeneralDeserializer> registry;

    /**
     * Decoder table provisioning.
     */
    @Override
    public void init() {
        registry = new HashMap<>();

        // register message deserializers
        MessageDeserializerInitializer.registerMessageDeserializers(this);

        // register additional message deserializers
        AdditionalMessageDeserializerInitializer.registerMessageDeserializers(this);

        // register common structure deserializers
        registerDeserializer(
                new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, EncodeConstants.EMPTY_VALUE, MatchV10.class),
                new OF10MatchDeserializer());
        registerDeserializer(
                new MessageCodeKey(EncodeConstants.OF13_VERSION_ID, EncodeConstants.EMPTY_VALUE, Match.class),
                new MatchDeserializer(this));

        // register match entry deserializers
        MatchEntryDeserializerInitializer.registerMatchEntryDeserializers(this);
        // register action deserializers
        ActionDeserializerInitializer.registerDeserializers(this);
        // register instruction deserializers
        InstructionDeserializerInitializer.registerDeserializers(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends OFGeneralDeserializer> T getDeserializer(final MessageCodeKey key) {
        OFGeneralDeserializer deserializer = registry.get(key);
        if (deserializer == null) {
            throw new IllegalStateException("Deserializer for key: " + key
                    + " was not found - please verify that all needed deserializers ale loaded correctly");
        }
        return (T) deserializer;
    }

    @Override
    public void registerDeserializer(final MessageCodeKey key, final OFGeneralDeserializer deserializer) {
        if (key == null || deserializer == null) {
            throw new IllegalArgumentException("MessageCodeKey or Deserializer is null");
        }
        OFGeneralDeserializer desInRegistry = registry.put(key, deserializer);
        if (desInRegistry != null) {
            LOG.debug("Deserializer for key {} overwritten. Old deserializer: {}, new deserializer: {}", key,
                    desInRegistry.getClass().getName(), deserializer.getClass().getName());
        }
        if (deserializer instanceof DeserializerRegistryInjector) {
            ((DeserializerRegistryInjector) deserializer).injectDeserializerRegistry(this);
        }
    }

    @Override
    public boolean unregisterDeserializer(final MessageCodeKey key) {
        if (key == null) {
            throw new IllegalArgumentException("MessageCodeKey is null");
        }
        OFGeneralDeserializer deserializer = registry.remove(key);
        if (deserializer == null) {
            return false;
        }
        return true;
    }

}
