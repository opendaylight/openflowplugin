/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.util;

import io.netty.buffer.ByteBuf;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerLookup;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Serializes list items and their headers.
 *
 * @author michal.polkorab
 */
public final class ListSerializer {
    private ListSerializer() {
        //not called
    }

    /**
     * Serializes item list.
     *
     * @param list list of items to be serialized
     * @param keyMaker creates key for registry lookup
     * @param registry stores serializers
     * @param outBuffer output buffer
     */
    public static <E extends DataObject> void serializeList(final List<E> list,
            final TypeKeyMaker<E> keyMaker, final SerializerLookup registry, final ByteBuf outBuffer) {
        if (list != null) {
            for (E item : list) {
                OFSerializer<E> serializer = registry.getSerializer(keyMaker.make(item));
                serializer.serialize(item, outBuffer);
            }
        }
    }

    /**
     * Serializes headers of items in list.
     *
     * @param list list of items to be serialized
     * @param keyMaker creates key for registry lookup
     * @param registry stores serializers
     * @param outBuffer output buffer
     */
    public static <E extends DataObject> void serializeHeaderList(final List<E> list,
            final TypeKeyMaker<E> keyMaker, final SerializerLookup registry, final ByteBuf outBuffer) {
        if (list != null) {
            for (E item : list) {
                HeaderSerializer<E> serializer = registry.getSerializer(keyMaker.make(item));
                serializer.serializeHeader(item, outBuffer);
            }
        }
    }
}
