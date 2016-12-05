/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import static org.junit.Assert.assertArrayEquals;

import com.google.common.primitives.Longs;
import java.math.BigInteger;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.MetadataBuilder;

public class MetadataEntrySerializerTest extends AbstractMatchEntrySerializerTest {

    @Test
    public void testSerialize() throws Exception {
        final byte[] metadata = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7 };
        final byte[] metadataMask = new byte[] { 30, 30, 30, 30, 30, 0, 0, 0 };


        final Match metadataMatch = new MatchBuilder()
                .setMetadata(new MetadataBuilder()
                        .setMetadata(BigInteger.valueOf(Longs.fromByteArray(metadata)))
                        .setMetadataMask(BigInteger.valueOf(Longs.fromByteArray(metadataMask)))
                        .build())
                .build();

        assertMatch(metadataMatch, true, (out) -> {
            byte[] addressBytes = new byte[8];
            out.readBytes(addressBytes);
            assertArrayEquals(addressBytes, metadata);

            byte[] maskBytes = new byte[8];
            out.readBytes(maskBytes);
            assertArrayEquals(maskBytes, metadataMask);
        });

        final Match metadataMatchNoMask = new MatchBuilder()
                .setMetadata(new MetadataBuilder()
                        .setMetadata(BigInteger.valueOf(Longs.fromByteArray(metadata)))
                        .build())
                .build();

        assertMatch(metadataMatchNoMask, false, (out) -> {
            byte[] addressBytes = new byte[8];
            out.readBytes(addressBytes);
            assertArrayEquals(addressBytes, metadata);
        });
    }

    @Override
    protected short getLength() {
        return EncodeConstants.SIZE_OF_LONG_IN_BYTES;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.METADATA;
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

}
