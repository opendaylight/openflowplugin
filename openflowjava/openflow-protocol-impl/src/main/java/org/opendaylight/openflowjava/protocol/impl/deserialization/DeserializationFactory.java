/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization;

import io.netty.buffer.ByteBuf;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.keys.TypeToClassKey;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Factory for deserialization.
 *
 * @author michal.polkorab
 * @author timotej.kubas
 * @author giuseppex.petralia@intel.com
 */
public class DeserializationFactory {
    private final Map<TypeToClassKey, Class<?>> messageClassMap = new ConcurrentHashMap<>();
    private final DeserializerRegistry registry;

    public DeserializationFactory(final DeserializerRegistry registry) {
        this.registry = registry;

        TypeToClassMapInitializer.initializeTypeToClassMap(messageClassMap);

        // Register type to class map for additional deserializers
        TypeToClassMapInitializer.initializeAdditionalTypeToClassMap(messageClassMap);
    }

    /**
     * Transforms ByteBuf into correct POJO message.
     *
     * @param rawMessage the message
     * @param version
     *            version decoded from OpenFlow protocol message
     * @return correct POJO as DataObject
     */
    public DataObject deserialize(final ByteBuf rawMessage, final short version) {
        DataObject dataObject = null;
        int type = rawMessage.readUnsignedByte();
        Class<?> clazz = messageClassMap.get(new TypeToClassKey(version, type));
        rawMessage.skipBytes(Short.BYTES);
        OFDeserializer<DataObject> deserializer = registry.getDeserializer(new MessageCodeKey(version, type, clazz));
        dataObject = deserializer.deserialize(rawMessage);
        return dataObject;
    }

    /**
     * Register new type to class mapping used to assign return type when deserializing message.
     *
     * @param key type to class key
     * @param clazz return class
     */
    public void registerMapping(final TypeToClassKey key, final Class<?> clazz) {
        messageClassMap.put(key, clazz);
    }

    /**
     * Unregister type to class mapping used to assign return type when deserializing message.
     *
     * @param key type to class key
     * @return true if mapping was successfully removed
     */
    public boolean unregisterMapping(final TypeToClassKey key) {
        if (key == null) {
            throw new IllegalArgumentException("TypeToClassKey is null");
        }

        return messageClassMap.remove(key) != null;
    }
}
