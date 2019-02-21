/*
 * Copyright (c) 2019 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import static java.util.Objects.requireNonNull;

import io.netty.buffer.ByteBuf;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer3Match;

/**
 * Abstract base AbstractMatchEntrySerializer to deal with IPv4/IPv6 source/destination polymorphism.
 */
abstract class AbstractPolymorphicEntrySerializer<A extends Layer3Match, N extends Layer3Match, E, M>
        extends AbstractMatchEntrySerializer<Object, Object> {
    private final Class<A> arbitraryClass;
    private final Class<N> normalClass;
    private final Class<E> entryClass;
    private final Class<M> arbitraryMaskClass;

    AbstractPolymorphicEntrySerializer(final int oxmClassCode, final int oxmFieldCode, final int valueLength,
            final Class<A> arbitraryClass, final Class<N> normalClass, final Class<E> entryClass,
            final Class<M> arbitraryMaskClass) {
        super(new ConstantHeaderWriter<>(oxmClassCode, oxmFieldCode, valueLength));
        this.arbitraryClass = requireNonNull(arbitraryClass);
        this.normalClass = requireNonNull(normalClass);
        this.entryClass = requireNonNull(entryClass);
        this.arbitraryMaskClass = requireNonNull(arbitraryMaskClass);
    }

    @Override
    protected final Object extractEntry(final Match match) {
        final Layer3Match layer3Match = match.getLayer3Match();
        if (normalClass.isInstance(layer3Match)) {
            return extractNormalEntry(normalClass.cast(layer3Match));
        } else if (arbitraryClass.isInstance(layer3Match) && useArbitraryEntry(arbitraryClass.cast(layer3Match))) {
            return layer3Match;
        } else {
            return null;
        }
    }

    @Override
    protected final Object extractEntryMask(final Object entry) {
        if (arbitraryClass.isInstance(entry)) {
            return extractArbitraryEntryMask(arbitraryClass.cast(entry));
        }
        return extractNormalEntryMask(entryClass.cast(entry));
    }

    @Override
    protected final void serializeEntry(final Object entry, final Object mask, final ByteBuf outBuffer) {
        if (entryClass.isInstance(entry)) {
            serializeNormalEntry(entryClass.cast(entry), (Integer) mask, outBuffer);
        } else {
            serializeArbitraryEntry(arbitraryClass.cast(entry), arbitraryMaskClass.cast(mask), outBuffer);
        }
    }

    abstract boolean useArbitraryEntry(@NonNull A arbitraryMatch);

    abstract @Nullable E extractNormalEntry(@NonNull N normalMatch);

    abstract @Nullable M extractArbitraryEntryMask(@NonNull A arbitraryMatch);

    abstract @Nullable Integer extractNormalEntryMask(@NonNull E entry);

    abstract void serializeArbitraryEntry(@NonNull A arbitraryMatch, @Nullable M mask, @NonNull ByteBuf outBuffer);

    abstract void serializeNormalEntry(@NonNull E entry, @Nullable Integer mask, @NonNull ByteBuf outBuffer);
}
