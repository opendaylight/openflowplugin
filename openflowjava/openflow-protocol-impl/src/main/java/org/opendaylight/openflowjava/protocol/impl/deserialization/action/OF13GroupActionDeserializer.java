/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import io.netty.buffer.ByteBuf;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.kohsuke.MetaInfServices;
import org.opendaylight.openflowjava.protocol.api.extensibility.ActionDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.GroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.GroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.group._case.GroupActionBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.osgi.service.component.annotations.Component;

/**
 * OF13GroupActionDeserializer.
 *
 * @author michal.polkorab
 */
@Singleton
@Component(service = ActionDeserializer.OFProvider.class)
@MetaInfServices(value = ActionDeserializer.OFProvider.class)
public class OF13GroupActionDeserializer extends AbstractSimpleActionCaseDeserializer<GroupCase> {
    @Inject
    public OF13GroupActionDeserializer() {
        super(new GroupCaseBuilder().build(), EncodeConstants.OF_VERSION_1_3,
            Uint16.valueOf(ActionConstants.GROUP_CODE));
    }

    @Override
    protected GroupCase deserializeAction(final ByteBuf input) {
        input.skipBytes(2 * Short.BYTES);
        return new GroupCaseBuilder()
            .setGroupAction(new GroupActionBuilder().setGroupId(readUint32(input)).build())
            .build();
    }
}
