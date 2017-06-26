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

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.HelloElementType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.Elements;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.ElementsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author michal.polkorab
 *
 */
public class HelloInputMessageFactoryTest {

    private static final Logger LOG = LoggerFactory.getLogger(HelloInputMessageFactoryTest.class);
    private SerializerRegistry registry;
    private OFSerializer<HelloInput> helloFactory;

    /**
     * Initializes serializer registry and stores correct factory in field
     */
    @Before
    public void startUp() {
        registry = new SerializerRegistryImpl();
        registry.init();
        helloFactory = registry.getSerializer(
                new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, HelloInput.class));
    }

    /**
     * Testing of {@link HelloInputMessageFactory} for correct translation from POJO
     * @throws Exception
     */
    @Test
    public void testWithoutElementsSet() throws Exception {
        HelloInputBuilder hib = new HelloInputBuilder();
        BufferHelper.setupHeader(hib, EncodeConstants.OF13_VERSION_ID);
        HelloInput hi = hib.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        helloFactory.serialize(hi, out);

        BufferHelper.checkHeaderV13(out,(byte) 0, EncodeConstants.OFHEADER_SIZE);
    }

    /**
     * Testing of {@link HelloInputMessageFactory} for correct translation from POJO
     * @throws Exception
     */
    @Test
    public void testWith4BitVersionBitmap() throws Exception {
        int lengthOfBitmap = 4;
        HelloInputBuilder builder = new HelloInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        List<Elements> expectedElement = createElement(lengthOfBitmap);
        builder.setElements(expectedElement);
        HelloInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        helloFactory.serialize(message, out);
        if (LOG.isDebugEnabled()) {
            LOG.debug("bytebuf: {}", ByteBufUtils.byteBufToHexString(out));
        }

        BufferHelper.checkHeaderV13(out, (byte) 0, 16);
        Elements element = readElement(out).get(0);
        Assert.assertEquals("Wrong element type", expectedElement.get(0).getType(), element.getType());
        Elements comparation = createComparationElement(lengthOfBitmap).get(0);
        Assert.assertArrayEquals("Wrong element bitmap", comparation.getVersionBitmap().toArray(), element.getVersionBitmap().toArray());
    }

    /**
     * Testing of {@link HelloInputMessageFactory} for correct translation from POJO
     * @throws Exception
     */
    @Test
    public void testWith64BitVersionBitmap() throws Exception {
        int lengthOfBitmap = 64;
        HelloInputBuilder builder = new HelloInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        List<Elements> expectedElement = createElement(lengthOfBitmap);
        builder.setElements(expectedElement);
        HelloInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        helloFactory.serialize(message, out);
        if (LOG.isDebugEnabled()) {
            LOG.debug("bytebuf: ", ByteBufUtils.byteBufToHexString(out));
        }

        BufferHelper.checkHeaderV13(out, (byte) 0, 24);
        Elements element = readElement(out).get(0);
        Assert.assertEquals("Wrong element type", expectedElement.get(0).getType(), element.getType());
        Elements comparation = createComparationElement(lengthOfBitmap).get(0);
        Assert.assertArrayEquals("Wrong element bitmap", comparation.getVersionBitmap().toArray(), element.getVersionBitmap().toArray());
    }

    private static List<Elements> createElement(int lengthOfBitmap) {
        ElementsBuilder elementsBuilder = new ElementsBuilder();
        List<Elements> elementsList = new ArrayList<>();
        List<Boolean> booleanList = new ArrayList<>();
        for (int i = 0; i < lengthOfBitmap; i++) {
            booleanList.add(true);
        }
        elementsBuilder.setType(HelloElementType.forValue(1));
        elementsBuilder.setVersionBitmap(booleanList);
        elementsList.add(elementsBuilder.build());
        return elementsList;
    }

    private static List<Elements> createComparationElement(int lengthOfBitmap) {
        ElementsBuilder elementsBuilder = new ElementsBuilder();
        List<Elements> elementsList = new ArrayList<>();
        List<Boolean> booleanList = new ArrayList<>();
        for (int i = 0; i < lengthOfBitmap; i++) {
            booleanList.add(true);
        }
        if ((lengthOfBitmap % Integer.SIZE) != 0) {
            for (int i = 0; i < (Integer.SIZE - (lengthOfBitmap % Integer.SIZE)); i++) {
                booleanList.add(false);
            }
        }
        LOG.debug("boolsize {}", booleanList.size());
        elementsBuilder.setType(HelloElementType.forValue(1));
        elementsBuilder.setVersionBitmap(booleanList);
        elementsList.add(elementsBuilder.build());
        return elementsList;
    }

    private static List<Elements> readElement(ByteBuf input) {
        List<Elements> elementsList = new ArrayList<>();
        while (input.readableBytes() > 0) {
            ElementsBuilder elementsBuilder = new ElementsBuilder();
            int type = input.readUnsignedShort();
            int elementLength = input.readUnsignedShort();
            if (type == HelloElementType.VERSIONBITMAP.getIntValue()) {
                elementsBuilder.setType(HelloElementType.forValue(type));
                int[] versionBitmap = new int[(elementLength - 4) / 4];
                for (int i = 0; i < versionBitmap.length; i++) {
                    versionBitmap[i] = (int) input.readUnsignedInt();
                }
                elementsBuilder.setVersionBitmap(readVersionBitmap(versionBitmap));
                int paddingRemainder = elementLength % EncodeConstants.PADDING;
                if (paddingRemainder != 0) {
                    input.readBytes(EncodeConstants.PADDING - paddingRemainder);
                }
            }
            elementsList.add(elementsBuilder.build());
        }
        return elementsList;
    }

    private static List<Boolean> readVersionBitmap(int[] input){
        List<Boolean> versionBitmapList = new ArrayList<>();
        for (int i = 0; i < input.length; i++) {
            int mask = input[i];
            for (int j = 0; j < Integer.SIZE; j++) {
                    versionBitmapList.add((mask & (1<<j)) != 0);
            }
        }
        return versionBitmapList;
    }

}
