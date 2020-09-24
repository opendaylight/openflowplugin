/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.TableModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.TableModInputBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for TableModInputMessageFactory.
 *
 * @author timotej.kubas
 * @author michal.polkorab
 */
public class TableModInputMessageFactoryTest {
    private static final byte MESSAGE_TYPE = 17;
    private static final byte PADDING_IN_TABLE_MOD_MESSAGE = 3;
    private SerializerRegistry registry;
    private OFSerializer<TableModInput> tableModFactory;

    /**
     * Initializes serializer registry and stores correct factory in field.
     */
    @Before
    public void startUp() {
        registry = new SerializerRegistryImpl();
        registry.init();
        tableModFactory = registry.getSerializer(
                new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, TableModInput.class));
    }

    /**
     * Testing of {@link TableModInputMessageFactory} for correct translation from POJO.
     */
    @Test
    public void testTableModInput() throws Exception {
        TableModInputBuilder builder = new TableModInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setTableId(new TableId(Uint32.valueOf(9)));
        builder.setConfig(new TableConfig(true));
        TableModInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        tableModFactory.serialize(message, out);

        BufferHelper.checkHeaderV13(out, MESSAGE_TYPE, 16);
        Assert.assertEquals("Wrong TableID", message.getTableId().getValue().intValue(), out.readUnsignedByte());
        out.skipBytes(PADDING_IN_TABLE_MOD_MESSAGE);
        Assert.assertEquals("Wrong TableConfig", 8, out.readUnsignedInt());
    }

}
