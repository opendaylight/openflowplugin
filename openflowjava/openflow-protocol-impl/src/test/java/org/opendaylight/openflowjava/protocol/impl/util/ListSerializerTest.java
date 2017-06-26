/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;

/**
 * @author michal.polkorab
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ListSerializerTest {

    @Mock TypeKeyMaker<Action> keyMaker;
    @Mock SerializerRegistry registry;

    /**
     * Tests {@link ListSerializer#serializeHeaderList(List, TypeKeyMaker, SerializerRegistry, ByteBuf)}
     * with null List
     */
    @Test
    public void test() {
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        ListSerializer.serializeHeaderList(null, keyMaker, registry, buffer);

        Assert.assertEquals("Data written to buffer", 0, buffer.readableBytes());
    }
}