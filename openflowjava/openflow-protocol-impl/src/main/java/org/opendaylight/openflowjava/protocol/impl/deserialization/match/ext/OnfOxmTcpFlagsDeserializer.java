/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.match.ext;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint16;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.approved.extensions.rev160802.TcpFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.approved.extensions.rev160802.TcpFlagsContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.approved.extensions.rev160802.oxm.container.match.entry.value.experimenter.id._case.TcpFlagsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.ExperimenterIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;

/**
 * Created by Anil Vishnoi (avishnoi@Brocade.com) on 7/26/16.
 */
public class OnfOxmTcpFlagsDeserializer extends AbstractOxmExperimenterMatchEntryDeserializer<TcpFlags> {
    public OnfOxmTcpFlagsDeserializer() {
        super(TcpFlags.class);
    }

    @Override
    protected void deserialize(final ByteBuf input, final MatchEntryBuilder builder) {
        ExperimenterIdCaseBuilder expCaseBuilder = createExperimenterIdCase(builder, input);
        addTcpFlagsAugmentation(input, expCaseBuilder, builder.getHasMask());
        builder.setMatchEntryValue(expCaseBuilder.build());
    }

    private static void addTcpFlagsAugmentation(final ByteBuf input, final ExperimenterIdCaseBuilder expCaseBuilder,
            final boolean hasMask) {
        TcpFlagsBuilder flagsBuilder = new TcpFlagsBuilder();
        flagsBuilder.setFlags(readUint16(input));
        if (hasMask) {
            byte[] mask = new byte[Short.BYTES];
            input.readBytes(mask);
            flagsBuilder.setMask(mask);
        }
        expCaseBuilder.addAugmentation(new TcpFlagsContainerBuilder().setTcpFlags(flagsBuilder.build()).build());
    }
}
