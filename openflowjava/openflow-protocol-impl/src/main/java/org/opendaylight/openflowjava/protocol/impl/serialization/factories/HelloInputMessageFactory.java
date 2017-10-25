/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import io.netty.buffer.ByteBuf;

import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.HelloElementType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.Elements;

/**
 * Translates Hello messages
 * @author michal.polkorab
 * @author timotej.kubas
 */
public class HelloInputMessageFactory implements OFSerializer<HelloInput>{

    /** Code type of Hello message */
    private static final byte MESSAGE_TYPE = 0;
    private static final byte HELLO_ELEMENT_HEADER_SIZE = 4;

    private static void serializeElementsList(HelloInput message, ByteBuf output) {
        int[] versionBitmap;
        if (message.getElements() != null) {
            for (Elements currElement : message.getElements()) {
                int elementStartIndex = output.writerIndex();
                output.writeShort(currElement.getType().getIntValue());
                if (currElement.getType().equals(HelloElementType.VERSIONBITMAP)) {
                    int elementLengthIndex = output.writerIndex();
                    output.writeShort(EncodeConstants.EMPTY_LENGTH);
                    versionBitmap = ByteBufUtils.fillBitMaskFromList(currElement.getVersionBitmap());
                    for (int i = 0; i < versionBitmap.length; i++) {
                        output.writeInt(versionBitmap[i]);
                    }
                    int length = output.writerIndex() - elementStartIndex;
                    int padding = length - versionBitmap.length * 4 - HELLO_ELEMENT_HEADER_SIZE;
                    output.writeZero(padding);
                    output.setShort(elementLengthIndex, output.writerIndex() - elementStartIndex);
                }
            }
        }
    }

    @Override
    public void serialize(HelloInput message, ByteBuf outBuffer) {
        int startWriterIndex = outBuffer.writerIndex();
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        serializeElementsList(message, outBuffer);
        int endWriterIndex = outBuffer.writerIndex();
        int paddingRemainder = (endWriterIndex - startWriterIndex) % EncodeConstants.PADDING;
        if (paddingRemainder != 0) {
            outBuffer.writeZero(EncodeConstants.PADDING - paddingRemainder);
        }
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }

}
