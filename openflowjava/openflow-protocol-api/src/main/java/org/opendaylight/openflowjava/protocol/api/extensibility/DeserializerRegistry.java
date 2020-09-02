/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.extensibility;

import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;

/**
 * Registry for deserializers.
 *
 * @author michal.polkorab
 */
public interface DeserializerRegistry {

    /**
     * Initializes deserializers.
     */
    void init();

    /**
     * Gets the deserializer for the given key.
     *
     * @param <T> type of particular deserializer
     * @param key used for deserializer lookup
     * @return deserializer found
     */
    <T extends OFGeneralDeserializer>
            T getDeserializer(MessageCodeKey key);

    /**
     * Registers a deserializer.
     * Throws IllegalStateException when there is
     * a deserializer already registered under given key.
     *
     * @param key used for deserializer lookup
     * @param deserializer deserializer instance
     */
    void registerDeserializer(MessageCodeKey key,
            OFGeneralDeserializer deserializer);

    /**
     * Unregisters a deserializer.
     *
     * @param key used for deserializer lookup
     * @return true if deserializer was removed,
     *     false if no deserializer was found under specified key
     */
    boolean unregisterDeserializer(MessageCodeKey key);
}
