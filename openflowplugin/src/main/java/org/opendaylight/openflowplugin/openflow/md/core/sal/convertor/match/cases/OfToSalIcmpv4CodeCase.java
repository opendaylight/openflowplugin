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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv4CodeCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv4.code._case.Icmpv4Code;

public class OfToSalIcmpv4CodeCase extends ConvertorCase<Icmpv4CodeCase, MatchBuilder, MatchResponseConvertorData> {
    public OfToSalIcmpv4CodeCase() {
        super(Icmpv4CodeCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<MatchBuilder> process(@Nonnull Icmpv4CodeCase source, MatchResponseConvertorData data, ConvertorExecutor convertorExecutor) {
        final MatchBuilder matchBuilder = data.getMatchBuilder();
        final Icmpv4MatchBuilder icmpv4MatchBuilder = data.getIcmpv4MatchBuilder();

        Icmpv4Code icmpv4Code = source.getIcmpv4Code();
        Short v4code = icmpv4Code.getIcmpv4Code();

        if (v4code != null) {
            icmpv4MatchBuilder.setIcmpv4Code(v4code);
            matchBuilder.setIcmpv4Match(icmpv4MatchBuilder.build());
        }

        return Optional.of(matchBuilder);
    }
}
