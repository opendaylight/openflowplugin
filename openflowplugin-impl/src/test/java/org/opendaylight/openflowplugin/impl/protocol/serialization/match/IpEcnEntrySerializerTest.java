/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;

public class IpEcnEntrySerializerTest extends AbstractMatchEntrySerializerTest {

    @Test
    public void testSerialize() throws Exception {
        final short ecn = (short) 58;

        final Match match = new MatchBuilder()
                .setIpMatch(new IpMatchBuilder()
                        .setIpEcn(ecn)
                        .build())
                .build();

        assertMatch(match, false, (out) -> assertEquals(out.readUnsignedByte(), ecn));
    }

    @Override
    protected short getLength() {
        return EncodeConstants.SIZE_OF_BYTE_IN_BYTES;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.IP_ECN;
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

}
