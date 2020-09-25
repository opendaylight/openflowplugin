/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.PbbIsid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.PbbIsidCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.pbb.isid._case.PbbIsidBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Translates OxmPbbIsid messages.
 *
 * @author michal.polkorab
 */
public class OxmPbbIsidDeserializer extends AbstractOxmMatchEntryDeserializer {
    public OxmPbbIsidDeserializer() {
        super(PbbIsid.class);
    }

    @Override
    protected void deserialize(final ByteBuf input, final MatchEntryBuilder builder) {
        final int isid = input.readUnsignedMedium();
        final PbbIsidBuilder isidBuilder = new PbbIsidBuilder()
                .setIsid(Uint32.valueOf(isid));
        if (builder.isHasMask()) {
            isidBuilder.setMask(OxmDeserializerHelper.convertMask(input, EncodeConstants.SIZE_OF_3_BYTES));
        }
        builder.setMatchEntryValue(new PbbIsidCaseBuilder().setPbbIsid(isidBuilder.build()).build());
    }
}
