/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.util;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * @author michal.polkorab
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ListDeserializerTest {

    @Mock CodeKeyMaker keyMaker;
    @Mock DeserializerRegistry registry;

    /**
     * Tests {@link ListDeserializer#deserializeList(short, int, ByteBuf, CodeKeyMaker, DeserializerRegistry)}
     */
    @Test
    public void test() {
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        List<DataObject> list = ListDeserializer.deserializeList(EncodeConstants.OF13_VERSION_ID,
                42, buffer, keyMaker, registry);

        Assert.assertNull("List is not null", list);
    }

    /**
     * Tests {@link ListDeserializer#deserializeHeaders(short, int, ByteBuf, CodeKeyMaker, DeserializerRegistry)}
     */
    @Test
    public void test2() {
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        List<DataObject> list = ListDeserializer.deserializeHeaders(EncodeConstants.OF13_VERSION_ID,
                42, buffer, keyMaker, registry);

        Assert.assertNull("List is not null", list);
    }
}