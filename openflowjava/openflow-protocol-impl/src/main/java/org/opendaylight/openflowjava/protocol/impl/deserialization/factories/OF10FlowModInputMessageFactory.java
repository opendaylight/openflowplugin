/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import io.netty.buffer.ByteBuf;
import java.math.BigInteger;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.CodeKeyMaker;
import org.opendaylight.openflowjava.protocol.impl.util.CodeKeyMakerFactory;
import org.opendaylight.openflowjava.protocol.impl.util.ListDeserializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlagsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;

/**
 * @author giuseppex.petralia@intel.com
 *
 */
public class OF10FlowModInputMessageFactory implements OFDeserializer<FlowModInput>, DeserializerRegistryInjector {

    private DeserializerRegistry registry;

    @Override
    public void injectDeserializerRegistry(DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }

    @Override
    public FlowModInput deserialize(ByteBuf rawMessage) {
        FlowModInputBuilder builder = new FlowModInputBuilder();
        builder.setVersion((short) EncodeConstants.OF10_VERSION_ID);
        builder.setXid(rawMessage.readUnsignedInt());
        OFDeserializer<MatchV10> matchDeserializer = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, EncodeConstants.EMPTY_VALUE, MatchV10.class));
        builder.setMatchV10(matchDeserializer.deserialize(rawMessage));
        byte[] cookie = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
        rawMessage.readBytes(cookie);
        builder.setCookie(new BigInteger(1, cookie));
        builder.setCommand(FlowModCommand.forValue(rawMessage.readUnsignedShort()));
        builder.setIdleTimeout(rawMessage.readUnsignedShort());
        builder.setHardTimeout(rawMessage.readUnsignedShort());
        builder.setPriority(rawMessage.readUnsignedShort());
        builder.setBufferId(rawMessage.readUnsignedInt());
        builder.setOutPort(new PortNumber((long) rawMessage.readUnsignedShort()));
        builder.setFlagsV10(createFlowModFlagsFromBitmap(rawMessage.readUnsignedShort()));
        CodeKeyMaker keyMaker = CodeKeyMakerFactory.createActionsKeyMaker(EncodeConstants.OF10_VERSION_ID);

        List<Action> actions = ListDeserializer.deserializeList(EncodeConstants.OF10_VERSION_ID,
                rawMessage.readableBytes(), rawMessage, keyMaker, registry);
        builder.setAction(actions);
        return builder.build();
    }

    private static FlowModFlagsV10 createFlowModFlagsFromBitmap(int input) {
        final Boolean _oFPFFSENDFLOWREM = (input & (1 << 0)) > 0;
        final Boolean _oFPFFCHECKOVERLAP = (input & (1 << 1)) > 0;
        final Boolean _oFPFFEMERG = (input & (1 << 2)) > 0;
        return new FlowModFlagsV10(_oFPFFCHECKOVERLAP, _oFPFFEMERG, _oFPFFSENDFLOWREM);
    }

}
