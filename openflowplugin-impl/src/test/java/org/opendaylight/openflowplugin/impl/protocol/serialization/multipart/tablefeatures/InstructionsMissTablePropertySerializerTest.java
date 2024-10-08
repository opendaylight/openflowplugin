/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableFeaturesPropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.TableFeaturePropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.InstructionsMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.InstructionsMissBuilder;
import org.opendaylight.yangtools.binding.util.BindingMap;

public class InstructionsMissTablePropertySerializerTest extends AbstractTablePropertySerializerTest {
    @Test
    public void testSerialize() {
        final InstructionsMiss property = new InstructionsMissBuilder()
                .setInstructionsMiss(new org.opendaylight.yang.gen.v1.urn.opendaylight
                        .table.types.rev131026.table.feature.prop.type.table.feature.prop.type.instructions.miss
                        .InstructionsMissBuilder()
                        .setInstruction(BindingMap.of(new InstructionBuilder()
                                .setOrder(0)
                                .setInstruction(new ApplyActionsCaseBuilder().build())
                                .build()))
                        .build())
                .build();

        assertProperty(property, out -> {
            assertEquals(out.readUnsignedShort(), InstructionConstants.APPLY_ACTIONS_TYPE);
            out.skipBytes(Short.BYTES); // Skip length of set field action
        });
    }

    @Override
    protected Class<? extends TableFeaturePropType> getClazz() {
        return InstructionsMiss.class;
    }

    @Override
    protected int getType() {
        return TableFeaturesPropType.OFPTFPTINSTRUCTIONSMISS.getIntValue();
    }
}
