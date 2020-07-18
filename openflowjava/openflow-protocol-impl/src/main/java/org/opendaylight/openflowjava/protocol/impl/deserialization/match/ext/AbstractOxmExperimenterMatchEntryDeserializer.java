/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.match.ext;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.AbstractOxmMatchEntryDeserializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.ExperimenterIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.experimenter.id._case.ExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;

/**
 * Created by Anil Vishnoi (avishnoi@Brocade.com) on 7/26/16.
 */
public abstract class AbstractOxmExperimenterMatchEntryDeserializer extends AbstractOxmMatchEntryDeserializer {

    protected ExperimenterIdCaseBuilder createExperimenterIdCase(MatchEntryBuilder entryBuilder, ByteBuf input) {
        ExperimenterIdCaseBuilder expCaseBuilder = new ExperimenterIdCaseBuilder();
        ExperimenterBuilder expBuilder = new ExperimenterBuilder();
        expBuilder.setExperimenter(new ExperimenterId(readUint32(input)));
        expCaseBuilder.setExperimenter(expBuilder.build());
        return expCaseBuilder;
    }

}
