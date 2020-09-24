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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.SwitchConfigFlag;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInputBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;

/**
 * Unit tests for SetConfigMessageFactory.
 *
 * @author timotej.kubas
 * @author michal.polkorab
 */
public class SetConfigMessageFactoryTest {
    private static final byte MESSAGE_TYPE = 9;
    private static final int MESSAGE_LENGTH = 12;
    private SerializerRegistry registry;
    private OFSerializer<SetConfigInput> setConfigFactory;

    /**
     * Initializes serializer registry and stores correct factory in field.
     */
    @Before
    public void startUp() {
        registry = new SerializerRegistryImpl();
        registry.init();
        setConfigFactory = registry.getSerializer(
                new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, SetConfigInput.class));
    }

    /**
     * Testing of {@link SetConfigMessageFactory} for correct translation from POJO.
     */
    @Test
    public void testSetConfigMessageV13() throws Exception {
        SetConfigInputBuilder builder = new SetConfigInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        SwitchConfigFlag flag = SwitchConfigFlag.FRAGNORMAL;
        builder.setFlags(flag);
        builder.setMissSendLen(Uint16.TEN);
        SetConfigInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        setConfigFactory.serialize(message, out);

        BufferHelper.checkHeaderV13(out, MESSAGE_TYPE, MESSAGE_LENGTH);
        Assert.assertEquals("Wrong flags", SwitchConfigFlag.FRAGNORMAL.getIntValue(), out.readUnsignedShort());
        Assert.assertEquals("Wrong missSendLen", 10, out.readUnsignedShort());
    }

    /**
     * Testing of {@link SetConfigMessageFactory} for correct translation from POJO.
     */
    @Test
    public void testSetConfigMessageV10() throws Exception {
        SetConfigInputBuilder builder = new SetConfigInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF10_VERSION_ID);
        SwitchConfigFlag flag = SwitchConfigFlag.OFPCFRAGDROP;
        builder.setFlags(flag);
        builder.setMissSendLen(Uint16.valueOf(85));
        SetConfigInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        setConfigFactory.serialize(message, out);

        BufferHelper.checkHeaderV10(out, MESSAGE_TYPE, MESSAGE_LENGTH);
        Assert.assertEquals("Wrong flags", SwitchConfigFlag.OFPCFRAGDROP.getIntValue(), out.readUnsignedShort());
        Assert.assertEquals("Wrong missSendLen", 85, out.readUnsignedShort());
    }
}
