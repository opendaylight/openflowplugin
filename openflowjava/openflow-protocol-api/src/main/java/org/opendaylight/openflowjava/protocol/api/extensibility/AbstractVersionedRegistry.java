/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.extensibility;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * A specialized OpenFlow-version-specific registry. The implementation provides a quick dispatch based on an
 * {@link Uint8} protocol version. It only supports versions from {@link EncodeConstants}.
 *
 * @param <T> Item type
 */
@Beta
public abstract class AbstractVersionedRegistry<T> implements Immutable {
    private final T[] items;

    protected AbstractVersionedRegistry(final Map<Uint8, T> versionToItem) {
        final Object[] tmp = new Object[4];
        for (Entry<Uint8, T> entry : versionToItem.entrySet()) {
            final short version = entry.getKey().toJava();
            switch (version) {
                case EncodeConstants.OF10_VERSION_ID:
                    tmp[0] = requireNonNull(entry.getValue());
                    break;
                case EncodeConstants.OF13_VERSION_ID:
                    tmp[1] = requireNonNull(entry.getValue());
                    break;
                case EncodeConstants.OF14_VERSION_ID:
                    tmp[2] = requireNonNull(entry.getValue());
                    break;
                case EncodeConstants.OF15_VERSION_ID:
                    tmp[3] = requireNonNull(entry.getValue());
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported version " + version);
            }
        }

        @SuppressWarnings("unchecked")
        final T[] cast = (T[]) tmp;
        this.items = cast;
    }

    public final @Nullable T lookupItem(final @NonNull Uint8 version) {
        // Note we do not perform sign conversion as all supported values are positive anyway
        // TODO: consider using a direct lookup (and a sparse array)
        switch (version.byteValue()) {
            case EncodeConstants.OF10_VERSION_ID:
                return items[0];
            case EncodeConstants.OF13_VERSION_ID:
                return items[1];
            case EncodeConstants.OF14_VERSION_ID:
                return items[2];
            case EncodeConstants.OF15_VERSION_ID:
                return items[3];
            default:
                return null;
        }
    }
}
