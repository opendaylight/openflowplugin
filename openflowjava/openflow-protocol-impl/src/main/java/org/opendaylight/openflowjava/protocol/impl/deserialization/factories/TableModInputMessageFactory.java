/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint8;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.TableModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.TableModInputBuilder;

/**
 * Translates TableModInput messages.
 *
 * @author giuseppex.petralia@intel.com
 */
public class TableModInputMessageFactory implements OFDeserializer<TableModInput> {

    private static final byte PADDING_IN_TABLE_MOD_MESSAGE = 3;

    @Override
    public TableModInput deserialize(ByteBuf rawMessage) {
        TableModInputBuilder builder = new TableModInputBuilder();
        builder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        builder.setXid(readUint32(rawMessage));
        builder.setTableId(new TableId(readUint8(rawMessage).toUint32()));
        rawMessage.skipBytes(PADDING_IN_TABLE_MOD_MESSAGE);
        builder.setConfig(createTableConfig(rawMessage.readUnsignedInt()));
        return builder.build();
    }

    private static TableConfig createTableConfig(long input) {
        boolean deprecated = (input & 3) != 0;
        return new TableConfig(deprecated);
    }
}
