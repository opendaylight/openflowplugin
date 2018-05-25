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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.OfjAugNxExpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.OfjAugNxExpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.NxExpMatchEntryValue;

public class AbstractExperimenterMatchCodecTest {

    private static final int VALUE_LENGTH = 4;
    private static final int FIELD_CODE = 16;
    private static final long EXPERIMENTER_ID = NiciraConstants.NX_NSH_VENDOR_ID;
    private ByteBuf buffer;
    private TestCodec testCodec;

    private class TestNxmField implements MatchField {
        // test class
    }

    private class TestCodec extends AbstractExperimenterMatchCodec {

        @Override
        protected void serializeValue(NxExpMatchEntryValue value, boolean hasMask, ByteBuf outBuffer) {
            // noop
        }

        @Override
        protected NxExpMatchEntryValue deserializeValue(ByteBuf outBuffer, boolean hasMask) {
            return null;
        }

        @Override
        protected long getExperimenterId() {
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
        assertEquals(EncodeConstants.SIZE_OF_INT_IN_BYTES + VALUE_LENGTH, buffer.readUnsignedByte());
        assertEquals(EXPERIMENTER_ID, buffer.readUnsignedInt());
        assertFalse(buffer.isReadable());
        verify(testCodec).serializeValue(null, false, buffer);
    }

    @Test
    public void testSerializeWithMask() {
        MatchEntry matchEntry = createMatchEntry(null, true);

        testCodec.serialize(matchEntry, buffer);

        assertEquals(EncodeConstants.EXPERIMENTER_VALUE, buffer.readUnsignedShort());
        assertEquals(FIELD_CODE << 1 | 1, buffer.readUnsignedByte());
        assertEquals(EncodeConstants.SIZE_OF_INT_IN_BYTES + (VALUE_LENGTH * 2), buffer.readUnsignedByte());
        assertEquals(EXPERIMENTER_ID, buffer.readUnsignedInt());
        assertFalse(buffer.isReadable());
        verify(testCodec).serializeValue(null, true, buffer);
    }

    @Test
    public void testDeserializeNoMask() {
        writeBuffer(buffer, false);

        MatchEntry matchEntry = testCodec.deserialize(buffer);

        assertEquals(ExperimenterClass.class, matchEntry.getOxmClass());
        assertEquals(TestNxmField.class, matchEntry.getOxmMatchField());
        assertEquals(false, matchEntry.isHasMask());
        Experimenter experimenter = ((ExperimenterIdCase) matchEntry.getMatchEntryValue()).getExperimenter();
        assertEquals(EXPERIMENTER_ID, experimenter.getExperimenter().getValue().longValue());
        assertFalse(buffer.isReadable());
        verify(testCodec).deserializeValue(buffer, false);
    }

    @Test
    public void testDeserializeWithMask() {
        writeBuffer(buffer, true);

        MatchEntry matchEntry = testCodec.deserialize(buffer);

        assertEquals(ExperimenterClass.class, matchEntry.getOxmClass());
        assertEquals(TestNxmField.class, matchEntry.getOxmMatchField());
        assertEquals(true, matchEntry.isHasMask());
        Experimenter experimenter = ((ExperimenterIdCase) matchEntry.getMatchEntryValue()).getExperimenter();
        assertEquals(EXPERIMENTER_ID, experimenter.getExperimenter().getValue().longValue());
        assertFalse(buffer.isReadable());
        verify(testCodec).deserializeValue(buffer, true);
    }

    static MatchEntry createMatchEntry(NxExpMatchEntryValue value, boolean hasMask) {
        OfjAugNxExpMatchBuilder ofjAugNxExpMatchBuilder = new OfjAugNxExpMatchBuilder();
        ofjAugNxExpMatchBuilder.setNxExpMatchEntryValue(value);
        ExperimenterBuilder experimenterBuilder = new ExperimenterBuilder();
        experimenterBuilder.setExperimenter(new ExperimenterId(EXPERIMENTER_ID));
        ExperimenterIdCaseBuilder experimenterIdCaseBuilder = new ExperimenterIdCaseBuilder();
        experimenterIdCaseBuilder.setExperimenter(experimenterBuilder.build());
        experimenterIdCaseBuilder.addAugmentation(OfjAugNxExpMatch.class, ofjAugNxExpMatchBuilder.build());
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(ExperimenterClass.class);
        matchEntryBuilder.setOxmMatchField(TestNxmField.class);
        matchEntryBuilder.setHasMask(hasMask);
        matchEntryBuilder.setMatchEntryValue(experimenterIdCaseBuilder.build());
        return matchEntryBuilder.build();
    }

    static void writeBuffer(ByteBuf message, boolean hasMask) {
        int fieldMask = FIELD_CODE << 1;
        int length = EncodeConstants.SIZE_OF_INT_IN_BYTES + VALUE_LENGTH;
        if (hasMask) {
            fieldMask |= 1;
            length += VALUE_LENGTH;
        }
        message.writeShort(OxmMatchConstants.EXPERIMENTER_CLASS);
        message.writeByte(fieldMask);
        message.writeByte(length);
        message.writeInt((int) EXPERIMENTER_ID);
    }
}