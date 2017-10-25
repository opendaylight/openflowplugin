/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.function.Consumer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.impl.protocol.serialization.AbstractSerializerTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.TableFeaturePropType;

public abstract class AbstractTablePropertySerializerTest extends AbstractSerializerTest {
    private OFSerializer<TableFeaturePropType> serializer;

    @Override
    protected void init() {
        serializer = getRegistry().getSerializer(new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, getClazz()));
    }

    protected void assertProperty(final TableFeaturePropType property, final Consumer<ByteBuf> assertBody) {
        // Header and body serialization
        final ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        serializer.serialize(property, buffer);
        assertEquals(buffer.readUnsignedShort(), getType());
        final int length = buffer.readUnsignedShort();
        assertBody.accept(buffer);

        int paddingRemainder = length % EncodeConstants.PADDING;
        int padding = 0;

        if (paddingRemainder != 0) {
            padding = EncodeConstants.PADDING - paddingRemainder;
        }

        buffer.skipBytes(padding);
        assertEquals(buffer.readableBytes(), 0);
    }

    protected OFSerializer<TableFeaturePropType> getSerializer() {
        return serializer;
    }

    protected abstract Class<? extends TableFeaturePropType> getClazz();

    protected abstract int getType();
}
