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
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActionsMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActionsMissBuilder;

public class ApplyActionsMissTablePropertySerializerTest extends AbstractTablePropertySerializerTest {

    @Test
    public void testSerialize() {
        final ApplyActionsMiss property = new ApplyActionsMissBuilder()
                .setApplyActionsMiss(new org.opendaylight.yang.gen.v1.urn.opendaylight
                        .table.types.rev131026.table.feature.prop.type.table.feature.prop.type.apply.actions.miss
                        .ApplyActionsMissBuilder()
                        .setAction(Collections.singletonList(new ActionBuilder()
                                .setOrder(0)
                                .setAction(new SetNwSrcActionCaseBuilder()
                                        .build())
                                .build()))
                        .build())
                .build();

        assertProperty(property, out -> {
            assertEquals(out.readUnsignedShort(), ActionConstants.SET_FIELD_CODE);
            out.skipBytes(Short.BYTES); // Skip length of set field action
        });
    }

    @Override
    protected Class<? extends TableFeaturePropType> getClazz() {
        return ApplyActionsMiss.class;
    }

    @Override
    protected int getType() {
        return TableFeaturesPropType.OFPTFPTAPPLYACTIONSMISS.getIntValue();
    }

}
