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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetMplsTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetMplsTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.mpls.ttl._case.SetMplsTtlActionBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.osgi.service.component.annotations.Component;

/**
 * OF13SetMplsTtlActionDeserializer.
 *
 * @author michal.polkorab
 */
@Singleton
@Component(service = ActionDeserializer.OFProvider.class)
@MetaInfServices(value = ActionDeserializer.OFProvider.class)
public final class OF13SetMplsTtlActionDeserializer extends AbstractSimpleActionCaseDeserializer<SetMplsTtlCase> {
    @Inject
    public OF13SetMplsTtlActionDeserializer() {
        super(new SetMplsTtlCaseBuilder().build(),EncodeConstants.OF_VERSION_1_3,
            Uint16.valueOf(ActionConstants.SET_MPLS_TTL_CODE));
    }

    @Override
    protected SetMplsTtlCase deserializeAction(final ByteBuf input) {
        input.skipBytes(2 * Short.BYTES);
        final var ttl = readUint8(input);
        input.skipBytes(ActionConstants.SET_MPLS_TTL_PADDING);

        return new SetMplsTtlCaseBuilder()
            .setSetMplsTtlAction(new SetMplsTtlActionBuilder().setMplsTtl(ttl).build())
            .build();
    }
}
