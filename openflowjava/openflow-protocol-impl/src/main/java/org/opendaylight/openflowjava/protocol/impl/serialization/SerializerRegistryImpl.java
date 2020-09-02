/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.serialization;

import java.util.HashMap;
import java.util.Map;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFGeneralSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.OF10MatchSerializer;
import org.opendaylight.openflowjava.protocol.impl.util.OF13MatchSerializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores and handles serializers. <br>
 * K - {@link MessageTypeKey} type<br>
 * S - returned serializer type
 *
 * @author michal.polkorab
 * @author timotej.kubas
 * @author giuseppex.petralia@intel.com
 *
 */
public class SerializerRegistryImpl implements SerializerRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(SerializerRegistryImpl.class);
    private static final short OF10 = EncodeConstants.OF10_VERSION_ID;
    private static final short OF13 = EncodeConstants.OF13_VERSION_ID;
    private Map<MessageTypeKey<?>, OFGeneralSerializer> registry;

    private boolean isGroupAddModEnabled = false;

    @Override
    public void init() {
        registry = new HashMap<>();
        // Openflow message type serializers
        MessageFactoryInitializer.registerMessageSerializers(this);

        // Register Additional serializers
        AdditionalMessageFactoryInitializer.registerMessageSerializers(this);

        // match structure serializers
        registerSerializer(new MessageTypeKey<>(OF10, MatchV10.class), new OF10MatchSerializer());
        registerSerializer(new MessageTypeKey<>(OF13, Match.class), new OF13MatchSerializer(this));

        // match entry serializers
        MatchEntriesInitializer.registerMatchEntrySerializers(this);
        // action serializers
        ActionsInitializer.registerActionSerializers(this);
        // instruction serializers
        InstructionsInitializer.registerInstructionSerializers(this);
    }

    @Override
    public void setGroupAddModConfig(final boolean value) {
        this.isGroupAddModEnabled = value;
    }

    @Override
    public boolean isGroupAddModEnabled() {
        return isGroupAddModEnabled;
    }

    /**
     * Gets the encoder for the given message type key.
     *
     * @param msgTypeKey the message type key
     * @return encoder for current type of message (msgTypeKey)
     */
    @Override
    @SuppressWarnings("unchecked")
    public <K, S extends OFGeneralSerializer> S getSerializer(final MessageTypeKey<K> msgTypeKey) {
        OFGeneralSerializer serializer = registry.get(msgTypeKey);
        if (serializer == null) {
            throw new IllegalStateException("Serializer for key: " + msgTypeKey
                    + " was not found - please verify that you are using correct message"
                    + " combination (e.g. OF v1.0 message to OF v1.0 device)");
        }
        return (S) serializer;
    }

    @Override
    public <K> void registerSerializer(final MessageTypeKey<K> msgTypeKey, final OFGeneralSerializer serializer) {
        if (msgTypeKey == null || serializer == null) {
            throw new IllegalArgumentException("MessageTypeKey or Serializer is null");
        }
        OFGeneralSerializer serInRegistry = registry.put(msgTypeKey, serializer);
        if (serInRegistry != null) {
            LOG.debug("Serializer for key {} overwritten. Old serializer: {}, new serializer: {}", msgTypeKey,
                    serInRegistry.getClass().getName(), serializer.getClass().getName());
        }
    }

    @Override
    public <K> boolean unregisterSerializer(final MessageTypeKey<K> msgTypeKey) {
        if (msgTypeKey == null) {
            throw new IllegalArgumentException("MessageTypeKey is null");
        }
        OFGeneralSerializer serializer = registry.remove(msgTypeKey);
        if (serializer == null) {
            return false;
        }
        return true;
    }
}
