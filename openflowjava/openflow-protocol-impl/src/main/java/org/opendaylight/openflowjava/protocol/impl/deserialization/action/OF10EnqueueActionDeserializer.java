/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint16;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import io.netty.buffer.ByteBuf;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.kohsuke.MetaInfServices;
import org.opendaylight.openflowjava.protocol.api.extensibility.ActionDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.EnqueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.EnqueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.enqueue._case.EnqueueActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.QueueId;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.osgi.service.component.annotations.Component;

/**
 * OF10EnqueueActionDeserializer.
 *
 * @author michal.polkorab
 */
@Singleton
@Component(service = ActionDeserializer.OFProvider.class)
@MetaInfServices(value = ActionDeserializer.OFProvider.class)
public final class OF10EnqueueActionDeserializer extends AbstractSimpleActionCaseDeserializer<EnqueueCase> {
    @Inject
    public OF10EnqueueActionDeserializer() {
        super(new EnqueueCaseBuilder().build(), EncodeConstants.OF_VERSION_1_0,
            Uint16.valueOf(ActionConstants.ENQUEUE_CODE));
    }

    @Override
    protected EnqueueCase deserializeAction(final ByteBuf input) {
        input.skipBytes(2 * Short.BYTES);
        final var port = new PortNumber(readUint16(input).toUint32());
        input.skipBytes(ActionConstants.PADDING_IN_ENQUEUE_ACTION);
        final var queueId = new QueueId(readUint32(input));

        return new EnqueueCaseBuilder()
            .setEnqueueAction(new EnqueueActionBuilder().setPort(port).setQueueId(queueId).build())
            .build();
    }
}
