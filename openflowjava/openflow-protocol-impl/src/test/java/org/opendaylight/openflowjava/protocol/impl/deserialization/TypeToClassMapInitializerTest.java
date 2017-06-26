/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.deserialization;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.keys.TypeToClassKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.TableModInput;

/**
 * Test for {@link org.opendaylight.openflowjava.protocol.impl.deserialization.TypeToClassMapInitializer}.
 * @author michal.polkorab
 * @author giuseppex.petralia@intel.com
 */
public class TypeToClassMapInitializerTest {

    private Map<TypeToClassKey, Class<?>> messageClassMap;

    @Test
    public void test() {
        messageClassMap = new HashMap<>();
        TypeToClassMapInitializer.initializeTypeToClassMap(messageClassMap);

        short version = EncodeConstants.OF10_VERSION_ID;
        assertEquals("Wrong class", HelloMessage.class, messageClassMap.get(new TypeToClassKey(version, 0)));
        assertEquals("Wrong class", ErrorMessage.class, messageClassMap.get(new TypeToClassKey(version, 1)));
        assertEquals("Wrong class", EchoRequestMessage.class, messageClassMap.get(new TypeToClassKey(version, 2)));
        assertEquals("Wrong class", EchoOutput.class, messageClassMap.get(new TypeToClassKey(version, 3)));
        assertEquals("Wrong class", ExperimenterMessage.class, messageClassMap.get(new TypeToClassKey(version, 4)));
        assertEquals("Wrong class", GetFeaturesOutput.class, messageClassMap.get(new TypeToClassKey(version, 6)));
        assertEquals("Wrong class", GetConfigOutput.class, messageClassMap.get(new TypeToClassKey(version, 8)));
        assertEquals("Wrong class", PacketInMessage.class, messageClassMap.get(new TypeToClassKey(version, 10)));
        assertEquals("Wrong class", FlowRemovedMessage.class, messageClassMap.get(new TypeToClassKey(version, 11)));
        assertEquals("Wrong class", PortStatusMessage.class, messageClassMap.get(new TypeToClassKey(version, 12)));
        assertEquals("Wrong class", MultipartReplyMessage.class, messageClassMap.get(new TypeToClassKey(version, 17)));
        assertEquals("Wrong class", BarrierOutput.class, messageClassMap.get(new TypeToClassKey(version, 19)));
        assertEquals("Wrong class", GetQueueConfigOutput.class, messageClassMap.get(new TypeToClassKey(version, 21)));

        version = EncodeConstants.OF13_VERSION_ID;
        assertEquals("Wrong class", HelloMessage.class, messageClassMap.get(new TypeToClassKey(version, 0)));
        assertEquals("Wrong class", ErrorMessage.class, messageClassMap.get(new TypeToClassKey(version, 1)));
        assertEquals("Wrong class", EchoRequestMessage.class, messageClassMap.get(new TypeToClassKey(version, 2)));
        assertEquals("Wrong class", EchoOutput.class, messageClassMap.get(new TypeToClassKey(version, 3)));
        assertEquals("Wrong class", ExperimenterMessage.class, messageClassMap.get(new TypeToClassKey(version, 4)));
        assertEquals("Wrong class", GetFeaturesOutput.class, messageClassMap.get(new TypeToClassKey(version, 6)));
        assertEquals("Wrong class", GetConfigOutput.class, messageClassMap.get(new TypeToClassKey(version, 8)));
        assertEquals("Wrong class", PacketInMessage.class, messageClassMap.get(new TypeToClassKey(version, 10)));
        assertEquals("Wrong class", FlowRemovedMessage.class, messageClassMap.get(new TypeToClassKey(version, 11)));
        assertEquals("Wrong class", PortStatusMessage.class, messageClassMap.get(new TypeToClassKey(version, 12)));
        assertEquals("Wrong class", MultipartReplyMessage.class, messageClassMap.get(new TypeToClassKey(version, 19)));
        assertEquals("Wrong class", BarrierOutput.class, messageClassMap.get(new TypeToClassKey(version, 21)));
        assertEquals("Wrong class", GetQueueConfigOutput.class, messageClassMap.get(new TypeToClassKey(version, 23)));
        assertEquals("Wrong class", RoleRequestOutput.class, messageClassMap.get(new TypeToClassKey(version, 25)));
        assertEquals("Wrong class", GetAsyncOutput.class, messageClassMap.get(new TypeToClassKey(version, 27)));

        version = EncodeConstants.OF14_VERSION_ID;
        assertEquals("Wrong class", HelloMessage.class, messageClassMap.get(new TypeToClassKey(version, 0)));
        assertEquals("Wrong class", EchoRequestMessage.class, messageClassMap.get(new TypeToClassKey(version, 2)));
        assertEquals("Wrong class", EchoOutput.class, messageClassMap.get(new TypeToClassKey(version, 3)));
        assertEquals("Wrong class", GetConfigOutput.class, messageClassMap.get(new TypeToClassKey(version, 8)));
        assertEquals("Wrong class", BarrierOutput.class, messageClassMap.get(new TypeToClassKey(version, 21)));

        version = EncodeConstants.OF15_VERSION_ID;
        assertEquals("Wrong class", HelloMessage.class, messageClassMap.get(new TypeToClassKey(version, 0)));
        assertEquals("Wrong class", EchoRequestMessage.class, messageClassMap.get(new TypeToClassKey(version, 2)));
        assertEquals("Wrong class", EchoOutput.class, messageClassMap.get(new TypeToClassKey(version, 3)));
        assertEquals("Wrong class", GetConfigOutput.class, messageClassMap.get(new TypeToClassKey(version, 8)));
        assertEquals("Wrong class", BarrierOutput.class, messageClassMap.get(new TypeToClassKey(version, 21)));
    }

