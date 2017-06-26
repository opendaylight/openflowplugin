/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.util;

import org.opendaylight.openflowjava.protocol.api.extensibility.OFGeneralSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.util.ExperimenterSerializerKeyFactory;

/**
 * Helper class for serializer registration.
 * @author michal.polkorab
 */
public class CommonMessageRegistryHelper {

    private short version;
    private SerializerRegistry serializerRegistry;

    /**
     * @param version wire protocol version
     * @param serializerRegistry registry to be filled with message serializers
     */
    public CommonMessageRegistryHelper(short version, SerializerRegistry serializerRegistry) {
        this.version = version;
        this.serializerRegistry = serializerRegistry;
    }

    /**
     * Registers serializer in registry.
     * @param msgType class of object that will be serialized by given serializer
     * @param serializer serializer instance
     */
    public void registerSerializer(Class<?> msgType, OFGeneralSerializer serializer) {
        serializerRegistry.registerSerializer(new MessageTypeKey<>(version, msgType), serializer);
    }

}
