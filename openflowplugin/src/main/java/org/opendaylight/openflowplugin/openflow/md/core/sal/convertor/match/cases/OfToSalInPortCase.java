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
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.data.MatchResponseConvertorData;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPortCase;

public class OfToSalInPortCase extends ConvertorCase<InPortCase, MatchBuilder, MatchResponseConvertorData> {
    public OfToSalInPortCase() {
        super(InPortCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<MatchBuilder> process(@Nonnull InPortCase source, MatchResponseConvertorData data, ConvertorExecutor convertorExecutor) {
        final MatchBuilder matchBuilder = data.getMatchBuilder();
        final OpenflowVersion ofVersion = OpenflowVersion.get(data.getVersion());
        final BigInteger datapathId = data.getDatapathId();

        final PortNumber portNumber = source.getInPort().getPortNumber();

        if (portNumber != null) {
            matchBuilder.setInPort(InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(datapathId, portNumber.getValue(), ofVersion));
        }

        return Optional.of(matchBuilder);
    }
}
