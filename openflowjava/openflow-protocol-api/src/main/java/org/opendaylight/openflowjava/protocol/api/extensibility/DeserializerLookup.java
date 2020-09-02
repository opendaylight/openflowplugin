/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.extensibility;

import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;

public interface DeserializerLookup {
    /**
     * Gets the deserializer for the given key.
     *
     * @param <T> type of particular deserializer
     * @param key used for deserializer lookup
     * @return deserializer found
     */
    <T extends OFGeneralDeserializer> T getDeserializer(MessageCodeKey key);

}
