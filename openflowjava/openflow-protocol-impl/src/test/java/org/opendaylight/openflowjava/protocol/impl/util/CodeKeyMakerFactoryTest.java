/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.util;

import io.netty.buffer.ByteBuf;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.keys.ActionDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.InstructionDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;

/**
 * @author michal.polkorab
 *
 */
public class CodeKeyMakerFactoryTest {

    /**
     * Tests {@link CodeKeyMakerFactory#createMatchEntriesKeyMaker(short)}
     */
    @Test
    public void testMatchEntriesKeyMaker() {
        CodeKeyMaker keyMaker = CodeKeyMakerFactory.createMatchEntriesKeyMaker(EncodeConstants.OF13_VERSION_ID);
        Assert.assertNotNull("Null key maker", keyMaker);

        ByteBuf buffer = BufferHelper.buildBuffer("80 00 00 04 00 00 00 01");
        buffer.skipBytes(4); // skip XID
        MessageCodeKey codeKey = keyMaker.make(buffer);

        Assert.assertNotNull("Null key", codeKey);
        Assert.assertEquals("Wrong key", new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID,
                        32768, 0), codeKey);
        Assert.assertEquals("Buffer index modified", 8, buffer.readableBytes());
    }

    /**
     * Tests {@link CodeKeyMakerFactory#createMatchEntriesKeyMaker(short)}
     */
    @Test
    public void testExperimenterMatchEntriesKeyMaker() {
        CodeKeyMaker keyMaker = CodeKeyMakerFactory.createMatchEntriesKeyMaker(EncodeConstants.OF13_VERSION_ID);
        Assert.assertNotNull("Null key maker", keyMaker);

        ByteBuf buffer = BufferHelper.buildBuffer("FF FF 00 04 00 00 00 01");
        buffer.skipBytes(4); // skip XID
        MessageCodeKey codeKey = keyMaker.make(buffer);

        Assert.assertNotNull("Null key", codeKey);
        MatchEntryDeserializerKey comparationKey = new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, 65535, 0);
        comparationKey.setExperimenterId(1L);
        Assert.assertEquals("Wrong key", comparationKey, codeKey);
        Assert.assertEquals("Buffer index modified", 8, buffer.readableBytes());
    }

    /**
     * Tests {@link CodeKeyMakerFactory#createActionsKeyMaker(short)}
     */
    @Test
    public void testActionKeyMaker() {
        CodeKeyMaker keyMaker = CodeKeyMakerFactory.createActionsKeyMaker(EncodeConstants.OF13_VERSION_ID);
        Assert.assertNotNull("Null key maker", keyMaker);

        ByteBuf buffer = BufferHelper.buildBuffer("00 00 00 10 00 00 00 01 00 02 00 00 00 00 00 00");
        buffer.skipBytes(4); // skip XID
        MessageCodeKey codeKey = keyMaker.make(buffer);

        Assert.assertNotNull("Null key", codeKey);
        Assert.assertEquals("Wrong key", new ActionDeserializerKey(EncodeConstants.OF13_VERSION_ID,
                        0, null), codeKey);
        Assert.assertEquals("Buffer index modified", 16, buffer.readableBytes());
    }

    /**
     * Tests {@link CodeKeyMakerFactory#createActionsKeyMaker(short)}
     */
    @Test
    public void testExperimenterActionKeyMaker() {
        CodeKeyMaker keyMaker = CodeKeyMakerFactory.createActionsKeyMaker(EncodeConstants.OF13_VERSION_ID);
        Assert.assertNotNull("Null key maker", keyMaker);

        ByteBuf buffer = BufferHelper.buildBuffer("FF FF 00 08 00 00 00 01");
        buffer.skipBytes(4); // skip XID
        MessageCodeKey codeKey = keyMaker.make(buffer);

        Assert.assertNotNull("Null key", codeKey);
        Assert.assertEquals("Wrong key", new ActionDeserializerKey(EncodeConstants.OF13_VERSION_ID,
                        65535, 1L), codeKey);
        Assert.assertEquals("Buffer index modified", 8, buffer.readableBytes());
    }

    /**
     * Tests {@link CodeKeyMakerFactory#createInstructionsKeyMaker(short)}
     */
    @Test
    public void testInstructionKeyMaker() {
        CodeKeyMaker keyMaker = CodeKeyMakerFactory.createInstructionsKeyMaker(EncodeConstants.OF13_VERSION_ID);
        Assert.assertNotNull("Null key maker", keyMaker);

        ByteBuf buffer = BufferHelper.buildBuffer("00 00 00 08");
        buffer.skipBytes(4); // skip XID
        MessageCodeKey codeKey = keyMaker.make(buffer);

        Assert.assertNotNull("Null key", codeKey);
        Assert.assertEquals("Wrong key", new InstructionDeserializerKey(EncodeConstants.OF13_VERSION_ID,
                        0, null), codeKey);
        Assert.assertEquals("Buffer index modified", 4, buffer.readableBytes());
    }

    /**
     * Tests {@link CodeKeyMakerFactory#createInstructionsKeyMaker(short)}
     */
    @Test
    public void testExperimenterInstructionKeyMaker() {
        CodeKeyMaker keyMaker = CodeKeyMakerFactory.createInstructionsKeyMaker(EncodeConstants.OF13_VERSION_ID);
        Assert.assertNotNull("Null key maker", keyMaker);

        ByteBuf buffer = BufferHelper.buildBuffer("FF FF 00 08 00 00 00 01");
        buffer.skipBytes(4); // skip XID
        MessageCodeKey codeKey = keyMaker.make(buffer);

        Assert.assertNotNull("Null key", codeKey);
        Assert.assertEquals("Wrong key", new InstructionDeserializerKey(EncodeConstants.OF13_VERSION_ID,
                        65535, 1L), codeKey);
        Assert.assertEquals("Buffer index modified", 8, buffer.readableBytes());
    }
}