/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorCase;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.nw.src._case.SetNwSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;

public class SalToOfSetNwSrcActionV10Case extends ConvertorCase<SetNwSrcActionCase, Action, ActionConvertorData> {
    public SalToOfSetNwSrcActionV10Case() {
        super(SetNwSrcActionCase.class, true, OFConstants.OFP_VERSION_1_0);
    }

    @Nonnull
    @Override
    public Optional<Action> process(@Nonnull final SetNwSrcActionCase source, final ActionConvertorData data, ConvertorExecutor convertorExecutor) {
        final ActionBuilder builder = new ActionBuilder();
        final Address address = source.getSetNwSrcAction().getAddress();

        if (address instanceof Ipv4) {
            //FIXME use of substring should be removed and OF models should distinguish where
            //FIXME to use Ipv4Prefix (with mask) and where to use Ipv4Address (without mask)

            String ipAddress = ((Ipv4) address).getIpv4Address().getValue();
            ipAddress = ipAddress.substring(0, ipAddress.indexOf("/"));
            Ipv4Address result = new Ipv4Address(ipAddress);
            SetNwSrcCaseBuilder nwSrcCaseBuilder = new SetNwSrcCaseBuilder();
            SetNwSrcActionBuilder nwSrcBuilder = new SetNwSrcActionBuilder();
            nwSrcBuilder.setIpAddress(new Ipv4Address(result));
            nwSrcCaseBuilder.setSetNwSrcAction(nwSrcBuilder.build());
            builder.setActionChoice(nwSrcCaseBuilder.build());
        } else {
            throw new IllegalArgumentException("Address is not supported by OF-1.0: " + address.getClass().getName());
        }

        return Optional.of(builder.build());
    }
}
