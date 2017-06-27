/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.extensibility;

import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;

/**
 * Stores and handles serializers<br>
 * K - {@link MessageTypeKey} parameter type,<br>
 * S - returned serializer type
 * @author michal.polkorab
 */
public interface SerializerRegistry {

    /**
     * Serializer registry provisioning
     */
    void init();

    /**
     * @param <K> input key type
     * @param <S> type of resulting serializer
     * @param msgTypeKey lookup key
     * @return serializer or NullPointerException if no serializer was found
     */
    <K, S extends OFGeneralSerializer> S getSerializer(MessageTypeKey<K> msgTypeKey);

    /**
     * Registers serializer
     * Throws IllegalStateException when there is
     * a serializer already registered under given key.
     *
     * If the serializer implements {@link SerializerRegistryInjector} interface,
     * the serializer is injected with SerializerRegistry instance.
     *
     * @param <K> serializer key type
     * @param key used for serializer lookup
     * @param serializer serializer implementation
     */
    <K> void registerSerializer(MessageTypeKey<K> key,
            OFGeneralSerializer serializer);

    /**
     * Unregisters serializer
     * @param <K> serializer key type
     * @param key used for serializer lookup
     * @return true if serializer was removed,
     *  false if no serializer was found under specified key
     */
    <K> boolean unregisterSerializer(MessageTypeKey<K> key);
}
