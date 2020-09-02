/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.match.ext;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.approved.extensions.rev160802.TcpFlagsContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.approved.extensions.rev160802.oxm.container.match.entry.value.experimenter.id._case.TcpFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.ExperimenterIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Created by Anil Vishnoi (avishnoi@Brocade.com) on 7/25/16.
 */
public class OnfOxmTcpFlagsSerializer extends AbstractOxmExperimenterMatchEntrySerializer {

    @Override
    public void serialize(final MatchEntry entry, final ByteBuf outBuffer) {
        super.serialize(entry, outBuffer);
        ExperimenterIdCase expCase = serializeExperimenterId(entry, outBuffer);
        TcpFlags tcpFlags = expCase.augmentation(TcpFlagsContainer.class).getTcpFlags();
        outBuffer.writeShort(tcpFlags.getFlags().toJava());
        if (entry.isHasMask()) {
            outBuffer.writeBytes(tcpFlags.getMask());
        }
    }

    /**
     * Returns the Experimenter match entry ID.
     */
    @Override
    protected Uint32 getExperimenterId() {
        return EncodeConstants.ONF_EXPERIMENTER_ID;
    }

    /**
     * Returns the numeric representation of oxm_field.
     */
    @Override
    protected int getOxmFieldCode() {
        return EncodeConstants.ONFOXM_ET_TCP_FLAGS;
    }

    /**
     * Returns the numeric representation of oxm_class.
     */
    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.EXPERIMENTER_CLASS;
    }

    /**
     * Returns the match entry value length (without mask length).
     */
    @Override
    protected int getValueLength() {
        return Short.BYTES;
    }
}
