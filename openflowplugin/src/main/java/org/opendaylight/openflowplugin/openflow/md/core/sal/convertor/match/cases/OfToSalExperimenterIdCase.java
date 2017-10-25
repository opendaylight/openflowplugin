/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.data.MatchResponseConvertorData;
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.TcpFlagsMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.approved.extensions.rev160802.TcpFlagsContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.ExperimenterIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.approved.extensions.rev160802.TcpFlags;

public class OfToSalExperimenterIdCase extends ConvertorCase<ExperimenterIdCase, MatchBuilder, MatchResponseConvertorData> {
    public OfToSalExperimenterIdCase() {
        super(ExperimenterIdCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<MatchBuilder> process(@Nonnull ExperimenterIdCase source, MatchResponseConvertorData data, ConvertorExecutor convertorExecutor) {
        final MatchBuilder matchBuilder = data.getMatchBuilder();

        if (data.getOxmMatchField().equals(TcpFlags.class)) {
            final TcpFlagsMatchBuilder tcpFlagsMatchBuilder = data.getTcpFlagsMatchBuilder();
            final TcpFlagsContainer tcpFlagsContainer = source.getAugmentation(TcpFlagsContainer.class);

            if (tcpFlagsContainer != null) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.
                        approved.extensions.rev160802.oxm.container.match.entry.
                        value.experimenter.id._case.TcpFlags tcpFlags = tcpFlagsContainer.getTcpFlags();

                tcpFlagsMatchBuilder.setTcpFlags(tcpFlags.getFlags());
                byte[] mask = tcpFlags.getMask();
                if (mask != null) {
                    tcpFlagsMatchBuilder.setTcpFlagsMask(ByteUtil.bytesToUnsignedShort(mask));
                }

                matchBuilder.setTcpFlagsMatch(tcpFlagsMatchBuilder.build());
            }
        }

        return Optional.of(matchBuilder);
    }
}
