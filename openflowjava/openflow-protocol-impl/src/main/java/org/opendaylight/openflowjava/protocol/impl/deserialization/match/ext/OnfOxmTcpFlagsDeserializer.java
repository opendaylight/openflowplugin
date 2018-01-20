/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.match.ext;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.approved.extensions.rev160802.TcpFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.approved.extensions.rev160802.TcpFlagsContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.approved.extensions.rev160802.TcpFlagsContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.approved.extensions.rev160802.oxm.container.match.entry.value.experimenter.id._case.TcpFlagsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.ExperimenterIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ExperimenterClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;

/**
 * Created by Anil Vishnoi (avishnoi@Brocade.com) on 7/26/16.
 */
public class OnfOxmTcpFlagsDeserializer extends AbstractOxmExperimenterMatchEntryDeserializer
        implements OFDeserializer<MatchEntry> {

    @Override
    public MatchEntry deserialize(ByteBuf input) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder(deserializeHeader(input));
        ExperimenterIdCaseBuilder expCaseBuilder = createExperimenterIdCase(matchEntryBuilder, input);
        addTcpFlagsAugmentation(input, expCaseBuilder, matchEntryBuilder.isHasMask());
        matchEntryBuilder.setMatchEntryValue(expCaseBuilder.build());
        return matchEntryBuilder.build();

    }

    private static void addTcpFlagsAugmentation(ByteBuf input, ExperimenterIdCaseBuilder expCaseBuilder,
            boolean hasMask) {
        TcpFlagsContainerBuilder flagsContainerBuilder = new TcpFlagsContainerBuilder();
        TcpFlagsBuilder flagsBuilder = new TcpFlagsBuilder();
        flagsBuilder.setFlags(input.readUnsignedShort());
        if (hasMask) {
            byte[] mask = new byte[EncodeConstants.SIZE_OF_SHORT_IN_BYTES];
            input.readBytes(mask);
            flagsBuilder.setMask(mask);
        }
        flagsContainerBuilder.setTcpFlags(flagsBuilder.build());
        expCaseBuilder.addAugmentation(TcpFlagsContainer.class, flagsContainerBuilder.build());
    }

    /**
     * Return the oxm_field class.
     */
    @Override
    protected Class<? extends MatchField> getOxmField() {
        return TcpFlags.class;
    }

    /**
     * Return the oxm_class class.
     */
    @Override
    protected Class<? extends OxmClassBase> getOxmClass() {
        return ExperimenterClass.class;
    }
}
