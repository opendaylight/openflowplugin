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
import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ControllerRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestInputBuilder;
import org.opendaylight.yangtools.yang.common.Uint64;

/**
 * Unit tests for RoleRequestInputMessageFactory.
 *
 * @author timotej.kubas
 * @author michal.polkorab
 */
public class RoleRequestInputMessageFactoryTest {
    private static final byte MESSAGE_TYPE = 24;
    private static final int MESSAGE_LENGTH = 24;
    private static final byte PADDING_IN_ROLE_REQUEST_MESSAGE = 4;
    private SerializerRegistry registry;
    private OFSerializer<RoleRequestInput> roleFactory;

    /**
     * Initializes serializer registry and stores correct factory in field.
     */
    @Before
    public void startUp() {
        registry = new SerializerRegistryImpl();
        registry.init();
        roleFactory = registry.getSerializer(
                new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, RoleRequestInput.class));
    }

    /**
     * Testing of {@link RoleRequestInputMessageFactory} for correct translation from POJO.
     */
    @Test
    public void testRoleRequestInputMessage() throws Exception {
        RoleRequestInputBuilder builder = new RoleRequestInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setRole(ControllerRole.forValue(2));
        builder.setGenerationId(Uint64.valueOf("0xFF01010101010101", 16));
        RoleRequestInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        roleFactory.serialize(message, out);

        BufferHelper.checkHeaderV13(out, MESSAGE_TYPE, MESSAGE_LENGTH);
        Assert.assertEquals("Wrong role", message.getRole().getIntValue(),
                ControllerRole.forValue((int) out.readUnsignedInt()).getIntValue());
        out.skipBytes(PADDING_IN_ROLE_REQUEST_MESSAGE);
        byte[] genId = new byte[Long.BYTES];
        out.readBytes(genId);
        Assert.assertEquals("Wrong generation ID", message.getGenerationId(), Uint64.valueOf(new BigInteger(1, genId)));
    }
}
