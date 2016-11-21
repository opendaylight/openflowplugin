/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.actions;

import org.opendaylight.openflowplugin.openflow.md.util.ActionUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Dscp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTosActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;

public class SetNwTosActionSerializer extends AbstractSetFieldActionSerializer {

    @Override
    protected SetFieldCase buildAction(Action input) {
        return new SetFieldCaseBuilder()
                .setSetField(new SetFieldBuilder()
                        .setIpMatch(new IpMatchBuilder()
                                .setIpDscp(new Dscp(
                                        ActionUtil.tosToDscp(SetNwTosActionCase.class.cast(input)
                                                .getSetNwTosAction().getTos().shortValue())))
                                .build())
                        .build())
                .build();
    }

}
