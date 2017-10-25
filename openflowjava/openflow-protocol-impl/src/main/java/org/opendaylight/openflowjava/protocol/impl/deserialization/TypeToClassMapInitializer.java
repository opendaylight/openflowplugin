/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization;

import java.util.Map;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.TypeToClassInitHelper;
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
 * Util class for init OF message type to class mapping.
 * @author michal.polkorab
 * @author giuseppex.petralia@intel.com
 */
public final class TypeToClassMapInitializer {

    private TypeToClassMapInitializer() {
        throw new UnsupportedOperationException("Utility class shouldn't be instantiated");
    }

    /**
     * Initializes standard types mapping.
     * @param messageClassMap type to class map
     */
    public static void initializeTypeToClassMap(final Map<TypeToClassKey, Class<?>> messageClassMap) {
        TypeToClassInitHelper helper;

        // init OF v1.0 mapping
        helper = new TypeToClassInitHelper(EncodeConstants.OF10_VERSION_ID, messageClassMap);
        helper.registerTypeToClass((short) 0, HelloMessage.class);
        helper.registerTypeToClass((short) 1, ErrorMessage.class);
        helper.registerTypeToClass((short) 2, EchoRequestMessage.class);
        helper.registerTypeToClass((short) 3, EchoOutput.class);
        helper.registerTypeToClass((short) 4, ExperimenterMessage.class);
        helper.registerTypeToClass((short) 6, GetFeaturesOutput.class);
        helper.registerTypeToClass((short) 8, GetConfigOutput.class);
        helper.registerTypeToClass((short) 10, PacketInMessage.class);
        helper.registerTypeToClass((short) 11, FlowRemovedMessage.class);
        helper.registerTypeToClass((short) 12, PortStatusMessage.class);
        helper.registerTypeToClass((short) 17, MultipartReplyMessage.class);
        helper.registerTypeToClass((short) 19, BarrierOutput.class);
        helper.registerTypeToClass((short) 21, GetQueueConfigOutput.class);

        // init OF v1.3 mapping
        helper = new TypeToClassInitHelper(EncodeConstants.OF13_VERSION_ID, messageClassMap);
        helper.registerTypeToClass((short) 0, HelloMessage.class);
        helper.registerTypeToClass((short) 1, ErrorMessage.class);
        helper.registerTypeToClass((short) 2, EchoRequestMessage.class);
        helper.registerTypeToClass((short) 3, EchoOutput.class);
        helper.registerTypeToClass((short) 4, ExperimenterMessage.class);
        helper.registerTypeToClass((short) 6, GetFeaturesOutput.class);
        helper.registerTypeToClass((short) 8, GetConfigOutput.class);
        helper.registerTypeToClass((short) 10, PacketInMessage.class);
        helper.registerTypeToClass((short) 11, FlowRemovedMessage.class);
        helper.registerTypeToClass((short) 12, PortStatusMessage.class);
        helper.registerTypeToClass((short) 19, MultipartReplyMessage.class);
        helper.registerTypeToClass((short) 21, BarrierOutput.class);
        helper.registerTypeToClass((short) 23, GetQueueConfigOutput.class);
        helper.registerTypeToClass((short) 25, RoleRequestOutput.class);
        helper.registerTypeToClass((short) 27, GetAsyncOutput.class);

        // init OF v1.4 mapping
        helper = new TypeToClassInitHelper(EncodeConstants.OF14_VERSION_ID, messageClassMap);
        helper.registerTypeToClass((short) 0, HelloMessage.class);
        helper.registerTypeToClass((short) 2, EchoRequestMessage.class);
        helper.registerTypeToClass((short) 3, EchoOutput.class);
        helper.registerTypeToClass((short) 8, GetConfigOutput.class);
        helper.registerTypeToClass((short) 21, BarrierOutput.class);

        // init OF v1.5 mapping
        helper = new TypeToClassInitHelper(EncodeConstants.OF15_VERSION_ID, messageClassMap);
        helper.registerTypeToClass((short) 0, HelloMessage.class);
        helper.registerTypeToClass((short) 2, EchoRequestMessage.class);
        helper.registerTypeToClass((short) 3, EchoOutput.class);
        helper.registerTypeToClass((short) 8, GetConfigOutput.class);
        helper.registerTypeToClass((short) 21, BarrierOutput.class);
    }

    /**
     * Initializes additional types mapping.
     * @param messageClassMap type to class map
     */
    public static void initializeAdditionalTypeToClassMap(final Map<TypeToClassKey, Class<?>> messageClassMap) {
        TypeToClassInitHelper helper;

        // init OF v1.0 mapping
        helper = new TypeToClassInitHelper(EncodeConstants.OF10_VERSION_ID, messageClassMap);
        helper.registerTypeToClass((short) 5, GetFeaturesInput.class);
        helper.registerTypeToClass((short) 7, GetConfigInput.class);
        helper.registerTypeToClass((short) 9, SetConfigInput.class);
        helper.registerTypeToClass((short) 13, PacketOutInput.class);
        helper.registerTypeToClass((short) 14, FlowModInput.class);
        helper.registerTypeToClass((short) 15, PortModInput.class);
        helper.registerTypeToClass((short) 16, MultipartRequestInput.class);
        helper.registerTypeToClass((short) 18, BarrierInput.class);
        helper.registerTypeToClass((short) 20, GetQueueConfigInput.class);

        // init OF v1.3 mapping
        helper = new TypeToClassInitHelper(EncodeConstants.OF13_VERSION_ID, messageClassMap);
        helper.registerTypeToClass((short) 5, GetFeaturesInput.class);
        helper.registerTypeToClass((short) 7, GetConfigInput.class);
        helper.registerTypeToClass((short) 9, SetConfigInput.class);
        helper.registerTypeToClass((short) 13, PacketOutInput.class);
        helper.registerTypeToClass((short) 14, FlowModInput.class);
        helper.registerTypeToClass((short) 15, GroupModInput.class);
        helper.registerTypeToClass((short) 16, PortModInput.class);
        helper.registerTypeToClass((short) 17, TableModInput.class);
        helper.registerTypeToClass((short) 18, MultipartRequestInput.class);
        helper.registerTypeToClass((short) 20, BarrierInput.class);
        helper.registerTypeToClass((short) 22, GetQueueConfigInput.class);
        helper.registerTypeToClass((short) 24, RoleRequestInput.class);
        helper.registerTypeToClass((short) 26, GetAsyncInput.class);
        helper.registerTypeToClass((short) 28, SetAsyncInput.class);
        helper.registerTypeToClass((short) 29, MeterModInput.class);

        // init OF v1.4 mapping
        helper = new TypeToClassInitHelper(EncodeConstants.OF14_VERSION_ID, messageClassMap);
        helper.registerTypeToClass((short) 7, GetConfigInput.class);
        helper.registerTypeToClass((short) 9, SetConfigInput.class);
        helper.registerTypeToClass((short) 20, BarrierInput.class);

        // init OF v1.5 mapping
        helper = new TypeToClassInitHelper(EncodeConstants.OF15_VERSION_ID, messageClassMap);
        helper.registerTypeToClass((short) 7, GetConfigInput.class);
        helper.registerTypeToClass((short) 9, SetConfigInput.class);
        helper.registerTypeToClass((short) 20, BarrierInput.class);
    }
}
