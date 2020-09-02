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
 * Stores and handles serializers. <br>
 * K - {@link MessageTypeKey} parameter type,<br>
 * S - returned serializer type
 * @author michal.polkorab
 */
public interface SerializerRegistry extends SerializerLookup {
    /**
     * Serializer registry provisioning.
     */
    void init();


    /**
     * Registers a serializer.
     *
     * <p>
     * Throws IllegalStateException when there is
     * a serializer already registered under given key.
     *
     * <p>
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
     * Unregisters a serializer.
     *
     * @param <K> serializer key type
     * @param key used for serializer lookup
     * @return true if serializer was removed,
     *         false if no serializer was found under specified key
     */
    <K> boolean unregisterSerializer(MessageTypeKey<K> key);

    /**
     * Checks if group add mod messages are enabled.
     * @return true if group add mod messages are enabled
     */
    boolean isGroupAddModEnabled();

    /**
     * Sets whether or not group add mod messages are enabled.
     *
     * @param isGroupAddModEnabled true if group add mod messages are enabled
     */
    void setGroupAddModConfig(boolean isGroupAddModEnabled);
}
