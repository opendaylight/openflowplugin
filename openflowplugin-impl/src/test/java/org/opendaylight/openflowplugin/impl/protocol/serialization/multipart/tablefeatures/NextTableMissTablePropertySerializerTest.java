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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableFeaturesPropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.TableFeaturePropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTableMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTableMissBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.next.table.miss.TablesMissBuilder;

public class NextTableMissTablePropertySerializerTest extends AbstractTablePropertySerializerTest {

    @Test
    public void testSerialize() throws Exception {
        final short tableId = 42;
        final NextTableMiss property = new NextTableMissBuilder()
                .setTablesMiss(new TablesMissBuilder()
                        .setTableIds(Collections.singletonList(tableId))
                        .build())
                .build();

        assertProperty(property, out -> assertEquals(out.readUnsignedByte(), tableId));
    }

    @Override
    protected Class<? extends TableFeaturePropType> getClazz() {
        return NextTableMiss.class;
    }

    @Override
    protected int getType() {
        return TableFeaturesPropType.OFPTFPTNEXTTABLESMISS.getIntValue();
    }

}
