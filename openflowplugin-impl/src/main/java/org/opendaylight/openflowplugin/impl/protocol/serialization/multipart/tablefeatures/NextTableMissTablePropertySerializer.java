/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableFeaturesPropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTableMiss;

public class NextTableMissTablePropertySerializer extends AbstractTablePropertySerializer<NextTableMiss> {

    @Override
    protected void serializeProperty(final NextTableMiss property, final ByteBuf byteBuf) {
        property
            .getTablesMiss()
            .getTableIds()
            .forEach(byteBuf::writeByte);
    }

    @Override
    protected TableFeaturesPropType getType() {
        return TableFeaturesPropType.OFPTFPTNEXTTABLESMISS;
    }

    @Override
    protected Class<NextTableMiss> getClazz() {
        return NextTableMiss.class;
    }

}
