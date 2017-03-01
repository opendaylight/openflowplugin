/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableFeaturesPropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.TableFeaturePropType;

public abstract class AbstractTablePropertySerializer<T extends TableFeaturePropType> implements OFSerializer<TableFeaturePropType> {

    @Override
    public void serialize(final TableFeaturePropType tableFeaturePropType, final ByteBuf byteBuf) {
        final int startIndex = byteBuf.writerIndex();
        byteBuf.writeShort(getType().getIntValue());
        final int lengthIndex = byteBuf.writerIndex();
        byteBuf.writeShort(EncodeConstants.EMPTY_LENGTH);

        serializeProperty(getClazz().cast(tableFeaturePropType), byteBuf);

        final int length = byteBuf.writerIndex() - startIndex;
        byteBuf.setShort(lengthIndex, length);

        int paddingRemainder = length % EncodeConstants.PADDING;
        int padding = 0;

        if (paddingRemainder != 0) {
            padding = EncodeConstants.PADDING - paddingRemainder;
        }

        byteBuf.writeZero(padding);
    }

    protected abstract void serializeProperty(final T property, final ByteBuf byteBuf);
    protected abstract TableFeaturesPropType getType();
    protected abstract Class<T> getClazz();

}
