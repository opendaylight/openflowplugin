/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.impl.protocol.serialization.util.InstructionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.OrderComparator;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableFeaturesPropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.InstructionsMiss;

public class InstructionsMissTablePropertySerializer extends
        AbstractTablePropertySerializer<InstructionsMiss> implements SerializerRegistryInjector {

    private SerializerRegistry registry;

    @Override
    protected void serializeProperty(final InstructionsMiss property, final ByteBuf byteBuf) {
        property
                .getInstructionsMiss()
                .getInstruction()
                .stream()
                .sorted(OrderComparator.build())
                .map(Instruction::getInstruction)
                .forEach(instruction -> InstructionUtil
                        .writeInstructionHeader(
                                instruction,
                                EncodeConstants.OF13_VERSION_ID,
                                registry,
                                byteBuf));
    }

    @Override
    protected TableFeaturesPropType getType() {
        return TableFeaturesPropType.OFPTFPTINSTRUCTIONSMISS;
    }

    @Override
    protected Class<InstructionsMiss> getClazz() {
        return InstructionsMiss.class;
    }

    @Override
    public void injectSerializerRegistry(final SerializerRegistry serializerRegistry) {
        registry = serializerRegistry;
    }

}
