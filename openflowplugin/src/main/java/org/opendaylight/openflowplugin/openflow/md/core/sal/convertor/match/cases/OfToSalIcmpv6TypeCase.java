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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv6MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv6TypeCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv6.type._case.Icmpv6Type;

public class OfToSalIcmpv6TypeCase extends ConvertorCase<Icmpv6TypeCase, MatchBuilder, MatchResponseConvertorData> {
    public OfToSalIcmpv6TypeCase() {
        super(Icmpv6TypeCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<MatchBuilder> process(@Nonnull Icmpv6TypeCase source, MatchResponseConvertorData data, ConvertorExecutor convertorExecutor) {
        final MatchBuilder matchBuilder = data.getMatchBuilder();
        final Icmpv6MatchBuilder icmpv6MatchBuilder = data.getIcmpv6MatchBuilder();

        Icmpv6Type icmpv6Type = source.getIcmpv6Type();
        Short v6type = icmpv6Type.getIcmpv6Type();

        if (v6type != null) {
            icmpv6MatchBuilder.setIcmpv6Type(v6type);
            matchBuilder.setIcmpv6Match(icmpv6MatchBuilder.build());
        }

        return Optional.of(matchBuilder);
    }
}
