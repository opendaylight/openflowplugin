/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableFeaturesPropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpOp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.TableFeaturePropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Wildcards;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WildcardsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.wildcards.WildcardSetfieldBuilder;

public class WildcardsTablePropertySerializerTest extends AbstractTablePropertySerializerTest {

    @Test
    public void testSerialize() {
        final Wildcards property = new WildcardsBuilder()
                .setWildcardSetfield(new WildcardSetfieldBuilder()
                        .setSetFieldMatch(ImmutableList
                                .<SetFieldMatch>builder()
                                .add(new SetFieldMatchBuilder()
                                        .setMatchType(ArpOp.class)
                                        .setHasMask(false)
                                        .build())
                                .build())
                        .build())
                .build();

        assertProperty(property, out -> {
            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.ARP_OP << 1);
            out.skipBytes(Byte.BYTES); // Skip match entry length
        });
    }

    @Override
    protected Class<? extends TableFeaturePropType> getClazz() {
        return Wildcards.class;
    }

    @Override
    protected int getType() {
        return TableFeaturesPropType.OFPTFPTWILDCARDS.getIntValue();
    }

}
