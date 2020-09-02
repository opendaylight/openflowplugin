/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.extensibility;

import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;

public interface SerializerLookup {
    /**
     * Gets the serializer for the given type.
     *
     * @param <K> input key type
     * @param <S> type of resulting serializer
     * @param msgTypeKey lookup key
     * @return serializer or NullPointerException if no serializer was found
     */
    <K, S extends OFGeneralSerializer> S getSerializer(MessageTypeKey<K> msgTypeKey);
}
