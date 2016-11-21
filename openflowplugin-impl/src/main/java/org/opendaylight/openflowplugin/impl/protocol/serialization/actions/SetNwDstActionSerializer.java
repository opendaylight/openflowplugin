/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.actions;

import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwDstActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv6;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchBuilder;

public class SetNwDstActionSerializer extends AbstractSetFieldActionSerializer {

    @Override
    protected SetFieldCase buildAction(Action input) {
        final Address address = SetNwDstActionCase.class.cast(input).getSetNwDstAction().getAddress();
        final SetFieldBuilder builder = new SetFieldBuilder();

        if (Ipv4.class.isInstance(address)) {
            builder.setLayer3Match(new Ipv4MatchBuilder()
                    .setIpv4Destination(Ipv4.class.cast(address).getIpv4Address())
                    .build());
        } else if (Ipv6.class.isInstance(address)) {
            builder.setLayer3Match(new Ipv6MatchBuilder()
                    .setIpv6Destination(Ipv6.class.cast(address).getIpv6Address())
                    .build());
        }

        return new SetFieldCaseBuilder().setSetField(builder.build()).build();
    }

}
