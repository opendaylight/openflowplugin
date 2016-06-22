/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.nx.api.impl;

import static org.junit.Assert.assertNull;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.nx.api.NiciraActionDeserializerKey;
import org.opendaylight.openflowjava.nx.api.NiciraConstants;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;

@RunWith(MockitoJUnitRunner.class)
public class ActionDeserializerTest {

    ActionDeserializer actionDeserializer;
    ByteBuf buffer;
    NiciraExtensionCodecRegistratorImpl niciraExtensionCodecRegistrator;
    List<SwitchConnectionProvider> providers;


    @Mock
    OFDeserializer<Action> deserializer;
    @Mock
    SwitchConnectionProvider provider;
    @Mock
    OFDeserializer<Action> ofDeserializer;




    private static final short VERSION = 4;
    private static final long EXPERIMENT_ID = NiciraConstants.NX_VENDOR_ID;
    private static final byte SUBTYPE = 10;

    @Before
    public void setUp() {
        actionDeserializer = new ActionDeserializer(VERSION);
        buffer = ByteBufAllocator.DEFAULT.buffer();
        providers = new LinkedList<>();


    }


    /**
     * If NiciraExtensionCodecRegistratorImpl.getActionDeserializer(actionSerializerKey) returns NULL
     * then NULL should be returned
     */
    @Test
    public void deserializeTest() {
        createBuffer(buffer);
        assertNull(actionDeserializer.deserialize(buffer));
    }

    /**
     * If experimentId is different from NiciraConstants.NX_VENDOR_ID
     * then exception should be thrown
     */
    @Test(expected = IllegalStateException.class)
    public void deserializeTest1() {
        createBufferWithWrongExperimentId(buffer);
        actionDeserializer.deserialize(buffer);
    }

    @Test
    public void deserializeTest2() {
        createBuffer(buffer);
        addRecordToMap();

        actionDeserializer.deserialize(buffer);

        Mockito.verify(deserializer).deserialize(buffer);
    }

    private void createBuffer(ByteBuf message) {
        //size of experiment type
        message.writeShort(1);
        //size of length
        message.writeShort(13);
        //experimentId
        message.writeInt((int)EXPERIMENT_ID);
        //subtype
        message.writeShort(SUBTYPE);
    }

    private void createBufferWithWrongExperimentId(ByteBuf message) {
        //size of experiment type
        message.writeShort(1);
        //size of length
        message.writeShort(13);
        //experimentId
        message.writeInt(1);
        //subtype
        message.writeShort(SUBTYPE);
    }

    private void addRecordToMap() {
        niciraExtensionCodecRegistrator = new NiciraExtensionCodecRegistratorImpl(providers);
        NiciraActionDeserializerKey key = new NiciraActionDeserializerKey(VERSION, SUBTYPE);

        niciraExtensionCodecRegistrator.registerActionDeserializer(key, deserializer);
    }

}