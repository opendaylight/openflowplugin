/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint16;

import io.netty.buffer.ByteBuf;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.kohsuke.MetaInfServices;
import org.opendaylight.openflowjava.protocol.api.extensibility.ActionDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetVlanVidCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetVlanVidCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.vlan.vid._case.SetVlanVidActionBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.osgi.service.component.annotations.Component;

/**
 * OF10SetVlanVidActionDeserializer.
 *
 * @author michal.polkorab
 */
@Singleton
@Component(service = ActionDeserializer.OFProvider.class)
@MetaInfServices(value = ActionDeserializer.OFProvider.class)
public final class OF10SetVlanVidActionDeserializer extends AbstractSimpleActionCaseDeserializer<SetVlanVidCase> {
    @Inject
    public OF10SetVlanVidActionDeserializer() {
        super(new SetVlanVidCaseBuilder().build(), EncodeConstants.OF_VERSION_1_0,
            Uint16.valueOf(ActionConstants.SET_VLAN_VID_CODE));
    }

    @Override
    protected SetVlanVidCase deserializeAction(final ByteBuf input) {
        input.skipBytes(2 * Short.BYTES);
        final var vlanVid = readUint16(input);
        input.skipBytes(ActionConstants.PADDING_IN_SET_VLAN_VID_ACTION);

        return new SetVlanVidCaseBuilder()
            .setSetVlanVidAction(new SetVlanVidActionBuilder().setVlanVid(vlanVid).build())
            .build();
    }
}
