/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.messages;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.BinContent;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowplugin.impl.protocol.serialization.AbstractSerializerTest;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.IetfYangUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortNumberUni;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumberValues;

public class PortMessageSerializerTest extends AbstractSerializerTest {

    private static final byte PADDING_IN_PORT_MOD_MESSAGE_01 = 4;
    private static final byte PADDING_IN_PORT_MOD_MESSAGE_02 = 2;
    private static final byte PADDING_IN_PORT_MOD_MESSAGE_03 = 4;
    private static final Long XID = 42L;
    private static final Short VERSION = EncodeConstants.OF13_VERSION_ID;
    private static final String PORT_NUMBER = OutputPortValues.ALL.toString();
    private static final Long PORT_NUMBER_VAL = BinContent.intToUnsignedLong(PortNumberValues.ALL.getIntValue());
    private static final String MAC_ADDRESS = "E9:2A:55:BA:FA:4D";

    // Port config
    private static final Boolean IS_NOFWD = false;
    private static final Boolean IS_NOPACKETIN = false;
    private static final Boolean IS_NORECV = true;
    private static final Boolean IS_PORTDOWN = false;

    // Port features
    private static final Boolean IS_AUTOENG = true;
    private static final Boolean IS_COPPER = false;
    private static final Boolean IS_FIBER = true;
    private static final Boolean IS_40GBFD = true;
    private static final Boolean IS_100GBFD = false;
    private static final Boolean IS_100MBFD = false;
    private static final Boolean IS_100MBHD = false;
    private static final Boolean IS_1GBFD = false;
    private static final Boolean IS_1GBHD = false;
    private static final Boolean IS_1TBFD = false;
    private static final Boolean IS_OTHER = false;
    private static final Boolean IS_PAUSE = false;
    private static final Boolean IS_PAUSE_ASYM = false;
    private static final Boolean IS_10GBFD = false;
    private static final Boolean IS_10MBFD = false;
    private static final Boolean IS_10MBHD = false;

    private static final PortMessage MESSAGE = new PortMessageBuilder()
            .setXid(XID)
            .setVersion(VERSION)
            .setPortNumber(new PortNumberUni(PORT_NUMBER))
            .setConfiguration(new PortConfig(IS_NOFWD, IS_NOPACKETIN, IS_NORECV, IS_PORTDOWN))
            .setAdvertisedFeatures(new PortFeatures(
                    IS_AUTOENG,
                    IS_COPPER,
                    IS_FIBER,
                    IS_40GBFD,
                    IS_100GBFD,
                    IS_100MBFD,
                    IS_100MBHD,
                    IS_1GBFD,
                    IS_1GBHD,
                    IS_1TBFD,
                    IS_OTHER,
                    IS_PAUSE,
                    IS_PAUSE_ASYM,
                    IS_10GBFD,
                    IS_10MBFD,
                    IS_10MBHD))
            .setHardwareAddress(new MacAddress(MAC_ADDRESS))
            .build();

    private PortMessageSerializer serializer;

    @Override
    protected void init() {
        serializer = new PortMessageSerializer();
        when(getBuffer().readableBytes()).thenReturn(0);
    }

    @Test
    public void testSerialize() throws Exception {
        serializer.serialize(MESSAGE, getBuffer());

        // Header
        bufferVerify().writeByte(VERSION);
        bufferVerify().writeByte(serializer.getMessageType());
        bufferVerify().writeShort(EncodeConstants.EMPTY_LENGTH);
        bufferVerify().writeInt(XID.intValue());

        // Body
        bufferVerify().writeInt(PORT_NUMBER_VAL.intValue());
        bufferVerify().writeZero(PADDING_IN_PORT_MOD_MESSAGE_01);
        bufferVerify().writeBytes(IetfYangUtil.INSTANCE.bytesFor(new MacAddress(MAC_ADDRESS)));
        bufferVerify().writeZero(PADDING_IN_PORT_MOD_MESSAGE_02);

        // Port config
        bufferVerify(times(2)).writeInt(ByteBufUtils.fillBitMaskFromMap(ImmutableMap
                .<Integer, Boolean>builder()
                .put(0, IS_PORTDOWN)
                .put(2, IS_NORECV)
                .put(5, IS_NOFWD)
                .put(6, IS_NOPACKETIN)
                .build()));

        // Port features
        bufferVerify().writeInt(ByteBufUtils.fillBitMask(0,
                IS_10MBHD,
                IS_10MBFD,
                IS_100MBHD,
                IS_100MBFD,
                IS_1GBHD,
                IS_1GBFD,
                IS_10GBFD,
                IS_40GBFD,
                IS_100GBFD,
                IS_1TBFD,
                IS_OTHER,
                IS_COPPER,
                IS_FIBER,
                IS_AUTOENG,
                IS_PAUSE,
                IS_PAUSE_ASYM));
        bufferVerify().writeZero(PADDING_IN_PORT_MOD_MESSAGE_03);

        bufferVerify().setShort(eq(2), anyInt());

    }

}