    @Test
    public void testAdditionalTypes() {
        messageClassMap = new HashMap<>();
        TypeToClassMapInitializer.initializeAdditionalTypeToClassMap(messageClassMap);

        short version = EncodeConstants.OF10_VERSION_ID;
        assertEquals("Wrong class", GetFeaturesInput.class, messageClassMap.get(new TypeToClassKey(version, 5)));
        assertEquals("Wrong class", GetConfigInput.class, messageClassMap.get(new TypeToClassKey(version, 7)));
        assertEquals("Wrong class", SetConfigInput.class, messageClassMap.get(new TypeToClassKey(version, 9)));
        assertEquals("Wrong class", PacketOutInput.class, messageClassMap.get(new TypeToClassKey(version, 13)));
        assertEquals("Wrong class", FlowModInput.class, messageClassMap.get(new TypeToClassKey(version, 14)));
        assertEquals("Wrong class", PortModInput.class, messageClassMap.get(new TypeToClassKey(version, 15)));
        assertEquals("Wrong class", MultipartRequestInput.class, messageClassMap.get(new TypeToClassKey(version, 16)));
        assertEquals("Wrong class", BarrierInput.class, messageClassMap.get(new TypeToClassKey(version, 18)));
        assertEquals("Wrong class", GetQueueConfigInput.class, messageClassMap.get(new TypeToClassKey(version, 20)));

        version = EncodeConstants.OF13_VERSION_ID;
        assertEquals("Wrong class", GetFeaturesInput.class, messageClassMap.get(new TypeToClassKey(version, 5)));
        assertEquals("Wrong class", GetConfigInput.class, messageClassMap.get(new TypeToClassKey(version, 7)));
        assertEquals("Wrong class", SetConfigInput.class, messageClassMap.get(new TypeToClassKey(version, 9)));
        assertEquals("Wrong class", PacketOutInput.class, messageClassMap.get(new TypeToClassKey(version, 13)));
        assertEquals("Wrong class", FlowModInput.class, messageClassMap.get(new TypeToClassKey(version, 14)));
        assertEquals("Wrong class", GroupModInput.class, messageClassMap.get(new TypeToClassKey(version, 15)));
        assertEquals("Wrong class", PortModInput.class, messageClassMap.get(new TypeToClassKey(version, 16)));
        assertEquals("Wrong class", TableModInput.class, messageClassMap.get(new TypeToClassKey(version, 17)));
        assertEquals("Wrong class", MultipartRequestInput.class, messageClassMap.get(new TypeToClassKey(version, 18)));
        assertEquals("Wrong class", BarrierInput.class, messageClassMap.get(new TypeToClassKey(version, 20)));
        assertEquals("Wrong class", GetQueueConfigInput.class, messageClassMap.get(new TypeToClassKey(version, 22)));
        assertEquals("Wrong class", RoleRequestInput.class, messageClassMap.get(new TypeToClassKey(version, 24)));
        assertEquals("Wrong class", GetAsyncInput.class, messageClassMap.get(new TypeToClassKey(version, 26)));
        assertEquals("Wrong class", SetAsyncInput.class, messageClassMap.get(new TypeToClassKey(version, 28)));
        assertEquals("Wrong class", MeterModInput.class, messageClassMap.get(new TypeToClassKey(version, 29)));

        version = EncodeConstants.OF14_VERSION_ID;
        assertEquals("Wrong class", GetConfigInput.class, messageClassMap.get(new TypeToClassKey(version, 7)));
        assertEquals("Wrong class", SetConfigInput.class, messageClassMap.get(new TypeToClassKey(version, 9)));
        assertEquals("Wrong class", BarrierInput.class, messageClassMap.get(new TypeToClassKey(version, 20)));

        version = EncodeConstants.OF15_VERSION_ID;
        assertEquals("Wrong class", GetConfigInput.class, messageClassMap.get(new TypeToClassKey(version, 7)));
        assertEquals("Wrong class", SetConfigInput.class, messageClassMap.get(new TypeToClassKey(version, 9)));
        assertEquals("Wrong class", BarrierInput.class, messageClassMap.get(new TypeToClassKey(version, 20)));
    }

}