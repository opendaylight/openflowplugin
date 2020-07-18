/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.VersionAssignableFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.HelloElementType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.Elements;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.ElementsBuilder;

/**
 * Translates Hello messages. OF protocol versions: 1.3, 1.4, 1.5.
 *
 * @author michal.polkorab
 * @author timotej.kubas
 */
public class HelloMessageFactory extends VersionAssignableFactory implements OFDeserializer<HelloMessage> {

    private static final byte HELLO_ELEMENT_HEADER_SIZE = 4;

    @Override
    public HelloMessage deserialize(ByteBuf rawMessage) {
        HelloMessageBuilder builder = new HelloMessageBuilder();
        builder.setVersion(getVersion());
        builder.setXid(readUint32(rawMessage));
        if (rawMessage.readableBytes() > 0) {
            builder.setElements(readElement(rawMessage));
        }
        return builder.build();
    }

    private static List<Elements> readElement(ByteBuf input) {
        List<Elements> elementsList = new ArrayList<>();
        while (input.readableBytes() > 0) {
            ElementsBuilder elementsBuilder = new ElementsBuilder();
            int type = input.readUnsignedShort();
            int elementLength = input.readUnsignedShort();
            if (type == HelloElementType.VERSIONBITMAP.getIntValue()) {
                elementsBuilder.setType(HelloElementType.forValue(type));
                int[] versionBitmap = new int[(elementLength - HELLO_ELEMENT_HEADER_SIZE) / 4];
                for (int i = 0; i < versionBitmap.length; i++) {
                    versionBitmap[i] = (int) input.readUnsignedInt();
                }
                elementsBuilder.setVersionBitmap(readVersionBitmap(versionBitmap));
                int paddingRemainder = elementLength % EncodeConstants.PADDING;
                if (paddingRemainder != 0) {
                    input.readBytes(EncodeConstants.PADDING - paddingRemainder);
                }
            } else {
                return elementsList;
            }
            elementsList.add(elementsBuilder.build());
        }
        return elementsList;
    }

    private static List<Boolean> readVersionBitmap(int[] input) {
        List<Boolean> versionBitmapList = new ArrayList<>();
        for (int mask : input) {
            for (int j = 0; j < Integer.SIZE; j++) {
                versionBitmapList.add((mask & 1 << j) != 0);
            }
        }
        return versionBitmapList;
    }
}
