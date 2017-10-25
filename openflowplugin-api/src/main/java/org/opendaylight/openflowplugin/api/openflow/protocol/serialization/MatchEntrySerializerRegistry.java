/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.protocol.serialization;

public interface MatchEntrySerializerRegistry {

    /**
     * Registers match entry serializer.

     * @param key used for deserializer lookup
     * @param serializer serializer instance
     */
    void registerEntrySerializer(MatchEntrySerializerKey key, MatchEntrySerializer serializer);

    /**
     * Unregisters match entry serializer.
     * @param key used for serializer lookup
     * @return true if serializer was removed, false if no serializer was found under specified key
     */
    boolean unregisterEntrySerializer(MatchEntrySerializerKey key);

}
