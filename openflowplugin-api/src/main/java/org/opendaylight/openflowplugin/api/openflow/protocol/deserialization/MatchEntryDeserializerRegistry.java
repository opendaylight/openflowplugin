/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.protocol.deserialization;

import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;

public interface MatchEntryDeserializerRegistry {

    /**
     * Registers match entry deserializer.
     *
     * @param key          used for deserializer lookup
     * @param deserializer deserializer instance
     */
    void registerEntryDeserializer(MatchEntryDeserializerKey key, MatchEntryDeserializer deserializer);

    /**
     * Unregisters match entry deserializer.
     *
     * @param key used for deserializer lookup
     * @return true if deserializer was removed, false if no deserializer was found under specified key
     */
    boolean unregisterEntryDeserializer(MatchEntryDeserializerKey key);

}
