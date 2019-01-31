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
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableFeaturesPropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.TableFeaturePropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActionsBuilder;

public class WriteActionsTablePropertySerializerTest extends AbstractTablePropertySerializerTest {

    @Test
    public void testSerialize() {
        final WriteActions property = new WriteActionsBuilder()
                .setWriteActions(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature
                        .prop.type.table.feature.prop.type.write.actions
                        .WriteActionsBuilder()
                        .setAction(Collections.singletonList(new ActionBuilder()
                                .setOrder(0)
                                .setAction(new SetNwSrcActionCaseBuilder()
                                        .build())
                                .build()))
                        .build())
                .build();

        assertProperty(property, out -> {
            assertEquals(out.readUnsignedShort(), ActionConstants.SET_FIELD_CODE);
            out.skipBytes(EncodeConstants.SIZE_OF_SHORT_IN_BYTES); // Skip length of set field action
        });
    }

    @Override
    protected Class<? extends TableFeaturePropType> getClazz() {
        return WriteActions.class;
    }

    @Override
    protected int getType() {
        return TableFeaturesPropType.OFPTFPTWRITEACTIONS.getIntValue();
    }

}
