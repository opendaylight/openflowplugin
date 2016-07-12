/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases;

import java.math.BigInteger;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.data.MatchResponseConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.TunnelBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.TunnelIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.tunnel.id._case.TunnelId;

public class OfToSalTunnelIdCase extends ConvertorCase<TunnelIdCase, MatchBuilder, MatchResponseConvertorData> {
    public OfToSalTunnelIdCase() {
        super(TunnelIdCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<MatchBuilder> process(@Nonnull TunnelIdCase source, MatchResponseConvertorData data, ConvertorExecutor convertorExecutor) {
        final MatchBuilder matchBuilder = data.getMatchBuilder();

        TunnelId tunnelId = source.getTunnelId();
        TunnelBuilder tunnelBuilder = new TunnelBuilder();

        if (tunnelId.getTunnelId() != null) {
            tunnelBuilder.setTunnelId(new BigInteger(OFConstants.SIGNUM_UNSIGNED, tunnelId.getTunnelId()));
            byte[] mask = tunnelId.getMask();

            if (null != mask) {
                tunnelBuilder.setTunnelMask(new BigInteger(OFConstants.SIGNUM_UNSIGNED, mask));
            }

            matchBuilder.setTunnel(tunnelBuilder.build());
        }

        return Optional.of(matchBuilder);
    }
}
