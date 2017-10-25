/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFieldsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.protocol.match.fields.PbbBuilder;

public class PbbEntrySerializerTest extends AbstractMatchEntrySerializerTest {

    @Test
    public void testSerialize() throws Exception {
        final long pbbId = 6789;
        final byte[] pbbIdMask = new byte[] { 0, 15, 10 };

        final Match pbbMatch = new MatchBuilder()
                .setProtocolMatchFields(new ProtocolMatchFieldsBuilder()
                        .setPbb(new PbbBuilder()
                                .setPbbIsid(pbbId)
                                .setPbbMask(ByteUtil.bytesToUnsignedMedium(pbbIdMask))
                                .build())
                        .build())
                .build();

        assertMatch(pbbMatch, true, (out) -> {
            assertEquals(out.readUnsignedMedium(), pbbId);

            final byte[] mask = new byte[3];
            out.readBytes(mask);
            assertArrayEquals(mask, pbbIdMask);
        });

        final Match pbbMatchNoMask = new MatchBuilder()
                .setProtocolMatchFields(new ProtocolMatchFieldsBuilder()
                        .setPbb(new PbbBuilder()
                                .setPbbIsid(pbbId)
                                .build())
                        .build())
                .build();

        assertMatch(pbbMatchNoMask, false, (out) -> assertEquals(out.readUnsignedMedium(), pbbId));
    }

    @Override
    protected short getLength() {
        return EncodeConstants.SIZE_OF_3_BYTES;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.PBB_ISID;
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

}
