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
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFieldsBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

public class MplsLabelEntrySerializerTest extends AbstractMatchEntrySerializerTest {
    @Test
    public void testSerialize() {
        final Match mplsLabelMatch = new MatchBuilder()
                .setProtocolMatchFields(new ProtocolMatchFieldsBuilder().setMplsLabel(Uint32.TEN).build())
                .build();

        assertMatch(mplsLabelMatch, false, (out) -> assertEquals(out.readUnsignedInt(), 10L));
    }

    @Override
    protected short getLength() {
        return Integer.BYTES;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.MPLS_LABEL;
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }
}
