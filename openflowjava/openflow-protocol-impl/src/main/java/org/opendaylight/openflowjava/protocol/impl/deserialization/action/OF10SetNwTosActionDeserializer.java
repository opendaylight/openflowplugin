/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint8;

import io.netty.buffer.ByteBuf;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.kohsuke.MetaInfServices;
import org.opendaylight.openflowjava.protocol.api.extensibility.ActionDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwTosCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwTosCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.nw.tos._case.SetNwTosActionBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.osgi.service.component.annotations.Component;

/**
 * OF10SetNwTosActionDeserializer.
 *
 * @author michal.polkorab
 */
@Singleton
@Component(service = ActionDeserializer.OFProvider.class)
@MetaInfServices(value = ActionDeserializer.OFProvider.class)
public class OF10SetNwTosActionDeserializer extends AbstractSimpleActionCaseDeserializer<SetNwTosCase> {
    @Inject
    public OF10SetNwTosActionDeserializer() {
        super(new SetNwTosCaseBuilder().build(), EncodeConstants.OF_VERSION_1_0,
            Uint16.valueOf(ActionConstants.SET_NW_TOS_CODE));
    }

    @Override
    protected SetNwTosCase deserializeAction(final ByteBuf input) {
        input.skipBytes(2 * Short.BYTES);
        final var nwTos = readUint8(input);
        input.skipBytes(ActionConstants.PADDING_IN_SET_NW_TOS_ACTION);

        return new SetNwTosCaseBuilder().setSetNwTosAction(new SetNwTosActionBuilder().setNwTos(nwTos).build()).build();
    }
}
