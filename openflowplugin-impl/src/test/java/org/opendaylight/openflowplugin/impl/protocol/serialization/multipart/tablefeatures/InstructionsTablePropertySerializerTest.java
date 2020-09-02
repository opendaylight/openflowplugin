/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableFeaturesPropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.TableFeaturePropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.InstructionsBuilder;

public class InstructionsTablePropertySerializerTest extends AbstractTablePropertySerializerTest {

    @Test
    public void testSerialize() {
        final Instructions property = new InstructionsBuilder()
                .setInstructions(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature
                        .prop.type.table.feature.prop.type.instructions
                        .InstructionsBuilder()
                        .setInstruction(Collections.singletonList(new InstructionBuilder()
                                .setOrder(0)
                                .setInstruction(new ApplyActionsCaseBuilder()
                                        .build())
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
        return Instructions.class;
    }

    @Override
    protected int getType() {
        return TableFeaturesPropType.OFPTFPTINSTRUCTIONS.getIntValue();
    }

}
