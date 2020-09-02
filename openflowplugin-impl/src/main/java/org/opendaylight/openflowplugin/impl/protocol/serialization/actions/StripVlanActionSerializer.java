/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.actions;

import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerLookup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanIdBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;

public class StripVlanActionSerializer extends AbstractSetFieldActionSerializer {
    public StripVlanActionSerializer(final SerializerLookup registry) {
        super(registry);
    }

    @Override
    protected SetFieldCase buildAction(final Action input) {
        return new SetFieldCaseBuilder()
                .setSetField(new SetFieldBuilder()
                    .setVlanMatch(new VlanMatchBuilder()
                        .setVlanId(new VlanIdBuilder()
                            .setVlanIdPresent(true)
                            .setVlanId(new VlanId(Uint16.ZERO))
                            .build())
                        .build())
                    .build())
                .build();
    }
}
