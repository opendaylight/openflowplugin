/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.codec.match;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.nx.api.NiciraConstants;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.ExperimenterIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.ExperimenterIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.experimenter.id._case.Experimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.experimenter.id._case.ExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ExperimenterClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.OfjAugNxExpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.NxExpMatchEntryValue;
import org.opendaylight.yangtools.yang.common.Uint32;

public class AbstractExperimenterMatchCodecTest {

    private static final int VALUE_LENGTH = 4;
    private static final int FIELD_CODE = 16;
    private static final Uint32 EXPERIMENTER_ID = NiciraConstants.NX_NSH_VENDOR_ID;
    private ByteBuf buffer;
    private TestCodec testCodec;

    private class TestNxmField implements MatchField {
        // test class
    }

    private class TestCodec extends AbstractExperimenterMatchCodec {

        @Override
        protected void serializeValue(final NxExpMatchEntryValue value, final boolean hasMask,
                final ByteBuf outBuffer) {
            // noop
        }

        @Override
        protected NxExpMatchEntryValue deserializeValue(final ByteBuf outBuffer, final boolean hasMask) {
            return null;
        }

        @Override
        protected Uint32 getExperimenterId() {
            return EXPERIMENTER_ID;
        }

        @Override
        public int getValueLength() {
            return VALUE_LENGTH;
        }

        @Override
        public int getNxmFieldCode() {
            return FIELD_CODE;
        }

        @Override
        public Class<? extends MatchField> getNxmField() {
            return TestNxmField.class;
        }
    }

    @Before
    public void setup() {
        buffer = ByteBufAllocator.DEFAULT.buffer();
        testCodec = spy(new TestCodec());
    }

    @Test
    public void testSerializeNoMask() {
        MatchEntry matchEntry = createMatchEntry(null, false);

        testCodec.serialize(matchEntry, buffer);

        assertEquals(EncodeConstants.EXPERIMENTER_VALUE, buffer.readUnsignedShort());
        assertEquals(FIELD_CODE << 1, buffer.readUnsignedByte());
        assertEquals(Integer.BYTES + VALUE_LENGTH, buffer.readUnsignedByte());
        assertEquals(EXPERIMENTER_ID.longValue(), buffer.readUnsignedInt());
        assertFalse(buffer.isReadable());
        verify(testCodec).serializeValue(null, false, buffer);
    }

    @Test
    public void testSerializeWithMask() {
        MatchEntry matchEntry = createMatchEntry(null, true);

        testCodec.serialize(matchEntry, buffer);

        assertEquals(EncodeConstants.EXPERIMENTER_VALUE, buffer.readUnsignedShort());
        assertEquals(FIELD_CODE << 1 | 1, buffer.readUnsignedByte());
        assertEquals(Integer.BYTES + VALUE_LENGTH * 2, buffer.readUnsignedByte());
        assertEquals(EXPERIMENTER_ID.longValue(), buffer.readUnsignedInt());
        assertFalse(buffer.isReadable());
        verify(testCodec).serializeValue(null, true, buffer);
    }

    @Test
    public void testDeserializeNoMask() {
        writeBuffer(buffer, false);

        MatchEntry matchEntry = testCodec.deserialize(buffer);

        assertEquals(ExperimenterClass.class, matchEntry.getOxmClass());
        assertEquals(TestNxmField.class, matchEntry.getOxmMatchField());
        assertEquals(false, matchEntry.getHasMask());
        Experimenter experimenter = ((ExperimenterIdCase) matchEntry.getMatchEntryValue()).getExperimenter();
        assertEquals(EXPERIMENTER_ID, experimenter.getExperimenter().getValue());
        assertFalse(buffer.isReadable());
        verify(testCodec).deserializeValue(buffer, false);
    }

    @Test
    public void testDeserializeWithMask() {
        writeBuffer(buffer, true);

        MatchEntry matchEntry = testCodec.deserialize(buffer);

        assertEquals(ExperimenterClass.class, matchEntry.getOxmClass());
        assertEquals(TestNxmField.class, matchEntry.getOxmMatchField());
        assertEquals(true, matchEntry.getHasMask());
        Experimenter experimenter = ((ExperimenterIdCase) matchEntry.getMatchEntryValue()).getExperimenter();
        assertEquals(EXPERIMENTER_ID, experimenter.getExperimenter().getValue());
        assertFalse(buffer.isReadable());
        verify(testCodec).deserializeValue(buffer, true);
    }

    static MatchEntry createMatchEntry(final NxExpMatchEntryValue value, final boolean hasMask) {
        return new MatchEntryBuilder()
                .setOxmClass(ExperimenterClass.class)
                .setOxmMatchField(TestNxmField.class)
                .setHasMask(hasMask)
                .setMatchEntryValue(new ExperimenterIdCaseBuilder()
                    .setExperimenter(new ExperimenterBuilder()
                        .setExperimenter(new ExperimenterId(EXPERIMENTER_ID))
                        .build())
                    .addAugmentation(new OfjAugNxExpMatchBuilder().setNxExpMatchEntryValue(value).build())
                    .build())
                .build();
    }

    static void writeBuffer(final ByteBuf message, final boolean hasMask) {
        int fieldMask = FIELD_CODE << 1;
        int length = Integer.BYTES + VALUE_LENGTH;
        if (hasMask) {
            fieldMask |= 1;
            length += VALUE_LENGTH;
        }
        message.writeShort(OxmMatchConstants.EXPERIMENTER_CLASS);
        message.writeByte(fieldMask);
        message.writeByte(length);
        message.writeInt(EXPERIMENTER_ID.intValue());
    }
}